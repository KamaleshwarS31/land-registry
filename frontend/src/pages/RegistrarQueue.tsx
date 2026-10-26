import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { MutationRequest } from '../types';
import { CheckSquare, ArrowRight, Clock, AlertTriangle, ShieldCheck } from 'lucide-react';

export const RegistrarQueue: React.FC = () => {
  const [mutations, setMutations] = useState<MutationRequest[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.mutations.list(0, 50)
      .then((data) => setMutations(data.content || []))
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
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <CheckSquare className="w-6 h-6 text-purple-600" />
          Registrar Review & Verification Queue
        </h1>
        <p className="text-slate-500 text-sm mt-0.5">
          Evaluate cross-registry conflicts (M5), uncertainty status (M8), and commit authorized mutations to blockchain
        </p>
      </div>

      {loading ? (
        <div className="text-center py-12 text-slate-400 text-sm">Loading queue...</div>
      ) : mutations.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-dashed border-slate-300 text-slate-500 text-sm">
          No pending applications in queue.
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase font-semibold">
              <tr>
                <th className="py-3 px-4">Mutation Type</th>
                <th className="py-3 px-4">Application ID</th>
                <th className="py-3 px-4">Requester</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Decision Reason</th>
                <th className="py-3 px-4 text-right">Verification Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {mutations.map((m) => (
                <tr key={m.id} className="hover:bg-slate-50/80 transition">
                  <td className="py-3.5 px-4">
                    <span className="font-bold text-slate-900">{m.mutationType}</span>
                  </td>
                  <td className="py-3.5 px-4 font-mono text-slate-500">
                    {m.id.substring(0, 13)}...
                  </td>
                  <td className="py-3.5 px-4 font-medium text-slate-700">
                    {m.requestedBy?.displayName}
                  </td>
                  <td className="py-3.5 px-4">
                    {getStatusBadge(m.status)}
                  </td>
                  <td className="py-3.5 px-4 text-slate-500 max-w-xs truncate">
                    {m.decisionReason || 'Awaiting evaluation'}
                  </td>
                  <td className="py-3.5 px-4 text-right">
                    <Link
                      to={`/mutations/${m.id}`}
                      className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-purple-50 hover:bg-purple-100 text-purple-700 font-bold transition"
                    >
                      <span>Open Workbench</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
