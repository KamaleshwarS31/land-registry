import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { Parcel } from '../types';
import { ArrowRightLeft, Scissors, Combine, AlertCircle } from 'lucide-react';

export const NewMutation: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'TRANSFER' | 'SPLIT' | 'MERGE'>('TRANSFER');
  const [myParcels, setMyParcels] = useState<Parcel[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Transfer state
  const [selectedParcelId, setSelectedParcelId] = useState('');
  const [buyerUserId, setBuyerUserId] = useState('u-bob');

  // Split state
  const [childA_Code, setChildA_Code] = useState('PARCEL-DL-001-A');
  const [childA_Area, setChildA_Area] = useState('6000.0');
  const [childA_Wkt, setChildA_Wkt] = useState('POLYGON ((0 0, 60 0, 60 100, 0 100, 0 0))');

  const [childB_Code, setChildB_Code] = useState('PARCEL-DL-001-B');
  const [childB_Area, setChildB_Area] = useState('4000.0');
  const [childB_Wkt, setChildB_Wkt] = useState('POLYGON ((60 0, 100 0, 100 100, 60 100, 60 0))');

  // Merge state
  const [mergeParcelA_Id, setMergeParcelA_Id] = useState('');
  const [mergeParcelB_Id, setMergeParcelB_Id] = useState('');
  const [mergedCode, setMergedCode] = useState('PARCEL-DL-MERGED');
  const [mergedArea, setMergedArea] = useState('18000.0');
  const [mergedWkt, setMergedWkt] = useState('POLYGON ((0 0, 180 0, 180 100, 0 100, 0 0))');

  useEffect(() => {
    api.parcels.myHoldings()
      .then((data) => {
        setMyParcels(data || []);
        if (data && data.length > 0) {
          setSelectedParcelId(data[0].id);
          setMergeParcelA_Id(data[0].id);
          if (data.length > 1) {
            setMergeParcelB_Id(data[1].id);
          }
        }
      })
      .catch((err) => console.error(err));
  }, []);

  const handleTransferSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const p = myParcels.find((x) => x.id === selectedParcelId);
    if (!p) {
      setError('Please select a valid parcel');
      return;
    }

    setLoading(true);
    try {
      const res = await api.mutations.submit({
        mutationType: 'TRANSFER',
        targetParcelIds: [p.id],
        expectedVersions: { [p.id]: p.version },
        proposedPayload: { buyerUserId },
      });
      navigate(`/mutations/${res.id}`);
    } catch (err: any) {
      setError(err.message || 'Transfer submission failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSplitSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const parent = myParcels.find((x) => x.id === selectedParcelId);
    if (!parent) {
      setError('Please select parent parcel');
      return;
    }

    setLoading(true);
    try {
      const res = await api.mutations.submit({
        mutationType: 'SPLIT',
        targetParcelIds: [parent.id],
        expectedVersions: { [parent.id]: parent.version },
        proposedPayload: {
          children: [
            { parcelCode: childA_Code, areaSqm: parseFloat(childA_Area), geometryWkt: childA_Wkt },
            { parcelCode: childB_Code, areaSqm: parseFloat(childB_Area), geometryWkt: childB_Wkt },
          ],
        },
      });
      navigate(`/mutations/${res.id}`);
    } catch (err: any) {
      setError(err.message || 'Split submission failed');
    } finally {
      setLoading(false);
    }
  };

  const handleMergeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const pA = myParcels.find((x) => x.id === mergeParcelA_Id);
    const pB = myParcels.find((x) => x.id === mergeParcelB_Id);
    if (!pA || !pB || pA.id === pB.id) {
      setError('Please select two distinct parent parcels to merge');
      return;
    }

    setLoading(true);
    try {
      const res = await api.mutations.submit({
        mutationType: 'MERGE',
        targetParcelIds: [pA.id, pB.id],
        expectedVersions: { [pA.id]: pA.version, [pB.id]: pB.version },
        proposedPayload: {
          mergedParcelCode: mergedCode,
          mergedAreaSqm: parseFloat(mergedArea),
          mergedGeometryWkt: mergedWkt,
        },
      });
      navigate(`/mutations/${res.id}`);
    } catch (err: any) {
      setError(err.message || 'Merge submission failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Initiate Land Mutation</h1>
        <p className="text-slate-500 text-sm mt-0.5">
          Submit ownership transfers, splits, or merges with automated topological checks
        </p>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Mutation Type Selector */}
      <div className="grid grid-cols-3 gap-3">
        <button
          type="button"
          onClick={() => setActiveTab('TRANSFER')}
          className={`p-4 rounded-xl border text-center transition flex flex-col items-center justify-center gap-1 ${
            activeTab === 'TRANSFER'
              ? 'border-emerald-600 bg-emerald-50/70 text-emerald-900 font-bold shadow-xs'
              : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
          }`}
        >
          <ArrowRightLeft className="w-5 h-5 text-emerald-600" />
          <span className="text-sm">Ownership Transfer</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('SPLIT')}
          className={`p-4 rounded-xl border text-center transition flex flex-col items-center justify-center gap-1 ${
            activeTab === 'SPLIT'
              ? 'border-emerald-600 bg-emerald-50/70 text-emerald-900 font-bold shadow-xs'
              : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
          }`}
        >
          <Scissors className="w-5 h-5 text-emerald-600" />
          <span className="text-sm">Parcel Split</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('MERGE')}
          className={`p-4 rounded-xl border text-center transition flex flex-col items-center justify-center gap-1 ${
            activeTab === 'MERGE'
              ? 'border-emerald-600 bg-emerald-50/70 text-emerald-900 font-bold shadow-xs'
              : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
          }`}
        >
          <Combine className="w-5 h-5 text-emerald-600" />
          <span className="text-sm">Parcel Merge</span>
        </button>
      </div>

      {/* Transfer Form */}
      {activeTab === 'TRANSFER' && (
        <form onSubmit={handleTransferSubmit} className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-xs space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Select Parcel to Transfer *
            </label>
            <select
              value={selectedParcelId}
              onChange={(e) => setSelectedParcelId(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white outline-none"
            >
              {myParcels.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.parcelCode} - {p.locality} ({p.areaSqm} sqm, Version {p.version})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Designated Buyer *
            </label>
            <select
              value={buyerUserId}
              onChange={(e) => setBuyerUserId(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white outline-none"
            >
              <option value="u-bob">Bob Smith (Verified Buyer - 0x3C44...)</option>
              <option value="u-alice">Alice Vance (0x7099...)</option>
              <option value="u-clara">Clara Oswald (0xf39F...)</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg shadow-sm transition"
          >
            {loading ? 'Submitting Transfer...' : 'Submit Transfer for M5/M8/M2 Verification'}
          </button>
        </form>
      )}

      {/* Split Form */}
      {activeTab === 'SPLIT' && (
        <form onSubmit={handleSplitSubmit} className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-xs space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Select Parent Parcel to Split *
            </label>
            <select
              value={selectedParcelId}
              onChange={(e) => setSelectedParcelId(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white outline-none"
            >
              {myParcels.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.parcelCode} ({p.areaSqm} sqm)
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2 border-t border-slate-100">
            {/* Child A */}
            <div className="p-3 rounded-lg border border-slate-200 bg-slate-50/60 space-y-2">
              <span className="text-xs font-bold text-slate-800 uppercase block">Child Parcel A</span>
              <input
                type="text"
                placeholder="Code"
                value={childA_Code}
                onChange={(e) => setChildA_Code(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-xs bg-white"
              />
              <input
                type="number"
                placeholder="Area (Sqm)"
                value={childA_Area}
                onChange={(e) => setChildA_Area(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-xs bg-white font-mono"
              />
              <textarea
                placeholder="Geometry WKT"
                rows={2}
                value={childA_Wkt}
                onChange={(e) => setChildA_Wkt(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-[11px] font-mono bg-white"
              />
            </div>

            {/* Child B */}
            <div className="p-3 rounded-lg border border-slate-200 bg-slate-50/60 space-y-2">
              <span className="text-xs font-bold text-slate-800 uppercase block">Child Parcel B</span>
              <input
                type="text"
                placeholder="Code"
                value={childB_Code}
                onChange={(e) => setChildB_Code(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-xs bg-white"
              />
              <input
                type="number"
                placeholder="Area (Sqm)"
                value={childB_Area}
                onChange={(e) => setChildB_Area(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-xs bg-white font-mono"
              />
              <textarea
                placeholder="Geometry WKT"
                rows={2}
                value={childB_Wkt}
                onChange={(e) => setChildB_Wkt(e.target.value)}
                className="w-full px-2.5 py-1.5 border border-slate-300 rounded text-[11px] font-mono bg-white"
              />
            </div>
          </div>

          <div className="p-2.5 bg-blue-50 text-blue-800 text-xs rounded border border-blue-200">
            Lineage check will automatically verify area conservation (sum of child areas equals parent area) and disjoint boundaries via JTS Topology Suite.
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg shadow-sm transition"
          >
            {loading ? 'Submitting Split...' : 'Submit Split for Verification'}
          </button>
        </form>
      )}

      {/* Merge Form */}
      {activeTab === 'MERGE' && (
        <form onSubmit={handleMergeSubmit} className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-xs space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Parent Parcel 1 *</label>
              <select
                value={mergeParcelA_Id}
                onChange={(e) => setMergeParcelA_Id(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white outline-none"
              >
                {myParcels.map((p) => (
                  <option key={p.id} value={p.id}>{p.parcelCode} ({p.areaSqm} sqm)</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Parent Parcel 2 *</label>
              <select
                value={mergeParcelB_Id}
                onChange={(e) => setMergeParcelB_Id(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white outline-none"
              >
                {myParcels.map((p) => (
                  <option key={p.id} value={p.id}>{p.parcelCode} ({p.areaSqm} sqm)</option>
                ))}
              </select>
            </div>
          </div>

          <div className="space-y-2 pt-2 border-t border-slate-100">
            <label className="block text-xs font-semibold text-slate-700 uppercase">Merged Child Parcel Code *</label>
            <input
              type="text"
              value={mergedCode}
              onChange={(e) => setMergedCode(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg shadow-sm transition"
          >
            {loading ? 'Submitting Merge...' : 'Submit Merge for Verification'}
          </button>
        </form>
      )}
    </div>
  );
};
