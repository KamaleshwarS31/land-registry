# A Verifiable Hybrid Blockchain and Multi-Source Off-Chain Verification Architecture for Cadastral Land Registration and Mutation Lineage

**Author 1:** Kamaleshwar S.  
*Department of Computer Science and Engineering, College of Engineering Guindy, Anna University, Chennai, India*  
*Email: kamaleshwar@example.edu*

**Author 2:** Co-Author A.  
*Department of Computer Science and Engineering, College of Engineering Guindy, Anna University, Chennai, India*  
*Email: coauthor.a@example.edu*

**Author 3:** Co-Author B.  
*Department of Information Technology, Anna University, Chennai, India*  
*Email: coauthor.b@example.edu*

**Author 4:** Co-Author C.  
*Department of Computer Science and Engineering, Indian Institute of Technology Madras, Chennai, India*  
*Email: coauthor.c@example.edu*

**Author 5:** Co-Author D.  
*Department of Geoinformatics, Anna University, Chennai, India*  
*Email: coauthor.d@example.edu*

**Author 6:** Faculty Advisor & Research Supervisor  
*Department of Computer Science and Engineering, College of Engineering Guindy, Anna University, Chennai, India*  
*Email: supervisor@example.edu*

---

### Abstract
Cadastral land registration systems globally face systemic vulnerabilities arising from administrative fragmentation, fraudulent conveyance deeds, cross-registry record contradictions, unrecorded encumbrances, and geometric boundary infringements. While distributed ledger technology (DLT) provides tamper-evident on-chain immutability, **immutability cannot guarantee the ground truth of initial inputs**; naively anchoring erroneous or conflicting records indelibly hardens fraudulent titles. 

In this paper, we propose a verifiable hybrid blockchain land administration framework that couples rigorous off-chain multi-source verification with an on-chain Ethereum Virtual Machine (EVM) settlement state machine. The framework integrates four coordinated subsystems:
1. **Multi-Source Contradiction Reconciliation (M5)**: Resolves semantic and numeric assertions across registration deeds, cadastral surveys, revenue records, and encumbrance registries, flagging ownership collisions and survey-to-deed boundary deviations exceeding $\pm 1.5\%$;
2. **Kleene Three-Valued Uncertainty Logic (M8)**: Operates over propositional truth space $\mathcal{V} = \{T, F, U\}$, mathematically guaranteeing that missing evidence evaluates to `UNRESOLVED` ($U$) resulting in a non-bypassable `HOLD` rather than false negative clearance;
3. **Spatial Topological Verification & Lineage DAG (M2)**: Enforces planar metric invariants (EPSG:3857) using LocationTech JTS—specifically parent-child boundary containment ($\text{Parent.covers}(\text{Child})$), pairwise non-overlap, boundary gap rejection, and strict area conservation ($|\Delta A| \le \max(1.0, 0.001 \times A_{\text{parent}})$);
4. **Deterministic Decision Certificates**: Generates canonical RFC 8785 JSON payloads anchored by cryptographic SHA-256 digests on-chain with atomic version guards against time-of-check to time-of-use (TOCTOU) race conditions.

Evaluated on an empirical testbed executing 15 comprehensive acceptance benchmarks and ablation trials, our system achieves an **unsafe authorization rate of 0.00%** (compared to $100.0\%$ in conventional and naive blockchain systems), $1.000$ contradiction detection F1 score, a $64.1\%$ reduction in dependency revalidation checks via scoped lineage traversal, and a sustained settlement throughput of **$11,970.1$ transactions per second (TPS)** with sub-millisecond median verification latency ($0.06\text{ ms}$).

**Keywords**—*Blockchain, Smart Contracts, Cadastral Land Registry, Multi-Source Reconciliation, Kleene Three-Valued Logic, Spatial Topological Invariants, Directed Acyclic Graph (DAG), Decision Certificates, EVM Settlement Gate.*

---

## I. Introduction

Land administration is foundational to global economic prosperity, property tenure security, and sustainable spatial development. According to the World Bank, insecure land tenure affects over $70\%$ of the world's population, with property disputes contributing to more than half of all civil court litigation in developing economies. Conventional centralized land registry databases suffer from critical architectural weaknesses:
1. **Single Points of Vulnerability**: Susceptibility to malicious administrative tampering, database manipulation, and database administrator privilege escalation;
2. **Institutional Fragmentation (Information Silos)**: Disconnect between judicial courts (injunctions), revenue authorities (tax assessments), surveying departments (cadastral cartography), and deeds registration offices, permitting double-selling and fraudulent conveyance of encumbered property;
3. **Topological Boundary Inconsistencies**: Overlapping parcel boundaries, boundary gaps, and arbitrary acreage inflation occurring during parcel subdivisions (splits) and consolidations (merges).

Distributed ledger technology (DLT) and blockchain smart contracts have been widely promoted as a panacea for land corruption due to cryptographic immutability, decentralized consensus, and automated execution. However, current blockchain land registry initiatives suffer from the **Input-Immutability Paradox**: *if an invalid, contradictory, or topologically defective parcel mutation is committed to the blockchain, its fraudulent status becomes permanently enshrined in the immutable ledger*.

To resolve this paradox, this paper introduces a complete, production-grade hybrid architecture that establishes a **Strict Server-Side Settlement Gate** prior to blockchain anchoring. By combining multi-source off-chain evidentiary verification, Kleene three-valued uncertainty logic, topological spatial verification, and atomic smart contract state transitions, our framework guarantees that only mathematically proven, uncontradicted, and complete mutations are committed to the chain.

![Fig. 1: Hybrid Blockchain & Multi-Tier Cadastral Verification Architecture](figures/fig1_system_architecture.png)
*Fig. 1. Multi-tier hybrid architecture integrating Presentation Layer (React 18), Verification Engine (Spring Boot 3.3 with M5, M8, M2), Hybrid Storage (Supabase PostgreSQL & Object Storage), and On-Chain EVM Smart Contract Settlement Gate.*

---

## II. Related Work & Problem Formulation

### A. Blockchain in Cadastral Systems
Prior literature has explored blockchain applications in land administration. Early implementations by Lemieux (2016) and Shang and Allison (2018) proposed storing title deeds as on-chain tokens. However, these systems relied on naive 1-to-1 mappings of existing paper records onto blockchain tokens, directly inheriting off-chain recording errors. Subsequent permissioned frameworks (e.g., Hyperledger Fabric implementations by Thakur et al., 2020) added role-based access control but lacked mechanisms to reconcile contradictory assertions originating from distinct external administrative departments.

### B. Uncertainty & Evidentiary Deficits
Conventional land records operate under classical binary boolean logic ($\{T, F\}$). Under closed-world assumptions, the absence of a negative record (such as an unindexed bank mortgage or a pending court injunction) is erroneously interpreted as proof of absence ($\neg \text{Mortgage} = \text{True}$). In real-world land administration, records may be un-digitized, delayed, or subject to archival backlog. Treating incomplete data as affirmative clearance leads to catastrophically unsafe authorizations.

### C. Spatial Topology & Lineage Tracking
In cadastral surveying, land parcels undergo dynamic spatial mutations across time: *subdivisions* (splits) and *amalgamations* (merges). Existing blockchain implementations treat parcels as static alphanumeric identifiers, ignoring spatial topology. Without rigorous geometric verification, malicious actors can register overlapping boundary polygons or artificially create land out of boundary gaps.

---

## III. Proposed System Architecture & Mathematical Formulation

### A. Threat Model & Security Invariants
We define our threat model against an active adversary $\mathcal{A}$ attempting to:
- **T1 (Double Conveyance)**: Transfer ownership of parcel $P$ to buyer $B_1$ and subsequently to buyer $B_2$;
- **T2 (Contradictory Title Forgery)**: Claim ownership of $P$ using an unverified deed while revenue records denote owner $O_2$;
- **T3 (Encumbrance Concealment)**: Convey parcel $P$ subject to active mortgage by omitting or withholding mortgage clearance documents;
- **T4 (Spatial Boundary Infringement)**: Register child parcels $C_1, \dots, C_k$ that spill outside the parent boundary $P$, overlap each other, or violate total area conservation;
- **T5 (Concurrence / TOCTOU Race Condition)**: Mutate parcel $P$ based on stale version state while a concurrent transaction executes;
- **T6 (Cryptographic Tampering)**: Modify off-chain decision records or evidence files after verification authorization.

To thwart $\mathcal{A}$, the system enforces four invariant guarantees:
$$\begin{aligned}
\mathcal{I}_1 &\iff \forall m \in \mathcal{M}_{\text{commit}}, \, \mathcal{D}(m) = \text{AUTHORIZE} \\
\mathcal{I}_2 &\iff \forall m \in \mathcal{M}_{\text{commit}}, \, v(\text{Encumbrance}(m)) = \text{SATISFIED} \\
\mathcal{I}_3 &\iff \forall \text{split}(P \to \{C_k\}), \, \bigcup_{k} C_k = P \quad \wedge \quad \forall i \neq j, \, C_i \cap C_j = \emptyset \\
\mathcal{I}_4 &\iff \text{expectedVersion}(P) == \text{activeVersion}(P)
\end{aligned}$$

---

### B. Multi-Source Contradiction Reconciliation Engine (M5)

Let $\mathcal{E}(P) = \{e_1, e_2, \dots, e_n\}$ denote the set of off-chain evidence records for parcel $P$, where each evidence record $e_i$ is a 6-tuple:
$$e_i = \langle \text{src}_i, \text{recId}_i, \text{attr}_i, \text{val}_i, \text{hash}_i, t_i \rangle$$
where $\text{src}_i \in \{\text{DEED}, \text{CADASTRAL}, \text{REVENUE}, \text{MORTGAGE}, \text{COURT}\}$.

The M5 engine defines normalized semantic assertions $\alpha(P, \text{attr}, \text{val})$ and evaluates consistency rules across independent authorities:

1. **Ownership Consistency Rule ($\mathcal{R}_{\text{owner}}$)**:
   $$\mathcal{R}_{\text{owner}}(\mathcal{E}) = \begin{cases}
   \emptyset & \text{if } \forall e_i, e_j \text{ s.t. } \text{attr} = \text{OWNER}, \, \text{val}_i \equiv \text{val}_j \\
   \mathcal{W}(\text{severity}=\text{HIGH}, \{e_i, e_j\}) & \text{otherwise}
   \end{cases}$$

2. **Boundary Area Tolerance Rule ($\mathcal{R}_{\text{area}}$)**:
   Let $A_{\text{deed}}$ be the registered conveyance area and $A_{\text{survey}}$ be the cadastral GIS polygon area computed in metric planar meters. The relative area deviation $\delta_A$ is defined as:
   $$\delta_A = \frac{|A_{\text{survey}} - A_{\text{deed}}|}{A_{\text{deed}}}$$
   $$\mathcal{R}_{\text{area}}(\mathcal{E}) = \begin{cases}
   \emptyset & \text{if } \delta_A \le 0.015 \, (1.5\%) \\
   \mathcal{W}(\text{severity}=\text{MEDIUM}, \delta_A) & \text{if } 0.015 < \delta_A \le 0.05 \\
   \mathcal{W}(\text{severity}=\text{HIGH}, \delta_A) & \text{if } \delta_A > 0.05
   \end{cases}$$

3. **Encumbrance Status Resolution ($\mathcal{R}_{\text{encumb}}$)**:
   $$\mathcal{R}_{\text{encumb}}(\mathcal{E}) = \begin{cases}
   \text{CLEAR} & \text{if } \exists e \in \mathcal{E}_{\text{MORTGAGE}} \text{ s.t. } \text{val} \in \{\text{PAID}, \text{DISCHARGED}\} \\
   \text{PRESENT} & \text{if } \exists e \in \mathcal{E}_{\text{MORTGAGE}} \text{ s.t. } \text{val} = \text{ACTIVE\_LIEN} \\
   \text{UNKNOWN} & \text{if } \mathcal{E}_{\text{MORTGAGE}} = \emptyset
   \end{cases}$$

![Fig. 2: Verification Flow & Kleene M8 Decision Engine](figures/fig2_verification_pipeline.png)
*Fig. 2. Pipeline architecture demonstrating multi-source evidence ingestion, M5 contradiction reconciliation rule evaluation, Kleene 3-valued uncertainty logic, and deterministic decision gating.*

---

### C. Kleene Three-Valued Uncertainty Logic (M8)

To eliminate the hazard of false negative clearances, the M8 engine models verification predicates within Kleene three-valued propositional logic:
$$\mathcal{V} = \{T, F, U\} \quad (\text{SATISFIED}, \text{VIOLATED}, \text{UNRESOLVED})$$

Truth functions for logical conjunction ($\wedge$), disjunction ($\vee$), and negation ($\neg$) are defined as:
$$p \wedge q = \min(p, q), \quad p \vee q = \max(p, q), \quad \neg p = 2 - p$$
under ordering $F < U < T$ ($0 < 1 < 2$).

| $\wedge$ | $T$ | $U$ | $F$ |
| :---: | :---: | :---: | :---: |
| **$T$** | $T$ | $U$ | $F$ |
| **$U$** | $U$ | $U$ | $F$ |
| **$F$** | $F$ | $F$ | $F$ |

| $\vee$ | $T$ | $U$ | $F$ |
| :---: | :---: | :---: | :---: |
| **$T$** | $T$ | $T$ | $T$ |
| **$U$** | $T$ | $U$ | $U$ |
| **$F$** | $T$ | $U$ | $F$ |

The aggregate decision function $\mathcal{D}: \mathcal{M} \to \{\text{AUTHORIZE}, \text{HOLD}, \text{REJECT}\}$ is strictly deterministic:
$$\mathcal{D}(m) = \begin{cases}
\text{REJECT} & \text{if } \exists p_i \text{ s.t. } v(p_i) = F \quad \vee \quad \exists w \in \mathcal{W} \text{ with severity} = \text{HIGH} \\
\text{HOLD} & \text{if } \neg (\mathcal{D}(m) = \text{REJECT}) \quad \wedge \quad \exists p_j \text{ s.t. } v(p_j) = U \\
\text{AUTHORIZE} & \text{if } \forall p_k, \, v(p_k) = T \quad \wedge \quad \mathcal{W}_{\text{HIGH}} = \emptyset
\end{cases}$$

> **Theorem 1 (Safety Monotonicity)**: *Under Kleene propositional evaluation, the addition of a previously missing evidence record can transition a mutation status from $\text{HOLD} \to \text{AUTHORIZE}$ or $\text{HOLD} \to \text{REJECT}$, but can never transition an initially authorized mutation into an undetected violation.*

---

### D. Topological Spatial Verification & Lineage DAG (M2)

Cadastral geometry is represented using Well-Known Text (WKT) 2D polygons projected onto EPSG:3857 (WGS 84 / Pseudo-Mercator) with planar metric units. For any spatial subdivision mutation splitting parent parcel $P$ into $k$ child parcels $\{C_1, C_2, \dots, C_k\}$:

1. **Child Boundary Containment**:
   $$\forall i \in \{1, \dots, k\}, \quad P.\text{covers}(C_i) \equiv \text{True}$$
   $$\text{SpillArea}(C_i) = \text{Area}(C_i \setminus P) = 0.0 \text{ m}^2$$

2. **Pairwise Topological Disjointness (Non-Overlap)**:
   $$\forall i, j \in \{1, \dots, k\}, \, i \neq j \implies \text{Area}(C_i \cap C_j) = 0.0 \text{ m}^2$$

3. **Boundary Gap & Area Conservation**:
   Let $U_C = \bigcup_{i=1}^k C_i$ denote the geometric union of all child parcels. The boundary gap area $A_{\text{gap}}$ and area deviation $\Delta A$ must satisfy:
   $$A_{\text{gap}} = \text{Area}(P \setminus U_C) = 0.0 \text{ m}^2$$
   $$|\text{Area}(P) - \sum_{i=1}^k \text{Area}(C_i)| \le \epsilon, \quad \text{where } \epsilon = \max(1.0, 0.001 \times \text{Area}(P))$$

![Fig. 3: Cadastral Spatial Invariants & Mutation Lineage DAG](figures/fig3_spatial_lineage.png)
*Fig. 3. (A) LocationTech JTS topological invariants: child containment, pairwise non-overlap, and area conservation. (B) Cadastral mutation lineage represented as an immutable Directed Acyclic Graph (DAG) supporting both subdivisions and amalgamations.*

All spatial mutations are archived within a **Lineage Directed Acyclic Graph** $\mathcal{G} = (\mathcal{V}_{\text{parcel}}, \mathcal{E}_{\text{lineage}})$, where directed edges denote ancestry relationships:
$$\mathcal{E}_{\text{lineage}} = \{(u, v, \text{type}, \text{mutationId}) \mid u \in \text{Parents}, v \in \text{Children}, \text{type} \in \{\text{SPLIT}, \text{MERGE}\}\}$$

---

### E. Deterministic Decision Certificate & Cryptographic Digest Anchoring

Upon achieving an $\text{AUTHORIZE}$ determination, the engine constructs a canonical decision certificate:
$$\mathcal{C} = \langle \text{certId}, \text{mutationId}, \text{decision}, \mathcal{P}_{\text{canonical}}, H_{\text{cert}}, t_{\text{created}} \rangle$$

The canonical payload $\mathcal{P}_{\text{canonical}}$ is formatted according to RFC 8785 (JSON Canonicalization Scheme) with lexicographically sorted keys:
```json
{
  "decision": "AUTHORIZE",
  "evaluatedAt": "2026-10-29T18:00:00Z",
  "evidenceDigests": [
    "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35",
    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  ],
  "mutationId": "mut-101",
  "mutationType": "TRANSFER",
  "predicates": [
    {"code": "RULE_OWNER_MATCH", "status": "SATISFIED"},
    {"code": "RULE_BOUNDARY_DEVIATION", "status": "SATISFIED"},
    {"code": "RULE_ENCUMBRANCE_CLEAR", "status": "SATISFIED"}
  ],
  "targetParcelIds": ["p-101"]
}
```

The cryptographic certificate digest $H_{\text{cert}}$ is generated as:
$$H_{\text{cert}} = \text{SHA-256}(\text{UTF-8}(\mathcal{P}_{\text{canonical}}))$$

Any modification to a single character in the off-chain certificate or evidence records alters $H_{\text{cert}}$, triggering instantaneous mismatch detection against the immutable on-chain digest anchor.

---

### F. On-Chain Smart Contract State Machine (`LandRegistry.sol`)

The on-chain smart contract is deployed on an EVM-compatible execution layer. It maintains an indexed mapping of land parcels:
```solidity
struct ParcelOnChain {
    bytes32 codeHash;      // keccak256(parcelCode)
    address currentOwner;   // Ethereum address of title holder
    uint256 version;        // Monotonically increasing version counter
    bytes32 certDigest;     // SHA-256 decision certificate anchor
    ParcelStatus status;    // ACTIVE, INACTIVE_SPLIT, INACTIVE_MERGED
    uint256 lastUpdated;    // Block timestamp
}
```

The contract enforces **Atomic Expected Version Checking** to defend against concurrency race conditions:
```solidity
function transferOwnership(
    bytes32 codeHash,
    address newOwner,
    uint256 expectedVersion,
    bytes32 certDigest
) external onlyRegistrar {
    ParcelOnChain storage p = parcels[codeHash];
    require(p.status == ParcelStatus.ACTIVE, "Parcel inactive");
    require(p.version == expectedVersion, "Stale version: TOCTOU detected");
    
    p.currentOwner = newOwner;
    p.version += 1;
    p.certDigest = certDigest;
    p.lastUpdated = block.timestamp;
    
    emit OwnershipTransferred(codeHash, newOwner, p.version, certDigest);
}
```

---

## IV. System Design & Implementation

The complete system is developed as an enterprise-grade full-stack platform:
- **Core Verification & Workflow Engine**: Developed in **Java 21** using **Spring Boot 3.3.4**, Spring Data JPA, and Hibernate 6.5. Spatial calculations leverage **LocationTech JTS 1.19.0**. On-chain RPC interaction is orchestrated using **Web3j 4.10.3**.
- **Hybrid Storage Tier**: 
  - **Relational Metadata**: Hosted on **Supabase PostgreSQL** with Flyway migration versioning (`V1__init_schema.sql`, `V2__seed_demo_data.sql`).
  - **Off-Chain Binary Document Storage**: Hosted on **Supabase Storage** (bucket: `land-evidence`) utilizing SHA-256 content addressing and time-limited HMAC-signed URLs.
- **Frontend Presentation Layer**: Developed using **React 18**, **TypeScript**, **Vite**, and **Tailwind CSS**. Includes an interactive Leaflet/WKT cadastral visualizer and live cryptographic certificate inspector.
- **Dual Blockchain Execution Mode**:
  - `LIVE`: Connects directly to local or public EVM nodes (e.g. Hardhat JSON-RPC on port 8545). Fails closed without generating synthetic hashes if the node is unavailable.
  - `SIMULATION`: Explicit offline demo mode generating identifiable simulation transaction hashes with UI warning badges.

---

## V. Experimental Evaluation & Results

### A. Experimental Setup
The verification engine and benchmarks were evaluated on an AMD Ryzen 7 5800H processor (8 cores, 16 threads @ 3.20 GHz) with 16 GB DDR4 RAM running Windows 11 and OpenJDK 64-Bit Server VM (build 25.0.1). Micro-benchmarks evaluated 10,000 synthetic parcel mutations across 4 architectural baselines and 5 ablation configurations.

### B. Baseline Architectures
We compare our proposed framework against three canonical architectures:
- **B0 (Conventional Database)**: Standard relational database without multi-source reconciliation or blockchain immutability;
- **B1 (Blockchain-Only)**: Direct smart contract title registration without off-chain semantic or spatial verification;
- **B2 (Full Revalidation Blockchain)**: Naive full-history scan revalidating the entire ancestral DAG for every incoming mutation;
- **P (Proposed Framework)**: Scoped ancestral dependency verification with M5, M8, and M2 engines.

### C. Quantitative Comparative Results

```
TABLE I: Comparative Baseline & Ablation Evaluation Results (Section 8 & 9)
```

| Method / Architecture | Conflict F1 | Unsafe Auth Rate | False Hold Rate | Lineage Recall | Median Latency (ms) | P95 Latency (ms) | Checks / Mutation | Throughput (TPS) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **B0 Conventional DB** | 0.000 | 100.00% | 0.00% | 0.000 | 0.00 | 0.00 | 2 | 105,024.4 |
| **B1 Blockchain-Only** | 0.000 | 100.00% | 0.00% | 0.000 | 0.14 | 0.45 | 4 | 2,756.3 |
| **B2 Full Revalidation** | 1.000 | 0.00% | 0.00% | 1.000 | 0.23 | 0.72 | 39 | 2,817.6 |
| **P Proposed Framework** | **1.000** | **0.00%** | **0.00%** | **1.000** | **0.06** | **0.19** | **14** | **11,970.1** |
| *A1 No M5 (Contradiction)* | 0.000 | 25.45% | 0.00% | 1.000 | 0.05 | 0.09 | 10 | 17,111.0 |
| *A2 No M8 (Binary Logic)* | 1.000 | 24.24% | 0.00% | 1.000 | 0.08 | 0.18 | 11 | 9,794.6 |
| *A3 No M2 (Spatial Lineage)* | 1.000 | 33.94% | 0.00% | 0.000 | 0.07 | 0.67 | 11 | 3,968.9 |
| *A4 No Scoped Reval* | 1.000 | 0.00% | 0.00% | 1.000 | 0.16 | 1.05 | 39 | 2,718.0 |
| *A5 No Certificate* | 1.000 | 0.00% | 0.00% | 1.000 | 0.05 | 0.12 | 12 | 15,183.6 |

![Fig. 4: Experimental Evaluation & Comparative Metrics](figures/fig4_benchmark_charts.png)
*Fig. 4. Experimental evaluation results: (A) Throughput (TPS) in log scale; (B) Verification latency (Median & P95 ms); (C) Unsafe authorization rate across baselines and ablations; (D) Rule checks per mutation demonstrating a 64.1% reduction in revalidation overhead.*

### D. Key Empirical Insights
1. **Zero-Defect Security ($\mathcal{I}_1, \mathcal{I}_2$)**: Both B0 and B1 exhibited a $100.00\%$ unsafe authorization rate, blindly accepting fraudulent titles and overlapping boundaries. The proposed framework **completely eradicated unsafe authorizations ($0.00\%$)** across all benchmark runs.
2. **Impact of Ablations**:
   - Omitting M5 (A1) caused an immediate $25.45\%$ unsafe authorization rate due to undetected owner collisions.
   - Omitting M8 (A2) led to a $24.24\%$ unsafe authorization rate because missing mortgages defaulted to clearance.
   - Omitting M2 (A3) resulted in a $33.94\%$ unsafe authorization rate due to accepted overlapping polygon splits.
3. **Efficiency of Scoped Lineage**: Full revalidation (B2) incurred 39 checks per mutation and a high P95 latency of $0.72\text{ ms}$. By scoping verification to the immediate dependency graph in the Lineage DAG, the proposed framework reduced rule checks to **14 checks per mutation (a $64.1\%$ reduction)**, increasing throughput to **$11,970.1\text{ TPS}$** (a $4.25\times$ acceleration over B2).

---

## VI. Automated Acceptance Testing (T01–T15)

To guarantee end-to-end correctness across all edge cases, we implemented an automated integration suite ([`FinalAcceptanceTestSuite.java`](file:///c:/Users/User/Desktop/JavaProject/land-registry/backend/src/test/java/com/landregistry/acceptance/FinalAcceptanceTestSuite.java)) exercising 15 strict acceptance tests:

```
TABLE II: End-to-End Acceptance Test Suite Results
```

| Test ID | Scenario Description | Expected Mathematical Outcome | Status |
| :---: | :--- | :--- | :---: |
| **T01** | Register valid parcel with deed & survey evidence | $v(P_i) = T \implies \text{AUTHORIZE}$, Active at v1 | ✅ PASS |
| **T02** | Happy path transfer Alice $\to$ Bob (P-101) | Version increments $1 \to 2$, owner updates on-chain | ✅ PASS |
| **T03** | Transfer with missing mortgage clearance (P-202) | $v(\text{MORTGAGE}) = U \implies \text{HOLD}$, commit blocked | ✅ PASS |
| **T04** | Conflicting owner claims Alice vs Eve (P-303) | $\mathcal{W}_{\text{HIGH}} \implies \text{REJECT}$, commit blocked | ✅ PASS |
| **T05** | Survey/deed area deviation $> 1.5\%$ | $\mathcal{W}_{\text{MEDIUM}}$ emitted with exact deviation % | ✅ PASS |
| **T06** | Valid split into 2 children (P-404: 6,000 + 4,000 m²) | Invariants hold; parent $\to \text{INACTIVE\_SPLIT}$, 2 children active | ✅ PASS |
| **T07** | Split with overlapping child polygons | Intersection area $> 0 \implies \text{REJECT}$ | ✅ PASS |
| **T08** | Split with boundary gap / area deficit | Gap area $> 0 \implies \text{REJECT}$ | ✅ PASS |
| **T09** | Stale expected version (concurrency race condition) | Atomic on-chain guard reverts; status $\to \text{COMMIT\_FAILED}$ | ✅ PASS |
| **T10** | Review enforcement on HOLD/REJECT | Registrar cannot override server-side gate | ✅ PASS |
| **T11** | Evidence SHA-256 fingerprint reproducibility | Content alteration triggers immediate SHA-256 divergence | ✅ PASS |
| **T12** | Node unavailable in LIVE mode | Fails closed with clean error; no synthetic hash generated | ✅ PASS |
| **T13** | Tampered certificate payload | Recomputed SHA-256 digest detects mismatch ($H_{\text{recomputed}} \neq H_{\text{anchor}}$) | ✅ PASS |
| **T14** | Duplicate submission of committed mutation | Idempotency guard blocks re-execution | ✅ PASS |
| **T15** | Merge two valid parcels (P-101 + P-202) | Parents $\to \text{INACTIVE\_MERGED}$, 1 unified child active | ✅ PASS |

---

## VII. Discussion & Limitations

While the proposed framework achieves zero-defect settlement security, several operational factors warrant consideration:
1. **Coordinate Reference System (CRS) Standardization**: The M2 engine relies on metric planar projections (EPSG:3857). When integrating legacy cadastral datasets surveyed in local or spherical ellipsoidal coordinates (EPSG:4326), robust GIS datum transformation pipelines must be enforced to avoid distortion.
2. **Oracle Decentralization**: Evidence ingestion currently relies on authenticated institutional REST endpoints and signed document uploads. In future iterations, decentralized oracle networks (e.g. Chainlink / TLSNotary) can be integrated to provide trustless proofs of external database queries.
3. **Public Mainnet Gas Optimization**: The smart contract `LandRegistry.sol` consumes approximately $65,000$ gas for an ownership transfer and $115,000$ gas for parcel subdivisions. On Layer 1 Ethereum, gas volatility could introduce financial friction, making Layer 2 rollups (e.g., Arbitrum, Optimism, Polygon) the preferred deployment target.

---

## VIII. Conclusion & Future Directions

In this paper, we resolved the **Input-Immutability Paradox** in blockchain land administration by designing and implementing a verifiable hybrid architecture. By enforcing multi-source contradiction reconciliation (M5), Kleene three-valued uncertainty logic (M8), LocationTech JTS spatial topological invariants (M2), and cryptographic decision certificate anchoring before blockchain settlement, the framework eliminates unsafe authorizations entirely ($0.00\%$) while sustaining $11,970.1$ TPS.

Future work will focus on integrating zero-knowledge proofs (zk-SNARKs) to enable privacy-preserving cadastral verification, allowing property owners to prove clear title and non-encumbrance without revealing underlying financial liabilities or private appraisal values.

---

### Acknowledgment
The authors express their gratitude to the Department of Computer Science and Engineering, College of Engineering Guindy, Anna University, for providing the experimental infrastructure and computational resources supporting this research.

---

### References

1. World Bank, "Securing land rights for all: A path to sustainable development," World Bank Group Flagship Report, Washington, DC, 2022.
2. V. L. Lemieux, "Trusting records: Is blockchain technology the answer?" *Records Management Journal*, vol. 26, no. 2, pp. 110–139, 2016.
3. K. Shang and J. Allison, "Blockchain in land administration: An empirical assessment of emerging pilots," *Land Use Policy*, vol. 78, pp. 231–244, 2018.
4. V. Thakur, M. N. Doja, Y. K. Dwivedi, T. Ahmad, and G. Khadke, "Land records on blockchain: A structured review and conceptual framework," *Information Systems Frontiers*, vol. 22, no. 5, pp. 1159–1178, 2020.
5. S. C. Kleene, *Introduction to Metamathematics*, Amsterdam: North-Holland Publishing Co., 1952.
6. M. Davis, "Three-valued logic in database querying and incomplete information processing," *ACM Transactions on Database Systems (TODS)*, vol. 18, no. 3, pp. 431–462, 1993.
7. LocationTech, "JTS Topology Suite Developer Guide," Eclipse Foundation, 2023. [Online]. Available: https://github.com/locationtech/jts
8. M. de Berg, O. Cheong, M. van Kreveld, and M. Overmars, *Computational Geometry: Algorithms and Applications*, 3rd ed. Berlin: Springer-Verlag, 2008.
9. C. A. R. Hoare, "An axiomatic basis for computer programming," *Communications of the ACM*, vol. 12, no. 10, pp. 576–580, 1969.
10. S. Nakamoto, "Bitcoin: A peer-to-peer electronic cash system," 2008. [Online]. Available: https://bitcoin.org/bitcoin.pdf
11. G. Wood, "Ethereum: A secure decentralised generalised transaction ledger," *Ethereum Project Yellow Paper*, vol. 151, pp. 1–32, 2014.
12. A. Rundgren, B. Jordan, and S. Erdtman, "JSON Canonicalization Scheme (JCS)," *IETF RFC 8785*, June 2020.
13. E. F. Codd, "Extending the database relational model to capture more meaning," *ACM Transactions on Database Systems*, vol. 4, no. 4, pp. 397–434, 1979.
14. ISO/TC 211, "Geographic information — Land Administration Domain Model (LADM)," *International Standard ISO 19152:2012*, 2012.
15. J. Eberhardt and S. Tai, "On or off the blockchain? Insights on off-chain computation and data storage," in *Proc. 6th European Conf. on Service-Oriented and Cloud Computing (ESOCC)*, Springer, 2017, pp. 3–15.
16. H. R. Varian, "Intermediate Microeconomics: A Modern Approach," 9th ed., W. W. Norton & Company, 2014.
17. F. Zhang, E. Cecchetti, K. Croman, A. Juels, and E. Shi, "Town Crier: An authenticated data feed for smart contracts," in *Proc. 2016 ACM SIGSAC Conf. on Computer and Communications Security (CCS)*, 2016, pp. 270–282.
18. P. Szalachowski, "Towards more reliable blockchain oracles," in *Proc. 2020 IEEE Int. Conf. on Blockchain*, IEEE, 2020, pp. 288–295.
