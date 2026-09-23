# 🚗 RentalWheels — Fraud-Protected P2P Car Rental System

RentalWheels is a full-stack peer-to-peer vehicle rental application built with modern native Android (Jetpack Compose), Django REST Framework backend, Next.js web frontend, and AI-powered identity and license plate verification.

## ✨ Key Features

- **📱 Android App (Jetpack Compose + Material 3):**
  - Explore & search vehicles by city and make/model.
  - Request bookings, manage rental dates, and monitor escrow payment status.
  - Host vehicle listing with photo upload for automated ANPR plate checks.
  - Digital Pick-Up & Return inspection check-ins with 4-side photo attachments and GPS coordinates.
  - AI Identity Verification with document scanning and selfie biometric face matching.
  - Dispute resolution module.

- **🐍 Django REST API Backend:**
  - JWT Authentication (SimpleJWT) with refresh tokens.
  - Role-based access control (`RENTER`, `OWNER`, `BOTH`).
  - Vehicle, Booking, Inspection, and Dispute management endpoints.
  - Auto-generated migrations & SQLite/PostgreSQL support.

- **🌐 Next.js Web Frontend:**
  - TypeScript, Tailwind CSS, and responsive dashboard for renters and hosts.
