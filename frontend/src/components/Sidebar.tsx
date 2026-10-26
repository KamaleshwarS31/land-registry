import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  MapPin,
  FilePlus,
  ArrowRightLeft,
  CheckSquare,
  Clock,
  Activity,
  Users,
  Layers,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();
  const role = user?.role;

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
      isActive
        ? 'bg-emerald-50 text-emerald-800 font-semibold border-r-4 border-emerald-600'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    }`;

  return (
    <aside className="w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] p-4 space-y-6 shrink-0">
      <div>
        <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">Main</div>
        <nav className="space-y-1">
          <NavLink to="/" className={linkClass}>
            <LayoutDashboard className="w-4 h-4" />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/parcels" className={linkClass}>
            <MapPin className="w-4 h-4" />
            <span>Parcels Explorer</span>
          </NavLink>
        </nav>
      </div>

      {/* Citizen / Landowner Section */}
      {(role === 'LANDOWNER' || role === 'ADMIN') && (
        <div>
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">Landowner</div>
          <nav className="space-y-1">
            <NavLink to="/parcels/new" className={linkClass}>
              <FilePlus className="w-4 h-4" />
              <span>Register New Parcel</span>
            </NavLink>
            <NavLink to="/mutations/new" className={linkClass}>
              <ArrowRightLeft className="w-4 h-4" />
              <span>Transfer / Split / Merge</span>
            </NavLink>
            <NavLink to="/my-requests" className={linkClass}>
              <Clock className="w-4 h-4" />
              <span>My Applications</span>
            </NavLink>
          </nav>
        </div>
      )}

      {/* Registrar / Surveyor Section */}
      {(role === 'REGISTRAR' || role === 'SURVEY_REVIEWER' || role === 'ADMIN') && (
        <div>
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">
            Verification & Review
          </div>
          <nav className="space-y-1">
            <NavLink to="/registrar/queue" className={linkClass}>
              <CheckSquare className="w-4 h-4 text-purple-600" />
              <span>Registrar Review Queue</span>
            </NavLink>
            <NavLink to="/mutations" className={linkClass}>
              <Layers className="w-4 h-4" />
              <span>All Mutations Ledger</span>
            </NavLink>
          </nav>
        </div>
      )}

      {/* Admin Section */}
      {role === 'ADMIN' && (
        <div>
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">System Admin</div>
          <nav className="space-y-1">
            <NavLink to="/admin/system" className={linkClass}>
              <Activity className="w-4 h-4 text-red-600" />
              <span>System & Node Health</span>
            </NavLink>
            <NavLink to="/admin/users" className={linkClass}>
              <Users className="w-4 h-4" />
              <span>User Management</span>
            </NavLink>
          </nav>
        </div>
      )}
    </aside>
  );
};
