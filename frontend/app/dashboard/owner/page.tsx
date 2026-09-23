'use client'

import { useState, useEffect } from 'react'
import { Car, ShieldCheck, CheckCircle2, Plus, AlertCircle } from 'lucide-react'
import { api } from '@/lib/api'

export default function OwnerDashboard() {
  const [vehicles, setVehicles] = useState<any[]>([])
  const [bookings, setBookings] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  // Form state for creating a new vehicle listing
  const [make, setMake] = useState('')
  const [model, setModel] = useState('')
  const [year, setYear] = useState('2022')
  const [plate, setPlate] = useState('')
  const [price, setPrice] = useState('50')
  const [location, setLocation] = useState('')
  const [photo, setPhoto] = useState<File | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [msg, setMsg] = useState('')

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [vRes, bRes] = await Promise.all([
        api.get('/vehicles/'),
        api.get('/bookings/'),
      ])
      setVehicles(vRes.data)
      setBookings(bRes.data)
    } catch (err) {
      console.error('Failed to load owner data', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCreateVehicle = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setMsg('')

    const formData = new FormData()
    formData.append('make', make)
    formData.append('model', model)
    formData.append('year', year)
    formData.append('plate_number', plate)
    formData.append('price_per_day', price)
    formData.append('location', location)
    if (photo) formData.append('vehicle_photo', photo)

    try {
      await api.post('/vehicles/', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setMsg('Vehicle listing created! ANPR plate check performed.')
      fetchData()
    } catch (err: any) {
      alert('Failed to list vehicle. Ensure plate number is unique.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleApprove = async (bookingId: number) => {
    try {
      await api.patch(`/bookings/${bookingId}/approve/`)
      fetchData()
    } catch (err) {
      alert('Failed to approve booking.')
    }
  }

  return (
    <div className="space-y-8">
      <div className="border-b border-gray-200 pb-4">
        <h1 className="text-2xl font-bold text-gray-900">Vehicle Owner (Host) Dashboard</h1>
        <p className="text-xs text-gray-500">Manage vehicle listings, approve bookings, and review automated ANPR security checks</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Cols: Listed Vehicles & Booking Requests */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
            <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
              <Car className="w-5 h-5 text-indigo-600" /> Listed Vehicles
            </h2>

            {vehicles.length === 0 ? (
              <p className="text-sm text-gray-500 py-4">No vehicles listed yet. Fill out the form on the right to list your car.</p>
            ) : (
              <div className="space-y-3">
                {vehicles.map((v) => (
                  <div key={v.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center">
                    <div>
                      <h3 className="font-bold text-gray-900">{v.year} {v.make} {v.model}</h3>
                      <p className="text-xs text-gray-500">Plate: <span className="font-mono uppercase font-semibold">{v.plate_number}</span> | ${v.price_per_day}/day</p>
                    </div>

                    <div className="flex gap-2 text-xs">
                      {v.is_flagged_stolen ? (
                        <span className="bg-red-100 text-red-800 px-2.5 py-1 rounded font-semibold flex items-center gap-1">
                          <AlertCircle className="w-3.5 h-3.5" /> Flagged Stolen
                        </span>
                      ) : v.is_verified ? (
                        <span className="bg-emerald-100 text-emerald-800 px-2.5 py-1 rounded font-semibold flex items-center gap-1">
                          <ShieldCheck className="w-3.5 h-3.5" /> Plate Verified
                        </span>
                      ) : (
                        <span className="bg-amber-100 text-amber-800 px-2.5 py-1 rounded font-semibold">
                          Pending Review
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
            <h2 className="text-lg font-bold text-gray-900">Incoming Booking Requests</h2>
            {bookings.length === 0 ? (
              <p className="text-sm text-gray-500 py-4">No booking requests found.</p>
            ) : (
              <div className="space-y-3">
                {bookings.map((b) => (
                  <div key={b.id} className="border border-gray-200 rounded-xl p-4 flex justify-between items-center">
                    <div className="space-y-1">
                      <p className="text-sm font-bold text-gray-900">
                        Renter: {b.renter_username} {b.renter_verified && <span className="text-xs text-emerald-600 font-normal">(ID Verified)</span>}
                      </p>
                      <p className="text-xs text-gray-500">Dates: {b.start_date} to {b.end_date} | Total: ${b.total_amount}</p>
                    </div>

                    {b.status === 'PENDING' ? (
                      <button
                        onClick={() => handleApprove(b.id)}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold px-4 py-2 rounded-lg transition"
                      >
                        Approve Booking
                      </button>
                    ) : (
                      <span className="text-xs font-semibold uppercase px-2.5 py-1 bg-gray-100 rounded text-gray-700">
                        {b.status}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Add Vehicle Form */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm h-fit space-y-4">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <Plus className="w-5 h-5 text-indigo-600" /> List a New Vehicle
          </h2>

          {msg && <p className="text-xs text-emerald-700 bg-emerald-50 p-2.5 rounded-lg border border-emerald-200">{msg}</p>}

          <form onSubmit={handleCreateVehicle} className="space-y-3 text-sm">
            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Make</label>
              <input type="text" required placeholder="Honda" value={make} onChange={(e) => setMake(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Model</label>
              <input type="text" required placeholder="Civic" value={model} onChange={(e) => setModel(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Year</label>
                <input type="number" required value={year} onChange={(e) => setYear(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Plate No.</label>
                <input type="text" required placeholder="LEB1234" value={plate} onChange={(e) => setPlate(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500 font-mono" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Price per Day ($)</label>
              <input type="number" required value={price} onChange={(e) => setPrice(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">City / Location</label>
              <input type="text" required placeholder="Lahore Gulberg" value={location} onChange={(e) => setLocation(e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Vehicle Photo</label>
              <input type="file" accept="image/*" onChange={(e) => setPhoto(e.target.files?.[0] || null)} className="w-full text-xs text-gray-500" />
            </div>

            <button type="submit" disabled={submitting} className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 rounded-lg transition text-sm">
              {submitting ? 'Submitting & Running ANPR...' : 'Publish Vehicle Listing'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
