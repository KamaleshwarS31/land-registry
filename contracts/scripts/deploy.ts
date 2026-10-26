import { ethers } from "hardhat";

async function main() {
  const [deployer, registrar] = await ethers.getSigners();

  console.log("Deploying LandRegistry contract with account:", deployer.address);
  const balance = await ethers.provider.getBalance(deployer.address);
  console.log("Account balance:", ethers.formatEther(balance), "ETH");

  const LandRegistry = await ethers.getContractFactory("LandRegistry");
  const landRegistry = await LandRegistry.deploy();
  await landRegistry.waitForDeployment();

  const contractAddress = await landRegistry.getAddress();
  console.log("LandRegistry deployed successfully at address:", contractAddress);

  if (registrar) {
    const tx = await landRegistry.setRegistrarAuthorization(registrar.address, true);
    await tx.wait();
    console.log(`Authorized registrar address: ${registrar.address}`);
  }

  console.log("\n=======================================================");
  console.log(`Update your .env file:`);
  console.log(`CONTRACT_ADDRESS=${contractAddress}`);
  console.log("=======================================================\n");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
