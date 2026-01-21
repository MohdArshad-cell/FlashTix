import React from 'react';
import { Ticket } from '../types';
import SeatCard from './SeatCard';

interface SeatGridProps {
  tickets: Ticket[];
  onSeatClick: (ticket: Ticket) => void;
}

const SeatGrid: React.FC<SeatGridProps> = ({ tickets, onSeatClick }) => {
  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Available Seats</h2>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 lg:grid-cols-10 gap-3">
        {tickets.map((ticket) => (
          <SeatCard
            key={ticket.id}
            ticket={ticket}
            onClick={() => onSeatClick(ticket)}
          />
        ))}
      </div>
    </div>
  );
};

export default SeatGrid;
