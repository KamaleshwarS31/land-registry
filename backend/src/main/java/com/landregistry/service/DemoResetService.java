package com.landregistry.service;

import com.landregistry.model.EvidenceRecord;
import com.landregistry.model.Parcel;
import com.landregistry.model.User;
import com.landregistry.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class DemoResetService {

    private static final Logger log = LoggerFactory.getLogger(DemoResetService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final ParcelRepository parcelRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final MutationRequestRepository mutationRequestRepository;
    private final PredicateResultRepository predicateResultRepository;
    private final DecisionCertificateRepository certificateRepository;
    private final BlockchainTransactionRepository blockchainTransactionRepository;
    private final ParcelLineageRepository lineageRepository;
    private final AuditEventRepository auditEventRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoResetService(
            UserRepository userRepository,
            ParcelRepository parcelRepository,
            EvidenceRecordRepository evidenceRecordRepository,
            MutationRequestRepository mutationRequestRepository,
            PredicateResultRepository predicateResultRepository,
            DecisionCertificateRepository certificateRepository,
            BlockchainTransactionRepository blockchainTransactionRepository,
            ParcelLineageRepository lineageRepository,
            AuditEventRepository auditEventRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.parcelRepository = parcelRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.mutationRequestRepository = mutationRequestRepository;
        this.predicateResultRepository = predicateResultRepository;
        this.certificateRepository = certificateRepository;
        this.blockchainTransactionRepository = blockchainTransactionRepository;
        this.lineageRepository = lineageRepository;
        this.auditEventRepository = auditEventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void resetDemoData() {
        log.info("Resetting demo dataset to clean state (P-101 to P-405)...");

        // 1. Delete dependent transactional records in batch
        lineageRepository.deleteAllInBatch();
        blockchainTransactionRepository.deleteAllInBatch();
        certificateRepository.deleteAllInBatch();
        predicateResultRepository.deleteAllInBatch();
        mutationRequestRepository.deleteAllInBatch();
        evidenceRecordRepository.deleteAllInBatch();
        parcelRepository.deleteAllInBatch();

        // Flush and clear persistence context to avoid detached merge collisions
        entityManager.flush();
        entityManager.clear();

        // 2. Ensure Users exist with valid password hash
        String passHash = "$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG"; // "password123"

        User alice = ensureUser("u-alice", "alice", passHash, "Alice Vance (Landowner)", "LANDOWNER", "0x70997970C51812dc3A010C7d01b50e0d17dc79C8");
        User bob = ensureUser("u-bob", "bob", passHash, "Bob Smith (Buyer)", "BUYER", "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC");
        User clara = ensureUser("u-clara", "clara", passHash, "Clara Oswald (Registrar)", "REGISTRAR", "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266");
        User dan = ensureUser("u-dan", "dan", passHash, "Dan Miller (Survey Reviewer)", "SURVEY_REVIEWER", "0x90F79bf6EB2c4f870365E785982E1f101E93b906");
        User eve = ensureUser("u-eve", "eve", passHash, "Eve Polastri (Admin)", "ADMIN", "0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65");

        // 3. Seed Section 8 Demo Parcels
        // P-101: Happy-path transfer Alice -> Bob
        Parcel p101 = parcelRepository.save(Parcel.builder()
                .id("p-101")
                .parcelCode("P-101")
                .surveyNumber("SY-2024-101")
                .locality("Greenfield Sector 1")
                .areaSqm(new BigDecimal("10000.0000"))
                .geometryWkt("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))")
                .status("ACTIVE")
                .owner(alice)
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        seedEvidence("ev-101-deed", p101, "REGISTRATION_DEED", "DEED-2022-101", "OWNER_NAME", "Alice Vance", "v1.0",
                "alice_deed_101.pdf", "land-evidence/P-101/deed.pdf", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", alice);
        seedEvidence("ev-101-survey", p101, "CADASTRAL_SURVEY", "SURVEY-2024-101", "SURVEY_AREA_SQM", "10000.0", "v2.0",
                "cadastral_survey_101.pdf", "land-evidence/P-101/survey.pdf", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35", dan);
        seedEvidence("ev-101-tax", p101, "REVENUE_RECORD", "TAX-2024-101", "TAX_CLEARANCE", "PAID_CLEARED", "v2024.1",
                "tax_clearance_101.pdf", "land-evidence/P-101/tax.pdf", "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a", alice);

        // P-202: Missing mortgage clearance -> UNRESOLVED -> HOLD
        Parcel p202 = parcelRepository.save(Parcel.builder()
                .id("p-202")
                .parcelCode("P-202")
                .surveyNumber("SY-2024-202")
                .locality("Greenfield Sector 2")
                .areaSqm(new BigDecimal("7500.0000"))
                .geometryWkt("POLYGON ((100 0, 175 0, 175 100, 100 100, 100 0))")
                .status("ACTIVE")
                .owner(alice)
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        seedEvidence("ev-202-deed", p202, "REGISTRATION_DEED", "DEED-2022-202", "OWNER_NAME", "Alice Vance", "v1.0",
                "alice_deed_202.pdf", "land-evidence/P-202/deed.pdf", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", alice);
        seedEvidence("ev-202-survey", p202, "CADASTRAL_SURVEY", "SURVEY-2024-202", "SURVEY_AREA_SQM", "7500.0", "v1.0",
                "survey_202.pdf", "land-evidence/P-202/survey.pdf", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35", dan);
        // Note: NO REVENUE_RECORD or MORTGAGE_REGISTRY seeded -> M8 will produce HOLD!

        // P-303: Conflicting ownership claims -> M5 Conflict Witness -> REJECT
        Parcel p303 = parcelRepository.save(Parcel.builder()
                .id("p-303")
                .parcelCode("P-303")
                .surveyNumber("SY-2024-303")
                .locality("Greenfield Sector 3")
                .areaSqm(new BigDecimal("12000.0000"))
                .geometryWkt("POLYGON ((200 0, 320 0, 320 100, 200 100, 200 0))")
                .status("ACTIVE")
                .owner(alice)
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        seedEvidence("ev-303-deed", p303, "REGISTRATION_DEED", "DEED-2021-303", "OWNER_NAME", "Alice Vance", "v1.0",
                "alice_deed_303.pdf", "land-evidence/P-303/deed.pdf", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", alice);
        seedEvidence("ev-303-conflict", p303, "REVENUE_RECORD", "REV-2024-303", "OWNER_NAME", "Eve Polastri", "v2024.1",
                "conflicting_revenue_record.pdf", "land-evidence/P-303/rev.pdf", "a1b2c3d4e5f678901234567890abcdef1234567890abcdef1234567890abcdef", eve);
        seedEvidence("ev-303-survey", p303, "CADASTRAL_SURVEY", "SURVEY-2024-303", "SURVEY_AREA_SQM", "12000.0", "v1.0",
                "survey_303.pdf", "land-evidence/P-303/survey.pdf", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35", dan);

        // P-404: Parent parcel for valid split into two children (6,000 + 4,000 sqm)
        Parcel p404 = parcelRepository.save(Parcel.builder()
                .id("p-404")
                .parcelCode("P-404")
                .surveyNumber("SY-2024-404")
                .locality("Greenfield North Sector")
                .areaSqm(new BigDecimal("10000.0000"))
                .geometryWkt("POLYGON ((0 150, 100 150, 100 250, 0 250, 0 150))")
                .status("ACTIVE")
                .owner(alice)
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        seedEvidence("ev-404-deed", p404, "REGISTRATION_DEED", "DEED-2022-404", "OWNER_NAME", "Alice Vance", "v1.0",
                "alice_deed_404.pdf", "land-evidence/P-404/deed.pdf", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", alice);
        seedEvidence("ev-404-survey", p404, "CADASTRAL_SURVEY", "SURVEY-2024-404", "SURVEY_AREA_SQM", "10000.0", "v1.0",
                "survey_404.pdf", "land-evidence/P-404/survey.pdf", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35", dan);
        seedEvidence("ev-404-tax", p404, "REVENUE_RECORD", "TAX-2024-404", "TAX_CLEARANCE", "PAID_CLEARED", "v2024.1",
                "tax_404.pdf", "land-evidence/P-404/tax.pdf", "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a", alice);

        // P-405: Parent parcel for invalid split (deliberate overlap or boundary gap)
        Parcel p405 = parcelRepository.save(Parcel.builder()
                .id("p-405")
                .parcelCode("P-405")
                .surveyNumber("SY-2024-405")
                .locality("Greenfield North Sector")
                .areaSqm(new BigDecimal("8000.0000"))
                .geometryWkt("POLYGON ((100 150, 180 150, 180 250, 100 250, 100 150))")
                .status("ACTIVE")
                .owner(alice)
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        seedEvidence("ev-405-deed", p405, "REGISTRATION_DEED", "DEED-2022-405", "OWNER_NAME", "Alice Vance", "v1.0",
                "alice_deed_405.pdf", "land-evidence/P-405/deed.pdf", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", alice);
        seedEvidence("ev-405-survey", p405, "CADASTRAL_SURVEY", "SURVEY-2024-405", "SURVEY_AREA_SQM", "8000.0", "v1.0",
                "survey_405.pdf", "land-evidence/P-405/survey.pdf", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35", dan);
        seedEvidence("ev-405-tax", p405, "REVENUE_RECORD", "TAX-2024-405", "TAX_CLEARANCE", "PAID_CLEARED", "v2024.1",
                "tax_405.pdf", "land-evidence/P-405/tax.pdf", "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a", alice);

        entityManager.flush();
        log.info("Demo dataset reset complete: P-101, P-202, P-303, P-404, P-405 active and ready.");
    }

    private User ensureUser(String id, String login, String passHash, String name, String role, String wallet) {
        return userRepository.findById(id).orElseGet(() -> userRepository.save(
                User.builder()
                        .id(id)
                        .login(login)
                        .passwordHash(passHash)
                        .displayName(name)
                        .role(role)
                        .walletAddress(wallet)
                        .enabled(true)
                        .createdAt(Instant.now())
                        .build()
        ));
    }

    private void seedEvidence(String id, Parcel parcel, String sourceId, String sourceRecordId,
                              String attrName, String attrVal, String versionLabel,
                              String fileName, String filePath, String sha256, User uploader) {
        evidenceRecordRepository.save(EvidenceRecord.builder()
                .id(id)
                .parcel(parcel)
                .sourceId(sourceId)
                .sourceRecordId(sourceRecordId)
                .attributeName(attrName)
                .attributeValue(attrVal)
                .versionLabel(versionLabel)
                .observedAt(Instant.now())
                .fileName(fileName)
                .filePath(filePath)
                .storageProvider("LOCAL")
                .sha256Hash(sha256)
                .uploadedBy(uploader)
                .createdAt(Instant.now())
                .build());
    }
}
