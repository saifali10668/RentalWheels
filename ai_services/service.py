"""
FastAPI Microservice for RentalWheels AI Security Pipelines.
Exposes REST endpoints for ID-vs-Selfie Face Verification and ANPR Vehicle Plate Cross-Checking.
"""

import logging
from fastapi import FastAPI, File, UploadFile, Form, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional

from face_verifier import FaceVerificationPipeline
from anpr_verifier import ANPRVerificationPipeline

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("rentalwheels_ai_service")

app = FastAPI(
    title="RentalWheels AI Security Service",
    description="Microservice providing Face Matching (CNIC/DL vs Selfie) and ANPR (Stolen Plate Checking)",
    version="1.0.0"
)

# Enable CORS for local development (Django DRF / Next.js / Mobile app)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global pipeline instances
face_pipeline = None
anpr_pipeline = None

@app.on_event("startup")
async def startup_event():
    """Initialize AI pipelines on server startup."""
    global face_pipeline, anpr_pipeline
    logger.info("Initializing AI Pipelines...")
    face_pipeline = FaceVerificationPipeline(match_threshold=0.65)
    anpr_pipeline = ANPRVerificationPipeline()
    logger.info("AI Service successfully initialized!")

@app.get("/health", tags=["Health Check"])
async def health_check():
    """Health check endpoint to verify AI pipelines status."""
    return {
        "status": "online",
        "service": "RentalWheels AI Verification Service",
        "pipelines": {
            "face_verification": face_pipeline is not None,
            "anpr": anpr_pipeline is not None
        }
    }

@app.post("/api/v1/ai/verify-face", tags=["Identity Verification"])
async def verify_face(
    id_document: UploadFile = File(..., description="Image of ID Document (CNIC or Driver's License)"),
    selfie: UploadFile = File(..., description="Live Selfie image of the user")
):
    """
    Compares face extracted from ID Document against live Selfie.
    Returns match probability score, cosine similarity, and verification determination.
    """
    if not id_document.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="id_document must be a valid image file")
    if not selfie.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="selfie must be a valid image file")

    try:
        id_doc_bytes = await id_document.read()
        selfie_bytes = await selfie.read()

        result = face_pipeline.compare_faces(id_doc_bytes, selfie_bytes)
        return result

    except Exception as e:
        logger.error(f"Error processing face verification request: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Face verification failed: {str(e)}"
        )

@app.post("/api/v1/ai/verify-plate", tags=["Vehicle Verification"])
async def verify_plate(
    vehicle_image: UploadFile = File(..., description="Photo of the vehicle showing license plate"),
    claimed_plate: Optional[str] = Form(None, description="Expected/Declared plate number")
):
    """
    Performs OCR on vehicle photo to extract plate text and cross-checks against stolen/flagged list.
    """
    if not vehicle_image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="vehicle_image must be a valid image file")

    try:
        image_bytes = await vehicle_image.read()
        result = anpr_pipeline.verify_plate(image_bytes, claimed_plate=claimed_plate)
        return result

    except Exception as e:
        logger.error(f"Error processing ANPR request: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"ANPR verification failed: {str(e)}"
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("service:app", host="0.0.0.0", port=8001, reload=True)
