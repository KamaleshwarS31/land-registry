import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Parcels } from './pages/Parcels';
import { ParcelDetail } from './pages/ParcelDetail';
import { NewParcel } from './pages/NewParcel';
import { NewMutation } from './pages/NewMutation';
import { RegistrarQueue } from './pages/RegistrarQueue';
import { MutationWorkbench } from './pages/MutationWorkbench';
import { CertificateDetail } from './pages/CertificateDetail';
import { AdminHealth } from './pages/AdminHealth';

const ProtectedLayout: React.FC = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-slate-400">Loading platform...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="flex-1 flex max-w-7xl w-full mx-auto">
        <Sidebar />
        <main className="flex-1 p-6 overflow-y-auto">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/parcels" element={<Parcels />} />
            <Route path="/parcels/new" element={<NewParcel />} />
            <Route path="/parcels/:id" element={<ParcelDetail />} />
            <Route path="/mutations" element={<RegistrarQueue />} />
            <Route path="/mutations/new" element={<NewMutation />} />
            <Route path="/mutations/:id" element={<MutationWorkbench />} />
            <Route path="/registrar/queue" element={<RegistrarQueue />} />
            <Route path="/certificates/:id" element={<CertificateDetail />} />
            <Route path="/admin/system" element={<AdminHealth />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/*" element={<ProtectedLayout />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
