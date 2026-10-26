# Blockchain-Based Land Registration System
## Provenance-Aware Uncertainty and Parcel-Lineage Verification Framework

A production-grade land registration platform integrating:
- **M5 Contradiction Reconciliation Engine**: Normalizes multi-source assertions (Registration Deeds, Cadastral Surveys, Revenue Records, Court Injunctions, Mortgage Registries) and generates source-linked `ConflictWitness` explanations.
- **M8 Uncertainty-Aware Predicate Evaluator**: Implements 3-valued logic (`SATISFIED`, `VIOLATED`, `UNRESOLVED`). It treats missing, ambiguous, or stale evidence as `UNRESOLVED` (holding the transaction for review) rather than falsely assuming validity, and rejects any transaction with a `VIOLATED` condition.
- **M2 Parcel-Lineage & Geometric Verifier**: Leverages LocationTech JTS Topology Suite to enforce parcel ancestry DAGs, parent deactivation, non-overlapping child partitions, contiguous merges, and strict area-conservation tolerances ($\pm 0.1\%$).
- **EVM Smart Contract (`LandRegistry.sol`)**: Atomic state version checking (TOCTOU defense against stale concurrent races) and cryptographic decision certificate digest anchoring.
- **Supabase Integration**: Off-chain evidence document storage via Supabase Storage bucket (`land-evidence`) with time-limited signed URLs, plus direct Supabase PostgreSQL compatibility.
- **Modern Web Application**: React 18, TypeScript, Vite, Tailwind CSS with role-tailored dashboards for Landowners, Buyers, Registrars, Survey Reviewers, and System Admins.

---

## Architecture Overview

```
land-registry/
├── backend/            # Spring Boot 3.3+ (Java 21), Spring Security, JPA, Flyway, JTS, Web3j
├── contracts/          # Solidity 0.8.24, Hardhat, TypeScript deployment & verification tests
├── frontend/           # React 18, TypeScript, Vite, Tailwind CSS, Lucide icons
├── experiments/        # Synthetic evaluation suite, baselines (B0-B2), ablations (A1-A5), CSV metrics
├── docker-compose.yml  # Local PostgreSQL container service
└── .env.example        # Environment variables & Supabase placeholders
```

---

## Quick Start Guide

### 1. Prerequisites
- **Java 21** & **Maven 3.9+**
- **Node.js 18+** & **npm**
- *(Optional)* Docker Desktop (if running local PostgreSQL instead of H2 or cloud Supabase)

### 2. Configure Environment
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Fill in your Supabase credentials when using Supabase Storage and PostgreSQL.
By default, the backend runs in `dev` profile with zero-dependency embedded H2 and local file storage fallback.

### 3. Local EVM Blockchain (Hardhat)
In a dedicated terminal:
```bash
cd contracts
npm install
npx hardhat test
npx hardhat node
```
In another terminal, deploy the contract:
```bash
npx hardhat run scripts/deploy.ts --network localhost
```

### 4. Start Spring Boot Backend
```bash
cd backend
mvn spring-boot:run
```
Backend starts on `http://localhost:8080`.
Pre-seeded demo accounts:
- **Alice Vance (Landowner)**: `alice` / `password123`
- **Bob Smith (Buyer)**: `bob` / `password123`
- **Clara Oswald (Registrar)**: `clara` / `password123`
- **Dan Miller (Surveyor)**: `dan` / `password123`
- **Eve Polastri (Admin)**: `eve` / `password123`

### 5. Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## Running Verification & Experiments

### 1. Smart Contract Tests (Hardhat)
```bash
cd contracts
npx hardhat test
```
*Validates parcel registration, transfer, split, merge, caller permissions, and atomic revert on stale versions.*

### 2. Backend Unit & Integration Tests (JUnit 5)
```bash
cd backend
mvn test
```
*Validates M5 contradiction fixtures, M8 3-valued truth table, M2 JTS geometric conservation, and end-to-end database workflow.*

### 3. Conference Benchmark Suite
```bash
cd backend
mvn test -Dtest=ConferenceBenchmarkTest
```
*Executes baselines (B0, B1, B2) vs Proposed (P) across 500 synthetic workloads. Generates `experiments/results/metrics.csv`.*
*Demonstrates that Proposed Framework P achieves **0.0% Unsafe Authorization Rate**, **1.000 Conflict F1**, and **64% validation work reduction**.*
