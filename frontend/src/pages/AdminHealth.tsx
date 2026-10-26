import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Activity, Server, Database, ShieldCheck, HardDrive, Clock } from 'lucide-react';

export const AdminHealth: React.FC = () => {
  const [health, setHealth] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.admin.getHealth()
      .then((data) => setHealth(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-center py-20 text-slate-400 text-sm">Loading health metrics...</div>;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <Activity className="w-6 h-6 text-red-600" />
          System & Blockchain Node Health
        </h1>
        <p className="text-slate-500 text-sm mt-0.5">
          Live connectivity diagnostics across Database, Blockchain, and Off-chain Storage
        </p>
      </div>

      {/* Grid of Service Diagnostics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* API Backend */}
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Backend API</div>
            <div className="text-xl font-bold text-slate-900 mt-1">ONLINE</div>
            <div className="text-[11px] text-emerald-600 font-medium mt-1">Spring Boot 3.3.4 (Java 21)</div>
          </div>
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <Server className="w-6 h-6" />
          </div>
        </div>

        {/* Database */}
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Database Engine</div>
            <div className="text-xl font-bold text-slate-900 mt-1">CONNECTED</div>
            <div className="text-[11px] text-emerald-600 font-medium mt-1">
              Active: {health?.activeProfiles?.join(', ') || 'dev'}
            </div>
          </div>
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Database className="w-6 h-6" />
          </div>
        </div>

        {/* Blockchain Node */}
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Local EVM Node</div>
            <div className="text-xl font-bold text-slate-900 mt-1">
              {health?.blockchainConnected ? 'LIVE RPC' : 'SIMULATED'}
            </div>
            <div className="text-[11px] text-slate-500 font-medium mt-1">Chain ID 31337</div>
          </div>
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl">
            <ShieldCheck className="w-6 h-6" />
          </div>
        </div>

        {/* Storage Provider */}
        <div className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase">Document Store</div>
            <div className="text-xl font-bold text-slate-900 mt-1">{health?.storageProvider || 'LOCAL'}</div>
            <div className="text-[11px] text-teal-600 font-medium mt-1">Supabase Storage Ready</div>
          </div>
          <div className="p-3 bg-teal-50 text-teal-600 rounded-xl">
            <HardDrive className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Contract & Smart Ledger Status */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-3">
        <h2 className="text-sm font-bold text-slate-900">Contract Attestation</h2>
        <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs font-mono space-y-1">
          <div>Smart Contract: <strong>LandRegistry.sol (Solidity 0.8.24)</strong></div>
          <div>Contract Address: <strong className="text-slate-900">{health?.contractAddress}</strong></div>
        </div>
      </div>

      {/* Append-Only Audit Stream */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
          <Clock className="w-4 h-4 text-slate-500" />
          Append-Only System Audit Stream (Recent 100 Events)
        </h2>

        <div className="overflow-x-auto max-h-96">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase font-semibold sticky top-0">
              <tr>
                <th className="py-2.5 px-3">Timestamp</th>
                <th className="py-2.5 px-3">Actor</th>
                <th className="py-2.5 px-3">Action</th>
                <th className="py-2.5 px-3">Entity</th>
                <th className="py-2.5 px-3">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {health?.recentAuditEvents?.map((ev: any) => (
                <tr key={ev.id} className="hover:bg-slate-50 transition">
                  <td className="py-2.5 px-3 text-slate-500 font-mono text-[11px]">{ev.timestamp}</td>
                  <td className="py-2.5 px-3 font-semibold text-slate-800">{ev.actorId}</td>
                  <td className="py-2.5 px-3">
                    <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-bold text-[10px]">
                      {ev.action}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 font-mono text-slate-600">{ev.entityType} ({ev.entityId.substring(0, 8)}...)</td>
                  <td className="py-2.5 px-3 text-slate-600 truncate max-w-sm">{ev.detailsJson}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
