-- V2: Seed Initial Demo Users, Parcels, and Evidence

-- Password for all demo accounts is 'password123'
-- Hash: $2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG
INSERT INTO users (id, login, password_hash, display_name, role, wallet_address, enabled, created_at) VALUES
('u-alice', 'alice', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Alice Vance (Landowner)', 'LANDOWNER', '0x70997970C51812dc3A010C7d01b50e0d17dc79C8', TRUE, CURRENT_TIMESTAMP),
('u-bob', 'bob', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Bob Smith (Buyer)', 'BUYER', '0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC', TRUE, CURRENT_TIMESTAMP),
('u-clara', 'clara', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Clara Oswald (Registrar)', 'REGISTRAR', '0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266', TRUE, CURRENT_TIMESTAMP),
('u-dan', 'dan', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Dan Miller (Survey Reviewer)', 'SURVEY_REVIEWER', '0x90F79bf6EB2c4f870365E785982E1f101E93b906', TRUE, CURRENT_TIMESTAMP),
('u-eve', 'eve', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Eve Polastri (Admin)', 'ADMIN', '0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65', TRUE, CURRENT_TIMESTAMP);

-- Sample Parcels owned by Alice
INSERT INTO parcels (id, parcel_code, survey_number, locality, area_sqm, geometry_wkt, status, owner_user_id, version, created_at, updated_at) VALUES
('p-001', 'PARCEL-DL-001', 'SY-2024-401', 'Greenfield Sector 1', 10000.0000, 'POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('p-002', 'PARCEL-DL-002', 'SY-2024-402', 'Greenfield Sector 1', 8000.0000, 'POLYGON ((100 0, 180 0, 180 100, 100 100, 100 0))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Multi-Source Evidence for PARCEL-DL-001
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-001', 'p-001', NULL, 'REGISTRATION_DEED', 'DEED-2022-8921', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_2022.pdf', 'land-evidence/PARCEL-DL-001/deed_2022.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-002', 'p-001', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2023-1102', 'SURVEY_AREA_SQM', '10000.0', 'v2.1', CURRENT_TIMESTAMP, 'cadastral_survey_2023.pdf', 'land-evidence/PARCEL-DL-001/survey_2023.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP),
('ev-003', 'p-001', NULL, 'REVENUE_RECORD', 'REV-2024-5541', 'TAX_CLEARANCE', 'PAID_CLEARED', 'v2024.1', CURRENT_TIMESTAMP, 'tax_clearance_2024.pdf', 'land-evidence/PARCEL-DL-001/tax_2024.pdf', 'LOCAL', '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', 'u-alice', CURRENT_TIMESTAMP);
