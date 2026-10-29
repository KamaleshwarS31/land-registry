-- V2: Seed Canonical Demo Users, Parcels, and Evidence (Report Section 8)

-- Password for all demo accounts is 'password123'
-- Hash: $2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG
INSERT INTO users (id, login, password_hash, display_name, role, wallet_address, enabled, created_at) VALUES
('u-alice', 'alice', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Alice Vance (Landowner)', 'LANDOWNER', '0x70997970C51812dc3A010C7d01b50e0d17dc79C8', TRUE, CURRENT_TIMESTAMP),
('u-bob', 'bob', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Bob Smith (Buyer)', 'BUYER', '0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC', TRUE, CURRENT_TIMESTAMP),
('u-clara', 'clara', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Clara Oswald (Registrar)', 'REGISTRAR', '0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266', TRUE, CURRENT_TIMESTAMP),
('u-dan', 'dan', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Dan Miller (Survey Reviewer)', 'SURVEY_REVIEWER', '0x90F79bf6EB2c4f870365E785982E1f101E93b906', TRUE, CURRENT_TIMESTAMP),
('u-eve', 'eve', '$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG', 'Eve Polastri (Admin)', 'ADMIN', '0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65', TRUE, CURRENT_TIMESTAMP);

-- Scripted Demo Parcels (P-101 to P-405)
INSERT INTO parcels (id, parcel_code, survey_number, locality, area_sqm, geometry_wkt, status, owner_user_id, version, created_at, updated_at) VALUES
('p-101', 'P-101', 'SY-2024-101', 'Greenfield Sector 1', 10000.0000, 'POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('p-202', 'P-202', 'SY-2024-202', 'Greenfield Sector 2', 7500.0000, 'POLYGON ((100 0, 175 0, 175 100, 100 100, 100 0))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('p-303', 'P-303', 'SY-2024-303', 'Greenfield Sector 3', 12000.0000, 'POLYGON ((200 0, 320 0, 320 100, 200 100, 200 0))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('p-404', 'P-404', 'SY-2024-404', 'Greenfield North Sector', 10000.0000, 'POLYGON ((0 150, 100 150, 100 250, 0 250, 0 150))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('p-405', 'P-405', 'SY-2024-405', 'Greenfield North Sector', 8000.0000, 'POLYGON ((100 150, 180 150, 180 250, 100 250, 100 150))', 'ACTIVE', 'u-alice', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Multi-Source Evidence Records
-- P-101 (Happy Path: Deed + Survey + Tax)
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-101-deed', 'p-101', NULL, 'REGISTRATION_DEED', 'DEED-2022-101', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_101.pdf', 'land-evidence/P-101/deed.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-101-survey', 'p-101', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2024-101', 'SURVEY_AREA_SQM', '10000.0', 'v2.0', CURRENT_TIMESTAMP, 'cadastral_survey_101.pdf', 'land-evidence/P-101/survey.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP),
('ev-101-tax', 'p-101', NULL, 'REVENUE_RECORD', 'TAX-2024-101', 'TAX_CLEARANCE', 'PAID_CLEARED', 'v2024.1', CURRENT_TIMESTAMP, 'tax_clearance_101.pdf', 'land-evidence/P-101/tax.pdf', 'LOCAL', '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', 'u-alice', CURRENT_TIMESTAMP);

-- P-202 (HOLD: Missing Tax / Mortgage Clearance)
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-202-deed', 'p-202', NULL, 'REGISTRATION_DEED', 'DEED-2022-202', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_202.pdf', 'land-evidence/P-202/deed.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-202-survey', 'p-202', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2024-202', 'SURVEY_AREA_SQM', '7500.0', 'v1.0', CURRENT_TIMESTAMP, 'survey_202.pdf', 'land-evidence/P-202/survey.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP);

-- P-303 (REJECT: Ownership Collision Alice vs Eve)
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-303-deed', 'p-303', NULL, 'REGISTRATION_DEED', 'DEED-2021-303', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_303.pdf', 'land-evidence/P-303/deed.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-303-conflict', 'p-303', NULL, 'REVENUE_RECORD', 'REV-2024-303', 'OWNER_NAME', 'Eve Polastri', 'v2024.1', CURRENT_TIMESTAMP, 'conflicting_revenue_record.pdf', 'land-evidence/P-303/rev.pdf', 'LOCAL', 'a1b2c3d4e5f678901234567890abcdef1234567890abcdef1234567890abcdef', 'u-eve', CURRENT_TIMESTAMP),
('ev-303-survey', 'p-303', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2024-303', 'SURVEY_AREA_SQM', '12000.0', 'v1.0', CURRENT_TIMESTAMP, 'survey_303.pdf', 'land-evidence/P-303/survey.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP);

-- P-404 (Valid Split: Deed + Survey + Tax)
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-404-deed', 'p-404', NULL, 'REGISTRATION_DEED', 'DEED-2022-404', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_404.pdf', 'land-evidence/P-404/deed.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-404-survey', 'p-404', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2024-404', 'SURVEY_AREA_SQM', '10000.0', 'v1.0', CURRENT_TIMESTAMP, 'survey_404.pdf', 'land-evidence/P-404/survey.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP),
('ev-404-tax', 'p-404', NULL, 'REVENUE_RECORD', 'TAX-2024-404', 'TAX_CLEARANCE', 'PAID_CLEARED', 'v2024.1', CURRENT_TIMESTAMP, 'tax_404.pdf', 'land-evidence/P-404/tax.pdf', 'LOCAL', '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', 'u-alice', CURRENT_TIMESTAMP);

-- P-405 (Invalid Split Parent: Deed + Survey + Tax)
INSERT INTO evidence_records (id, parcel_id, request_id, source_id, source_record_id, attribute_name, attribute_value, version_label, observed_at, file_name, file_path, storage_provider, sha256_hash, uploaded_by, created_at) VALUES
('ev-405-deed', 'p-405', NULL, 'REGISTRATION_DEED', 'DEED-2022-405', 'OWNER_NAME', 'Alice Vance', 'v1.0', CURRENT_TIMESTAMP, 'alice_deed_405.pdf', 'land-evidence/P-405/deed.pdf', 'LOCAL', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'u-alice', CURRENT_TIMESTAMP),
('ev-405-survey', 'p-405', NULL, 'CADASTRAL_SURVEY', 'SURVEY-2024-405', 'SURVEY_AREA_SQM', '8000.0', 'v1.0', CURRENT_TIMESTAMP, 'survey_405.pdf', 'land-evidence/P-405/survey.pdf', 'LOCAL', 'd4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35', 'u-dan', CURRENT_TIMESTAMP),
('ev-405-tax', 'p-405', NULL, 'REVENUE_RECORD', 'TAX-2024-405', 'TAX_CLEARANCE', 'PAID_CLEARED', 'v2024.1', CURRENT_TIMESTAMP, 'tax_405.pdf', 'land-evidence/P-405/tax.pdf', 'LOCAL', '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', 'u-alice', CURRENT_TIMESTAMP);
