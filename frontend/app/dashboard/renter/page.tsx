'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { Car, Clock, ShieldCheck, Camera, AlertTriangle } from 'lucide-react'
import { api } from '@/lib/api'

export default function RenterDashboard() {
  const [bookings, setBookings] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchBookings()
  }, [])

  const fetchBookings = async () => {
    try {
      const res = await api.get('/bookings/')
      setBookings(res.data)
    } catch (err) {
      console.error('Failed to load renter bookings', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center border-b border-gray-200 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Renter Dashboard</h1>
          <p className="text-xs text-gray-500">Manage your active car rentals, check-ins, and dispute evidence</p>
        </div>
        <Link href="/" className="bg-indigo-600 text-white text-xs font-semibold px-4 py-2 rounded-lg hover:bg-indigo-700 transition">
          Browse Vehicles
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">Loading your bookings...</div>
      ) : bookings.length === 0 ? (
        <div className="bg-white rounded-xl p-12 text-center border border-gray-200 text-gray-500 space-y-3">
          <Car className="w-12 h-12 text-gray-400 mx-auto" />
          <p className="text-lg font-medium">No bookings found.</p>
          <p className="text-sm text-gray-400">Explore verified listings and request your first rental.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <div key={b.id} className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-lg text-gray-900">Booking #{b.id}</span>
                  <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full uppercase ${
                    b.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-800' :
                    b.status === 'ACTIVE' ? 'bg-blue-100 text-blue-800' :
                    b.status === 'PENDING' ? 'bg-amber-100 text-amber-800' : 'bg-gray-100 text-gray-800'
                  }`}>
                    {b.status}
                  </span>
                </div>

                <p className="text-sm text-gray-700 font-semibold">
                  {b.vehicle_details?.year} {b.vehicle_details?.make} {b.vehicle_details?.model} ({b.vehicle_details?.plate_number})
                </p>

                <p className="text-xs text-gray-500 flex items-center gap-2">
                  <Clock className="w-3.5 h-3.5" /> Dates: {b.start_date} to {b.end_date} | Total Amount: ${b.total_amount}
                </p>
              </div>

              <div className="flex items-center gap-3 w-full md:w-auto">
                <Link
                  href={`/checkin/${b.id}`}
                  className="flex-1 md:flex-initial bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-4 py-2.5 rounded-lg transition flex items-center justify-center gap-1.5"
                >
                  <Camera className="w-4 h-4" /> Inspection Check-In
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
