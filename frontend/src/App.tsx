import React, { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import toast from 'react-hot-toast';
import Navbar from './components/Navbar';
import StatsDisplay from './components/StatsDisplay';
import SeatGrid from './components/SeatGrid';
import BookingModal from './components/BookingModal';
import LoadTestPanel from './components/LoadTestPanel';
import { useTickets } from './hooks/useTickets';
import { Ticket } from './types';
import { ticketAPI } from './services/api';

function App() {
  const { tickets, updateTicketStatus } = useTickets();
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);

  const handleSeatClick = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setIsModalOpen(true);
  };

  const handleBookingConfirm = async (userId: number) => {
    if (!selectedTicket) return;

    setBookingLoading(true);
    try {
      const bookedTicket = await ticketAPI.bookTicket(selectedTicket.id, userId);
      updateTicketStatus(selectedTicket.id, 'SOLD', userId);
      toast.success(`✅ Ticket ${bookedTicket.seatNumber} booked successfully!`);
      setIsModalOpen(false);
      setSelectedTicket(null);
    } catch (error: any) {
      if (error.response?.status === 409) {
        toast.error('⚠️ This ticket is already booked! Someone was faster.');
        updateTicketStatus(selectedTicket.id, 'SOLD');
      } else {
        toast.error(error.response?.data?.message || 'Booking failed. Try again.');
      }
    } finally {
      setBookingLoading(false);
    }
  };

  const handleSeedTickets = async () => {
    try {
      await ticketAPI.seedTickets();
      toast.success('✅ 100 tickets seeded successfully!');
      window.location.reload();
    } catch (error) {
      toast.error('Failed to seed tickets');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Toaster position="top-right" />
      <Navbar />
      
      <div className="container mx-auto px-4 py-8">
        {/* Seed Button */}
        <div className="mb-6 flex justify-end">
          <button
            onClick={handleSeedTickets}
            className="bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition shadow-md"
          >
            🌱 Seed 100 Tickets
          </button>
        </div>

        {/* Stats */}
        <StatsDisplay tickets={tickets} />

        {/* Seat Grid */}
        <div className="mt-8">
          <SeatGrid tickets={tickets} onSeatClick={handleSeatClick} />
        </div>

        {/* Load Test Panel */}
        <div className="mt-8">
          <LoadTestPanel />
        </div>
      </div>

      {/* Booking Modal */}
      <BookingModal
        ticket={selectedTicket}
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setSelectedTicket(null);
        }}
        onConfirm={handleBookingConfirm}
        loading={bookingLoading}
      />
    </div>
  );
}

export default App;
