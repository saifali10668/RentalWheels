'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { ShieldCheck, Upload, AlertCircle, CheckCircle2 } from 'lucide-react'
import { api } from '@/lib/api'

export default function VerifyIDPage() {
  const router = useRouter()
  const [cnic, setCnic] = useState('')
  const [license, setLicense] = useState('')
  const [idDoc, setIdDoc] = useState<File | null>(null)
  const [selfie, setSelfie] = useState<File | null>(null)

  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<any>(null)
  const [error, setError] = useState('')

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!idDoc || !selfie) {
      setError('Please upload both your ID Document and a live Selfie.')
      return
    }

    setLoading(true)
    setError('')
    setResult(null)

    const formData = new FormData()
    formData.append('cnic_number', cnic)
    formData.append('license_number', license)
    formData.append('id_document', idDoc)
    formData.append('selfie_photo', selfie)

    try {
      const res = await api.post('/auth/verify-id/', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setResult(res.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Verification service failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto my-8 bg-white rounded-2xl border border-gray-200 p-8 shadow-sm space-y-6">
      <div className="flex items-center gap-3 border-b border-gray-100 pb-4">
        <div className="w-10 h-10 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-gray-900">Identity & Face Match Verification</h1>
          <p className="text-xs text-gray-500">Instant AI comparison between official ID (CNIC/DL) and your selfie</p>
        </div>
      </div>

      {result && (
        <div className={`p-4 rounded-xl border flex items-start gap-3 ${
          result.is_id_verified ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-amber-50 border-amber-200 text-amber-800'
        }`}>
          <CheckCircle2 className="w-5 h-5 shrink-0 mt-0.5" />
          <div className="space-y-1 text-sm">
            <p className="font-bold">{result.message}</p>
            <p>Verification Status: <span className="font-semibold uppercase">{result.verification_status}</span></p>
            {result.verification_score && (
              <p className="text-xs">AI Match Score: {(result.verification_score * 100).toFixed(1)}%</p>
            )}
          </div>
        </div>
      )}

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg text-sm flex items-center gap-2 border border-red-200">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
        </div>
      )}

      <form onSubmit={handleVerify} className="space-y-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">CNIC / National ID</label>
            <input
              type="text"
              required
              placeholder="35201-XXXXXXX-X"
              value={cnic}
              onChange={(e) => setCnic(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Driver&apos;s License No.</label>
            <input
              type="text"
              required
              placeholder="DL-XXXXXX"
              value={license}
              onChange={(e) => setLicense(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
            />
          </div>
        </div>

        {/* Upload ID Document */}
        <div className="border-2 border-dashed border-gray-300 rounded-xl p-4 text-center hover:border-indigo-500 transition cursor-pointer">
          <input
            type="file"
            id="idDocInput"
            accept="image/*"
            className="hidden"
            onChange={(e) => setIdDoc(e.target.files?.[0] || null)}
          />
          <label htmlFor="idDocInput" className="cursor-pointer space-y-1 block">
            <Upload className="w-6 h-6 text-gray-400 mx-auto" />
            <p className="text-sm font-medium text-gray-700">
              {idDoc ? idDoc.name : 'Upload CNIC / Driver License Photo'}
            </p>
            <p className="text-xs text-gray-400">Ensure text and face photo on ID are clearly visible</p>
          </label>
        </div>

        {/* Upload Selfie */}
        <div className="border-2 border-dashed border-gray-300 rounded-xl p-4 text-center hover:border-indigo-500 transition cursor-pointer">
          <input
            type="file"
            id="selfieInput"
            accept="image/*"
            className="hidden"
            onChange={(e) => setSelfie(e.target.files?.[0] || null)}
          />
          <label htmlFor="selfieInput" className="cursor-pointer space-y-1 block">
            <Upload className="w-6 h-6 text-gray-400 mx-auto" />
            <p className="text-sm font-medium text-gray-700">
              {selfie ? selfie.name : 'Upload Live Selfie Photo'}
            </p>
            <p className="text-xs text-gray-400">Front-facing photo in good lighting with neutral expression</p>
          </label>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition text-sm flex items-center justify-center gap-2"
        >
          <ShieldCheck className="w-4 h-4" />
          {loading ? 'Running AI Face Matching...' : 'Submit ID for Verification'}
        </button>
      </form>
    </div>
  )
}
