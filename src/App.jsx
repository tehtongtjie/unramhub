import { useState } from 'react';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import './App.css'; // Pastikan mengimpor CSS global App di sini

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(() => localStorage.getItem('is_logged_in') === 'true');

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
  };

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
