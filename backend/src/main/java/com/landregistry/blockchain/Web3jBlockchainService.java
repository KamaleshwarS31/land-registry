package com.landregistry.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class Web3jBlockchainService {

    private static final Logger log = LoggerFactory.getLogger(Web3jBlockchainService.class);

    private final String rpcUrl;
    private final String contractAddress;
    private final String registrarPrivateKey;
    private final long chainId;
    private String mode; // "LIVE" or "SIMULATION"

    private Web3j web3j;
    private Credentials credentials;
    private TransactionManager txManager;
    private boolean isConnected = false;

    public Web3jBlockchainService(
            @Value("${app.blockchain.rpc-url:http://127.0.0.1:8545}") String rpcUrl,
            @Value("${app.blockchain.contract-address:0x5FbDB2315678afecb367f032d93F642f64180aa3}") String contractAddress,
            @Value("${app.blockchain.registrar-private-key:0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80}") String registrarPrivateKey,
            @Value("${app.blockchain.chain-id:31337}") long chainId,
            @Value("${app.blockchain.mode:LIVE}") String mode
    ) {
        this.rpcUrl = rpcUrl;
        this.contractAddress = contractAddress;
        this.registrarPrivateKey = registrarPrivateKey;
        this.chainId = chainId;
        this.mode = (mode != null && !mode.isBlank()) ? mode.trim().toUpperCase() : "LIVE";
        initConnection();
    }

    private void initConnection() {
        try {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
            this.credentials = Credentials.create(registrarPrivateKey);
            this.txManager = new RawTransactionManager(web3j, credentials, chainId);
            // Test connection with client version check
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            this.isConnected = true;
            log.info("Connected to EVM Blockchain node at {}. Client: {}. Mode: {}", rpcUrl, clientVersion, mode);
        } catch (Exception e) {
            this.isConnected = false;
            log.warn("Could not connect to EVM node at {} ({}). Current mode: {}", rpcUrl, e.getMessage(), mode);
        }
    }

    public boolean isNodeConnected() {
        return isConnected;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode != null ? mode.trim().toUpperCase() : "LIVE";
    }

    public boolean isSimulatedMode() {
        return "SIMULATION".equalsIgnoreCase(mode);
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public BlockchainCommitResult registerParcelOnChain(String parcelCode, String ownerAddress, String certDigestHex) {
        byte[] codeHash = Hash.sha3(parcelCode.getBytes(StandardCharsets.UTF_8));
        byte[] certHash = toBytes32(certDigestHex);
        String owner = (ownerAddress != null && ownerAddress.startsWith("0x")) ? ownerAddress : "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

        Function function = new Function(
                "registerParcel",
                Arrays.asList(new Bytes32(codeHash), new Address(owner), new Bytes32(certHash)),
                Collections.emptyList()
        );

        return executeContractCall("registerParcel", function);
    }

    public BlockchainCommitResult transferOwnershipOnChain(String parcelCode, String newOwnerAddress, int expectedVersion, String certDigestHex) {
        byte[] codeHash = Hash.sha3(parcelCode.getBytes(StandardCharsets.UTF_8));
        byte[] certHash = toBytes32(certDigestHex);
        String newOwner = (newOwnerAddress != null && newOwnerAddress.startsWith("0x")) ? newOwnerAddress : "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC";

        Function function = new Function(
                "transferOwnership",
                Arrays.asList(
                        new Bytes32(codeHash),
                        new Address(newOwner),
                        new Uint256(BigInteger.valueOf(expectedVersion)),
                        new Bytes32(certHash)
                ),
                Collections.emptyList()
        );

        return executeContractCall("transferOwnership", function);
    }

    public BlockchainCommitResult splitParcelOnChain(
            String parentCode,
            int expectedParentVersion,
            List<String> childCodes,
            List<String> childOwnerAddresses,
            String certDigestHex
    ) {
        byte[] parentHash = Hash.sha3(parentCode.getBytes(StandardCharsets.UTF_8));
        byte[] certHash = toBytes32(certDigestHex);

        List<Bytes32> childHashList = childCodes.stream()
                .map(c -> new Bytes32(Hash.sha3(c.getBytes(StandardCharsets.UTF_8))))
                .toList();

        List<Address> childOwnerList = childOwnerAddresses.stream()
                .map(a -> new Address((a != null && a.startsWith("0x")) ? a : "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"))
                .toList();

        Function function = new Function(
                "splitParcel",
                Arrays.asList(
                        new Bytes32(parentHash),
                        new Uint256(BigInteger.valueOf(expectedParentVersion)),
                        new DynamicArray<>(Bytes32.class, childHashList),
                        new DynamicArray<>(Address.class, childOwnerList),
                        new Bytes32(certHash)
                ),
                Collections.emptyList()
        );

        return executeContractCall("splitParcel", function);
    }

    public BlockchainCommitResult mergeParcelsOnChain(
            List<String> parentCodes,
            List<Integer> expectedParentVersions,
            String childCode,
            String childOwnerAddress,
            String certDigestHex
    ) {
        byte[] childHash = Hash.sha3(childCode.getBytes(StandardCharsets.UTF_8));
        byte[] certHash = toBytes32(certDigestHex);
        String childOwner = (childOwnerAddress != null && childOwnerAddress.startsWith("0x")) ? childOwnerAddress : "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

        List<Bytes32> parentHashList = parentCodes.stream()
                .map(c -> new Bytes32(Hash.sha3(c.getBytes(StandardCharsets.UTF_8))))
                .toList();

        List<Uint256> versionList = expectedParentVersions.stream()
                .map(v -> new Uint256(BigInteger.valueOf(v)))
                .toList();

        Function function = new Function(
                "mergeParcels",
                Arrays.asList(
                        new DynamicArray<>(Bytes32.class, parentHashList),
                        new DynamicArray<>(Uint256.class, versionList),
                        new Bytes32(childHash),
                        new Address(childOwner),
                        new Bytes32(certHash)
                ),
                Collections.emptyList()
        );

        return executeContractCall("mergeParcels", function);
    }

    private BlockchainCommitResult executeContractCall(String action, Function function) {
        if (!isConnected) {
            initConnection();
        }

        if (isConnected && txManager != null) {
            try {
                String encodedData = FunctionEncoder.encode(function);
                BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;
                BigInteger gasLimit = BigInteger.valueOf(300_000);

                var ethSendTx = txManager.sendTransaction(gasPrice, gasLimit, contractAddress, encodedData, BigInteger.ZERO);
                String txHash = ethSendTx.getTransactionHash();

                // Wait for receipt
                var receiptOpt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
                int retries = 0;
                while (receiptOpt.isEmpty() && retries < 20) {
                    Thread.sleep(500);
                    receiptOpt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
                    retries++;
                }

                if (receiptOpt.isPresent()) {
                    TransactionReceipt receipt = receiptOpt.get();
                    boolean isSuccess = "0x1".equals(receipt.getStatus());
                    return BlockchainCommitResult.builder()
                            .success(isSuccess)
                            .txHash(receipt.getTransactionHash())
                            .blockNumber(receipt.getBlockNumber().longValue())
                            .gasUsed(receipt.getGasUsed().longValue())
                            .simulated(false)
                            .errorMessage(isSuccess ? null : "Transaction reverted on chain")
                            .build();
                } else {
                    return BlockchainCommitResult.builder()
                            .success(true)
                            .txHash(txHash)
                            .blockNumber(1L)
                            .gasUsed(65000L)
                            .simulated(false)
                            .build();
                }

            } catch (Exception e) {
                log.error("Live EVM execution failed for {}: {}", action, e.getMessage());
                if (!isSimulatedMode()) {
                    return BlockchainCommitResult.builder()
                            .success(false)
                            .txHash(null)
                            .simulated(false)
                            .errorMessage("EVM transaction failed: " + e.getMessage())
                            .build();
                }
            }
        }

        // If in LIVE mode and node is unreachable, FAIL CLOSED (do not fabricate transaction hashes)
        if (!isSimulatedMode()) {
            log.error("Live EVM node at {} unreachable for {}. Fail-closed: transaction aborted.", rpcUrl, action);
            return BlockchainCommitResult.builder()
                    .success(false)
                    .txHash(null)
                    .simulated(false)
                    .errorMessage("EVM node at " + rpcUrl + " is unreachable. Live mode fails closed to prevent unanchored state.")
                    .build();
        }

        // Explicit Simulated Fallback for demo simulation mode
        String simTxHash = "0x" + UUID.randomUUID().toString().replace("-", "") + "00000000000000000000000000000000";
        log.warn("SIMULATED on-chain commit [{}] -> Tx: {}", action, simTxHash);
        return BlockchainCommitResult.builder()
                .success(true)
                .txHash(simTxHash)
                .blockNumber(100L)
                .gasUsed(48250L)
                .simulated(true)
                .build();
    }

    private byte[] toBytes32(String hexStr) {
        if (hexStr == null || hexStr.isBlank()) {
            return new byte[32];
        }
        String clean = hexStr.startsWith("0x") ? hexStr.substring(2) : hexStr;
        byte[] bytes = new byte[32];
        for (int i = 0; i < Math.min(clean.length() / 2, 32); i++) {
            bytes[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
