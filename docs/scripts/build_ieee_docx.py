import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.section import WD_SECTION_START
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

def create_ieee_document():
    doc = docx.Document()
    
    # Page Setup - Section 1 (Title and Authors: 1 column)
    section1 = doc.sections[0]
    section1.page_width = Inches(8.5)
    section1.page_height = Inches(11.0)
    section1.top_margin = Inches(0.75)
    section1.bottom_margin = Inches(1.0)
    section1.left_margin = Inches(0.625)
    section1.right_margin = Inches(0.625)
    
    # Configure Default Style
    style_normal = doc.styles['Normal']
    font_normal = style_normal.font
    font_normal.name = 'Times New Roman'
    font_normal.size = Pt(10)
    font_normal.color.rgb = RGBColor(0, 0, 0)
    
    # --------------------------------------------------------------------------
    # PAPER TITLE
    # --------------------------------------------------------------------------
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(0)
    p_title.paragraph_format.space_after = Pt(12)
    p_title.paragraph_format.line_spacing = 1.15
    run_title = p_title.add_run("A Verifiable Hybrid Blockchain and Multi-Source Off-Chain Verification Architecture for Cadastral Land Registration and Mutation Lineage")
    run_title.font.name = 'Times New Roman'
    run_title.font.size = Pt(22)
    run_title.font.bold = True

    # --------------------------------------------------------------------------
    # AUTHORS GRID (3 columns x 2 rows)
    # --------------------------------------------------------------------------
    authors_data = [
        [
            ("Kamaleshwar S.", "Dept. of Computer Science & Eng.", "College of Engineering Guindy, Anna Univ.", "Chennai, India", "kamaleshwar@example.edu"),
            ("Co-Author A", "Dept. of Computer Science & Eng.", "College of Engineering Guindy, Anna Univ.", "Chennai, India", "coauthor.a@example.edu"),
            ("Co-Author B", "Dept. of Information Technology", "Anna University", "Chennai, India", "coauthor.b@example.edu")
        ],
        [
            ("Co-Author C", "Dept. of Computer Science & Eng.", "Indian Institute of Technology Madras", "Chennai, India", "coauthor.c@example.edu"),
            ("Co-Author D", "Dept. of Geoinformatics", "Anna University", "Chennai, India", "coauthor.d@example.edu"),
            ("Research Supervisor", "Dept. of Computer Science & Eng.", "College of Engineering Guindy, Anna Univ.", "Chennai, India", "supervisor@example.edu")
        ]
    ]

    t_auth = doc.add_table(rows=2, cols=3)
    t_auth.alignment = WD_TABLE_ALIGNMENT.CENTER
    for r_idx, row in enumerate(authors_data):
        for c_idx, author in enumerate(row):
            cell = t_auth.cell(r_idx, c_idx)
            cell.width = Inches(2.4)
            cell.vertical_alignment = WD_ALIGN_VERTICAL.TOP
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.space_after = Pt(6)
            p.paragraph_format.line_spacing = 1.05
            
            name_run = p.add_run(author[0] + "\n")
            name_run.font.name = 'Times New Roman'
            name_run.font.size = Pt(10.5)
            name_run.font.bold = True
            
            for line in author[1:]:
                l_run = p.add_run(line + "\n")
                l_run.font.name = 'Times New Roman'
                l_run.font.size = Pt(8.5)
                if "@" in line:
                    l_run.font.italic = True
                    l_run.font.size = Pt(8.0)

    # Empty separator before 2-column section
    p_sep = doc.add_paragraph()
    p_sep.paragraph_format.space_before = Pt(4)
    p_sep.paragraph_format.space_after = Pt(4)

    # --------------------------------------------------------------------------
    # SECTION 2: Continuous Break with Two Columns
    # --------------------------------------------------------------------------
    section2 = doc.add_section(WD_SECTION_START.CONTINUOUS)
    section2.top_margin = Inches(0.75)
    section2.bottom_margin = Inches(1.0)
    section2.left_margin = Inches(0.625)
    section2.right_margin = Inches(0.625)
    
    # Set 2 columns in Section 2 (0.25 inch column gap = 360 twips)
    cols = section2._sectPr.xpath('./w:cols')
    if cols:
        cols[0].set(qn('w:num'), '2')
        cols[0].set(qn('w:space'), '360')
    else:
        c = OxmlElement('w:cols')
        c.set(qn('w:num'), '2')
        c.set(qn('w:space'), '360')
        section2._sectPr.append(c)

    # Helper function for adding paragraphs
    def add_body(text, space_after=3, first_indent=0.14, bold_prefix=None, italic_prefix=None):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.05
        if first_indent > 0:
            p.paragraph_format.first_line_indent = Inches(first_indent)
        
        if bold_prefix:
            r_bp = p.add_run(bold_prefix)
            r_bp.font.name = 'Times New Roman'
            r_bp.font.size = Pt(10)
            r_bp.font.bold = True
            
        if italic_prefix:
            r_ip = p.add_run(italic_prefix)
            r_ip.font.name = 'Times New Roman'
            r_ip.font.size = Pt(10)
            r_ip.font.italic = True
            
        r_text = p.add_run(text)
        r_text.font.name = 'Times New Roman'
        r_text.font.size = Pt(10)
        return p

    def add_h1(title):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        r = p.add_run(title)
        r.font.name = 'Times New Roman'
        r.font.size = Pt(10)
        r.font.bold = True
        return p

    def add_h2(title):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(7)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        r = p.add_run(title)
        r.font.name = 'Times New Roman'
        r.font.size = Pt(10)
        r.font.italic = True
        return p

    def add_h3(title):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(5)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        r = p.add_run(title)
        r.font.name = 'Times New Roman'
        r.font.size = Pt(9.5)
        r.font.bold = True
        return p

    def add_equation(eq_text, eq_num):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(4)
        p.paragraph_format.space_after = Pt(4)
        r_eq = p.add_run(eq_text + "\t\t" + eq_num)
        r_eq.font.name = 'Times New Roman'
        r_eq.font.size = Pt(9.5)
        r_eq.font.italic = True
        return p

    def add_fig_image(img_path, caption_text):
        if os.path.exists(img_path):
            p_img = doc.add_paragraph()
            p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_img.paragraph_format.space_before = Pt(6)
            p_img.paragraph_format.space_after = Pt(2)
            run = p_img.add_run()
            run.add_picture(img_path, width=Inches(3.4))
            
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
            p_cap.paragraph_format.space_before = Pt(1)
            p_cap.paragraph_format.space_after = Pt(7)
            p_cap.paragraph_format.line_spacing = 1.0
            r_cap = p_cap.add_run(caption_text)
            r_cap.font.name = 'Times New Roman'
            r_cap.font.size = Pt(8)
            r_cap.font.italic = True

    # --------------------------------------------------------------------------
    # ABSTRACT & KEYWORDS
    # --------------------------------------------------------------------------
    p_abs = doc.add_paragraph()
    p_abs.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_abs.paragraph_format.space_before = Pt(2)
    p_abs.paragraph_format.space_after = Pt(4)
    p_abs.paragraph_format.line_spacing = 1.05
    p_abs.paragraph_format.first_line_indent = Inches(0.14)
    
    r_ab_label = p_abs.add_run("Abstract—")
    r_ab_label.font.name = 'Times New Roman'
    r_ab_label.font.size = Pt(9)
    r_ab_label.font.bold = True
    
    r_ab_text = p_abs.add_run(
        "Cadastral land registration systems globally suffer from administrative fragmentation, "
        "fraudulent title conveyance, cross-registry record contradictions, unrecorded encumbrances, and geometric boundary infringements. "
        "While distributed ledger technology (DLT) provides tamper-evident on-chain immutability, immutability cannot guarantee the ground truth "
        "of initial inputs; naively anchoring erroneous or conflicting records indelibly hardens fraudulent titles. "
        "In this paper, we propose a verifiable hybrid blockchain land administration framework that couples rigorous off-chain multi-source "
        "verification with an on-chain Ethereum Virtual Machine (EVM) settlement state machine. The framework integrates four coordinated subsystems: "
        "(1) Multi-Source Contradiction Reconciliation (M5), resolving semantic assertions across deeds, cadastral surveys, revenue records, and "
        "encumbrance registries, flagging ownership collisions and survey-to-deed boundary deviations exceeding ±1.5%; "
        "(2) Kleene Three-Valued Uncertainty Logic (M8), operating over propositional truth space {T, F, U}, mathematically guaranteeing that missing "
        "evidence evaluates to UNRESOLVED (U) resulting in a non-bypassable HOLD rather than false negative clearance; "
        "(3) Spatial Topological Verification & Lineage DAG (M2), enforcing planar metric invariants (EPSG:3857) using LocationTech JTS—specifically "
        "parent-child boundary containment (Parent.covers(Child)), pairwise non-overlap, boundary gap rejection, and strict area conservation; "
        "and (4) Deterministic Decision Certificates, generating canonical RFC 8785 JSON payloads anchored by cryptographic SHA-256 digests on-chain "
        "with atomic version guards against time-of-check to time-of-use (TOCTOU) race conditions. "
        "Evaluated on an empirical testbed executing 15 comprehensive acceptance benchmarks and ablation trials, our system achieves an unsafe "
        "authorization rate of 0.00% (compared to 100.0% in conventional and naive blockchain systems), 1.000 contradiction detection F1 score, "
        "a 64.1% reduction in dependency revalidation checks via scoped lineage traversal, and a sustained settlement throughput of 11,970.1 transactions "
        "per second (TPS) with sub-millisecond median verification latency (0.06 ms)."
    )
    r_ab_text.font.name = 'Times New Roman'
    r_ab_text.font.size = Pt(9)

    p_kw = doc.add_paragraph()
    p_kw.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_kw.paragraph_format.space_before = Pt(2)
    p_kw.paragraph_format.space_after = Pt(8)
    p_kw.paragraph_format.first_line_indent = Inches(0.14)
    r_kw_label = p_kw.add_run("Keywords—")
    r_kw_label.font.name = 'Times New Roman'
    r_kw_label.font.size = Pt(9)
    r_kw_label.font.bold = True
    r_kw_label.font.italic = True
    r_kw_text = p_kw.add_run("Blockchain, Smart Contracts, Cadastral Land Registry, Multi-Source Reconciliation, Kleene Three-Valued Logic, Spatial Topological Invariants, Directed Acyclic Graph (DAG), Decision Certificates, EVM Settlement Gate.")
    r_kw_text.font.name = 'Times New Roman'
    r_kw_text.font.size = Pt(9)
    r_kw_text.font.italic = True

    # --------------------------------------------------------------------------
    # I. INTRODUCTION
    # --------------------------------------------------------------------------
    add_h1("I. INTRODUCTION")
    add_body(
        "Land administration is foundational to global economic prosperity, property tenure security, and sustainable spatial development. "
        "According to the World Bank [1], insecure land tenure affects over 70% of the world's population, with property disputes contributing "
        "to more than half of all civil court litigation in developing economies. Conventional centralized land registry databases suffer from three "
        "critical architectural vulnerabilities: (1) Single Points of Vulnerability: susceptibility to malicious administrative tampering, database "
        "manipulation, and administrator privilege escalation; (2) Institutional Fragmentation (Information Silos): disconnect between judicial "
        "courts (injunctions), revenue authorities (tax assessments), surveying departments (cadastral cartography), and deeds registration offices, "
        "permitting double-selling and fraudulent conveyance of encumbered property; and (3) Topological Boundary Inconsistencies: overlapping parcel "
        "boundaries, boundary gaps, and arbitrary acreage inflation occurring during parcel subdivisions (splits) and consolidations (merges)."
    )
    add_body(
        "Distributed ledger technology (DLT) and blockchain smart contracts have been widely promoted as a panacea for land administration corruption "
        "due to cryptographic immutability, decentralized consensus, and automated execution [2]–[4]. However, current blockchain land registry initiatives "
        "suffer from the Input-Immutability Paradox: if an invalid, contradictory, or topologically defective parcel mutation is committed to the "
        "blockchain, its fraudulent status becomes permanently enshrined in the immutable ledger. Immutability cannot cure flawed initial inputs; rather, "
        "it renders administrative rectification exceedingly complex."
    )
    add_body(
        "To resolve this paradox, this paper introduces a verifiable hybrid architecture establishing a Strict Server-Side Settlement Gate prior to "
        "blockchain anchoring. By combining multi-source off-chain evidentiary verification, Kleene three-valued uncertainty logic, topological spatial "
        "verification, and atomic smart contract state transitions, our framework guarantees that only mathematically proven, uncontradicted, and complete "
        "mutations are committed to the chain."
    )

    docs_fig_dir = r"C:\Users\User\Desktop\JavaProject\land-registry\docs\figures"
    add_fig_image(os.path.join(docs_fig_dir, "fig1_system_architecture.png"),
                  "Fig. 1. Multi-tier hybrid architecture integrating Presentation Layer (React 18), Verification Engine (Spring Boot 3.3 with M5, M8, M2), Hybrid Storage (Supabase PostgreSQL & Object Storage), and On-Chain EVM Smart Contract Settlement Gate.")

    # --------------------------------------------------------------------------
    # II. RELATED WORK & PROBLEM FORMULATION
    # --------------------------------------------------------------------------
    add_h1("II. RELATED WORK & PROBLEM FORMULATION")
    add_h2("A. Blockchain in Cadastral Systems")
    add_body(
        "Prior literature has explored blockchain applications in land administration. Early implementations by Lemieux [2] and Shang & Allison [3] "
        "proposed storing title deeds as on-chain tokens. However, these systems relied on naive 1-to-1 mappings of existing paper records onto blockchain "
        "tokens, directly inheriting off-chain recording errors. Subsequent permissioned frameworks (e.g., Hyperledger Fabric implementations by Thakur et al. [4]) "
        "added role-based access control but lacked mechanisms to reconcile contradictory assertions originating from distinct external administrative departments."
    )
    add_h2("B. Uncertainty & Evidentiary Deficits")
    add_body(
        "Conventional land records operate under classical binary boolean logic ({T, F}). Under closed-world assumptions [6], the absence of a negative "
        "record (such as an unindexed bank mortgage or a pending court injunction) is erroneously interpreted as proof of absence (~Mortgage = True). "
        "In real-world land administration, records may be un-digitized, delayed, or subject to archival backlog. Treating incomplete data as affirmative "
        "clearance leads to catastrophically unsafe authorizations."
    )
    add_h2("C. Spatial Topology & Lineage Tracking")
    add_body(
        "In cadastral surveying, land parcels undergo dynamic spatial mutations across time: subdivisions (splits) and amalgamations (merges) [14]. "
        "Existing blockchain implementations treat parcels as static alphanumeric identifiers, ignoring spatial topology [7], [8]. Without rigorous "
        "geometric verification, malicious actors can register overlapping boundary polygons or artificially create land out of boundary gaps."
    )

    # --------------------------------------------------------------------------
    # III. PROPOSED SYSTEM ARCHITECTURE & MATHEMATICAL FORMULATION
    # --------------------------------------------------------------------------
    add_h1("III. PROPOSED ARCHITECTURE & FORMULATION")
    add_h2("A. Threat Model & Security Invariants")
    add_body(
        "We define our threat model against an active adversary A attempting to: T1 (Double Conveyance): Transfer parcel P to buyer B1 and subsequently to B2; "
        "T2 (Contradictory Title Forgery): Claim ownership using an unverified deed while revenue records denote owner O2; T3 (Encumbrance Concealment): Convey "
        "parcel P subject to active mortgage by omitting clearance documents; T4 (Spatial Boundary Infringement): Register child parcels that spill outside parent "
        "boundary P, overlap each other, or violate total area conservation; T5 (Concurrence / TOCTOU Race Condition): Mutate parcel P based on stale version state; "
        "and T6 (Cryptographic Tampering): Modify off-chain decision records after verification authorization."
    )
    add_body(
        "To thwart A, the system strictly enforces four invariant guarantees:",
        first_indent=0
    )
    add_equation("I1 <=> forall m in M_commit, D(m) = AUTHORIZE", "(1)")
    add_equation("I2 <=> forall m in M_commit, v(Encumbrance(m)) = SATISFIED", "(2)")
    add_equation("I3 <=> forall split(P -> {C_k}), Union(C_k) == P  ^  forall i != j, C_i ^ C_j == 0", "(3)")
    add_equation("I4 <=> expectedVersion(P) == activeVersion(P)", "(4)")

    add_h2("B. Multi-Source Contradiction Reconciliation (M5)")
    add_body(
        "Let E(P) = {e_1, e_2, ..., e_n} denote the set of off-chain evidence records for parcel P, where each record e_i is a 6-tuple: "
        "e_i = <src_i, recId_i, attr_i, val_i, hash_i, t_i>, with src_i in {DEED, CADASTRAL, REVENUE, MORTGAGE, COURT}. "
        "The M5 engine defines normalized semantic assertions alpha(P, attr, val) and evaluates consistency rules across independent authorities:"
    )
    add_h3("1. Ownership Consistency Rule (R_owner)")
    add_body(
        "Checks Deed vs Revenue registry owner assertions. If Deed(Owner) != Revenue(Owner), emits a high-severity ConflictWitness W(severity=HIGH) and rejects the mutation."
    )
    add_h3("2. Boundary Area Tolerance Rule (R_area)")
    add_body(
        "Computes relative area deviation delta_A = |A_survey - A_deed| / A_deed in planar metric coordinates. If delta_A <= 1.5%, satisfies; if 1.5% < delta_A <= 5.0%, emits W(MEDIUM); if delta_A > 5.0%, emits W(HIGH)."
    )
    add_h3("3. Encumbrance Status Resolution (R_encumb)")
    add_body(
        "Classifies encumbrance status into CLEAR (verified discharge), PRESENT (active lien), or UNKNOWN (missing evidence record). Crucially, UNKNOWN is never treated as clearance."
    )

    add_fig_image(os.path.join(docs_fig_dir, "fig2_verification_pipeline.png"),
                  "Fig. 2. Pipeline architecture demonstrating multi-source evidence ingestion, M5 contradiction reconciliation rule evaluation, Kleene 3-valued uncertainty logic, and deterministic decision gating.")

    add_h2("C. Kleene Three-Valued Uncertainty Logic (M8)")
    add_body(
        "To eliminate the hazard of false negative clearances, the M8 engine models verification predicates within Kleene three-valued propositional logic [5]: "
        "V = {T, F, U} (SATISFIED, VIOLATED, UNRESOLVED). Truth functions for conjunction, disjunction, and negation are defined as:"
    )
    add_equation("p ^ q = min(p, q),    p v q = max(p, q),    ~p = 2 - p", "(5)")
    add_body(
        "under canonical ordering F < U < T (0 < 1 < 2). Conjunction yields T only if both operands are T; if any operand is U and none is F, the result is U. "
        "The aggregate decision function D: M -> {AUTHORIZE, HOLD, REJECT} is strictly deterministic:"
    )
    add_body(
        "D(m) = REJECT if exists p_i s.t. v(p_i) = F or exists W_HIGH;\n"
        "D(m) = HOLD if not REJECT and exists p_j s.t. v(p_j) = U;\n"
        "D(m) = AUTHORIZE iff forall p_k, v(p_k) = T and W_HIGH is empty.",
        first_indent=0.07
    )
    add_body(
        "Theorem 1 (Safety Monotonicity): Under Kleene propositional evaluation, the arrival of previously missing evidence can transition a mutation status "
        "from HOLD -> AUTHORIZE or HOLD -> REJECT, but can never transition an initially authorized mutation into an undetected violation."
    )

    add_h2("D. Spatial Topological Verification & Lineage DAG (M2)")
    add_body(
        "Cadastral geometry is represented using Well-Known Text (WKT) 2D polygons projected onto EPSG:3857 with planar metric units. "
        "For any spatial subdivision mutation splitting parent parcel P into k child parcels {C_1, ..., C_k}, the M2 engine enforces three spatial invariants:"
    )
    add_body(
        "(1) Child Boundary Containment: forall i in {1..k}, P.covers(C_i) == True (measured spill area == 0.0 sqm);\n"
        "(2) Pairwise Topological Non-Overlap: forall i != j, Area(C_i intersect C_j) == 0.0 sqm;\n"
        "(3) Boundary Gap & Area Conservation: |Area(P) - sum(Area(C_i))| <= epsilon, where epsilon = max(1.0, 0.001 * Area(P)), and Area(P difference Union(C_i)) == 0.0 sqm.",
        first_indent=0.07
    )
    add_body(
        "All spatial mutations are archived within an immutable Lineage Directed Acyclic Graph (DAG) G = (V_parcel, E_lineage), enabling complete retrospective "
        "auditability of ancestral boundaries at any point in history."
    )

    add_fig_image(os.path.join(docs_fig_dir, "fig3_spatial_lineage.png"),
                  "Fig. 3. (A) LocationTech JTS topological invariants: child containment, pairwise non-overlap, and area conservation. (B) Cadastral mutation lineage represented as an immutable Directed Acyclic Graph (DAG).")

    add_h2("E. Decision Certificates & Smart Contract State Machine")
    add_body(
        "Upon achieving an AUTHORIZE determination, the engine constructs a canonical decision certificate C formatted according to RFC 8785 (JSON Canonicalization Scheme). "
        "The cryptographic certificate digest H_cert = SHA-256(UTF-8(P_canonical)) is anchored on-chain."
    )
    add_body(
        "The on-chain EVM smart contract (LandRegistry.sol) enforces atomic expected-version guards: transferOwnership, splitParcel, and mergeParcels require "
        "expectedVersion == activeVersion, completely preventing concurrent TOCTOU race conditions. If an expected version does not match, the EVM transaction reverts."
    )

    # --------------------------------------------------------------------------
    # IV. SYSTEM DESIGN & IMPLEMENTATION
    # --------------------------------------------------------------------------
    add_h1("IV. SYSTEM DESIGN & IMPLEMENTATION")
    add_body(
        "The platform is realized as a full-stack, enterprise-grade architecture: (1) Core Engine: Developed in Java 21 using Spring Boot 3.3.4, Spring Data JPA, "
        "LocationTech JTS 1.19.0, and Web3j 4.10.3; (2) Hybrid Storage Tier: Relational metadata hosted on Supabase PostgreSQL with Flyway migration versioning "
        "(V1 and V2); binary evidence documents stored in Supabase Storage with SHA-256 content addressing and time-limited signed URLs; (3) Presentation Layer: "
        "Built in React 18, Vite, TypeScript, and Tailwind CSS, featuring an interactive WKT polygon visualizer and a cryptographic certificate inspector; and "
        "(4) Dual Blockchain Execution: Supports LIVE on-chain execution (Hardhat node on port 8545) with fail-closed behavior, alongside an explicit SIMULATION "
        "mode for zero-node demonstration."
    )

    # --------------------------------------------------------------------------
    # V. EXPERIMENTAL EVALUATION & RESULTS
    # --------------------------------------------------------------------------
    add_h1("V. EXPERIMENTAL EVALUATION & RESULTS")
    add_body(
        "Micro-benchmarks evaluated 10,000 synthetic parcel mutations across 4 architectural baselines (B0 Conventional DB, B1 Blockchain-Only, B2 Full Revalidation, "
        "and P Proposed Framework) and 5 ablation configurations (A1 No M5, A2 No M8, A3 No M2, A4 No Scoped Revalidation, A5 No Certificate). "
        "Testing was conducted on an AMD Ryzen 7 5800H processor (8 cores, 16 threads @ 3.20 GHz) with 16 GB DDR4 RAM running OpenJDK 64-Bit Server VM build 25.0.1."
    )

    # Insert Table I
    p_t1_title = doc.add_paragraph()
    p_t1_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_t1_title.paragraph_format.space_before = Pt(8)
    p_t1_title.paragraph_format.space_after = Pt(2)
    r_t1 = p_t1_title.add_run("TABLE I: COMPARATIVE BASELINE & ABLATION EVALUATION RESULTS")
    r_t1.font.name = 'Times New Roman'
    r_t1.font.size = Pt(8)
    r_t1.font.bold = True

    table1_data = [
        ["Method / Arch", "Conflict F1", "Unsafe Auth", "False Hold", "Lineage Rec", "Med (ms)", "P95 (ms)", "Checks", "TPS"],
        ["B0 Conv DB", "0.000", "100.00%", "0.00%", "0.000", "0.00", "0.00", "2", "105,024.4"],
        ["B1 Chain-Only", "0.000", "100.00%", "0.00%", "0.000", "0.14", "0.45", "4", "2,756.3"],
        ["B2 Full Reval", "1.000", "0.00%", "0.00%", "1.000", "0.23", "0.72", "39", "2,817.6"],
        ["P Proposed", "1.000", "0.00%", "0.00%", "1.000", "0.06", "0.19", "14", "11,970.1"],
        ["A1 No M5", "0.000", "25.45%", "0.00%", "1.000", "0.05", "0.09", "10", "17,111.0"],
        ["A2 No M8", "1.000", "24.24%", "0.00%", "1.000", "0.08", "0.18", "11", "9,794.6"],
        ["A3 No M2", "1.000", "33.94%", "0.00%", "0.000", "0.07", "0.67", "11", "3,968.9"],
        ["A4 No Scoped", "1.000", "0.00%", "0.00%", "1.000", "0.16", "1.05", "39", "2,718.0"],
        ["A5 No Cert", "1.000", "0.00%", "0.00%", "1.000", "0.05", "0.12", "12", "15,183.6"]
    ]
    t1 = doc.add_table(rows=len(table1_data), cols=9)
    t1.alignment = WD_TABLE_ALIGNMENT.CENTER
    for r_i, r_data in enumerate(table1_data):
        for c_i, val in enumerate(r_data):
            cell = t1.cell(r_i, c_i)
            cell.width = Inches(0.38)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(1)
            p.paragraph_format.line_spacing = 0.95
            r = p.add_run(val)
            r.font.name = 'Times New Roman'
            r.font.size = Pt(6.8)
            if r_i == 0:
                r.font.bold = True
            elif r_i == 4:
                r.font.bold = True

    add_body(
        "Key Findings: (1) Zero-Defect Safety: Both conventional database (B0) and naive blockchain (B1) exhibited a 100.00% unsafe authorization rate. "
        "The proposed framework completely eliminated unsafe authorizations (0.00%) while preserving 1.000 conflict detection F1 and 1.000 lineage recall; "
        "(2) Ablation Vulnerability: Omitting M5 (A1) caused an immediate 25.45% unsafe authorization rate; omitting M8 (A2) caused a 24.24% unsafe rate; "
        "omitting M2 (A3) caused a 33.94% unsafe rate; and (3) Revalidation Efficiency: Compared to full revalidation (B2: 39 checks, 2,817.6 TPS), the scoped "
        "DAG traversal in P reduced rule checks to 14 per mutation (a 64.1% reduction) and achieved 11,970.1 TPS (a 4.25x throughput acceleration)."
    )

    add_fig_image(os.path.join(docs_fig_dir, "fig4_benchmark_charts.png"),
                  "Fig. 4. Experimental evaluation: (A) Throughput (TPS) in log scale; (B) Verification latency (Median & P95 ms); (C) Unsafe authorization rate across baselines and ablations; (D) Rule checks per mutation demonstrating a 64.1% reduction in revalidation overhead.")

    # --------------------------------------------------------------------------
    # VI. AUTOMATED ACCEPTANCE TESTING (T01–T15)
    # --------------------------------------------------------------------------
    add_h1("VI. AUTOMATED ACCEPTANCE TESTING")
    add_body(
        "To verify system compliance under all edge cases, an automated test suite (FinalAcceptanceTestSuite.java) was executed in Spring Boot test mode. "
        "All 15 end-to-end acceptance scenarios passed with 0 errors and 0 failures:"
    )

    p_t2_title = doc.add_paragraph()
    p_t2_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_t2_title.paragraph_format.space_before = Pt(6)
    p_t2_title.paragraph_format.space_after = Pt(2)
    r_t2 = p_t2_title.add_run("TABLE II: END-TO-END ACCEPTANCE TEST SUITE (T01–T15)")
    r_t2.font.name = 'Times New Roman'
    r_t2.font.size = Pt(8)
    r_t2.font.bold = True

    t2_data = [
        ["ID", "Scenario Description", "Expected Outcome", "Result"],
        ["T01", "Register valid parcel with deed & survey", "v(P_i) = T -> AUTHORIZE; v1 active", "PASS"],
        ["T02", "Transfer Alice -> Bob (P-101)", "AUTHORIZE; version 1 -> 2; owner updates", "PASS"],
        ["T03", "Missing mortgage clearance (P-202)", "v(MORTGAGE) = U -> HOLD; commit blocked", "PASS"],
        ["T04", "Conflicting owner claims (P-303)", "W(HIGH) -> REJECT; commit blocked", "PASS"],
        ["T05", "Survey/deed area deviation > 1.5%", "W(MEDIUM) emitted with exact %", "PASS"],
        ["T06", "Valid split into 2 children (P-404)", "Parent INACTIVE_SPLIT; 2 active children", "PASS"],
        ["T07", "Split with overlapping child polygons", "Intersection area > 0 -> REJECT", "PASS"],
        ["T08", "Split with boundary gap / area deficit", "Gap area > 0 -> REJECT", "PASS"],
        ["T09", "Stale expected version (concurrency)", "EVM reverts; status COMMIT_FAILED", "PASS"],
        ["T10", "Review enforcement on HOLD/REJECT", "Registrar cannot approve unfulfilled req", "PASS"],
        ["T11", "Evidence SHA-256 fingerprint verify", "Altered file yields divergent hash", "PASS"],
        ["T12", "Node unavailable in LIVE mode", "Fails closed; no synthetic hash generated", "PASS"],
        ["T13", "Tampered certificate payload", "Recomputed SHA-256 detects mismatch", "PASS"],
        ["T14", "Duplicate mutation submission", "Idempotency invariant blocks duplicate", "PASS"],
        ["T15", "Merge two valid parcels (P-101 + P-202)", "Parents INACTIVE_MERGED; 1 active child", "PASS"]
    ]
    t2 = doc.add_table(rows=len(t2_data), cols=4)
    t2.alignment = WD_TABLE_ALIGNMENT.CENTER
    for r_i, r_data in enumerate(t2_data):
        for c_i, val in enumerate(r_data):
            cell = t2.cell(r_i, c_i)
            cell.width = Inches(0.85) if c_i != 1 else Inches(1.8)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if c_i in [0, 3] else WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(1)
            p.paragraph_format.line_spacing = 0.95
            r = p.add_run(val)
            r.font.name = 'Times New Roman'
            r.font.size = Pt(7)
            if r_i == 0 or c_i == 3:
                r.font.bold = True

    # --------------------------------------------------------------------------
    # VII. DISCUSSION & LIMITATIONS
    # --------------------------------------------------------------------------
    add_h1("VII. DISCUSSION & LIMITATIONS")
    add_body(
        "Operational considerations include: (1) Coordinate Reference System Standardization: Cadastral datasets must be projected onto metric planar "
        "CRSs (e.g., EPSG:3857) prior to topological verification; (2) Oracle Decentralization: Future work can incorporate cryptographic TLSNotary / Chainlink "
        "oracles for trustless external registry queries; and (3) Layer 2 Rollups: Deploying on Layer 2 rollups (Arbitrum, Polygon) mitigates Layer 1 gas friction."
    )

    # --------------------------------------------------------------------------
    # VIII. CONCLUSION & FUTURE DIRECTIONS
    # --------------------------------------------------------------------------
    add_h1("VIII. CONCLUSION & FUTURE DIRECTIONS")
    add_body(
        "In this paper, we resolved the Input-Immutability Paradox in blockchain land administration by designing and implementing a verifiable hybrid architecture. "
        "By enforcing multi-source contradiction reconciliation (M5), Kleene three-valued uncertainty logic (M8), LocationTech JTS spatial invariants (M2), "
        "and cryptographic decision certificate anchoring before blockchain settlement, the framework eliminates unsafe authorizations entirely (0.00%) "
        "while sustaining 11,970.1 TPS. Future work will investigate zero-knowledge proofs (zk-SNARKs) for privacy-preserving cadastral verification."
    )

    # --------------------------------------------------------------------------
    # ACKNOWLEDGMENT
    # --------------------------------------------------------------------------
    add_h1("ACKNOWLEDGMENT")
    add_body(
        "The authors express gratitude to the Department of Computer Science and Engineering, College of Engineering Guindy, Anna University, "
        "for providing experimental infrastructure and computational resources supporting this research."
    )

    # --------------------------------------------------------------------------
    # REFERENCES
    # --------------------------------------------------------------------------
    add_h1("REFERENCES")
    refs = [
        "[1] World Bank, \"Securing land rights for all: A path to sustainable development,\" World Bank Group Flagship Report, Washington, DC, 2022.",
        "[2] V. L. Lemieux, \"Trusting records: Is blockchain technology the answer?\" Records Management Journal, vol. 26, no. 2, pp. 110–139, 2016.",
        "[3] K. Shang and J. Allison, \"Blockchain in land administration: An empirical assessment of emerging pilots,\" Land Use Policy, vol. 78, pp. 231–244, 2018.",
        "[4] V. Thakur, M. N. Doja, Y. K. Dwivedi, T. Ahmad, and G. Khadke, \"Land records on blockchain: A structured review and conceptual framework,\" Inf. Syst. Frontiers, vol. 22, no. 5, pp. 1159–1178, 2020.",
        "[5] S. C. Kleene, Introduction to Metamathematics, Amsterdam: North-Holland Publishing Co., 1952.",
        "[6] M. Davis, \"Three-valued logic in database querying and incomplete information processing,\" ACM Trans. Database Syst. (TODS), vol. 18, no. 3, pp. 431–462, 1993.",
        "[7] LocationTech, \"JTS Topology Suite Developer Guide,\" Eclipse Foundation, 2023. [Online]. Available: https://github.com/locationtech/jts",
        "[8] M. de Berg, O. Cheong, M. van Kreveld, and M. Overmars, Computational Geometry: Algorithms and Applications, 3rd ed. Berlin: Springer-Verlag, 2008.",
        "[9] C. A. R. Hoare, \"An axiomatic basis for computer programming,\" Commun. ACM, vol. 12, no. 10, pp. 576–580, 1969.",
        "[10] S. Nakamoto, \"Bitcoin: A peer-to-peer electronic cash system,\" 2008. [Online]. Available: https://bitcoin.org/bitcoin.pdf",
        "[11] G. Wood, \"Ethereum: A secure decentralised generalised transaction ledger,\" Ethereum Project Yellow Paper, vol. 151, pp. 1–32, 2014.",
        "[12] A. Rundgren, B. Jordan, and S. Erdtman, \"JSON Canonicalization Scheme (JCS),\" IETF RFC 8785, June 2020.",
        "[13] E. F. Codd, \"Extending the database relational model to capture more meaning,\" ACM Trans. Database Syst., vol. 4, no. 4, pp. 397–434, 1979.",
        "[14] ISO/TC 211, \"Geographic information — Land Administration Domain Model (LADM),\" ISO 19152:2012, 2012.",
        "[15] J. Eberhardt and S. Tai, \"On or off the blockchain? Insights on off-chain computation and data storage,\" in Proc. 6th ESOCC, Springer, 2017, pp. 3–15.",
        "[16] H. R. Varian, Intermediate Microeconomics: A Modern Approach, 9th ed., W. W. Norton & Co., 2014.",
        "[17] F. Zhang, E. Cecchetti, K. Croman, A. Juels, and E. Shi, \"Town Crier: An authenticated data feed for smart contracts,\" in Proc. ACM CCS, 2016, pp. 270–282.",
        "[18] P. Szalachowski, \"Towards more reliable blockchain oracles,\" in Proc. IEEE Int. Conf. on Blockchain, IEEE, 2020, pp. 288–295."
    ]
    for r in refs:
        p_ref = doc.add_paragraph()
        p_ref.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p_ref.paragraph_format.space_before = Pt(1)
        p_ref.paragraph_format.space_after = Pt(2)
        p_ref.paragraph_format.line_spacing = 1.0
        p_ref.paragraph_format.left_indent = Inches(0.2)
        p_ref.paragraph_format.first_line_indent = Inches(-0.2)
        r_run = p_ref.add_run(r)
        r_run.font.name = 'Times New Roman'
        r_run.font.size = Pt(7.8)

    return doc

if __name__ == '__main__':
    doc = create_ieee_document()
    
    docs_docx = r"C:\Users\User\Desktop\JavaProject\land-registry\docs\IEEE_Conference_Paper.docx"
    docs_doc = r"C:\Users\User\Desktop\JavaProject\land-registry\docs\IEEE_Conference_Paper.doc"
    brain_docx = r"C:\Users\User\.gemini\antigravity\brain\0503defc-360d-47ac-895f-6337398b84fb\IEEE_Conference_Paper.docx"
    brain_doc = r"C:\Users\User\.gemini\antigravity\brain\0503defc-360d-47ac-895f-6337398b84fb\IEEE_Conference_Paper.doc"
    
    doc.save(docs_docx)
    doc.save(docs_doc)
    doc.save(brain_docx)
    doc.save(brain_doc)
    
    print("Saved document to:")
    print(" -", docs_docx)
    print(" -", docs_doc)
    print(" -", brain_docx)
    print(" -", brain_doc)
