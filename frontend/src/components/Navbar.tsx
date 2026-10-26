import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Shield, UserCheck, LogOut, RefreshCw } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, quickLogin, logout } = useAuth();

  const getRoleBadgeColor = (role?: string) => {
    switch (role) {
      case 'REGISTRAR': return 'bg-purple-100 text-purple-800 border-purple-300';
      case 'SURVEY_REVIEWER': return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'ADMIN': return 'bg-red-100 text-red-800 border-red-300';
      case 'BUYER': return 'bg-amber-100 text-amber-800 border-amber-300';
      default: return 'bg-emerald-100 text-emerald-800 border-emerald-300';
    }
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo & Platform Name */}
        <div className="flex items-center space-x-3">
          <div className="bg-emerald-600 p-2 rounded-lg text-white shadow-sm">
            <Shield className="w-5 h-5" />
          </div>
          <div>
            <span className="font-bold text-lg text-slate-900 tracking-tight">LandRegistry</span>
            <span className="ml-2 text-xs font-semibold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
              M5•M8•M2 Verified
            </span>
          </div>
        </div>

        {/* Persona Switcher & User Profile */}
        <div className="flex items-center space-x-4">
          <div className="hidden lg:flex items-center space-x-1.5 text-xs bg-slate-100 p-1 rounded-lg border border-slate-200">
            <span className="text-slate-500 font-medium px-2 flex items-center gap-1">
              <RefreshCw className="w-3 h-3" /> Quick Switch:
            </span>
            <button
              onClick={() => quickLogin('alice')}
              className={`px-2 py-1 rounded font-medium transition ${user?.login === 'alice' ? 'bg-white text-emerald-700 shadow-xs' : 'text-slate-600 hover:text-slate-900'}`}
            >
              Alice (Owner)
            </button>
            <button
              onClick={() => quickLogin('bob')}
              className={`px-2 py-1 rounded font-medium transition ${user?.login === 'bob' ? 'bg-white text-amber-700 shadow-xs' : 'text-slate-600 hover:text-slate-900'}`}
            >
              Bob (Buyer)
            </button>
            <button
              onClick={() => quickLogin('clara')}
              className={`px-2 py-1 rounded font-medium transition ${user?.login === 'clara' ? 'bg-white text-purple-700 shadow-xs' : 'text-slate-600 hover:text-slate-900'}`}
            >
              Clara (Registrar)
            </button>
            <button
              onClick={() => quickLogin('dan')}
              className={`px-2 py-1 rounded font-medium transition ${user?.login === 'dan' ? 'bg-white text-blue-700 shadow-xs' : 'text-slate-600 hover:text-slate-900'}`}
            >
              Dan (Surveyor)
            </button>
            <button
              onClick={() => quickLogin('eve')}
              className={`px-2 py-1 rounded font-medium transition ${user?.login === 'eve' ? 'bg-white text-red-700 shadow-xs' : 'text-slate-600 hover:text-slate-900'}`}
            >
              Eve (Admin)
            </button>
          </div>

          {user && (
            <div className="flex items-center space-x-3 pl-2 border-l border-slate-200">
              <div className="text-right">
                <div className="text-sm font-semibold text-slate-800 leading-tight">{user.displayName}</div>
                <span className={`inline-block text-[10px] font-bold px-1.5 py-0.2 rounded border ${getRoleBadgeColor(user.role)}`}>
                  {user.role}
                </span>
              </div>
              <button
                onClick={logout}
                title="Sign out"
                className="text-slate-400 hover:text-slate-600 p-1.5 rounded-lg hover:bg-slate-100 transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
