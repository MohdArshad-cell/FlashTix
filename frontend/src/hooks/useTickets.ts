import { useState, useEffect } from 'react';
import { Ticket } from '../types';

export const useTickets = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);

  // Generate mock tickets (since backend might not have GET all endpoint)
  const generateMockTickets = (): Ticket[] => {
    return Array.from({ length: 100 }, (_, i) => ({
      id: i + 1,
      seatNumber: `Seat-${i + 1}`,
      status: 'AVAILABLE' as const,
    }));
  };

  useEffect(() => {
    // Initialize with mock tickets
    setTickets(generateMockTickets());
    setLoading(false);
  }, []);

  const refreshTickets = () => {
    // In real app, fetch from backend
    // For now, keep existing state
  };

  const updateTicketStatus = (ticketId: number, status: 'AVAILABLE' | 'SOLD', userId?: number) => {
    setTickets(prev => 
      prev.map(ticket => 
        ticket.id === ticketId 
          ? { ...ticket, status, userId }
          : ticket
      )
    );
  };

  return {
    tickets,
    loading,
    refreshTickets,
    updateTicketStatus,
  };
};
