'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { ShieldCheck, MapPin, Calendar, Car, AlertCircle, CheckCircle } from 'lucide-react'
import { api } from '@/lib/api'

export default function VehicleDetailPage() {
  const params = useParams()
  const router = useRouter()
  const id = params.id

  const [vehicle, setVehicle] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [bookingMsg, setBookingMsg] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (id) fetchVehicle()
  }, [id])

  const fetchVehicle = async () => {
    try {
      const res = await api.get(`/vehicles/${id}/`)
      setVehicle(res.data)
    } catch (err) {
      console.error('Failed to load vehicle details', err)
    } finally {
      setLoading(false)
    }
  }

  const handleBooking = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    setBookingMsg('')

    try {
      const days = Math.max(1, Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 3600 * 24)))
      const total = (parseFloat(vehicle.price_per_day) * days).toFixed(2)

      const res = await api.post('/bookings/', {
        vehicle: vehicle.id,
        start_date: startDate,
        end_date: endDate,
        total_amount: total,
      })

      setBookingMsg('Booking request submitted! Awaiting owner approval.')
      setTimeout(() => router.push('/dashboard/renter'), 2000)
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Failed to submit booking request. Please check login status.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="text-center py-12 text-gray-500">Loading vehicle details...</div>
  if (!vehicle) return <div className="text-center py-12 text-gray-500">Vehicle not found.</div>

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
      {/* Left Column: Vehicle Photos & Specs */}
      <div className="lg:col-span-2 space-y-6">
        <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-sm">
          <div className="h-80 bg-gray-100 relative flex items-center justify-center">
            {vehicle.vehicle_photo ? (
              <img src={vehicle.vehicle_photo} alt={`${vehicle.make} ${vehicle.model}`} className="w-full h-full object-cover" />
            ) : (
              <Car className="w-24 h-24 text-gray-300" />
            )}

            <div className="absolute top-4 left-4 flex gap-2">
              {vehicle.is_verified && (
                <span className="bg-emerald-600 text-white text-xs font-semibold px-3 py-1 rounded-md flex items-center gap-1 shadow">
                  <ShieldCheck className="w-4 h-4" /> Vehicle Verified
                </span>
              )}
            </div>
          </div>

          <div className="p-6 space-y-4">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-3xl font-extrabold text-gray-900">
                  {vehicle.year} {vehicle.make} {vehicle.model}
                </h1>
                <p className="text-sm text-gray-500 flex items-center gap-1 mt-1">
                  <MapPin className="w-4 h-4 text-gray-400" /> {vehicle.location}
                </p>
              </div>
              <div className="text-right">
                <span className="text-3xl font-extrabold text-indigo-600">${vehicle.price_per_day}</span>
                <span className="text-xs text-gray-500 block">per day</span>
              </div>
            </div>

            <div className="border-t border-gray-100 pt-4 grid grid-cols-2 sm:grid-cols-3 gap-4 text-sm">
              <div className="bg-gray-50 p-3 rounded-lg">
                <span className="text-xs text-gray-400 block">Registration Plate</span>
                <span className="font-mono font-bold text-gray-800 uppercase">{vehicle.plate_number}</span>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <span className="text-xs text-gray-400 block">Host Name</span>
                <span className="font-semibold text-gray-800">{vehicle.owner_username}</span>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <span className="text-xs text-gray-400 block">Host Identity</span>
                <span className={`font-semibold text-xs inline-block mt-0.5 px-2 py-0.5 rounded ${
                  vehicle.owner_verified ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-200 text-gray-700'
                }`}>
                  {vehicle.owner_verified ? 'ID Verified' : 'Unverified Host'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Column: Escrow Booking Form */}
      <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm h-fit space-y-6">
        <div>
          <h2 className="text-lg font-bold text-gray-900">Book this Vehicle</h2>
          <p className="text-xs text-gray-500">Escrow-style authorization hold on card</p>
        </div>

        {bookingMsg && (
          <div className="bg-emerald-50 text-emerald-800 p-3 rounded-lg text-sm flex items-center gap-2 border border-emerald-200">
            <CheckCircle className="w-4 h-4 shrink-0" /> {bookingMsg}
          </div>
        )}

        {error && (
          <div className="bg-red-50 text-red-700 p-3 rounded-lg text-sm flex items-center gap-2 border border-red-200">
            <AlertCircle className="w-4 h-4 shrink-0" /> {error}
          </div>
        )}

        <form onSubmit={handleBooking} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Start Date</label>
            <input
              type="date"
              required
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">End Date</label>
            <input
              type="date"
              required
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
            />
          </div>

          <div className="bg-indigo-50/60 p-4 rounded-xl space-y-2 border border-indigo-100 text-sm">
            <div className="flex justify-between text-gray-600">
              <span>Rate per day:</span>
              <span>${vehicle.price_per_day}</span>
            </div>
            <div className="flex justify-between font-bold text-gray-900 border-t border-indigo-100 pt-2">
              <span>Payment Deposit Hold:</span>
              <span>
                ${startDate && endDate ? (parseFloat(vehicle.price_per_day) * Math.max(1, Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 3600 * 24)))).toFixed(2) : vehicle.price_per_day}
              </span>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition text-sm flex items-center justify-center gap-2"
          >
            <Calendar className="w-4 h-4" />
            {submitting ? 'Submitting Request...' : 'Request Booking'}
          </button>
        </form>
      </div>
    </div>
  )
}
