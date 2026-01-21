import axios from 'axios';
import { Ticket, LoadTestResult } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const ticketAPI = {
  // Seed tickets
  seedTickets: async (): Promise<string> => {
    const response = await api.post('/tickets/seed');
    return response.data;
  },

  // Book a ticket
  bookTicket: async (ticketId: number, userId: number): Promise<Ticket> => {
    const response = await api.post(`/tickets/book?ticketId=${ticketId}&userId=${userId}`);
    return response.data;
  },

  // Simulate load test (client-side)
  simulateLoadTest: async (
    ticketId: number, 
    numberOfUsers: number,
    onProgress?: (progress: number) => void
  ): Promise<LoadTestResult> => {
    const startTime = Date.now();
    let successCount = 0;
    let conflictCount = 0;
    let otherErrors = 0;
    let winnerUserId: number | undefined;
    let completed = 0;

    const promises = Array.from({ length: numberOfUsers }, (_, i) => {
      const userId = 1000 + i;
      return api.post(`/tickets/book?ticketId=${ticketId}&userId=${userId}`)
        .then((response) => {
          successCount++;
          winnerUserId = response.data.userId;
          return { success: true };
        })
        .catch((error) => {
          if (error.response?.status === 409) {
            conflictCount++;
          } else {
            otherErrors++;
          }
          return { success: false };
        })
        .finally(() => {
          completed++;
          if (onProgress) {
            onProgress((completed / numberOfUsers) * 100);
          }
        });
    });

    await Promise.all(promises);

    const duration = Date.now() - startTime;

    return {
      totalRequests: numberOfUsers,
      successCount,
      conflictCount,
      otherErrors,
      duration,
      winnerUserId,
    };
  },
};

export default api;
