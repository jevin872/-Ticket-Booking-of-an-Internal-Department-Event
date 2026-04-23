import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FormInput from '../components/FormInput';
import { Mail, Lock, User, Briefcase, Phone, IdCard, UserPlus, Loader2, CheckCircle } from 'lucide-react';
import { toast } from 'react-hot-toast';

const Register = () => {
  const [formData, setFormData] = useState({
    employeeId: '',
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    department: '',
    phone: '',
  });
  const [loading, setLoading] = useState(false);
  const [registered, setRegistered] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      return toast.error('Passwords do not match');
    }
    
    setLoading(true);
    try {
      await register(formData);
      setRegistered(true);
      toast.success('Registration successful!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  if (registered) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="glass-card max-w-md text-center p-12">
          <div className="w-20 h-20 bg-green-500/10 rounded-full flex items-center justify-center mx-auto mb-8 border border-green-500/20">
            <CheckCircle className="text-green-500" size={40} />
          </div>
          <h2 className="text-3xl font-bold mb-4">Check Your Email</h2>
          <p className="text-gray-400 mb-8 leading-relaxed">
            We've sent a verification link to <span className="text-white font-bold">{formData.email}</span>. 
            Please verify your email to activate your account.
          </p>
          <Link to="/login" className="btn-primary py-3 px-8 rounded-xl w-full">
            Back to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="flex justify-center items-center py-10">
      <div className="w-full max-w-2xl">
        <div className="glass-card shadow-2xl relative overflow-hidden">
          <div className="absolute -bottom-16 -left-16 w-32 h-32 bg-purple-600/10 rounded-full blur-2xl"></div>
          
          <div className="relative z-10">
            <div className="text-center mb-10">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-purple-500/10 border border-purple-500/20 mb-4">
                <UserPlus className="text-purple-500" size={32} />
              </div>
              <h2 className="text-3xl font-bold mb-2">Create Account</h2>
              <p className="text-gray-400">Join the TechFest ticketing community</p>
            </div>

            <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <FormInput 
                label="Full Name"
                icon={User}
                placeholder="John Doe"
                required
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              />

              <FormInput 
                label="Employee/Student ID"
                icon={IdCard}
                placeholder="EMP123"
                required
                value={formData.employeeId}
                onChange={(e) => setFormData({ ...formData, employeeId: e.target.value })}
              />

              <FormInput 
                label="Email Address"
                icon={Mail}
                type="email"
                placeholder="john@college.edu"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />

              <FormInput 
                label="Phone Number"
                icon={Phone}
                placeholder="9876543210"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />

              <FormInput 
                label="Department"
                icon={Briefcase}
                placeholder="Computer Science"
                required
                value={formData.department}
                onChange={(e) => setFormData({ ...formData, department: e.target.value })}
              />

              <div className="hidden md:block"></div>

              <FormInput 
                label="Password"
                icon={Lock}
                type="password"
                placeholder="••••••••"
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />

              <FormInput 
                label="Confirm Password"
                icon={Lock}
                type="password"
                placeholder="••••••••"
                required
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
              />

              <div className="md:col-span-2 pt-4">
                <button 
                  type="submit" 
                  disabled={loading}
                  className="btn-primary w-full py-4 text-lg bg-gradient-to-r from-indigo-600 to-purple-600"
                >
                  {loading ? <Loader2 className="animate-spin mr-2" size={20} /> : 'Create Account'}
                </button>
              </div>
            </form>

            <div className="mt-8 pt-8 border-t border-gray-800/50 text-center">
              <p className="text-gray-400 text-sm">
                Already have an account?{' '}
                <Link to="/login" className="text-indigo-400 hover:text-indigo-300 font-bold">
                  Sign In
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
