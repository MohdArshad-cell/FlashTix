import React from 'react';
import { Ticket } from '../types';
import { CheckCircleIcon, XCircleIcon, TicketIcon } from '@heroicons/react/24/solid';

interface StatsDisplayProps {
  tickets: Ticket[];
}

const StatsDisplay: React.FC<StatsDisplayProps> = ({ tickets }) => {
  const totalSeats = tickets.length;
  const availableSeats = tickets.filter(t => t.status === 'AVAILABLE').length;
  const soldSeats = tickets.filter(t => t.status === 'SOLD').length;

  const stats = [
    {
      label: 'Total Seats',
      value: totalSeats,
      icon: TicketIcon,
      color: 'bg-blue-500',
    },
    {
      label: 'Available',
      value: availableSeats,
      icon: CheckCircleIcon,
      color: 'bg-green-500',
    },
    {
      label: 'Sold',
      value: soldSeats,
      icon: XCircleIcon,
      color: 'bg-red-500',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {stats.map((stat, index) => (
        <div
          key={index}
          className="bg-white rounded-lg shadow-md p-6 flex items-center space-x-4"
        >
          <div className={`${stat.color} p-3 rounded-lg`}>
            <stat.icon className="w-8 h-8 text-white" />
          </div>
          <div>
            <p className="text-gray-500 text-sm">{stat.label}</p>
            <p className="text-3xl font-bold text-gray-800">{stat.value}</p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatsDisplay;
