import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FormInput from '../components/FormInput';
import { Mail, Lock, LogIn, ArrowRight, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(formData.email, formData.password);
      toast.success('Welcome back!');
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center py-12">
      <div className="w-full max-w-md">
        <div className="glass-card shadow-2xl relative overflow-hidden">
          {/* Decorative element */}
          <div className="absolute top-0 right-0 -mr-16 -mt-16 w-32 h-32 bg-indigo-600/10 rounded-full blur-2xl"></div>
          
          <div className="relative z-10">
            <div className="text-center mb-10">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 mb-4">
                <LogIn className="text-indigo-500" size={32} />
              </div>
              <h2 className="text-3xl font-bold mb-2">Welcome Back</h2>
              <p className="text-gray-400">Sign in to manage your tickets and events</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <FormInput 
                label="Email Address"
                icon={Mail}
                type="email"
                placeholder="john@college.edu"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />

              <div className="space-y-1">
                <FormInput 
                  label="Password"
                  icon={Lock}
                  type="password"
                  placeholder="••••••••"
                  required
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
                <div className="flex justify-end">
                  <Link to="/forgot-password" size="sm" className="text-xs text-indigo-400 hover:text-indigo-300 font-medium">
                    Forgot Password?
                  </Link>
                </div>
              </div>

              <button 
                type="submit" 
                disabled={loading}
                className="btn-primary w-full py-3.5 text-lg"
              >
                {loading ? <Loader2 className="animate-spin" size={20} /> : 'Sign In'}
                {!loading && <ArrowRight size={20} />}
              </button>
            </form>

            <div className="mt-8 pt-8 border-t border-gray-800/50 text-center">
              <p className="text-gray-400 text-sm">
                Don't have an account?{' '}
                <Link to="/register" className="text-indigo-400 hover:text-indigo-300 font-bold">
                  Create Account
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
