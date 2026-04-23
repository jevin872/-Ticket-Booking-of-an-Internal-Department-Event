import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import EventCard from '../components/EventCard';
import { EventCardSkeleton } from '../components/Skeleton';
import { Search, Filter, Loader2, CalendarRange } from 'lucide-react';

const Home = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');

  const categories = ['TECHFEST', 'SEMINAR', 'WORKSHOP', 'CULTURAL', 'SPORTS', 'OTHER'];

  useEffect(() => {
    fetchEvents();
  }, [category]);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      let url = '/events/public';
      if (category) {
        url = `/events/public/search?keyword=${category}`;
      }
      const response = await api.get(url);
      // Backend returns a Page object or direct list depending on endpoint
      const data = response.data.data.content || response.data.data;
      setEvents(data);
    } catch (err) {
      console.error('Failed to fetch events', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredEvents = events.filter(e => 
    e.title.toLowerCase().includes(search.toLowerCase()) || 
    e.description.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-10">
      {/* Hero Section */}
      <section className="relative py-20 px-8 rounded-[2rem] overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-600/20 via-purple-600/10 to-transparent"></div>
        <div className="absolute -top-24 -right-24 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-24 -left-24 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl"></div>
        
        <div className="relative z-10 max-w-3xl">
          <h1 className="text-5xl md:text-6xl font-extrabold mb-6 tracking-tight leading-tight">
            Discover <span className="text-indigo-400">Amazing</span> Department Events
          </h1>
          <p className="text-xl text-gray-400 mb-10 max-w-2xl leading-relaxed">
            Your gateway to seminars, workshops, and fests. Book your spot in seconds and join the community of learners and creators.
          </p>
          
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
              <input 
                type="text" 
                placeholder="Search events, topics, or venues..."
                className="w-full pl-12 pr-4 py-4 bg-slate-900/50 backdrop-blur-md border-gray-700 rounded-2xl text-lg focus:ring-2 focus:ring-indigo-500/50"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button className="btn-primary py-4 px-8 rounded-2xl text-lg">
              Explore Now
            </button>
          </div>
        </div>
      </section>

      {/* Filter Section */}
      <div className="flex flex-wrap items-center justify-between gap-6">
        <div className="flex items-center gap-3 overflow-x-auto pb-2 no-scrollbar">
          <button 
            onClick={() => setCategory('')}
            className={`px-5 py-2.5 rounded-xl font-semibold text-sm transition-all ${
              category === '' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-gray-400 hover:bg-slate-700'
            }`}
          >
            All Events
          </button>
          {categories.map(cat => (
            <button 
              key={cat}
              onClick={() => setCategory(cat)}
              className={`px-5 py-2.5 rounded-xl font-semibold text-sm transition-all whitespace-nowrap ${
                category === cat ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-gray-400 hover:bg-slate-700'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
        
        <div className="flex items-center gap-2 text-sm text-gray-400 font-medium">
          <Filter size={18} />
          <span>{filteredEvents.length} Events Found</span>
        </div>
      </div>

      {/* Events Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {[1, 2, 3, 4, 5, 6].map(i => <EventCardSkeleton key={i} />)}
        </div>
      ) : filteredEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {filteredEvents.map(event => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 glass-card">
          <div className="bg-slate-800 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
            <CalendarRange size={40} className="text-gray-600" />
          </div>
          <h2 className="text-2xl font-bold mb-2">No events found</h2>
          <p className="text-gray-400">Try adjusting your filters or search terms.</p>
        </div>
      )}
    </div>
  );
};

export default Home;
