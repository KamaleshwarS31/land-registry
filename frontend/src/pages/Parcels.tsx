import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { Parcel } from '../types';
import { Search, Filter, MapPin, Eye, Plus } from 'lucide-react';
import { WktPolygonViewer } from '../components/WktPolygonViewer';

export const Parcels: React.FC = () => {
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchParcels = () => {
    setLoading(true);
    api.parcels.list(query, status, 0, 50)
      .then((data) => setParcels(data.content || []))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchParcels();
  }, [status]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchParcels();
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Cadastral Parcels Directory</h1>
          <p className="text-slate-500 text-sm mt-0.5">Explore active and historical cadastral land parcels</p>
        </div>
        <Link
          to="/parcels/new"
          className="inline-flex items-center space-x-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs px-4 py-2.5 rounded-lg shadow-sm transition"
        >
          <Plus className="w-4 h-4" />
          <span>Register New Parcel</span>
        </Link>
      </div>

      {/* Filter / Search Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200/80 shadow-xs flex flex-col md:flex-row items-center gap-3">
        <form onSubmit={handleSearch} className="flex-1 flex items-center relative w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search by Parcel Code, Locality, or Survey Number..."
            className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
          />
          <button
            type="submit"
            className="ml-2 px-4 py-2 bg-slate-900 text-white rounded-lg text-sm font-semibold hover:bg-slate-800 transition"
          >
            Search
          </button>
        </form>

        <div className="flex items-center space-x-2 w-full md:w-auto">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="py-2 px-3 border border-slate-300 rounded-lg text-sm bg-white text-slate-700 outline-none"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE_SPLIT">INACTIVE_SPLIT</option>
            <option value="INACTIVE_MERGED">INACTIVE_MERGED</option>
            <option value="DISPUTED">DISPUTED</option>
          </select>
        </div>
      </div>

      {/* Parcels Grid / Cards */}
      {loading ? (
        <div className="text-center py-12 text-slate-400 text-sm">Loading parcels...</div>
      ) : parcels.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-dashed border-slate-300 text-slate-500 text-sm">
          No parcels found matching criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {parcels.map((p) => (
            <div
              key={p.id}
              className="bg-white rounded-xl border border-slate-200/80 shadow-xs hover:shadow-md transition overflow-hidden flex flex-col"
            >
              {/* Polygon Visualizer Preview */}
              <div className="p-3 bg-slate-50 border-b border-slate-100">
                <WktPolygonViewer wkt={p.geometryWkt} className="w-full h-36" />
              </div>

              {/* Details */}
              <div className="p-4 flex-1 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800">
                      v{p.version}
                    </span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                        p.status === 'ACTIVE'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {p.status}
                    </span>
                  </div>
                  <h3 className="font-bold text-base text-slate-900 tracking-tight">{p.parcelCode}</h3>
                  <div className="text-xs text-slate-500 flex items-center gap-1 mt-1">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" />
                    <span>{p.locality}</span>
                  </div>

                  <div className="mt-3 grid grid-cols-2 gap-2 text-xs pt-3 border-t border-slate-100">
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase">Survey No</span>
                      <span className="font-semibold text-slate-700">{p.surveyNumber}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase">Area (Sqm)</span>
                      <span className="font-semibold text-slate-700 font-mono">{p.areaSqm} sqm</span>
                    </div>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <div className="text-[11px] text-slate-500">
                    Owner: <span className="font-semibold text-slate-700">{p.owner?.displayName || 'Registered Owner'}</span>
                  </div>
                  <Link
                    to={`/parcels/${p.id}`}
                    className="inline-flex items-center space-x-1 text-xs font-bold text-emerald-600 hover:text-emerald-700"
                  >
                    <span>Inspect</span>
                    <Eye className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
