-- V1: Initial Land Registry Schema (PostgreSQL & H2 compatible)

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    wallet_address VARCHAR(42),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE parcels (
    id VARCHAR(36) PRIMARY KEY,
    parcel_code VARCHAR(50) NOT NULL UNIQUE,
    survey_number VARCHAR(50) NOT NULL,
    locality VARCHAR(100) NOT NULL,
    area_sqm NUMERIC(15, 4) NOT NULL,
    geometry_wkt TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    owner_user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    version INT NOT NULL DEFAULT 1,
    blockchain_tx_hash VARCHAR(66),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_parcels_owner ON parcels(owner_user_id);
CREATE INDEX idx_parcels_status ON parcels(status);

CREATE TABLE evidence_records (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) REFERENCES parcels(id),
    request_id VARCHAR(36),
    source_id VARCHAR(50) NOT NULL,
    source_record_id VARCHAR(100) NOT NULL,
    attribute_name VARCHAR(50) NOT NULL,
    attribute_value TEXT NOT NULL,
    version_label VARCHAR(30) NOT NULL,
    observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    storage_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    sha256_hash VARCHAR(64) NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_evidence_parcel ON evidence_records(parcel_id);
CREATE INDEX idx_evidence_request ON evidence_records(request_id);

CREATE TABLE mutation_requests (
    id VARCHAR(36) PRIMARY KEY,
    mutation_type VARCHAR(30) NOT NULL,
    target_parcel_ids TEXT NOT NULL,
    expected_versions TEXT NOT NULL,
    proposed_payload TEXT NOT NULL,
    requested_by VARCHAR(36) NOT NULL REFERENCES users(id),
    assigned_reviewer VARCHAR(36) REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    decision_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mutations_status ON mutation_requests(status);
CREATE INDEX idx_mutations_requester ON mutation_requests(requested_by);

CREATE TABLE parcel_lineage (
    id VARCHAR(36) PRIMARY KEY,
    parent_parcel_id VARCHAR(36) NOT NULL REFERENCES parcels(id),
    child_parcel_id VARCHAR(36) NOT NULL REFERENCES parcels(id),
    mutation_id VARCHAR(36) NOT NULL REFERENCES mutation_requests(id),
    relation_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lineage_parent ON parcel_lineage(parent_parcel_id);
CREATE INDEX idx_lineage_child ON parcel_lineage(child_parcel_id);

CREATE TABLE predicate_results (
    id VARCHAR(36) PRIMARY KEY,
    mutation_id VARCHAR(36) NOT NULL REFERENCES mutation_requests(id),
    predicate_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    explanation TEXT NOT NULL,
    evidence_refs TEXT,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_predicates_mutation ON predicate_results(mutation_id);

CREATE TABLE decision_certificates (
    id VARCHAR(36) PRIMARY KEY,
    mutation_id VARCHAR(36) NOT NULL UNIQUE REFERENCES mutation_requests(id),
    decision VARCHAR(20) NOT NULL,
    canonical_payload TEXT NOT NULL,
    certificate_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE blockchain_transactions (
    id VARCHAR(36) PRIMARY KEY,
    mutation_id VARCHAR(36) NOT NULL REFERENCES mutation_requests(id),
    tx_hash VARCHAR(66) NOT NULL UNIQUE,
    chain_id BIGINT NOT NULL,
    contract_address VARCHAR(42) NOT NULL,
    block_number BIGINT,
    receipt_status INT,
    gas_used BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_events (
    id VARCHAR(36) PRIMARY KEY,
    actor_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(36) NOT NULL,
    details_json TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_entity ON audit_events(entity_type, entity_id);
