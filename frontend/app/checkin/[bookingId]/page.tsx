'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { Camera, MapPin, ShieldCheck, CheckCircle2, AlertCircle } from 'lucide-react'
import { api } from '@/lib/api'

export default function CheckinPage() {
  const params = useParams()
  const router = useRouter()
  const bookingId = params.bookingId

  const [checkinType, setCheckinType] = useState('PICKUP')
  const [odometer, setOdometer] = useState('')
  const [lat, setLat] = useState<number | null>(null)
  const [lng, setLng] = useState<number | null>(null)

  const [frontPhoto, setFrontPhoto] = useState<File | null>(null)
  const [rearPhoto, setRearPhoto] = useState<File | null>(null)
  const [leftPhoto, setLeftPhoto] = useState<File | null>(null)
  const [rightPhoto, setRightPhoto] = useState<File | null>(null)

  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    // Capture GPS Geolocation on component load
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLat(pos.coords.latitude)
          setLng(pos.coords.longitude)
        },
        (err) => console.warn('Geolocation access denied or unavailable', err)
      )
    }
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!frontPhoto || !rearPhoto || !leftPhoto || !rightPhoto) {
      setError('Please upload all 4 inspection photos (Front, Rear, Left, Right).')
      return
    }

    setLoading(true)
    setError('')

    const formData = new FormData()
    formData.append('booking', bookingId as string)
    formData.append('checkin_type', checkinType)
    formData.append('odometer_reading', odometer)
    formData.append('gps_latitude', lat ? lat.toFixed(6) : '31.520400')
    formData.append('gps_longitude', lng ? lng.toFixed(6) : '74.358700')

    formData.append('front_photo', frontPhoto)
    formData.append('rear_photo', rearPhoto)
    formData.append('left_photo', leftPhoto)
    formData.append('right_photo', rightPhoto)

    try {
      await api.post('/checkins/', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setMsg(`${checkinType} inspection record saved successfully!`)
      setTimeout(() => router.push('/dashboard/renter'), 2000)
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Failed to submit inspection record.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto my-8 bg-white rounded-2xl border border-gray-200 p-8 shadow-sm space-y-6">
      <div className="flex items-center gap-3 border-b border-gray-100 pb-4">
        <div className="w-10 h-10 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center">
          <Camera className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-gray-900">Vehicle Inspection Check-In</h1>
          <p className="text-xs text-gray-500">Capture timestamped 4-sided photos & GPS location for Booking #{bookingId}</p>
        </div>
      </div>

      {msg && (
        <div className="bg-emerald-50 text-emerald-800 p-3 rounded-lg text-sm flex items-center gap-2 border border-emerald-200">
          <CheckCircle2 className="w-5 h-5 shrink-0" /> {msg}
        </div>
      )}

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg text-sm flex items-center gap-2 border border-red-200">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Inspection Type</label>
            <select
              value={checkinType}
              onChange={(e) => setCheckinType(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 bg-white"
            >
              <option value="PICKUP">Pickup Handover</option>
              <option value="DROPOFF">Drop-off Handover</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Odometer Reading (KM)</label>
            <input
              type="number"
              required
              placeholder="e.g. 45200"
              value={odometer}
              onChange={(e) => setOdometer(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        {/* GPS Coordinates info badge */}
        <div className="bg-gray-50 p-3 rounded-xl border border-gray-200 flex items-center gap-2 text-xs text-gray-600">
          <MapPin className="w-4 h-4 text-indigo-600 shrink-0" />
          <span>
            Captured GPS Location:{' '}
            <strong className="text-gray-900 font-mono">
              {lat && lng ? `${lat.toFixed(4)}, ${lng.toFixed(4)}` : 'Detecting GPS coordinates...'}
            </strong>
          </span>
        </div>

        {/* 4 Photo Uploads Grid */}
        <div className="grid grid-cols-2 gap-4 text-xs">
          <div className="border border-gray-200 p-3 rounded-xl space-y-1">
            <span className="font-semibold text-gray-700 block">Front View Photo</span>
            <input type="file" accept="image/*" onChange={(e) => setFrontPhoto(e.target.files?.[0] || null)} className="w-full text-gray-500" />
          </div>

          <div className="border border-gray-200 p-3 rounded-xl space-y-1">
            <span className="font-semibold text-gray-700 block">Rear View Photo</span>
            <input type="file" accept="image/*" onChange={(e) => setRearPhoto(e.target.files?.[0] || null)} className="w-full text-gray-500" />
          </div>

          <div className="border border-gray-200 p-3 rounded-xl space-y-1">
            <span className="font-semibold text-gray-700 block">Left Side Photo</span>
            <input type="file" accept="image/*" onChange={(e) => setLeftPhoto(e.target.files?.[0] || null)} className="w-full text-gray-500" />
          </div>

          <div className="border border-gray-200 p-3 rounded-xl space-y-1">
            <span className="font-semibold text-gray-700 block">Right Side Photo</span>
            <input type="file" accept="image/*" onChange={(e) => setRightPhoto(e.target.files?.[0] || null)} className="w-full text-gray-500" />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition text-sm flex items-center justify-center gap-2"
        >
          <Camera className="w-4 h-4" />
          {loading ? 'Saving Inspection Record...' : 'Submit Inspection Record'}
        </button>
      </form>
    </div>
  )
}
