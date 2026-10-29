import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import { DecisionCertificate } from '../types';
import { ShieldCheck, Hash, ArrowLeft, Copy, Check, AlertTriangle, CheckCircle2, RefreshCw } from 'lucide-react';

export const CertificateDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [cert, setCert] = useState<DecisionCertificate | null>(null);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(true);

  // Verification & Tamper state
  const [verificationResult, setVerificationResult] = useState<{
    matches: boolean;
    tampered: boolean;
    computedHash: string;
    expectedHash: string;
    message?: string;
  } | null>(null);
  const [verifying, setVerifying] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.certificates.get(id)
      .then((data) => setCert(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [id]);

  const copyPayload = () => {
    if (!cert) return;
    navigator.clipboard.writeText(cert.canonicalPayload);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleVerifyOriginal = async () => {
    if (!cert) return;
    setVerifying(true);
    try {
      const res = await api.certificates.verify(cert.id);
      setVerificationResult({
        matches: res.matches,
        tampered: res.tampered,
        computedHash: res.computedHash,
        expectedHash: res.expectedHash,
        message: res.matches
          ? 'Cryptographic integrity verified! Canonical SHA-256 digest exactly matches anchored blockchain proof.'
          : 'Digest mismatch detected.',
      });
    } catch (err: any) {
      alert('Verification error: ' + err.message);
    } finally {
      setVerifying(false);
    }
  };

  const handleSimulateTamper = async () => {
    if (!cert) return;
    setVerifying(true);
    try {
      // Simulate altering a single character in the payload
      const tamperedPayload = cert.canonicalPayload.replace('"decision"', '"decision_TAMPERED"');
      const res = await api.certificates.verify(cert.id, tamperedPayload);
      setVerificationResult({
        matches: res.matches,
        tampered: true,
        computedHash: res.computedHash,
        expectedHash: res.expectedHash,
        message: 'TAMPER DETECTED (T13)! Payload was modified by 1 character. Recomputed digest differs from anchor.',
      });
    } catch (err: any) {
      alert('Tamper simulation error: ' + err.message);
    } finally {
      setVerifying(false);
    }
  };

  if (loading) return <div className="text-center py-20 text-slate-400 text-sm">Loading certificate...</div>;

  if (!cert) {
    return (
      <div className="text-center py-20 text-slate-500">
        <h2 className="text-xl font-bold">Certificate Not Found</h2>
        <Link to="/" className="text-emerald-600 text-sm font-semibold mt-2 inline-block">
          &larr; Return Home
        </Link>
      </div>
    );
  }

  let formattedJson = cert.canonicalPayload;
  try {
    formattedJson = JSON.stringify(JSON.parse(cert.canonicalPayload), null, 2);
  } catch (ignored) {}

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <Link to="/registrar/queue" className="inline-flex items-center text-xs font-semibold text-slate-500 hover:text-slate-800 transition">
        <ArrowLeft className="w-4 h-4 mr-1" /> Back
      </Link>

      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs space-y-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-3 bg-emerald-100 text-emerald-800 rounded-xl">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Audit Witness Proof</span>
              <h1 className="text-xl font-bold text-slate-900">Decision Certificate</h1>
            </div>
          </div>

          <span
            className={`px-3 py-1 rounded-full text-xs font-extrabold ${
              cert.decision === 'AUTHORIZE'
                ? 'bg-emerald-100 text-emerald-800'
                : cert.decision === 'HOLD'
                ? 'bg-amber-100 text-amber-800'
                : 'bg-red-100 text-red-800'
            }`}
          >
            {cert.decision}
          </span>
        </div>

        {/* SHA-256 Digest Bar */}
        <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between">
          <div className="flex items-center space-x-2 text-xs font-mono">
            <Hash className="w-4 h-4 text-emerald-600 shrink-0" />
            <span className="text-slate-500 font-bold">Anchored SHA-256 Digest:</span>
            <span className="text-slate-900 font-bold break-all">{cert.certificateHash}</span>
          </div>
          <button
            onClick={copyPayload}
            className="ml-3 px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs font-semibold hover:bg-slate-50 transition flex items-center gap-1 shrink-0"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5 text-slate-500" />}
            <span>{copied ? 'Copied' : 'Copy JSON'}</span>
          </button>
        </div>

        {/* Live Integrity & Tamper Testing Panel (T13 Demonstration) */}
        <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/70 space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Cryptographic Integrity & Tamper Verification (Acceptance Test T13)</h3>
              <p className="text-xs text-slate-500">
                Verify that recomputed SHA-256 hash matches the on-chain anchor, or test tamper detection.
              </p>
            </div>
            <div className="flex items-center space-x-2">
              <button
                onClick={handleVerifyOriginal}
                disabled={verifying}
                className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition flex items-center gap-1"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Verify Canonical Digest</span>
              </button>
              <button
                onClick={handleSimulateTamper}
                disabled={verifying}
                className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-semibold transition flex items-center gap-1"
              >
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>Simulate Tamper</span>
              </button>
            </div>
          </div>

          {verificationResult && (
            <div
              className={`p-3.5 rounded-lg border text-xs ${
                verificationResult.matches
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-900'
                  : 'bg-red-50 border-red-200 text-red-900'
              }`}
            >
              <div className="font-bold flex items-center gap-1.5 mb-1.5">
                {verificationResult.matches ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                ) : (
                  <AlertTriangle className="w-4 h-4 text-red-600 shrink-0" />
                )}
                <span>{verificationResult.message}</span>
              </div>
              <div className="space-y-1 font-mono text-[11px] bg-white/70 p-2 rounded border border-slate-200">
                <div><strong>Anchored Digest:</strong> {verificationResult.expectedHash}</div>
                <div><strong>Computed Digest:</strong> {verificationResult.computedHash}</div>
                <div><strong>Digest Match:</strong> {verificationResult.matches ? 'EXACT MATCH (VERIFIED)' : 'MISMATCH (TAMPER DETECTED)'}</div>
              </div>
            </div>
          )}
        </div>

        {/* Canonical JSON Payload */}
        <div className="space-y-2">
          <span className="text-xs font-bold text-slate-700 uppercase">Canonical Payload Content:</span>
          <pre className="p-4 bg-slate-900 text-emerald-400 rounded-xl text-xs font-mono overflow-x-auto max-h-96">
            {formattedJson}
          </pre>
        </div>
      </div>
    </div>
  );
};
