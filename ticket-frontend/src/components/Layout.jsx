import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, User, LayoutDashboard, Ticket, Calendar, Shield } from 'lucide-react';

const Navbar = () => {
  const { user, logout, isAdmin, isOrganizer } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="glass sticky top-0 z-50 py-4 px-6 mb-8">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <Link to="/" className="flex items-center gap-2">
          <div className="w-10 h-10 bg-indigo-600 rounded-lg flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <Ticket className="text-white" />
          </div>
          <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
            TechFest Ticketing
          </span>
        </Link>

        <div className="flex items-center gap-6">
          <Link to="/" className="text-gray-300 hover:text-white flex items-center gap-2 hover-underline">
            <Calendar size={18} />
            <span>Events</span>
          </Link>

          {user ? (
            <>
              <Link to="/my-bookings" className="text-gray-300 hover:text-white flex items-center gap-2 hover-underline">
                <Ticket size={18} />
                <span>My Tickets</span>
              </Link>

              {isOrganizer && (
                <Link to="/organizer" className="text-gray-300 hover:text-white flex items-center gap-2">
                  <LayoutDashboard size={18} />
                  <span>Organizer</span>
                </Link>
              )}

              {isAdmin && (
                <Link to="/admin" className="text-indigo-400 hover:text-indigo-300 flex items-center gap-2">
                  <Shield size={18} />
                  <span>Admin</span>
                </Link>
              )}

              <div className="h-6 w-px bg-gray-700"></div>

              <div className="flex items-center gap-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-indigo-500/20 border border-indigo-500/50 flex items-center justify-center">
                    <User size={16} className="text-indigo-400" />
                  </div>
                  <span className="text-sm font-medium hidden md:block">{user.fullName}</span>
                </div>
                <button 
                  onClick={handleLogout}
                  className="p-2 text-gray-400 hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
                  title="Logout"
                >
                  <LogOut size={20} />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-3">
              <Link to="/login" className="btn-outline text-sm">Login</Link>
              <Link to="/register" className="btn-primary text-sm">Sign Up</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen relative">
      {/* Background Mesh */}
      <div className="bg-mesh">
        <div className="bg-blob blob-1"></div>
        <div className="bg-blob blob-2"></div>
        <div className="bg-blob blob-3"></div>
      </div>

      <Navbar />
      <main className="max-w-7xl mx-auto px-6 fade-in relative z-10">
        {children}
      </main>
      
      <footer className="mt-20 py-10 border-t border-gray-800/50 text-center text-gray-500 text-sm relative z-10">
        <p>&copy; 2026 TechFest Ticket Booking System. Built with Spring Boot & React.</p>
      </footer>
    </div>
  );
};

export default Layout;
