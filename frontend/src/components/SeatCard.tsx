import React from 'react';
import { Ticket } from '../types';
import { motion } from 'framer-motion';

interface SeatCardProps {
  ticket: Ticket;
  onClick: () => void;
}

const SeatCard: React.FC<SeatCardProps> = ({ ticket, onClick }) => {
  const isAvailable = ticket.status === 'AVAILABLE';

  return (
    <motion.div
      whileHover={isAvailable ? { scale: 1.05 } : {}}
      whileTap={isAvailable ? { scale: 0.95 } : {}}
      className={`
        rounded-lg p-4 shadow-md cursor-pointer transition-all
        ${isAvailable 
          ? 'bg-green-50 border-2 border-green-300 hover:bg-green-100' 
          : 'bg-red-50 border-2 border-red-300 opacity-60 cursor-not-allowed'
        }
      `}
      onClick={isAvailable ? onClick : undefined}
    >
      <div className="flex flex-col items-center space-y-2">
        <div className={`
          w-12 h-12 rounded-full flex items-center justify-center font-bold text-white
          ${isAvailable ? 'bg-green-500' : 'bg-red-500'}
        `}>
          {ticket.id}
        </div>
        <p className="font-semibold text-gray-700">{ticket.seatNumber}</p>
        <span className={`
          px-3 py-1 rounded-full text-xs font-semibold
          ${isAvailable 
            ? 'bg-green-200 text-green-800' 
            : 'bg-red-200 text-red-800'
          }
        `}>
          {ticket.status}
        </span>
        {ticket.userId && (
          <p className="text-xs text-gray-500">User: {ticket.userId}</p>
        )}
      </div>
    </motion.div>
  );
};

export default SeatCard;
