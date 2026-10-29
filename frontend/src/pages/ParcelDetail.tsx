import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import { Parcel, EvidenceRecord } from '../types';
import { WktPolygonViewer } from '../components/WktPolygonViewer';
import {
  MapPin,
  FileText,
  GitBranch,
  ShieldCheck,
  Download,
  ExternalLink,
  ArrowLeft,
  Hash,
  Clock,
  Layers,
} from 'lucide-react';

export const ParcelDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [parcel, setParcel] = useState<Parcel | null>(null);
  const [evidence, setEvidence] = useState<EvidenceRecord[]>([]);
  const [lineage, setLineage] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'boundary' | 'evidence' | 'lineage' | 'blockchain'>('boundary');
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setErrorMsg(null);

    // Primary parcel fetch
    api.parcels.get(id)
      .then((pData) => {
        setParcel(pData);
        // Non-blocking parallel fetch of secondary metadata
        api.evidence.getForParcel(id)
          .then((eData) => setEvidence(Array.isArray(eData) ? eData : []))
          .catch((err) => {
            console.warn('Could not load evidence for parcel:', err);
            setEvidence([]);
          });

        api.parcels.getLineage(id)
          .then((lData) => setLineage(lData || { parents: [], children: [] }))
          .catch((err) => {
            console.warn('Could not load lineage for parcel:', err);
            setLineage({ parents: [], children: [] });
          });
      })
      .catch((err) => {
        console.error('Failed to load parcel:', err);
        setErrorMsg(err.message || 'Parcel not found');
        setParcel(null);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return <div className="text-center py-20 text-slate-400 text-sm">Loading parcel details...</div>;
  }

  if (!parcel) {
    return (
      <div className="text-center py-20 text-slate-500">
        <h2 className="text-xl font-bold">Parcel Not Found</h2>
        {errorMsg && <p className="text-xs text-rose-500 mt-1">{errorMsg}</p>}
        <Link to="/parcels" className="text-emerald-600 text-sm font-semibold mt-3 inline-block">
          &larr; Back to Directory
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Breadcrumb & Actions */}
      <div className="flex items-center justify-between">
        <Link to="/parcels" className="inline-flex items-center text-xs font-semibold text-slate-500 hover:text-slate-800 transition">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Parcels
        </Link>
        <div className="flex items-center space-x-2">
          <Link
            to="/mutations/new"
            className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs px-3.5 py-2 rounded-lg shadow-sm transition"
          >
            Initiate Mutation
          </Link>
        </div>
      </div>

      {/* Parcel Header Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center space-x-2 mb-2">
            <span className="text-xs font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800">
              {parcel.status}
            </span>
            <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 font-mono">
              Version {parcel.version}
            </span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">{parcel.parcelCode}</h1>
          <div className="flex items-center space-x-4 text-xs text-slate-500 mt-2">
            <span className="flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-slate-400" /> {parcel.locality}
            </span>
            <span>Survey No: <strong>{parcel.surveyNumber}</strong></span>
            <span>Area: <strong className="font-mono">{parcel.areaSqm} sqm</strong></span>
          </div>
        </div>

        <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs space-y-1.5 md:min-w-[280px]">
          <div className="text-slate-400 text-[10px] uppercase font-bold tracking-wider">Registered Owner</div>
          <div className="font-bold text-slate-900 text-sm">{parcel.owner?.displayName}</div>
          <div className="text-slate-500 font-mono text-[11px] truncate">
            Wallet: {parcel.owner?.walletAddress || 'None'}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-slate-200 flex space-x-8 text-sm">
        <button
          onClick={() => setActiveTab('boundary')}
          className={`pb-3 font-semibold transition border-b-2 flex items-center gap-1.5 ${
            activeTab === 'boundary'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Layers className="w-4 h-4" /> Cadastral Geometry
        </button>
        <button
          onClick={() => setActiveTab('evidence')}
          className={`pb-3 font-semibold transition border-b-2 flex items-center gap-1.5 ${
            activeTab === 'evidence'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <FileText className="w-4 h-4" /> Evidence Documents ({evidence.length})
        </button>
        <button
          onClick={() => setActiveTab('lineage')}
          className={`pb-3 font-semibold transition border-b-2 flex items-center gap-1.5 ${
            activeTab === 'lineage'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <GitBranch className="w-4 h-4" /> Ancestry & Lineage
        </button>
        <button
          onClick={() => setActiveTab('blockchain')}
          className={`pb-3 font-semibold transition border-b-2 flex items-center gap-1.5 ${
            activeTab === 'blockchain'
              ? 'border-emerald-600 text-emerald-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <ShieldCheck className="w-4 h-4" /> Blockchain Proof
        </button>
      </div>

      {/* Tab 1: Cadastral Boundary */}
      {activeTab === 'boundary' && (
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-sm text-slate-900">Geospatial Polygon Representation</h3>
            <span className="text-xs text-slate-500 font-mono">EPSG:4326 WKT Format</span>
          </div>

          <WktPolygonViewer wkt={parcel.geometryWkt} className="w-full h-80" />

          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs font-mono text-slate-700 overflow-x-auto">
            <span className="text-slate-400 block text-[10px] uppercase font-bold mb-1">Raw WKT Geometry</span>
            {parcel.geometryWkt}
          </div>
        </div>
      )}

      {/* Tab 2: Evidence Records with Supabase Storage */}
      {activeTab === 'evidence' && (
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-sm text-slate-900">Off-Chain Evidence Records</h3>
              <p className="text-xs text-slate-500">
                Documents stored off-chain in <strong>Supabase Storage</strong> and anchored by SHA-256 digests
              </p>
            </div>
          </div>

          {evidence.length === 0 ? (
            <div className="text-center py-10 text-slate-400 text-xs">No evidence records uploaded yet.</div>
          ) : (
            <div className="divide-y divide-slate-100">
              {evidence.map((ev) => (
                <div key={ev.id} className="py-3.5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-slate-900">{ev.sourceId}</span>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-blue-50 text-blue-700 border border-blue-200">
                        {ev.storageProvider}
                      </span>
                      <span className="text-xs text-slate-500 font-mono">({ev.sourceRecordId})</span>
                    </div>
                    <div className="text-xs text-slate-700">
                      <strong>{ev.attributeName}</strong>: {ev.attributeValue}
                    </div>
                    <div className="text-[11px] text-slate-400 font-mono flex items-center gap-1">
                      <Hash className="w-3 h-3" /> SHA-256: {ev.sha256Hash}
                    </div>
                  </div>

                  <a
                    href={`/api/evidence/${ev.id}/url`}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
                  >
                    <Download className="w-3.5 h-3.5 text-slate-400" />
                    <span>View / Download</span>
                  </a>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Ancestry & Lineage Tree */}
      {activeTab === 'lineage' && (
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
          <h3 className="font-bold text-sm text-slate-900">Parcel Lineage Graph (DAG)</h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/60">
              <span className="text-xs font-bold text-slate-600 block mb-2 uppercase tracking-wider">
                Parent Parcels (Ancestors)
              </span>
              {lineage?.parents?.length === 0 ? (
                <div className="text-xs text-slate-400 italic">Original parcel (no parent recorded)</div>
              ) : (
                <div className="space-y-2">
                  {lineage?.parents?.map((p: any, idx: number) => (
                    <div key={idx} className="bg-white p-2.5 rounded-lg border border-slate-200 text-xs font-medium">
                      Code: <Link to={`/parcels/${p.parcelId}`} className="text-emerald-600 font-bold hover:underline">{p.parcelCode}</Link>
                      <span className="ml-2 text-slate-400">({p.relationType})</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="p-4 rounded-xl border border-slate-200 bg-slate-50/60">
              <span className="text-xs font-bold text-slate-600 block mb-2 uppercase tracking-wider">
                Child Parcels (Derived Splits / Merges)
              </span>
              {lineage?.children?.length === 0 ? (
                <div className="text-xs text-slate-400 italic">No child splits or merges derived from this parcel.</div>
              ) : (
                <div className="space-y-2">
                  {lineage?.children?.map((c: any, idx: number) => (
                    <div key={idx} className="bg-white p-2.5 rounded-lg border border-slate-200 text-xs font-medium">
                      Code: <Link to={`/parcels/${c.parcelId}`} className="text-emerald-600 font-bold hover:underline">{c.parcelCode}</Link>
                      <span className="ml-2 text-slate-400">({c.relationType})</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 4: Blockchain Proof */}
      {activeTab === 'blockchain' && (
        <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-sm text-slate-900">EVM Smart Contract Attestation</h3>
            {parcel.blockchainTxHash?.startsWith('0x') && parcel.blockchainTxHash?.length > 40 && !parcel.blockchainTxHash?.includes('00000000000000000000000000000000') ? (
              <span className="px-2.5 py-1 rounded-full text-[10px] font-extrabold bg-emerald-100 text-emerald-800 border border-emerald-300 flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse"></span>
                LIVE ON-CHAIN
              </span>
            ) : (
              <span className="px-2.5 py-1 rounded-full text-[10px] font-extrabold bg-amber-100 text-amber-800 border border-amber-300">
                SIMULATED / SEED PROOF
              </span>
            )}
          </div>
          <div className="p-4 rounded-xl border border-emerald-200 bg-emerald-50/60 text-xs space-y-2 font-mono">
            <div>
              <span className="text-slate-500">Contract Address:</span>{' '}
              <strong className="text-slate-900">0x5FbDB2315678afecb367f032d93F642f64180aa3</strong>
            </div>
            <div>
              <span className="text-slate-500">Transaction Hash:</span>{' '}
              <strong className="text-slate-900 break-all">{parcel.blockchainTxHash || 'Genesis / Seed Transaction'}</strong>
            </div>
            <div>
              <span className="text-slate-500">Committed State Version:</span>{' '}
              <strong className="text-emerald-800">Version {parcel.version}</strong>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
