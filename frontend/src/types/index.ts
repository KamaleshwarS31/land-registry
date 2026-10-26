export type UserRole = 'LANDOWNER' | 'BUYER' | 'REGISTRAR' | 'SURVEY_REVIEWER' | 'ADMIN';

export interface User {
  id: string;
  login: string;
  displayName: string;
  role: UserRole;
  walletAddress?: string;
  enabled?: boolean;
}

export interface Parcel {
  id: string;
  parcelCode: string;
  surveyNumber: string;
  locality: string;
  areaSqm: number;
  geometryWkt: string;
  status: 'PENDING' | 'ACTIVE' | 'INACTIVE_SPLIT' | 'INACTIVE_MERGED' | 'DISPUTED';
  owner: User;
  version: number;
  blockchainTxHash?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EvidenceRecord {
  id: string;
  parcelId?: string;
  requestId?: string;
  sourceId: string;
  sourceRecordId: string;
  attributeName: string;
  attributeValue: string;
  versionLabel: string;
  observedAt: string;
  fileName: string;
  filePath: string;
  storageProvider: string;
  sha256Hash: string;
  uploadedBy?: User;
  createdAt: string;
}

export interface PredicateResult {
  id: string;
  predicateCode: string;
  status: 'SATISFIED' | 'VIOLATED' | 'UNRESOLVED';
  explanation: string;
  evidenceRefs?: string;
  evaluatedAt: string;
}

export interface DecisionCertificate {
  id: string;
  mutationId: string;
  decision: 'AUTHORIZE' | 'HOLD' | 'REJECT';
  canonicalPayload: string;
  certificateHash: string;
  createdAt: string;
}

export interface MutationRequest {
  id: string;
  mutationType: 'REGISTRATION' | 'TRANSFER' | 'SPLIT' | 'MERGE';
  targetParcelIds: string;
  expectedVersions: string;
  proposedPayload: string;
  requestedBy: User;
  assignedReviewer?: User;
  status: 'SUBMITTED' | 'EVALUATING' | 'HELD' | 'REJECTED' | 'READY_FOR_COMMIT' | 'COMMITTED' | 'COMMIT_FAILED';
  decisionReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConflictWitness {
  ruleCode: string;
  sourceIds: string[];
  conflictingAssertionIds: string[];
  explanation: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
}
