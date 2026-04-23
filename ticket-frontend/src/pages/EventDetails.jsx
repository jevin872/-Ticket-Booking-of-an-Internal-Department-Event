import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Calendar, MapPin, Tag, Users, Clock, ArrowLeft, ShieldCheck, Info, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    fetchEvent();
  }, [id]);

  const fetchEvent = async () => {
    try {
      const response = await api.get(`/events/public/${id}`);
      setEvent(response.data.data);
    } catch (err) {
      toast.error('Failed to load event details');
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const handleBooking = async () => {
    if (!user) {
      toast.error('Please login to book tickets');
      return navigate('/login');
    }

    try {
      setBookingLoading(true);
      await api.post('/bookings', { eventId: id, quantity });
      toast.success('Tickets booked successfully!');
      navigate('/my-bookings');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Booking failed');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-40 gap-4">
        <Loader2 className="animate-spin text-indigo-500" size={48} />
        <p className="text-gray-400">Loading event information...</p>
      </div>
    );
  }

  const formattedDate = new Date(event.eventDate).toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const deadline = new Date(event.registrationDeadline).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <button 
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-400 hover:text-white transition-colors group"
      >
        <ArrowLeft size={20} className="group-hover:-translate-x-1 transition-transform" />
        <span>Back to Events</span>
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-8">
          <div className="rounded-3xl overflow-hidden aspect-video bg-slate-800 shadow-2xl">
            {event.bannerImageUrl ? (
              <img src={event.bannerImageUrl} alt={event.title} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-indigo-900/50 to-purple-900/50">
                <Calendar size={80} className="text-indigo-400/20" />
              </div>
            )}
          </div>

          <div className="glass-card">
            <div className="flex flex-wrap gap-3 mb-6">
              <span className="px-4 py-1.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-sm font-bold uppercase">
                {event.category}
              </span>
              <span className="px-4 py-1.5 rounded-full bg-slate-800 text-gray-300 border border-slate-700 text-sm font-medium">
                {event.department}
              </span>
            </div>

            <h1 className="text-4xl font-extrabold mb-6 leading-tight">{event.title}</h1>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
              <div className="flex items-start gap-3">
                <div className="p-3 bg-indigo-500/10 rounded-xl text-indigo-400">
                  <Calendar size={24} />
                </div>
                <div>
                  <p className="text-xs text-gray-500 font-bold uppercase">Date & Time</p>
                  <p className="font-semibold">{formattedDate}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="p-3 bg-purple-500/10 rounded-xl text-purple-400">
                  <MapPin size={24} />
                </div>
                <div>
                  <p className="text-xs text-gray-500 font-bold uppercase">Location</p>
                  <p className="font-semibold">{event.venue}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="p-3 bg-cyan-500/10 rounded-xl text-cyan-400">
                  <Tag size={24} />
                </div>
                <div>
                  <p className="text-xs text-gray-500 font-bold uppercase">Organizer</p>
                  <p className="font-semibold">{event.organizer}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="p-3 bg-green-500/10 rounded-xl text-green-400">
                  <Clock size={24} />
                </div>
                <div>
                  <p className="text-xs text-gray-500 font-bold uppercase">Reg. Deadline</p>
                  <p className="font-semibold">{deadline}</p>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="text-2xl font-bold flex items-center gap-2">
                <Info className="text-indigo-400" />
                About Event
              </h3>
              <p className="text-gray-400 leading-relaxed text-lg whitespace-pre-wrap">
                {event.description}
              </p>
            </div>
          </div>
        </div>

        {/* Sidebar Booking */}
        <div className="lg:col-span-1">
          <div className="glass-card sticky top-28 border-indigo-500/20 border-2">
            <div className="flex justify-between items-center mb-6">
              <span className="text-gray-400 font-semibold">Ticket Price</span>
              <span className="text-3xl font-extrabold text-white">
                {event.ticketPrice === 0 ? 'FREE' : `₹${event.ticketPrice}`}
              </span>
            </div>

            <div className="space-y-6">
              <div className="flex flex-col gap-2">
                <label className="text-sm font-bold text-gray-500 uppercase tracking-wider">Select Tickets</label>
                <div className="flex items-center gap-4 bg-slate-900 rounded-xl p-2 border border-slate-700">
                  <button 
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="w-10 h-10 rounded-lg bg-slate-800 hover:bg-slate-700 flex items-center justify-center font-bold text-xl"
                  >-</button>
                  <span className="flex-1 text-center font-bold text-xl">{quantity}</span>
                  <button 
                    onClick={() => setQuantity(Math.min(event.maxTicketsPerUser, quantity + 1))}
                    className="w-10 h-10 rounded-lg bg-slate-800 hover:bg-slate-700 flex items-center justify-center font-bold text-xl"
                  >+</button>
                </div>
                <p className="text-xs text-gray-500 text-center">Max {event.maxTicketsPerUser} tickets per user</p>
              </div>

              <div className="flex items-center justify-between py-4 border-y border-slate-800">
                <span className="font-medium text-gray-400">Total Price</span>
                <span className="text-2xl font-bold text-indigo-400">
                  {event.ticketPrice === 0 ? 'FREE' : `₹${event.ticketPrice * quantity}`}
                </span>
              </div>

              <button 
                onClick={handleBooking}
                disabled={bookingLoading || event.availableSeats === 0}
                className={`btn-primary w-full py-4 text-lg rounded-2xl ${
                  event.availableSeats === 0 ? 'grayscale opacity-50 cursor-not-allowed' : ''
                }`}
              >
                {bookingLoading ? (
                  <Loader2 className="animate-spin" size={24} />
                ) : event.availableSeats === 0 ? (
                  'Sold Out'
                ) : (
                  <>
                    Confirm Booking
                    <ArrowRight size={20} />
                  </>
                )}
              </button>

              <div className="flex items-center gap-3 p-4 bg-indigo-500/5 rounded-2xl border border-indigo-500/10">
                <ShieldCheck className="text-indigo-400" size={24} />
                <p className="text-xs text-gray-400 leading-tight">
                  Tickets will be sent to your email immediately after confirmation.
                </p>
              </div>

              <div className="flex items-center justify-center gap-2 text-sm text-gray-500 font-medium">
                <Users size={16} />
                <span>Only {event.availableSeats} seats remaining!</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventDetails;
