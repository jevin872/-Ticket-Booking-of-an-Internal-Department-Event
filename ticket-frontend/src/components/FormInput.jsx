import React from 'react';

const FormInput = ({ label, icon: Icon, error, ...props }) => {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      <label className="text-sm font-semibold text-gray-400 ml-1">
        {label}
      </label>
      <div className="relative group">
        {Icon && (
          <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500 group-focus-within:text-indigo-500 transition-colors">
            <Icon size={18} />
          </div>
        )}
        <input 
          {...props}
          className={`w-full ${Icon ? 'pl-11' : 'pl-4'} pr-4 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 outline-none transition-all ${
            error ? 'border-red-500 focus:border-red-500 focus:ring-red-500/10' : ''
          }`}
        />
      </div>
      {error && <span className="text-xs text-red-400 ml-1 mt-0.5">{error}</span>}
    </div>
  );
};

export default FormInput;
