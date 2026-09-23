import './globals.css'
import Link from 'next/link'
import { ShieldCheck, Car, User, LogIn } from 'lucide-react'

export const metadata = {
  title: 'RentalWheels | Peer-to-Peer Car Rental Security Platform',
  description: 'Verified, fraud-protected peer-to-peer car rentals with AI identity verification and ANPR plate checks.',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col justify-between">
        <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <Link href="/" className="flex items-center gap-2 text-xl font-bold text-indigo-600">
              <Car className="w-7 h-7" />
              <span>RentalWheels</span>
              <span className="bg-indigo-100 text-indigo-800 text-xs px-2 py-0.5 rounded-full flex items-center gap-1 font-normal">
                <ShieldCheck className="w-3.5 h-3.5" /> Verified
              </span>
            </Link>

            <nav className="flex items-center gap-6 text-sm font-medium text-gray-700">
              <Link href="/" className="hover:text-indigo-600 transition">Explore Vehicles</Link>
              <Link href="/dashboard/owner" className="hover:text-indigo-600 transition">List Your Car</Link>
              <Link href="/dashboard/renter" className="hover:text-indigo-600 transition">My Bookings</Link>
              <Link href="/verify-id" className="hover:text-indigo-600 transition flex items-center gap-1 text-emerald-600 font-semibold">
                <ShieldCheck className="w-4 h-4" /> Verify ID
              </Link>
              <Link href="/login" className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition flex items-center gap-1">
                <LogIn className="w-4 h-4" /> Login
              </Link>
            </nav>
          </div>
        </header>

        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </main>

        <footer className="bg-gray-900 text-gray-400 py-8 border-t border-gray-800 text-center text-sm">
          <div className="max-w-7xl mx-auto px-4">
            <p>© {new Date().getFullYear()} RentalWheels Security Platform. All rights reserved.</p>
            <p className="mt-1 text-gray-500">Protected by AI Face Matching, ANPR Plate Verification, and Escrow Authorizations.</p>
          </div>
        </footer>
      </body>
    </html>
  )
}
