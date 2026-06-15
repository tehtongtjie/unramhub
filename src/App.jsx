import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import './App.css'; // Pastikan mengimpor CSS global App di sini

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const isLogged = localStorage.getItem('is_logged_in');
    setIsAuthenticated(isLogged === 'true');
    setLoading(false);
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
  };

  if (loading) {
    return <div className="global-loading">Memuat UnramHUB...</div>;
  }

  return (
    <div className="app-wrapper">
      {isAuthenticated ? (
        <Dashboard onLogout={handleLogout} />
      ) : (
        <Login onLoginSuccess={() => setIsAuthenticated(true)} />
      )}
    </div>
  );
}

export default App;