import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import { MutationRequest, PredicateResult, DecisionCertificate, ConflictWitness } from '../types';
import {
  Play,
  CheckCircle2,
  XCircle,
  AlertCircle,
  ShieldCheck,
  Hash,
  ExternalLink,
  Layers,
  ArrowRight,
  RefreshCw,
  GitCommit,
} from 'lucide-react';

export const MutationWorkbench: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [mutation, setMutation] = useState<MutationRequest | null>(null);
  const [predicates, setPredicates] = useState<PredicateResult[]>([]);
  const [certificate, setCertificate] = useState<DecisionCertificate | null>(null);
  const [conflicts, setConflicts] = useState<ConflictWitness[]>([]);

  const [evaluating, setEvaluating] = useState(false);
  const [committing, setCommitting] = useState(false);
  const [commitResult, setCommitResult] = useState<any>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchMutation = () => {
    if (!id) return;
    setLoading(true);
    api.mutations.get(id)
      .then((data) => {
        setMutation(data.mutation);
        setPredicates(data.predicates || []);
        setCertificate(data.certificate);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchMutation();
  }, [id]);

  const handleEvaluate = async () => {
    if (!id) return;
    setError('');
    setEvaluating(true);
    try {
      const res = await api.mutations.evaluate(id);
      setConflicts(res.conflictWitnesses || []);
      fetchMutation();
    } catch (err: any) {
      setError(err.message || 'Evaluation failed');
    } finally {
      setEvaluating(false);
    }
  };

  const handleCommit = async () => {
    if (!id) return;
    setError('');
    setCommitting(true);
    try {
      const res = await api.mutations.commit(id);
      setCommitResult(res);
      fetchMutation();
    } catch (err: any) {
      setError(err.message || 'On-chain commit failed');
    } finally {
      setCommitting(false);
    }
  };

  if (loading) {
    return <div className="text-center py-20 text-slate-400 text-sm">Loading workbench...</div>;
  }

  if (!mutation) {
    return (
      <div className="text-center py-20 text-slate-500">
        <h2 className="text-xl font-bold">Mutation Not Found</h2>
        <Link to="/registrar/queue" className="text-purple-600 text-sm font-semibold mt-2 inline-block">
          &larr; Back to Review Queue
        </Link>
      </div>
    );
  }

  const getPredicateBadge = (status: string) => {
    switch (status) {
      case 'SATISFIED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-bold bg-emerald-100 text-emerald-800">
            <CheckCircle2 className="w-3.5 h-3.5" /> SATISFIED
          </span>
        );
      case 'VIOLATED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-bold bg-red-100 text-red-800">
            <XCircle className="w-3.5 h-3.5" /> VIOLATED
          </span>
        );
      case 'UNRESOLVED':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-bold bg-amber-100 text-amber-800">
            <AlertCircle className="w-3.5 h-3.5" /> UNRESOLVED (HOLD)
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 mb-2">
            <span className="text-xs font-bold px-2 py-0.5 rounded bg-purple-100 text-purple-800">
              {mutation.mutationType}
            </span>
            <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 font-mono">
              Status: {mutation.status}
            </span>
          </div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Mutation Verification Workbench
          </h1>
          <p className="text-xs text-slate-500 font-mono mt-1">
            Application ID: {mutation.id} • Requester: {mutation.requestedBy?.displayName}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center space-x-2">
          <button
            onClick={handleEvaluate}
            disabled={evaluating || mutation.status === 'COMMITTED'}
            className="flex items-center space-x-1.5 px-4 py-2.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm transition disabled:opacity-50"
          >
            <Play className={`w-4 h-4 ${evaluating ? 'animate-spin' : ''}`} />
            <span>{evaluating ? 'Evaluating M5•M8•M2...' : 'Run Verification Engine'}</span>
          </button>

          {mutation.status === 'READY_FOR_COMMIT' && (
            <button
              onClick={handleCommit}
              disabled={committing}
              className="flex items-center space-x-1.5 px-4 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm transition disabled:opacity-50 animate-pulse"
            >
              <GitCommit className="w-4 h-4" />
              <span>{committing ? 'Mining on Blockchain...' : 'Commit to Blockchain'}</span>
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Success Banner if Committed */}
      {mutation.status === 'COMMITTED' && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-900 text-xs space-y-1.5">
          <div className="flex items-center gap-2 font-bold text-sm text-emerald-800">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            Mutation Approved and Committed to Blockchain
          </div>
          <div className="font-mono text-slate-600">
            State updated in PostgreSQL projection and anchored with cryptographic Decision Certificate.
          </div>
        </div>
      )}

      {/* Warning Banner if HELD */}
      {mutation.status === 'HELD' && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl text-amber-900 text-xs space-y-1.5">
          <div className="flex items-center gap-2 font-bold text-sm text-amber-800">
            <AlertCircle className="w-5 h-5 text-amber-600" />
            Settlement Blocked: Application on HOLD (Unresolved Evidence)
          </div>
          <div className="text-amber-800">
            {mutation.decisionReason || 'Missing or stale evidence prevents clearance. Under the M8 three-valued logic, absence of evidence is not proof of clearance. On-chain commitment is disabled.'}
          </div>
        </div>
      )}

      {/* Error Banner if REJECTED */}
      {mutation.status === 'REJECTED' && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-900 text-xs space-y-1.5">
          <div className="flex items-center gap-2 font-bold text-sm text-red-800">
            <XCircle className="w-5 h-5 text-red-600" />
            Settlement Blocked: Application REJECTED (Hard Invariant Violation)
          </div>
          <div className="text-red-800">
            {mutation.decisionReason || 'One or more mandatory predicates were violated (ownership contradiction, area discrepancy > 1.5%, or topological overlap).'}
          </div>
        </div>
      )}

      {/* SECTION 1: M5 Contradiction Reconciliation */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <span className="w-6 h-6 rounded-md bg-purple-100 text-purple-700 font-extrabold flex items-center justify-center text-xs">
              M5
            </span>
            Cross-Registry Contradiction Reconciliation
          </h2>
          <span className="text-xs text-slate-500 font-medium">
            {conflicts.length === 0 ? 'No conflicts detected' : `${conflicts.length} Conflict Witness(es)`}
          </span>
        </div>

        {conflicts.length === 0 ? (
          <div className="p-3 rounded-lg bg-slate-50 text-slate-500 text-xs italic">
            Zero contradiction witnesses produced. Registry deed, cadastral survey, and revenue assertions are consistent.
          </div>
        ) : (
          <div className="space-y-2">
            {conflicts.map((w, idx) => (
              <div key={idx} className="p-3 rounded-lg border border-red-200 bg-red-50/60 text-xs space-y-1">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-red-900 font-mono">{w.ruleCode}</span>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-red-200 text-red-800">
                    {w.severity} SEVERITY
                  </span>
                </div>
                <div className="text-slate-800">{w.explanation}</div>
                <div className="text-[10px] text-slate-500 font-mono">
                  Sources: {w.sourceIds?.join(', ')}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* SECTION 2: M8 Uncertainty-Aware Evaluation */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <span className="w-6 h-6 rounded-md bg-amber-100 text-amber-700 font-extrabold flex items-center justify-center text-xs">
              M8
            </span>
            Three-Valued Uncertainty Predicate Matrix
          </h2>
          {certificate && (
            <span
              className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                certificate.decision === 'AUTHORIZE'
                  ? 'bg-emerald-100 text-emerald-800'
                  : certificate.decision === 'HOLD'
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-red-100 text-red-800'
              }`}
            >
              DECISION: {certificate.decision}
            </span>
          )}
        </div>

        {predicates.length === 0 ? (
          <div className="text-center py-6 text-slate-400 text-xs italic">
            Click "Run Verification Engine" to evaluate predicates against live records.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase font-semibold">
                <tr>
                  <th className="py-2.5 px-3">Predicate</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Explanation & Witness Proof</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {predicates.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-3 font-mono font-bold text-slate-900">{p.predicateCode}</td>
                    <td className="py-3 px-3">{getPredicateBadge(p.status)}</td>
                    <td className="py-3 px-3 text-slate-700 font-medium">{p.explanation}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* SECTION 3: Decision Certificate */}
      {certificate && (
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-600" />
              Cryptographic Decision Certificate
            </h2>
            <Link
              to={`/certificates/${certificate.id}`}
              className="text-xs font-semibold text-emerald-600 hover:underline flex items-center gap-1"
            >
              <span>Full Certificate</span>
              <ExternalLink className="w-3 h-3" />
            </Link>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs font-mono space-y-1">
            <div className="text-slate-500 flex items-center gap-1">
              <Hash className="w-3.5 h-3.5" />
              <span>SHA-256 Digest:</span>
              <strong className="text-slate-900">{certificate.certificateHash}</strong>
            </div>
            <div className="text-slate-500">
              Canonical Payload: <span className="text-slate-700 truncate">{certificate.canonicalPayload.substring(0, 80)}...</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
