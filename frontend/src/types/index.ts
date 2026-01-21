export interface Ticket {
  id: number;
  seatNumber: string;
  status: 'AVAILABLE' | 'SOLD';
  userId?: number;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface BookingResponse {
  ticket: Ticket;
  message: string;
}

export interface LoadTestResult {
  totalRequests: number;
  successCount: number;
  conflictCount: number;
  otherErrors: number;
  duration: number;
  winnerUserId?: number;
}

export interface Stats {
  totalSeats: number;
  availableSeats: number;
  soldSeats: number;
  lastBooking?: Ticket;
}
