import React from 'react';

const Skeleton = ({ className }) => {
  return (
    <div className={`animate-pulse bg-slate-800/50 rounded-xl ${className}`}></div>
  );
};

export const EventCardSkeleton = () => (
  <div className="glass-card flex flex-col h-full">
    <Skeleton className="h-48 mb-6" />
    <Skeleton className="h-8 w-3/4 mb-4" />
    <Skeleton className="h-4 w-full mb-2" />
    <Skeleton className="h-4 w-5/6 mb-6" />
    <div className="pt-4 border-t border-gray-800/50 flex justify-between items-center">
      <Skeleton className="h-10 w-20" />
      <Skeleton className="h-10 w-24" />
    </div>
  </div>
);

export default Skeleton;
