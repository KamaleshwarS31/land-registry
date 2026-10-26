// SPDX-License-Identifier: MIT
pragma solidity 0.8.24;

/**
 * @title LandRegistry
 * @dev Provenance-Aware and Parcel-Lineage Smart Contract for Land Registration.
 * Enforces atomic state version checks (TOCTOU protection), registrar access controls,
 * immutable lineage tracking for splits/merges, and cryptographic decision certificate digests.
 */
contract LandRegistry {
    // Mutation types
    uint8 public constant MUTATION_SPLIT = 1;
    uint8 public constant MUTATION_MERGE = 2;

    struct ParcelRecord {
        bytes32 parcelCodeHash;
        address currentOwner;
        uint256 version;
        bool isActive;
        bytes32 lastCertificateDigest;
        uint256 lastUpdatedBlock;
    }

    struct LineageRecord {
        bytes32 parentHash;
        bytes32 childHash;
        uint8 mutationType;
        uint256 blockNumber;
    }

    address public contractOwner;
    mapping(address => bool) public authorizedRegistrars;
    mapping(bytes32 => ParcelRecord) private _parcels;
    LineageRecord[] private _lineageHistory;

    // Events
    event RegistrarAuthorized(address indexed registrar);
    event RegistrarRevoked(address indexed registrar);

    event ParcelRegistered(
        bytes32 indexed parcelCodeHash,
        address indexed owner,
        bytes32 certificateDigest,
        uint256 blockNumber
    );

    event OwnershipTransferred(
        bytes32 indexed parcelCodeHash,
        address indexed oldOwner,
        address indexed newOwner,
        uint256 newVersion,
        bytes32 certificateDigest,
        uint256 blockNumber
    );

    event ParcelSplit(
        bytes32 indexed parentCodeHash,
        bytes32[] childCodeHashes,
        bytes32 certificateDigest,
        uint256 blockNumber
    );

    event ParcelMerged(
        bytes32[] parentCodeHashes,
        bytes32 indexed childCodeHash,
        bytes32 certificateDigest,
        uint256 blockNumber
    );

    modifier onlyOwner() {
        require(msg.sender == contractOwner, "Only contract owner can execute");
        _;
    }

    modifier onlyRegistrar() {
        require(
            authorizedRegistrars[msg.sender] || msg.sender == contractOwner,
            "Caller is not an authorized registrar"
        );
        _;
    }

    constructor() {
        contractOwner = msg.sender;
        authorizedRegistrars[msg.sender] = true;
        emit RegistrarAuthorized(msg.sender);
    }

    function setRegistrarAuthorization(address registrar, bool authorized) external onlyOwner {
        require(registrar != address(0), "Invalid registrar address");
        authorizedRegistrars[registrar] = authorized;
        if (authorized) {
            emit RegistrarAuthorized(registrar);
        } else {
            emit RegistrarRevoked(registrar);
        }
    }

    /**
     * @dev Registers a new parcel.
     */
    function registerParcel(
        bytes32 parcelCodeHash,
        address owner,
        bytes32 certificateDigest
    ) external onlyRegistrar {
        require(parcelCodeHash != bytes32(0), "Invalid parcel code hash");
        require(owner != address(0), "Invalid owner address");
        require(_parcels[parcelCodeHash].version == 0, "Parcel already registered");

        _parcels[parcelCodeHash] = ParcelRecord({
            parcelCodeHash: parcelCodeHash,
            currentOwner: owner,
            version: 1,
            isActive: true,
            lastCertificateDigest: certificateDigest,
            lastUpdatedBlock: block.number
        });

        emit ParcelRegistered(parcelCodeHash, owner, certificateDigest, block.number);
    }

    /**
     * @dev Transfers ownership with strict expected-version check to prevent stale-state race conditions.
     */
    function transferOwnership(
        bytes32 parcelCodeHash,
        address newOwner,
        uint256 expectedVersion,
        bytes32 certificateDigest
    ) external onlyRegistrar {
        require(newOwner != address(0), "Invalid new owner address");
        ParcelRecord storage parcel = _parcels[parcelCodeHash];

        require(parcel.version > 0, "Parcel does not exist");
        require(parcel.isActive, "Parcel is inactive");
        require(parcel.version == expectedVersion, "Stale state version: expected version does not match");

        address oldOwner = parcel.currentOwner;
        parcel.currentOwner = newOwner;
        parcel.version = expectedVersion + 1;
        parcel.lastCertificateDigest = certificateDigest;
        parcel.lastUpdatedBlock = block.number;

        emit OwnershipTransferred(
            parcelCodeHash,
            oldOwner,
            newOwner,
            parcel.version,
            certificateDigest,
            block.number
        );
    }

    /**
     * @dev Splits an active parent parcel into multiple child parcels.
     */
    function splitParcel(
        bytes32 parentCodeHash,
        uint256 expectedParentVersion,
        bytes32[] calldata childCodeHashes,
        address[] calldata childOwners,
        bytes32 certificateDigest
    ) external onlyRegistrar {
        require(childCodeHashes.length >= 2, "Split requires at least 2 child parcels");
        require(childCodeHashes.length == childOwners.length, "Children and owners count mismatch");

        ParcelRecord storage parent = _parcels[parentCodeHash];
        require(parent.version > 0, "Parent parcel does not exist");
        require(parent.isActive, "Parent parcel is not active");
        require(parent.version == expectedParentVersion, "Stale parent version");

        // Deactivate parent parcel
        parent.isActive = false;
        parent.version = expectedParentVersion + 1;
        parent.lastCertificateDigest = certificateDigest;
        parent.lastUpdatedBlock = block.number;

        // Instantiate child parcels
        for (uint256 i = 0; i < childCodeHashes.length; i++) {
            bytes32 childHash = childCodeHashes[i];
            address childOwner = childOwners[i];
            require(childHash != bytes32(0), "Invalid child code hash");
            require(childOwner != address(0), "Invalid child owner");
            require(_parcels[childHash].version == 0, "Child parcel already exists");

            _parcels[childHash] = ParcelRecord({
                parcelCodeHash: childHash,
                currentOwner: childOwner,
                version: 1,
                isActive: true,
                lastCertificateDigest: certificateDigest,
                lastUpdatedBlock: block.number
            });

            _lineageHistory.push(LineageRecord({
                parentHash: parentCodeHash,
                childHash: childHash,
                mutationType: MUTATION_SPLIT,
                blockNumber: block.number
            }));
        }

        emit ParcelSplit(parentCodeHash, childCodeHashes, certificateDigest, block.number);
    }

    /**
     * @dev Merges multiple active parent parcels into a single child parcel.
     */
    function mergeParcels(
        bytes32[] calldata parentCodeHashes,
        uint256[] calldata expectedParentVersions,
        bytes32 childCodeHash,
        address childOwner,
        bytes32 certificateDigest
    ) external onlyRegistrar {
        require(parentCodeHashes.length >= 2, "Merge requires at least 2 parent parcels");
        require(parentCodeHashes.length == expectedParentVersions.length, "Parents and versions count mismatch");
        require(childCodeHash != bytes32(0), "Invalid child parcel hash");
        require(childOwner != address(0), "Invalid child owner");
        require(_parcels[childCodeHash].version == 0, "Child parcel already exists");

        // Verify and deactivate each parent
        for (uint256 i = 0; i < parentCodeHashes.length; i++) {
            bytes32 pHash = parentCodeHashes[i];
            ParcelRecord storage parent = _parcels[pHash];
            require(parent.version > 0, "Parent parcel does not exist");
            require(parent.isActive, "Parent parcel is inactive");
            require(parent.version == expectedParentVersions[i], "Stale parent version");

            parent.isActive = false;
            parent.version = expectedParentVersions[i] + 1;
            parent.lastCertificateDigest = certificateDigest;
            parent.lastUpdatedBlock = block.number;

            _lineageHistory.push(LineageRecord({
                parentHash: pHash,
                childHash: childCodeHash,
                mutationType: MUTATION_MERGE,
                blockNumber: block.number
            }));
        }

        // Instantiate merged child parcel
        _parcels[childCodeHash] = ParcelRecord({
            parcelCodeHash: childCodeHash,
            currentOwner: childOwner,
            version: 1,
            isActive: true,
            lastCertificateDigest: certificateDigest,
            lastUpdatedBlock: block.number
        });

        emit ParcelMerged(parentCodeHashes, childCodeHash, certificateDigest, block.number);
    }

    /**
     * @dev Retrieves a parcel record by its code hash.
     */
    function getParcel(bytes32 parcelCodeHash) external view returns (
        bytes32 codeHash,
        address currentOwner,
        uint256 version,
        bool isActive,
        bytes32 lastCertificateDigest,
        uint256 lastUpdatedBlock
    ) {
        ParcelRecord memory p = _parcels[parcelCodeHash];
        return (
            p.parcelCodeHash,
            p.currentOwner,
            p.version,
            p.isActive,
            p.lastCertificateDigest,
            p.lastUpdatedBlock
        );
    }

    /**
     * @dev Returns total lineage edges.
     */
    function getLineageCount() external view returns (uint256) {
        return _lineageHistory.length;
    }

    /**
     * @dev Returns lineage edge by index.
     */
    function getLineageRecord(uint256 index) external view returns (
        bytes32 parentHash,
        bytes32 childHash,
        uint8 mutationType,
        uint256 blockNumber
    ) {
        require(index < _lineageHistory.length, "Index out of bounds");
        LineageRecord memory record = _lineageHistory[index];
        return (record.parentHash, record.childHash, record.mutationType, record.blockNumber);
    }
}
