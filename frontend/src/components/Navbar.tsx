import React from 'react';
import { TicketIcon } from '@heroicons/react/24/solid';

const Navbar: React.FC = () => {
  return (
    <nav className="bg-gradient-to-r from-blue-600 to-blue-800 text-white shadow-lg">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <TicketIcon className="w-8 h-8" />
            <div>
              <h1 className="text-2xl font-bold">FlashTix</h1>
              <p className="text-sm text-blue-200">High-Concurrency Ticket Booking System</p>
            </div>
          </div>
          <div className="hidden md:flex items-center space-x-6">
            <a 
              href="http://localhost:9090" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-blue-200 transition text-sm"
            >
              📊 Prometheus
            </a>
            <a 
              href="http://localhost:3000" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-blue-200 transition text-sm"
            >
              📈 Grafana
            </a>
            <a 
              href="http://localhost:8080/swagger-ui.html" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-blue-200 transition text-sm"
            >
              📖 API Docs
            </a>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
