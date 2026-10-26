import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { WktPolygonViewer } from '../components/WktPolygonViewer';
import { FilePlus, Upload, Shield, CheckCircle } from 'lucide-react';

export const NewParcel: React.FC = () => {
  const navigate = useNavigate();

  const [parcelCode, setParcelCode] = useState('');
  const [surveyNumber, setSurveyNumber] = useState('');
  const [locality, setLocality] = useState('');
  const [areaSqm, setAreaSqm] = useState('');
  const [geometryWkt, setGeometryWkt] = useState('POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))');

  const [evidenceFile, setEvidenceFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // 1. Submit Registration Mutation Request
      const mutation = await api.mutations.submit({
        mutationType: 'REGISTRATION',
        targetParcelIds: [],
        expectedVersions: {},
        proposedPayload: {
          parcelCode,
          surveyNumber,
          locality,
          areaSqm,
          geometryWkt,
        },
      });

      // 2. If evidence file is attached, upload it
      if (evidenceFile) {
        const formData = new FormData();
        formData.append('file', evidenceFile);
        formData.append('requestId', mutation.id);
        formData.append('sourceId', 'REGISTRATION_DEED');
        formData.append('sourceRecordId', 'DEED-' + Date.now());
        formData.append('attributeName', 'OWNER_NAME');
        formData.append('attributeValue', parcelCode);
        formData.append('versionLabel', 'v1.0');
        await api.evidence.upload(formData);
      }

      navigate(`/mutations/${mutation.id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to submit registration request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Submit New Parcel Registration</h1>
        <p className="text-slate-500 text-sm mt-0.5">
          Enter cadastral boundary information and upload deed evidence for registrar verification
        </p>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-slate-200/80 p-6 shadow-xs space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Parcel Code *</label>
            <input
              type="text"
              required
              value={parcelCode}
              onChange={(e) => setParcelCode(e.target.value)}
              placeholder="e.g. PARCEL-DL-101"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Survey Number *</label>
            <input
              type="text"
              required
              value={surveyNumber}
              onChange={(e) => setSurveyNumber(e.target.value)}
              placeholder="e.g. SY-2024-501"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Locality / District *</label>
            <input
              type="text"
              required
              value={locality}
              onChange={(e) => setLocality(e.target.value)}
              placeholder="e.g. Greenfield Sector 2"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Area (Square Meters) *</label>
            <input
              type="number"
              step="0.01"
              required
              value={areaSqm}
              onChange={(e) => setAreaSqm(e.target.value)}
              placeholder="10000.00"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-emerald-500 outline-none"
            />
          </div>
        </div>

        {/* Geometry WKT and Live Visualizer */}
        <div className="space-y-2 pt-2 border-t border-slate-100">
          <label className="block text-xs font-semibold text-slate-700 uppercase">
            Cadastral Boundary Geometry (WKT Polygon) *
          </label>
          <input
            type="text"
            required
            value={geometryWkt}
            onChange={(e) => setGeometryWkt(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs font-mono text-slate-800 focus:ring-2 focus:ring-emerald-500 outline-none"
          />

          <div className="mt-2">
            <span className="text-[10px] uppercase font-bold text-slate-400 block mb-1">Live Boundary Shape Preview:</span>
            <WktPolygonViewer wkt={geometryWkt} className="w-full h-40" />
          </div>
        </div>

        {/* Evidence Upload */}
        <div className="space-y-2 pt-2 border-t border-slate-100">
          <label className="block text-xs font-semibold text-slate-700 uppercase">
            Deed or Survey Evidence Document (PDF / Image)
          </label>
          <div className="border-2 border-dashed border-slate-300 rounded-lg p-6 text-center hover:bg-slate-50 transition">
            <Upload className="w-6 h-6 text-slate-400 mx-auto mb-2" />
            <input
              type="file"
              onChange={(e) => setEvidenceFile(e.target.files ? e.target.files[0] : null)}
              className="text-xs text-slate-500"
            />
            <p className="text-[11px] text-slate-400 mt-1">Uploaded securely to Supabase Storage</p>
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm rounded-lg shadow-sm transition"
        >
          {loading ? 'Submitting Application...' : 'Submit Parcel for Verification'}
        </button>
      </form>
    </div>
  );
};
