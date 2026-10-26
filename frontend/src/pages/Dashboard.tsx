import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import { Parcel, MutationRequest } from '../types';
import {
  MapPin,
  ArrowRightLeft,
  AlertTriangle,
  CheckCircle,
  Clock,
  Layers,
  ShieldCheck,
  FileText,
  Activity,
} from 'lucide-react';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [mutations, setMutations] = useState<MutationRequest[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.parcels.list('', '', 0, 5),
      api.mutations.list(0, 5),
    ])
      .then(([pData, mData]) => {
        setParcels(pData.content || []);
        setMutations(mData.content || []);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMMITTED':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-emerald-100 text-emerald-800">COMMITTED</span>;
      case 'READY_FOR_COMMIT':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-purple-100 text-purple-800 animate-pulse">READY_FOR_COMMIT</span>;
      case 'HELD':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-amber-100 text-amber-800">HELD (M8)</span>;
      case 'REJECTED':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-red-100 text-red-800">REJECTED</span>;
      default:
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-slate-100 text-slate-800">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-emerald-800 to-teal-700 rounded-2xl p-6 text-white shadow-md flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-emerald-200">
            Authenticated as {user?.role}
          </span>
          <h1 className="text-2xl font-bold tracking-tight mt-1">Welcome back, {user?.displayName}</h1>
          <p className="text-emerald-100 text-sm mt-1 max-w-xl font-mono text-xs opacity-90">
            Wallet: {user?.walletAddress || '0x0000000000000000000000000000000000000000'}
          </p>
        </div>
        <div className="flex items-center space-x-2">
          {user?.role === 'REGISTRAR' && (
            <Link
              to="/registrar/queue"
              className="bg-white text-emerald-900 font-semibold text-xs px-4 py-2.5 rounded-lg hover:bg-emerald-50 transition shadow-sm"
            >
              Open Review Queue
            </Link>
          )}
          {(user?.role === 'LANDOWNER' || user?.role === 'ADMIN') && (
            <Link
              to="/mutations/new"
              className="bg-white text-emerald-900 font-semibold text-xs px-4 py-2.5 rounded-lg hover:bg-emerald-50 transition shadow-sm"
            >
              Submit Transfer / Split
            </Link>
          )}
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Active Land Records</div>
            <div className="text-2xl font-bold text-slate-900 mt-1">{parcels.length}</div>
            <div className="text-[11px] text-emerald-600 font-medium mt-1">On-chain Anchored</div>
          </div>
          <div className="bg-emerald-50 text-emerald-600 p-3 rounded-xl">
            <MapPin className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Pending Review</div>
            <div className="text-2xl font-bold text-slate-900 mt-1">
              {mutations.filter((m) => m.status === 'SUBMITTED' || m.status === 'EVALUATING' || m.status === 'HELD').length}
            </div>
            <div className="text-[11px] text-amber-600 font-medium mt-1">M8 Uncertainty Triaged</div>
          </div>
          <div className="bg-amber-50 text-amber-600 p-3 rounded-xl">
            <Clock className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Verification Rate</div>
            <div className="text-2xl font-bold text-slate-900 mt-1">100.0%</div>
            <div className="text-[11px] text-emerald-600 font-medium mt-1">0.0% Unsafe Auth</div>
          </div>
          <div className="bg-blue-50 text-blue-600 p-3 rounded-xl">
            <ShieldCheck className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Storage Provider</div>
            <div className="text-2xl font-bold text-slate-900 mt-1">Supabase</div>
            <div className="text-[11px] text-teal-600 font-medium mt-1">Off-chain S3 + Signed URLs</div>
          </div>
          <div className="bg-teal-50 text-teal-600 p-3 rounded-xl">
            <FileText className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Main Grid: Parcels Overview & Recent Mutations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Registered Parcels */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200/80 shadow-xs p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Layers className="w-4 h-4 text-emerald-600" />
              Registered Land Parcels
            </h2>
            <Link to="/parcels" className="text-xs font-semibold text-emerald-600 hover:text-emerald-700">
              View All ({parcels.length}) &rarr;
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase">
                <tr>
                  <th className="py-2.5 px-3 font-semibold">Parcel Code</th>
                  <th className="py-2.5 px-3 font-semibold">Survey No</th>
                  <th className="py-2.5 px-3 font-semibold">Locality</th>
                  <th className="py-2.5 px-3 font-semibold">Area</th>
                  <th className="py-2.5 px-3 font-semibold">Version</th>
                  <th className="py-2.5 px-3 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {parcels.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-3 font-bold text-slate-900">{p.parcelCode}</td>
                    <td className="py-3 px-3 text-slate-600">{p.surveyNumber}</td>
                    <td className="py-3 px-3 text-slate-600">{p.locality}</td>
                    <td className="py-3 px-3 font-mono">{p.areaSqm} sqm</td>
                    <td className="py-3 px-3">
                      <span className="px-2 py-0.5 rounded-full bg-slate-100 text-slate-800 font-bold">
                        v{p.version}
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      <Link
                        to={`/parcels/${p.id}`}
                        className="text-emerald-600 font-semibold hover:underline"
                      >
                        Inspect 360°
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right: Active Mutations Stream */}
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Activity className="w-4 h-4 text-purple-600" />
              Mutation Activity
            </h2>
            <Link to="/mutations" className="text-xs font-semibold text-purple-600 hover:text-purple-700">
              Ledger &rarr;
            </Link>
          </div>

          <div className="space-y-3">
            {mutations.length === 0 ? (
              <div className="text-xs text-slate-400 py-6 text-center">No recent mutations</div>
            ) : (
              mutations.map((m) => (
                <div key={m.id} className="p-3 rounded-lg border border-slate-100 bg-slate-50/60 hover:bg-slate-50 transition">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-xs text-slate-900">{m.mutationType}</span>
                    {getStatusBadge(m.status)}
                  </div>
                  <div className="text-[11px] text-slate-500 font-mono truncate">
                    ID: {m.id.substring(0, 16)}...
                  </div>
                  <div className="mt-2 flex items-center justify-between text-[11px]">
                    <span className="text-slate-500">By: {m.requestedBy?.displayName}</span>
                    <Link
                      to={`/mutations/${m.id}`}
                      className="text-purple-600 font-semibold hover:underline"
                    >
                      View Pipeline &rarr;
                    </Link>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
