import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { Ticket, Calendar, MapPin, Download, XCircle, Clock, Loader2, QrCode } from 'lucide-react';
import { toast } from 'react-hot-toast';

const MyBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      const response = await api.get('/bookings/my');
      setBookings(response.data.data);
    } catch (err) {
      toast.error('Failed to load your bookings');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    
    try {
      await api.patch(`/bookings/${id}/cancel`);
      toast.success('Booking cancelled successfully');
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cancellation failed');
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-40 gap-4">
        <Loader2 className="animate-spin text-indigo-500" size={48} />
        <p className="text-gray-400">Loading your tickets...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-4xl font-extrabold">My Tickets</h1>
        <div className="flex items-center gap-2 text-sm text-gray-500 bg-slate-800/50 px-4 py-2 rounded-full border border-slate-700/50">
          <Ticket size={16} className="text-indigo-400" />
          <span>{bookings.length} Bookings Total</span>
        </div>
      </div>

      {bookings.length > 0 ? (
        <div className="grid grid-cols-1 gap-6">
          {bookings.map(booking => (
            <div key={booking.id} className="glass-card flex flex-col md:flex-row gap-8 p-8 border-l-4 border-l-indigo-500">
              {/* Event Info */}
              <div className="flex-1 space-y-6">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-2xl font-bold mb-2 text-white">{booking.eventTitle}</h2>
                    <div className="flex flex-wrap gap-4 text-sm text-gray-400">
                      <div className="flex items-center gap-1.5">
                        <Calendar size={16} className="text-indigo-400" />
                        <span>{new Date(booking.eventDate).toLocaleDateString()}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <MapPin size={16} className="text-indigo-400" />
                        <span>{booking.venue}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock size={16} className="text-indigo-400" />
                        <span>{new Date(booking.eventDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                      </div>
                    </div>
                  </div>
                  <div className={`px-4 py-1 rounded-full text-xs font-bold uppercase tracking-widest ${
                    booking.status === 'CONFIRMED' ? 'bg-green-500/10 text-green-400 border border-green-500/20' : 
                    booking.status === 'CANCELLED' ? 'bg-red-500/10 text-red-400 border border-red-500/20' : 
                    'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20'
                  }`}>
                    {booking.status}
                  </div>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-6 border-t border-slate-800/50">
                  <div>
                    <p className="text-[10px] uppercase font-bold text-gray-500 mb-1">Booking Ref</p>
                    <p className="font-mono font-bold text-indigo-400">{booking.bookingReference}</p>
                  </div>
                  <div>
                    <p className="text-[10px] uppercase font-bold text-gray-500 mb-1">Quantity</p>
                    <p className="font-bold">{booking.quantity} Tickets</p>
                  </div>
                  <div>
                    <p className="text-[10px] uppercase font-bold text-gray-500 mb-1">Total Paid</p>
                    <p className="font-bold">₹{booking.totalAmount}</p>
                  </div>
                  <div>
                    <p className="text-[10px] uppercase font-bold text-gray-500 mb-1">Booked On</p>
                    <p className="font-bold">{new Date(booking.bookedAt).toLocaleDateString()}</p>
                  </div>
                </div>
              </div>

              {/* QR and Actions */}
              <div className="md:w-64 flex flex-col items-center justify-between border-t md:border-t-0 md:border-l border-slate-800/50 pt-8 md:pt-0 md:pl-8">
                {booking.status === 'CONFIRMED' ? (
                  <div className="flex flex-col items-center gap-4 w-full">
                    <div className="bg-white p-3 rounded-2xl shadow-xl shadow-indigo-500/10 transition-transform hover:scale-105">
                      <QrCode size={120} className="text-slate-900" />
                    </div>
                    <div className="flex flex-col gap-2 w-full">
                      <button className="btn-outline w-full text-xs py-2.5 flex items-center justify-center gap-2">
                        <Download size={14} />
                        Download PDF
                      </button>
                      <button 
                        onClick={() => handleCancel(booking.id)}
                        className="text-red-500 hover:text-red-400 text-xs font-bold flex items-center justify-center gap-1.5 py-2 transition-colors"
                      >
                        <XCircle size={14} />
                        Cancel Booking
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center h-full text-center py-10">
                    <XCircle size={48} className="text-gray-700 mb-4" />
                    <p className="text-gray-500 text-sm italic">
                      {booking.status === 'CANCELLED' ? 'This booking was cancelled' : 'Ticket is pending'}
                    </p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 glass-card">
          <div className="bg-slate-800 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
            <Ticket size={40} className="text-gray-600" />
          </div>
          <h2 className="text-2xl font-bold mb-2">No bookings yet</h2>
          <p className="text-gray-400 mb-8">Ready to join some events? Explore our catalog.</p>
          <button onClick={() => navigate('/')} className="btn-primary py-3 px-8 rounded-xl">
            Browse Events
          </button>
        </div>
      )}
    </div>
  );
};

export default MyBookings;
