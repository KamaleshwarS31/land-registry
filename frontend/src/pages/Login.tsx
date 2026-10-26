import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User as UserIcon, ArrowRight, CheckCircle2 } from 'lucide-react';

export const Login: React.FC = () => {
  const { login, quickLogin } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleQuick = async (role: 'alice' | 'bob' | 'clara' | 'dan' | 'eve') => {
    setError('');
    setLoading(true);
    try {
      await quickLogin(role);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Quick login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-emerald-600 text-white shadow-lg mb-4">
          <Shield className="w-8 h-8" />
        </div>
        <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">LandRegistry</h2>
        <p className="mt-2 text-sm text-slate-600">
          Provenance-Aware Uncertainty & Lineage Verification Platform
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-xl sm:rounded-2xl sm:px-10 border border-slate-200/80">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg">
              {error}
            </div>
          )}

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Username
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <UserIcon className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="e.g. alice, clara"
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="password123"
                  className="block w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 transition"
            >
              {loading ? 'Authenticating...' : 'Sign In'}
              <ArrowRight className="ml-2 w-4 h-4" />
            </button>
          </form>

          {/* Quick Demo Personas */}
          <div className="mt-6 pt-6 border-t border-slate-200">
            <div className="text-xs font-bold text-slate-400 uppercase tracking-wider text-center mb-3">
              Or Instant Demo Login:
            </div>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => handleQuick('alice')}
                className="flex items-center p-2 rounded-lg border border-emerald-200 bg-emerald-50/60 hover:bg-emerald-100 text-emerald-900 font-medium transition text-left"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-emerald-600 shrink-0" />
                <div>
                  <div className="font-bold">Alice Vance</div>
                  <div className="text-[10px] text-emerald-700">Landowner</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuick('bob')}
                className="flex items-center p-2 rounded-lg border border-amber-200 bg-amber-50/60 hover:bg-amber-100 text-amber-900 font-medium transition text-left"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-amber-600 shrink-0" />
                <div>
                  <div className="font-bold">Bob Smith</div>
                  <div className="text-[10px] text-amber-700">Buyer</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuick('clara')}
                className="flex items-center p-2 rounded-lg border border-purple-200 bg-purple-50/60 hover:bg-purple-100 text-purple-900 font-medium transition text-left"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-purple-600 shrink-0" />
                <div>
                  <div className="font-bold">Clara Oswald</div>
                  <div className="text-[10px] text-purple-700">Registrar</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => handleQuick('dan')}
                className="flex items-center p-2 rounded-lg border border-blue-200 bg-blue-50/60 hover:bg-blue-100 text-blue-900 font-medium transition text-left"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-blue-600 shrink-0" />
                <div>
                  <div className="font-bold">Dan Miller</div>
                  <div className="text-[10px] text-blue-700">Surveyor</div>
                </div>
              </button>
            </div>
            <button
              type="button"
              onClick={() => handleQuick('eve')}
              className="mt-2 w-full py-1.5 rounded-lg border border-red-200 bg-red-50/60 hover:bg-red-100 text-red-900 font-medium transition text-xs flex items-center justify-center"
            >
              <CheckCircle2 className="w-3.5 h-3.5 mr-1.5 text-red-600" />
              Sign in as <strong>Eve Polastri (System Admin)</strong>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
