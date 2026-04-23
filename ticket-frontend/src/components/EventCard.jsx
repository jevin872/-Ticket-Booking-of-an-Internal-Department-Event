import React from 'react';
import { Calendar, MapPin, Tag, ArrowRight, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';

const EventCard = ({ event }) => {
  const formattedDate = new Date(event.eventDate).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  const isFree = event.free || event.ticketPrice === 0;

  return (
    <div className="glass-card flex flex-col h-full group">
      <div className="relative h-48 mb-6 rounded-xl overflow-hidden bg-slate-700">
        {event.bannerImageUrl ? (
          <img 
            src={event.bannerImageUrl} 
            alt={event.title} 
            className="w-full h-full object-cover transition-transform group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-indigo-900/50 to-purple-900/50">
            <Calendar size={48} className="text-indigo-400/50" />
          </div>
        )}
        <div className="absolute top-4 left-4">
          <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${
            isFree ? 'bg-green-500/20 text-green-400 border border-green-500/50' : 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/50'
          }`}>
            {isFree ? 'Free' : `₹${event.ticketPrice}`}
          </span>
        </div>
        <div className="absolute top-4 right-4">
          <span className="px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider bg-slate-900/80 text-white backdrop-blur-md">
            {event.category}
          </span>
        </div>
      </div>

      <div className="flex-1">
        <h3 className="text-xl font-bold mb-2 group-hover:text-indigo-400 transition-colors">
          {event.title}
        </h3>
        <p className="text-gray-400 text-sm mb-4 line-clamp-2">
          {event.description}
        </p>

        <div className="space-y-2 mb-6">
          <div className="flex items-center gap-2 text-sm text-gray-300">
            <Calendar size={16} className="text-indigo-400" />
            <span>{formattedDate}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-gray-300">
            <MapPin size={16} className="text-indigo-400" />
            <span className="truncate">{event.venue}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-gray-300">
            <Tag size={16} className="text-indigo-400" />
            <span>{event.organizer}</span>
          </div>
        </div>
      </div>

      <div className="pt-4 border-t border-gray-800/50 flex items-center justify-between">
        <div className="flex flex-col">
          <span className="text-[10px] uppercase text-gray-500 font-bold">Remaining</span>
          <span className="text-sm font-semibold">{event.availableSeats} / {event.totalCapacity}</span>
        </div>
        <Link 
          to={`/events/${event.id}`}
          className="btn-primary py-2 px-4 text-sm"
        >
          Book Now
          <ArrowRight size={16} />
        </Link>
      </div>
    </div>
  );
};

export default EventCard;
