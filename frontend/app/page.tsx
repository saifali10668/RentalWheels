'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { ShieldCheck, Search, MapPin, DollarSign, Calendar, Car } from 'lucide-react'
import { api } from '@/lib/api'

interface Vehicle {
  id: number
  make: string
  model: string
  year: number
  plate_number: string
  is_verified: boolean
  price_per_day: string
  location: string
  status: string
  owner_username: string
  owner_verified: boolean
  vehicle_photo: string | null
}

export default function HomePage() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([])
  const [loading, setLoading] = useState(true)
  const [locationFilter, setLocationFilter] = useState('')
  const [makeFilter, setMakeFilter] = useState('')

  useEffect(() => {
    fetchVehicles()
  }, [])

  const fetchVehicles = async () => {
    try {
      const res = await api.get('/vehicles/')
      setVehicles(res.data)
    } catch (err) {
      console.error('Failed to load vehicles', err)
    } finally {
      setLoading(false)
    }
  }

  const filteredVehicles = vehicles.filter((v) => {
    const matchesLoc = v.location.toLowerCase().includes(locationFilter.toLowerCase())
    const matchesMake = v.make.toLowerCase().includes(makeFilter.toLowerCase())
    return matchesLoc && matchesMake && v.status === 'AVAILABLE'
  })

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-indigo-900 via-indigo-800 to-indigo-900 text-white rounded-2xl p-8 sm:p-12 shadow-xl">
        <div className="max-w-3xl space-y-4">
          <span className="bg-indigo-700/60 text-indigo-200 text-xs font-semibold px-3 py-1 rounded-full border border-indigo-500/40 inline-flex items-center gap-1">
            <ShieldCheck className="w-4 h-4 text-emerald-400" /> Fraud-Protected Car Rentals
          </span>
          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight">
            Rent Cars with Total Peace of Mind
          </h1>
          <p className="text-indigo-200 text-base sm:text-lg">
            Every host identity is verified via AI face-matching, and every vehicle plate is cross-checked against stolen registries.
          </p>

          {/* Search Bar */}
          <div className="bg-white text-gray-900 p-3 rounded-xl shadow-lg flex flex-col sm:flex-row gap-3 mt-6">
            <div className="flex-1 flex items-center gap-2 px-3 border-b sm:border-b-0 sm:border-r border-gray-200 py-2 sm:py-0">
              <MapPin className="w-5 h-5 text-indigo-600 shrink-0" />
              <input
                type="text"
                placeholder="City or Location (e.g. Lahore, Karachi)"
                className="w-full focus:outline-none text-sm"
                value={locationFilter}
                onChange={(e) => setLocationFilter(e.target.value)}
              />
            </div>

            <div className="flex-1 flex items-center gap-2 px-3 py-2 sm:py-0">
              <Car className="w-5 h-5 text-indigo-600 shrink-0" />
              <input
                type="text"
                placeholder="Make or Model (e.g. Civic, Corolla)"
                className="w-full focus:outline-none text-sm"
                value={makeFilter}
                onChange={(e) => setMakeFilter(e.target.value)}
              />
            </div>

            <button
              onClick={fetchVehicles}
              className="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-6 py-3 rounded-lg flex items-center justify-center gap-2 transition"
            >
              <Search className="w-4 h-4" /> Search
            </button>
          </div>
        </div>
      </section>

      {/* Listings Grid */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold text-gray-900">Available Vehicles</h2>
          <span className="text-sm text-gray-500">{filteredVehicles.length} vehicles found</span>
        </div>

        {loading ? (
          <div className="text-center py-12 text-gray-500">Loading verified vehicles...</div>
        ) : filteredVehicles.length === 0 ? (
          <div className="bg-white rounded-xl p-12 text-center border border-gray-200 text-gray-500 space-y-3">
            <Car className="w-12 h-12 text-gray-400 mx-auto" />
            <p className="text-lg font-medium">No vehicles matched your search.</p>
            <p className="text-sm text-gray-400">Try clearing your search filters or exploring another city.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredVehicles.map((vehicle) => (
              <div key={vehicle.id} className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm hover:shadow-md transition flex flex-col justify-between">
                <div>
                  <div className="relative h-48 bg-gray-100 flex items-center justify-center overflow-hidden">
                    {vehicle.vehicle_photo ? (
                      <img
                        src={vehicle.vehicle_photo}
                        alt={`${vehicle.make} ${vehicle.model}`}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <Car className="w-16 h-16 text-gray-300" />
                    )}

                    {/* Verified Badges */}
                    <div className="absolute top-3 left-3 flex gap-2">
                      {vehicle.is_verified && (
                        <span className="bg-emerald-600 text-white text-xs font-semibold px-2.5 py-1 rounded-md flex items-center gap-1 shadow">
                          <ShieldCheck className="w-3.5 h-3.5" /> Vehicle Verified
                        </span>
                      )}
                      {vehicle.owner_verified && (
                        <span className="bg-blue-600 text-white text-xs font-semibold px-2.5 py-1 rounded-md flex items-center gap-1 shadow">
                          <ShieldCheck className="w-3.5 h-3.5" /> Owner ID Verified
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="p-5 space-y-3">
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="text-lg font-bold text-gray-900">
                          {vehicle.year} {vehicle.make} {vehicle.model}
                        </h3>
                        <p className="text-xs text-gray-500 flex items-center gap-1 mt-1">
                          <MapPin className="w-3.5 h-3.5 text-gray-400" /> {vehicle.location}
                        </p>
                      </div>
                      <div className="text-right">
                        <span className="text-xl font-extrabold text-indigo-600">${vehicle.price_per_day}</span>
                        <span className="text-xs text-gray-500 block">/ day</span>
                      </div>
                    </div>

                    <div className="pt-2 border-t border-gray-100 text-xs text-gray-500 flex justify-between items-center">
                      <span>Hosted by <strong className="text-gray-700">{vehicle.owner_username}</strong></span>
                      <span className="font-mono uppercase bg-gray-100 px-2 py-0.5 rounded text-gray-600">{vehicle.plate_number}</span>
                    </div>
                  </div>
                </div>

                <div className="p-5 pt-0">
                  <Link
                    href={`/vehicles/${vehicle.id}`}
                    className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 rounded-lg flex items-center justify-center transition text-sm"
                  >
                    View Listing & Book
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
