import os
from PIL import Image, ImageDraw, ImageFont

def get_font(size, bold=False):
    try:
        if bold:
            return ImageFont.truetype("arialbd.ttf", size)
        return ImageFont.truetype("arial.ttf", size)
    except:
        return ImageFont.load_default()

def draw_rounded_rect(draw, coords, fill, outline, width=2, radius=8):
    x0, y0, x1, y1 = coords
    draw.rounded_rectangle([x0, y0, x1, y1], radius=radius, fill=fill, outline=outline, width=width)

def draw_arrow(draw, start, end, fill="#334155", width=2, arrow_size=8):
    x0, y0 = start
    x1, y1 = end
    draw.line([x0, y0, x1, y1], fill=fill, width=width)
    import math
    angle = math.atan2(y1 - y0, x1 - x0)
    angle1 = angle + math.pi * 5 / 6
    angle2 = angle - math.pi * 5 / 6
    p1 = (x1 + arrow_size * math.cos(angle1), y1 + arrow_size * math.sin(angle1))
    p2 = (x1 + arrow_size * math.cos(angle2), y1 + arrow_size * math.sin(angle2))
    draw.polygon([end, p1, p2], fill=fill)

def generate_fig1(output_paths):
    img = Image.new("RGB", (1200, 680), color="#F8FAFC")
    draw = ImageDraw.Draw(img)
    
    # Title Banner
    draw.rectangle([0, 0, 1200, 50], fill="#0F172A")
    title_font = get_font(18, bold=True)
    draw.text((30, 14), "Fig. 1: Hybrid Blockchain & Multi-Tier Cadastral Verification Architecture", fill="#FFFFFF", font=title_font)
    
    # 4 Main Columns / Layers
    # Layer 1: Client Tier
    draw_rounded_rect(draw, (30, 80, 260, 640), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw_rounded_rect(draw, (30, 80, 260, 125), fill="#E2E8F0", outline="#CBD5E1", width=1)
    draw.text((45, 95), "1. PRESENTATION TIER", fill="#1E293B", font=get_font(14, bold=True))
    draw.text((45, 135), "(React 18 + Vite + Tailwind)", fill="#64748B", font=get_font(11))
    
    client_boxes = [
        ("Landowner Portal", "Mutation submissions (Transfer,\nSplit, Merge), Document upload\n& Holding inspection", "#F0FDF4", "#86EFAC"),
        ("Registrar Workbench", "Verification inspection, M5/M8\nwitness review, Escalate/Hold,\nAuthorize commitment", "#EFF6FF", "#93C5FD"),
        ("Certificate Inspector", "Cryptographic tamper testing,\nSHA-256 digest recomputation,\nOn-chain receipt viewer", "#FAF5FF", "#D8B4FE"),
        ("Cadastral Visualizer", "WKT polygon viewer, spatial\nboundary overlays (EPSG:3857),\nDAG lineage graph", "#FEF3C7", "#FDE68A")
    ]
    y = 160
    for name, desc, bg, border in client_boxes:
        draw_rounded_rect(draw, (45, y, 245, y + 100), fill=bg, outline=border, width=2)
        draw.text((55, y + 8), name, fill="#0F172A", font=get_font(12, bold=True))
        draw.text((55, y + 30), desc, fill="#334155", font=get_font(10))
        y += 115

    # Layer 2: Core Spring Boot Application & Verification Engine
    draw_rounded_rect(draw, (310, 80, 680, 640), fill="#FFFFFF", outline="#94A3B8", width=2)
    draw_rounded_rect(draw, (310, 80, 680, 125), fill="#1E293B", outline="#0F172A", width=1)
    draw.text((325, 95), "2. VERIFICATION & WORKFLOW ENGINE (Spring Boot 3.3 / Java 21)", fill="#FFFFFF", font=get_font(13, bold=True))
    
    engine_boxes = [
        ("M5 Contradiction Reconciliation Engine", "• Cross-registry assertion grouping (Deed, Cadastral, Tax, Court)\n• Ownership collision witness generation (HIGH severity)\n• Area discrepancy detection (> 1.5% tolerance threshold)\n• Encumbrance predicate resolution (CLEAR vs UNKNOWN)", 140, 105, "#FEF2F2", "#FCA5A5"),
        ("M8 Uncertainty Evaluation Engine", "• Kleene 3-valued propositional logic: {SATISFIED, VIOLATED, UNRESOLVED}\n• Conservative settlement rule: Authorize iff all satisfied\n• Missing evidence maps strictly to UNRESOLVED -> HOLD\n• Complete audit trace with explanation witnesses", 260, 105, "#FFFBEB", "#FCD34D"),
        ("M2 Spatial & Lineage Verifier (JTS Topology Suite)", "• Metric planar coordinate validation (EPSG:3857/planar meters)\n• Strict child containment: parent.covers(child)\n• Pairwise topological non-overlap & boundary gap detection\n• Area conservation invariant: |Area(P) - sum(Area(C))| <= epsilon", 380, 105, "#F0FDF4", "#86EFAC"),
        ("Decision Certificate & Settlement Gate", "• Deterministic canonical JSON serialization & SHA-256 digest hashing\n• Strict server-side gate: Commit blocked if status is HOLD/REJECT\n• Re-check expected parcel versions (TOCTOU race condition defense)", 500, 105, "#F8FAFC", "#CBD5E1")
    ]
    for name, desc, by, bh, bg, border in engine_boxes:
        draw_rounded_rect(draw, (325, by, 665, by + bh), fill=bg, outline=border, width=2)
        draw.text((340, by + 8), name, fill="#0F172A", font=get_font(12, bold=True))
        draw.text((340, by + 30), desc, fill="#334155", font=get_font(10))

    # Layer 3: Hybrid Storage Layer
    draw_rounded_rect(draw, (730, 80, 930, 640), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw_rounded_rect(draw, (730, 80, 930, 125), fill="#E2E8F0", outline="#CBD5E1", width=1)
    draw.text((745, 95), "3. HYBRID STORAGE", fill="#1E293B", font=get_font(13, bold=True))
    draw.text((745, 135), "(PostgreSQL + Object Store)", fill="#64748B", font=get_font(11))

    storage_boxes = [
        ("Supabase PostgreSQL", "• Cadastral Parcels (WKT)\n• Evidence Records metadata\n• Mutation Requests & States\n• Lineage DAG Edges\n• Immutable Audit Events\n• Decision Certificates", 160, 220, "#EFF6FF", "#93C5FD"),
        ("Supabase Storage", "• Off-chain document store\n• Private 'land-evidence' bucket\n• SHA-256 fingerprint verify\n• Time-limited signed URLs\n• Deed PDFs, Survey TIFFs", 400, 210, "#F0FDF4", "#86EFAC")
    ]
    for name, desc, by, bh, bg, border in storage_boxes:
        draw_rounded_rect(draw, (745, by, 915, by + bh), fill=bg, outline=border, width=2)
        draw.text((755, by + 10), name, fill="#0F172A", font=get_font(12, bold=True))
        draw.text((755, by + 34), desc, fill="#334155", font=get_font(10))

    # Layer 4: On-Chain EVM Blockchain Tier
    draw_rounded_rect(draw, (980, 80, 1170, 640), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw_rounded_rect(draw, (980, 80, 1170, 125), fill="#1E293B", outline="#0F172A", width=1)
    draw.text((995, 95), "4. ON-CHAIN LAYER", fill="#FFFFFF", font=get_font(13, bold=True))
    draw.text((995, 135), "(EVM / Web3j / Hardhat)", fill="#94A3B8", font=get_font(11))

    chain_boxes = [
        ("LandRegistry.sol", "Smart Contract State Machine\nAddress: 0x5FbDB...aa3\n• registerParcel()\n• transferOwnership()\n• splitParcel()\n• mergeParcels()", 160, 150, "#FAF5FF", "#D8B4FE"),
        ("Atomic Version Guard", "Requires expectedVersion ==\nactiveVersion, preventing\nstale / concurrently mutated\nstate commit", 330, 130, "#FEF2F2", "#FCA5A5"),
        ("Certificate Digest Anchor", "bytes32 certDigest stored\nimmutably on-chain with\nblock number & tx hash for\nthird-party verification", 480, 130, "#EFF6FF", "#93C5FD")
    ]
    for name, desc, by, bh, bg, border in chain_boxes:
        draw_rounded_rect(draw, (995, by, 1155, by + bh), fill=bg, outline=border, width=2)
        draw.text((1005, by + 10), name, fill="#0F172A", font=get_font(12, bold=True))
        draw.text((1005, by + 34), desc, fill="#334155", font=get_font(10))

    # Connecting Arrows
    draw_arrow(draw, (260, 210), (310, 210), fill="#0284C7", width=3)
    draw_arrow(draw, (260, 480), (310, 480), fill="#0284C7", width=3)
    draw_arrow(draw, (680, 270), (730, 270), fill="#16A34A", width=3)
    draw_arrow(draw, (680, 500), (730, 500), fill="#16A34A", width=3)
    draw_arrow(draw, (680, 550), (980, 550), fill="#7C3AED", width=3)
    draw_arrow(draw, (930, 500), (980, 500), fill="#64748B", width=2)
    
    draw.text((710, 555), "Authorize & Commit", fill="#7C3AED", font=get_font(10, bold=True))

    for path in output_paths:
        img.save(path)
    print("Saved Fig 1 to", output_paths)

def generate_fig2(output_paths):
    img = Image.new("RGB", (1200, 620), color="#F8FAFC")
    draw = ImageDraw.Draw(img)
    
    # Title Banner
    draw.rectangle([0, 0, 1200, 50], fill="#0F172A")
    draw.text((30, 14), "Fig. 2: Multi-Source Evidence Ingestion, M5 Contradiction Reconciliation & Kleene M8 Decision Engine", fill="#FFFFFF", font=get_font(17, bold=True))
    
    # Stage 1: Evidence Sources
    draw_rounded_rect(draw, (30, 80, 240, 580), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((45, 95), "1. Evidence Ingestion", fill="#0F172A", font=get_font(14, bold=True))
    draw.text((45, 115), "Multi-source off-chain inputs", fill="#64748B", font=get_font(11))
    
    sources = [
        ("Registration Deed", "Deed ID, Owner Name,\nConveyance Date", "#EFF6FF", "#93C5FD"),
        ("Cadastral Survey", "Survey No, Polygon WKT,\nBoundary coordinates, Area", "#F0FDF4", "#86EFAC"),
        ("Revenue Records", "Patta / Jamabandi No,\nTax Clearance status", "#FFFBEB", "#FCD34D"),
        ("Encumbrance Registry", "Bank Mortgage, Charge,\nLien satisfaction certificate", "#FAF5FF", "#D8B4FE"),
        ("Judicial / Court Order", "Dispute injunction,\nAttachment, Clearance", "#FEF2F2", "#FCA5A5")
    ]
    sy = 145
    for sname, sdesc, sbg, sborder in sources:
        draw_rounded_rect(draw, (45, sy, 225, sy + 70), fill=sbg, outline=sborder, width=2)
        draw.text((55, sy + 8), sname, fill="#0F172A", font=get_font(11, bold=True))
        draw.text((55, sy + 28), sdesc, fill="#334155", font=get_font(9))
        sy += 85

    # Stage 2: M5 Contradiction Reconciliation
    draw_rounded_rect(draw, (290, 80, 560, 580), fill="#FFFFFF", outline="#94A3B8", width=2)
    draw.text((305, 95), "2. M5 Contradiction Reconciliation", fill="#0F172A", font=get_font(14, bold=True))
    draw.text((305, 115), "Cross-registry consistency rules", fill="#64748B", font=get_font(11))

    m5_rules = [
        ("RULE_OWNER_MATCH", "Check Deed vs Revenue assertion:\nDeed(Owner) == Revenue(Owner)\nCollision -> Witness(severity=HIGH)", 145, "#FEF2F2", "#FCA5A5"),
        ("RULE_BOUNDARY_DEVIATION", "Check Cadastral vs Registered area:\n|SurveyArea - DeedArea| / DeedArea\nDeviation > 1.5% -> Witness(MEDIUM)", 245, "#FFFBEB", "#FCD34D"),
        ("RULE_ENCUMBRANCE_CLEAR", "Check Mortgage / Lien registry:\nCLEAR vs PRESENT vs UNKNOWN\nActive mortgage -> Witness(HIGH)", 345, "#FAF5FF", "#D8B4FE"),
        ("RULE_NO_PENDING_DISPUTE", "Check Court / Injunction Registry:\nActive dispute -> Witness(CRITICAL)", 445, "#FEF2F2", "#FCA5A5")
    ]
    for rname, rdesc, ry, rbg, rborder in m5_rules:
        draw_rounded_rect(draw, (305, ry, 545, ry + 80), fill=rbg, outline=rborder, width=2)
        draw.text((315, ry + 8), rname, fill="#0F172A", font=get_font(11, bold=True))
        draw.text((315, ry + 28), rdesc, fill="#334155", font=get_font(9))

    # Stage 3: Kleene 3-Valued Propositional Logic (M8)
    draw_rounded_rect(draw, (610, 80, 870, 580), fill="#FFFFFF", outline="#94A3B8", width=2)
    draw.text((625, 95), "3. M8 Uncertainty Engine", fill="#0F172A", font=get_font(14, bold=True))
    draw.text((625, 115), "Kleene Logic: {T, F, U}", fill="#64748B", font=get_font(11))

    # Truth Table visual
    draw_rounded_rect(draw, (625, 145, 855, 300), fill="#F8FAFC", outline="#CBD5E1", width=1)
    draw.text((635, 155), "Kleene Conjunction Matrix (P1 ^ P2)", fill="#0F172A", font=get_font(11, bold=True))
    matrix_text = (
        "  ^  | SATISFIED (T) | UNRESOLVED (U) | VIOLATED (F)\n"
        "-----+---------------+----------------+-------------\n"
        "  T  |       T       |       U        |      F      \n"
        "  U  |       U       |       U        |      F      \n"
        "  F  |       F       |       F        |      F      "
    )
    draw.text((635, 185), matrix_text, fill="#334155", font=get_font(9))
    draw.text((635, 275), "Rule: Missing evidence maps strictly to U (NOT F)", fill="#D97706", font=get_font(9, bold=True))

    m8_eval = [
        ("SATISFIED (T)", "Sufficient positive evidence exists;\nno contradiction detected.", 320, "#F0FDF4", "#86EFAC"),
        ("UNRESOLVED (U)", "Required evidence missing or ambiguous\n(e.g., pending tax / mortgage verification).", 400, "#FFFBEB", "#FCD34D"),
        ("VIOLATED (F)", "Contradiction proven or boundary check\nfailed (e.g., overlapping child polygons).", 480, "#FEF2F2", "#FCA5A5")
    ]
    for mname, mdesc, my, mbg, mborder in m8_eval:
        draw_rounded_rect(draw, (625, my, 855, my + 65), fill=mbg, outline=mborder, width=2)
        draw.text((635, my + 8), mname, fill="#0F172A", font=get_font(11, bold=True))
        draw.text((635, my + 28), mdesc, fill="#334155", font=get_font(9))

    # Stage 4: Deterministic Decision Gate & Outcome
    draw_rounded_rect(draw, (920, 80, 1170, 580), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((935, 95), "4. Decision Gate & Settlement", fill="#0F172A", font=get_font(14, bold=True))
    draw.text((935, 115), "Deterministic decision function", fill="#64748B", font=get_font(11))

    outcomes = [
        ("AUTHORIZE", "Condition: All predicates == T\nOutcome: Generate Decision Certificate\n& Dispatch on-chain commitment.", 150, 110, "#DCFCE7", "#22C55E", "#15803D"),
        ("HOLD", "Condition: Any predicate == U\nOutcome: Server-side settlement gate blocks\ncommit. Registrar requests missing evidence.", 290, 110, "#FEF3C7", "#F59E0B", "#B45309"),
        ("REJECT", "Condition: Any predicate == F or\nHigh severity ConflictWitness.\nOutcome: Mutation terminated permanently.", 430, 110, "#FEE2E2", "#EF4444", "#B91C1C")
    ]
    for oname, odesc, oy, oh, obg, oborder, otcolor in outcomes:
        draw_rounded_rect(draw, (935, oy, 1155, oy + oh), fill=obg, outline=oborder, width=3)
        draw.text((950, oy + 12), oname, fill=otcolor, font=get_font(13, bold=True))
        draw.text((950, oy + 38), odesc, fill="#1F2937", font=get_font(9))

    # Arrows between stages
    draw_arrow(draw, (240, 330), (290, 330), fill="#475569", width=3)
    draw_arrow(draw, (560, 330), (610, 330), fill="#475569", width=3)
    draw_arrow(draw, (870, 205), (920, 205), fill="#16A34A", width=3)
    draw_arrow(draw, (870, 345), (920, 345), fill="#D97706", width=3)
    draw_arrow(draw, (870, 485), (920, 485), fill="#DC2626", width=3)

    for path in output_paths:
        img.save(path)
    print("Saved Fig 2 to", output_paths)

def generate_fig3(output_paths):
    img = Image.new("RGB", (1200, 560), color="#F8FAFC")
    draw = ImageDraw.Draw(img)
    
    # Title Banner
    draw.rectangle([0, 0, 1200, 50], fill="#0F172A")
    draw.text((30, 14), "Fig. 3: Cadastral Spatial Verification Invariants (M2) & Directed Acyclic Graph (DAG) Mutation Lineage", fill="#FFFFFF", font=get_font(17, bold=True))

    # Left Section: M2 Spatial Invariants
    draw_rounded_rect(draw, (30, 80, 580, 520), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((45, 95), "(A) Topological Spatial Invariants (LocationTech JTS)", fill="#0F172A", font=get_font(13, bold=True))

    # Invariant 1: Child Containment
    draw_rounded_rect(draw, (45, 130, 565, 235), fill="#F0FDF4", outline="#86EFAC", width=1)
    draw.text((55, 138), "1. Parent Containment Invariant: parent.covers(child)", fill="#15803D", font=get_font(11, bold=True))
    draw.text((55, 158), "Every child polygon C_k must lie completely within parent boundary P:\nUnion(C_k) difference P == EmptyGeometry (measured spill area == 0.0 sqm)", fill="#334155", font=get_font(10))
    # visual illustration of rectangle inside rectangle
    draw.rectangle([450, 140, 550, 225], fill="#DCFCE7", outline="#15803D", width=2)
    draw.rectangle([460, 150, 540, 215], fill="#BBF7D0", outline="#16A34A", width=2)
    draw.text((465, 175), "P covers C", fill="#166534", font=get_font(9, bold=True))

    # Invariant 2: Non-Overlap
    draw_rounded_rect(draw, (45, 250, 565, 365), fill="#EFF6FF", outline="#93C5FD", width=1)
    draw.text((55, 258), "2. Pairwise Topological Non-Overlap: intersection.isEmpty()", fill="#1D4ED8", font=get_font(11, bold=True))
    draw.text((55, 278), "For any distinct children C_i and C_j (i != j):\nArea(C_i intersect C_j) == 0.0 sqm (enforced at metric precision EPSG:3857)", fill="#334155", font=get_font(10))
    draw.rectangle([440, 265, 495, 350], fill="#DBEAFE", outline="#2563EB", width=2)
    draw.rectangle([500, 265, 555, 350], fill="#E0E7FF", outline="#4F46E5", width=2)
    draw.text((448, 300), "C_1", fill="#1E40AF", font=get_font(10, bold=True))
    draw.text((508, 300), "C_2", fill="#3730A3", font=get_font(10, bold=True))

    # Invariant 3: Area Conservation
    draw_rounded_rect(draw, (45, 380, 565, 500), fill="#FFFBEB", outline="#FCD34D", width=1)
    draw.text((55, 388), "3. Area Conservation & Boundary Gap Detection", fill="#B45309", font=get_font(11, bold=True))
    draw.text((55, 408), "Sum of child areas must conserve parent area within tolerance:\n|Area(P) - sum(Area(C_k))| <= max(1.0, 0.001 * Area(P))\nP difference Union(C_k) == Empty (no internal unassigned gaps)", fill="#334155", font=get_font(10))
    draw.text((55, 470), "Validated on metric planar coordinates (rejects unprojected lat/lon)", fill="#92400E", font=get_font(9, bold=True))

    # Right Section: Cadastral Lineage DAG
    draw_rounded_rect(draw, (620, 80, 1170, 520), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((635, 95), "(B) Cadastral Mutation Lineage DAG (Directed Acyclic Graph)", fill="#0F172A", font=get_font(13, bold=True))

    # Diagram nodes for Split Scenario (P-404 -> P-404-A, P-404-B)
    draw_rounded_rect(draw, (640, 140, 810, 200), fill="#FEF3C7", outline="#F59E0B", width=2)
    draw.text((650, 150), "Parent Parcel: P-404", fill="#0F172A", font=get_font(11, bold=True))
    draw.text((650, 170), "Area: 10,000 sqm | v1\nStatus: INACTIVE_SPLIT", fill="#334155", font=get_font(9))

    draw_rounded_rect(draw, (930, 120, 1140, 175), fill="#DCFCE7", outline="#22C55E", width=2)
    draw.text((940, 128), "Child: P-404-A", fill="#15803D", font=get_font(11, bold=True))
    draw.text((940, 146), "Area: 6,000 sqm | v1 | ACTIVE", fill="#166534", font=get_font(9))

    draw_rounded_rect(draw, (930, 185, 1140, 240), fill="#DCFCE7", outline="#22C55E", width=2)
    draw.text((940, 193), "Child: P-404-B", fill="#15803D", font=get_font(11, bold=True))
    draw.text((940, 211), "Area: 4,000 sqm | v1 | ACTIVE", fill="#166534", font=get_font(9))

    draw_arrow(draw, (810, 160), (930, 145), fill="#16A34A", width=2)
    draw_arrow(draw, (810, 180), (930, 210), fill="#16A34A", width=2)
    draw.text((825, 150), "SPLIT", fill="#16A34A", font=get_font(10, bold=True))

    # Diagram nodes for Merge Scenario (P-101 + P-202 -> P-MERGE-1)
    draw_rounded_rect(draw, (640, 290, 810, 345), fill="#EFF6FF", outline="#3B82F6", width=2)
    draw.text((650, 298), "Parent: P-101 (10,000 sqm)", fill="#1E40AF", font=get_font(11, bold=True))
    draw.text((650, 316), "Status: INACTIVE_MERGED", fill="#334155", font=get_font(9))

    draw_rounded_rect(draw, (640, 360, 810, 415), fill="#EFF6FF", outline="#3B82F6", width=2)
    draw.text((650, 368), "Parent: P-202 (7,500 sqm)", fill="#1E40AF", font=get_font(11, bold=True))
    draw.text((650, 386), "Status: INACTIVE_MERGED", fill="#334155", font=get_font(9))

    draw_rounded_rect(draw, (930, 320, 1140, 385), fill="#FAF5FF", outline="#A855F7", width=2)
    draw.text((940, 328), "Merged: P-MERGE-1", fill="#6B21A8", font=get_font(11, bold=True))
    draw.text((940, 348), "Area: 17,500 sqm | v1\nStatus: ACTIVE", fill="#334155", font=get_font(9))

    draw_arrow(draw, (810, 320), (930, 345), fill="#7C3AED", width=2)
    draw_arrow(draw, (810, 385), (930, 360), fill="#7C3AED", width=2)
    draw.text((825, 340), "MERGE", fill="#7C3AED", font=get_font(10, bold=True))

    draw_rounded_rect(draw, (640, 440, 1140, 500), fill="#F8FAFC", outline="#E2E8F0", width=1)
    draw.text((650, 450), "Lineage Properties: Acyclic (no cycles allowed), Multi-Parent (merges), Multi-Child (splits).", fill="#0F172A", font=get_font(10, bold=True))
    draw.text((650, 472), "Enables historical auditability: reconstruct any ancestral boundary at arbitrary timestamp T.", fill="#64748B", font=get_font(9))

    for path in output_paths:
        img.save(path)
    print("Saved Fig 3 to", output_paths)

def generate_fig4(output_paths):
    img = Image.new("RGB", (1200, 640), color="#F8FAFC")
    draw = ImageDraw.Draw(img)
    
    # Title Banner
    draw.rectangle([0, 0, 1200, 50], fill="#0F172A")
    draw.text((30, 14), "Fig. 4: Experimental Evaluation: Comparative Baseline Benchmarks & Ablation Metrics", fill="#FFFFFF", font=get_font(17, bold=True))

    # Panel 1: Throughput (TPS) Comparison
    draw_rounded_rect(draw, (30, 80, 580, 340), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((45, 95), "(A) System Throughput (Transactions Per Second - Log Scale)", fill="#0F172A", font=get_font(12, bold=True))
    
    tps_data = [
        ("B0 Conventional DB", 105024.4, "#94A3B8"),
        ("B1 Blockchain-Only", 2756.3, "#F87171"),
        ("B2 Full Revalidation", 2817.6, "#FB923C"),
        ("P Proposed Framework", 11970.1, "#22C55E")
    ]
    import math
    by = 135
    for label, val, col in tps_data:
        draw.text((45, by + 4), label, fill="#334155", font=get_font(10, bold=True))
        draw.text((220, by + 4), f"{val:,.1f} TPS", fill="#0F172A", font=get_font(10))
        # Log scale bar
        bar_len = int(math.log10(max(val, 1)) / 5.5 * 250)
        draw.rectangle([320, by + 2, 320 + bar_len, by + 22], fill=col)
        by += 45

    # Panel 2: Latency Comparison (Median vs P95 ms)
    draw_rounded_rect(draw, (620, 80, 1170, 340), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((635, 95), "(B) Verification Latency (Median & P95 in ms - Lower is Better)", fill="#0F172A", font=get_font(12, bold=True))
    
    lat_data = [
        ("B1 Blockchain-Only", 0.14, 0.45),
        ("B2 Full Revalidation", 0.23, 0.72),
        ("P Proposed Framework", 0.06, 0.19),
        ("A4 No Scoped Reval", 0.16, 1.05)
    ]
    ly = 135
    for label, med, p95 in lat_data:
        draw.text((635, ly + 4), label, fill="#334155", font=get_font(10, bold=True))
        draw.text((820, ly + 4), f"Med: {med:.2f}ms | P95: {p95:.2f}ms", fill="#0F172A", font=get_font(10))
        # Bar for median
        m_len = int(med * 220)
        p_len = int(p95 * 220)
        draw.rectangle([1000, ly + 2, 1000 + m_len, ly + 10], fill="#3B82F6")
        draw.rectangle([1000, ly + 13, 1000 + p_len, ly + 21], fill="#EF4444")
        ly += 45
    draw.rectangle([1000, 310, 1015, 320], fill="#3B82F6")
    draw.text((1022, 309), "Median Latency", fill="#64748B", font=get_font(9))
    draw.rectangle([1095, 310, 1110, 320], fill="#EF4444")
    draw.text((1117, 309), "P95 Latency", fill="#64748B", font=get_font(9))

    # Panel 3: Safety Comparison (Unsafe Authorization Rate %)
    draw_rounded_rect(draw, (30, 360, 580, 610), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((45, 375), "(C) Unsafe Authorization Rate (% of Flawed Mutations Committed)", fill="#0F172A", font=get_font(12, bold=True))
    
    safety_data = [
        ("B0 Conventional DB", 100.0, "#DC2626"),
        ("B1 Blockchain-Only", 100.0, "#DC2626"),
        ("A1 No M5 (Contradiction)", 25.45, "#F59E0B"),
        ("A2 No M8 (Binary Logic)", 24.24, "#F59E0B"),
        ("A3 No M2 (Spatial Lineage)", 33.94, "#EF4444"),
        ("P Proposed Framework", 0.00, "#16A34A")
    ]
    sy = 405
    for label, rate, col in safety_data:
        draw.text((45, sy + 3), label, fill="#334155", font=get_font(10, bold=True))
        draw.text((245, sy + 3), f"{rate:5.2f}%", fill=col, font=get_font(10, bold=True))
        bar_len = int(rate * 2.2)
        if bar_len > 0:
            draw.rectangle([320, sy + 2, 320 + bar_len, sy + 18], fill=col)
        else:
            draw.rectangle([320, sy + 2, 330, sy + 18], fill="#16A34A")
            draw.text((340, sy + 3), "0.00% Zero-Defect Gating", fill="#15803D", font=get_font(9, bold=True))
        sy += 32

    # Panel 4: Revalidation Efficiency (Checks per Mutation)
    draw_rounded_rect(draw, (620, 360, 1170, 610), fill="#FFFFFF", outline="#CBD5E1", width=2)
    draw.text((635, 375), "(D) Revalidation Overhead & Rule Checks Per Mutation", fill="#0F172A", font=get_font(12, bold=True))

    checks_data = [
        ("B2 Full Revalidation", 39, "39 checks (Full history scan over all prior mutations)", "#DC2626"),
        ("A4 No Scoped Revalidation", 39, "39 checks (No incremental dependency scoping)", "#F97316"),
        ("P Proposed Framework", 14, "14 checks (64.1% reduction via Scoped Ancestry Graph)", "#16A34A")
    ]
    cy = 415
    for label, nchecks, desc, col in checks_data:
        draw.text((635, cy), label, fill="#0F172A", font=get_font(11, bold=True))
        draw.text((635, cy + 18), desc, fill="#64748B", font=get_font(9))
        bar_len = int(nchecks * 6.5)
        draw.rectangle([900, cy + 2, 900 + bar_len, cy + 22], fill=col)
        draw.text((900 + bar_len + 8, cy + 4), f"{nchecks}", fill=col, font=get_font(11, bold=True))
        cy += 60

    for path in output_paths:
        img.save(path)
    print("Saved Fig 4 to", output_paths)

if __name__ == "__main__":
    brain_dir = r"C:\Users\User\.gemini\antigravity\brain\0503defc-360d-47ac-895f-6337398b84fb"
    docs_dir = r"C:\Users\User\Desktop\JavaProject\land-registry\docs\figures"
    
    os.makedirs(docs_dir, exist_ok=True)
    os.makedirs(brain_dir, exist_ok=True)
    
    generate_fig1([os.path.join(docs_dir, "fig1_system_architecture.png"), os.path.join(brain_dir, "fig1_system_architecture.png")])
    generate_fig2([os.path.join(docs_dir, "fig2_verification_pipeline.png"), os.path.join(brain_dir, "fig2_verification_pipeline.png")])
    generate_fig3([os.path.join(docs_dir, "fig3_spatial_lineage.png"), os.path.join(brain_dir, "fig3_spatial_lineage.png")])
    generate_fig4([os.path.join(docs_dir, "fig4_benchmark_charts.png"), os.path.join(brain_dir, "fig4_benchmark_charts.png")])
    print("All 4 IEEE figures successfully generated!")
