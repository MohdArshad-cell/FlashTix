import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { ticketAPI } from '../services/api';
import toast from 'react-hot-toast';
import { LoadTestResult } from '../types';

const LoadTestPanel: React.FC = () => {
  const [ticketId, setTicketId] = useState<string>('1');
  const [numberOfUsers, setNumberOfUsers] = useState<string>('10');
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<LoadTestResult | null>(null);

  const handleLoadTest = async () => {
    const ticketIdNum = parseInt(ticketId);
    const usersNum = parseInt(numberOfUsers);

    if (!ticketIdNum || !usersNum || usersNum < 1 || usersNum > 1000) {
      toast.error('Please enter valid values (1-1000 users)');
      return;
    }

    setLoading(true);
    setProgress(0);
    setResult(null);

    try {
      const testResult = await ticketAPI.simulateLoadTest(
        ticketIdNum,
        usersNum,
        (prog) => setProgress(prog)
      );
      
      setResult(testResult);
      
      if (testResult.successCount === 1) {
        toast.success(`✅ Test Complete! Winner: User ${testResult.winnerUserId}`);
      } else if (testResult.successCount === 0) {
        toast.error('❌ All requests failed or ticket already sold');
      } else {
        toast.success(`✅ Test Complete! ${testResult.successCount} succeeded`);
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Load test failed');
    } finally {
      setLoading(false);
      setProgress(0);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">🚀 Load Test Simulator</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Ticket ID
          </label>
          <input
            type="number"
            value={ticketId}
            onChange={(e) => setTicketId(e.target.value)}
            className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none"
            min="1"
            disabled={loading}
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Number of Concurrent Users
          </label>
          <input
            type="number"
            value={numberOfUsers}
            onChange={(e) => setNumberOfUsers(e.target.value)}
            className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none"
            min="1"
            max="1000"
            disabled={loading}
          />
        </div>
      </div>

      {loading && (
        <div className="mb-6">
          <div className="flex justify-between text-sm text-gray-600 mb-2">
            <span>Progress</span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
            <motion.div
              className="bg-blue-600 h-full"
              initial={{ width: 0 }}
              animate={{ width: `${progress}%` }}
              transition={{ duration: 0.3 }}
            />
          </div>
        </div>
      )}

      <button
        onClick={handleLoadTest}
        disabled={loading}
        className="w-full bg-gradient-to-r from-purple-600 to-blue-600 text-white py-3 rounded-lg font-bold text-lg hover:from-purple-700 hover:to-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {loading ? 'Running Test...' : '⚡ Start Load Test'}
      </button>

      {result && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mt-6 bg-gray-50 rounded-lg p-4"
        >
          <h3 className="font-bold text-lg text-gray-800 mb-3">📊 Test Results</h3>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="bg-white p-3 rounded">
              <p className="text-gray-600">Total Requests</p>
              <p className="text-2xl font-bold text-gray-800">{result.totalRequests}</p>
            </div>
            <div className="bg-white p-3 rounded">
              <p className="text-gray-600">Duration</p>
              <p className="text-2xl font-bold text-blue-600">{result.duration}ms</p>
            </div>
            <div className="bg-white p-3 rounded">
              <p className="text-gray-600">✅ Success</p>
              <p className="text-2xl font-bold text-green-600">{result.successCount}</p>
            </div>
            <div className="bg-white p-3 rounded">
              <p className="text-gray-600">⚠️ Conflicts</p>
              <p className="text-2xl font-bold text-yellow-600">{result.conflictCount}</p>
            </div>
            <div className="bg-white p-3 rounded col-span-2">
              <p className="text-gray-600">🏆 Winner</p>
              <p className="text-2xl font-bold text-purple-600">
                {result.winnerUserId ? `User ${result.winnerUserId}` : 'No Winner'}
              </p>
            </div>
          </div>
        </motion.div>
      )}

      <div className="mt-4 p-3 bg-blue-50 rounded-lg">
        <p className="text-sm text-blue-800">
          💡 <strong>Tip:</strong> This simulates multiple users trying to book the same ticket simultaneously. 
          Only one should succeed due to optimistic locking!
        </p>
      </div>
    </div>
  );
};

export default LoadTestPanel;
