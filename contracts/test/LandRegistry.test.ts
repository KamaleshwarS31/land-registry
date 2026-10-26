import { expect } from "chai";
import { ethers } from "hardhat";
import { LandRegistry } from "../typechain-types";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";

describe("LandRegistry Smart Contract", () => {
  let landRegistry: LandRegistry;
  let owner: HardhatEthersSigner;
  let registrar: HardhatEthersSigner;
  let alice: HardhatEthersSigner;
  let bob: HardhatEthersSigner;
  let mallory: HardhatEthersSigner;

  const parcel1Code = ethers.keccak256(ethers.toUtf8Bytes("PARCEL-DL-001"));
  const parcel2Code = ethers.keccak256(ethers.toUtf8Bytes("PARCEL-DL-002"));
  const child1Code = ethers.keccak256(ethers.toUtf8Bytes("PARCEL-DL-001-A"));
  const child2Code = ethers.keccak256(ethers.toUtf8Bytes("PARCEL-DL-001-B"));
  const mergedChildCode = ethers.keccak256(ethers.toUtf8Bytes("PARCEL-DL-MERGED"));

  const certDigest1 = ethers.keccak256(ethers.toUtf8Bytes("CERT-DECISION-001"));
  const certDigest2 = ethers.keccak256(ethers.toUtf8Bytes("CERT-DECISION-002"));

  beforeEach(async () => {
    [owner, registrar, alice, bob, mallory] = await ethers.getSigners();
    const Factory = await ethers.getContractFactory("LandRegistry");
    landRegistry = (await Factory.deploy()) as LandRegistry;
    await landRegistry.waitForDeployment();

    // Authorize registrar
    await landRegistry.setRegistrarAuthorization(registrar.address, true);
  });

  describe("Access Control", () => {
    it("should allow owner to authorize registrar", async () => {
      expect(await landRegistry.authorizedRegistrars(registrar.address)).to.be.true;
    });

    it("should reject non-registrar attempts to register parcel", async () => {
      await expect(
        landRegistry.connect(mallory).registerParcel(parcel1Code, alice.address, certDigest1)
      ).to.be.revertedWith("Caller is not an authorized registrar");
    });
  });

  describe("Parcel Registration", () => {
    it("should register a new parcel with version 1 and active status", async () => {
      await expect(
        landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1)
      )
        .to.emit(landRegistry, "ParcelRegistered")
        .withArgs(parcel1Code, alice.address, certDigest1, (await ethers.provider.getBlockNumber()) + 1);

      const record = await landRegistry.getParcel(parcel1Code);
      expect(record.codeHash).to.equal(parcel1Code);
      expect(record.currentOwner).to.equal(alice.address);
      expect(record.version).to.equal(1n);
      expect(record.isActive).to.be.true;
      expect(record.lastCertificateDigest).to.equal(certDigest1);
    });

    it("should reject duplicate registration of the same parcel", async () => {
      await landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1);
      await expect(
        landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1)
      ).to.be.revertedWith("Parcel already registered");
    });
  });

  describe("Ownership Transfer & Atomic Version Checking (TOCTOU Defense)", () => {
    beforeEach(async () => {
      await landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1);
    });

    it("should successfully transfer ownership when expectedVersion matches", async () => {
      await expect(
        landRegistry
          .connect(registrar)
          .transferOwnership(parcel1Code, bob.address, 1, certDigest2)
      )
        .to.emit(landRegistry, "OwnershipTransferred")
        .withArgs(
          parcel1Code,
          alice.address,
          bob.address,
          2n,
          certDigest2,
          (await ethers.provider.getBlockNumber()) + 1
        );

      const record = await landRegistry.getParcel(parcel1Code);
      expect(record.currentOwner).to.equal(bob.address);
      expect(record.version).to.equal(2n);
      expect(record.isActive).to.be.true;
    });

    it("should revert if expectedVersion is stale (race condition defense)", async () => {
      // Transfer to Bob updates version to 2
      await landRegistry.connect(registrar).transferOwnership(parcel1Code, bob.address, 1, certDigest2);

      // Attempt concurrent transfer expecting stale version 1
      await expect(
        landRegistry
          .connect(registrar)
          .transferOwnership(parcel1Code, mallory.address, 1, certDigest2)
      ).to.be.revertedWith("Stale state version: expected version does not match");
    });
  });

  describe("Parcel Split Operations", () => {
    beforeEach(async () => {
      await landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1);
    });

    it("should split parent parcel into children and record lineage edges", async () => {
      await expect(
        landRegistry
          .connect(registrar)
          .splitParcel(
            parcel1Code,
            1,
            [child1Code, child2Code],
            [alice.address, bob.address],
            certDigest2
          )
      )
        .to.emit(landRegistry, "ParcelSplit")
        .withArgs(parcel1Code, [child1Code, child2Code], certDigest2, (await ethers.provider.getBlockNumber()) + 1);

      // Parent must be deactivated
      const parentRecord = await landRegistry.getParcel(parcel1Code);
      expect(parentRecord.isActive).to.be.false;
      expect(parentRecord.version).to.equal(2n);

      // Children must be active with version 1
      const child1 = await landRegistry.getParcel(child1Code);
      expect(child1.isActive).to.be.true;
      expect(child1.currentOwner).to.equal(alice.address);
      expect(child1.version).to.equal(1n);

      const child2 = await landRegistry.getParcel(child2Code);
      expect(child2.isActive).to.be.true;
      expect(child2.currentOwner).to.equal(bob.address);
      expect(child2.version).to.equal(1n);

      // Verify lineage count
      expect(await landRegistry.getLineageCount()).to.equal(2n);
    });

    it("should reject split with stale expected parent version", async () => {
      await expect(
        landRegistry
          .connect(registrar)
          .splitParcel(
            parcel1Code,
            99, // incorrect expected version
            [child1Code, child2Code],
            [alice.address, bob.address],
            certDigest2
          )
      ).to.be.revertedWith("Stale parent version");
    });
  });

  describe("Parcel Merge Operations", () => {
    beforeEach(async () => {
      await landRegistry.connect(registrar).registerParcel(parcel1Code, alice.address, certDigest1);
      await landRegistry.connect(registrar).registerParcel(parcel2Code, alice.address, certDigest1);
    });

    it("should merge parents into child and deactivate all parents", async () => {
      await expect(
        landRegistry
          .connect(registrar)
          .mergeParcels(
            [parcel1Code, parcel2Code],
            [1, 1],
            mergedChildCode,
            alice.address,
            certDigest2
          )
      )
        .to.emit(landRegistry, "ParcelMerged")
        .withArgs([parcel1Code, parcel2Code], mergedChildCode, certDigest2, (await ethers.provider.getBlockNumber()) + 1);

      const p1 = await landRegistry.getParcel(parcel1Code);
      const p2 = await landRegistry.getParcel(parcel2Code);
      expect(p1.isActive).to.be.false;
      expect(p2.isActive).to.be.false;

      const merged = await landRegistry.getParcel(mergedChildCode);
      expect(merged.isActive).to.be.true;
      expect(merged.currentOwner).to.equal(alice.address);
      expect(merged.version).to.equal(1n);

      expect(await landRegistry.getLineageCount()).to.equal(2n);
    });
  });
});
