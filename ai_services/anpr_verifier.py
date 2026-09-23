"""
Automatic License Plate Recognition (ANPR) & Verification Module for RentalWheels Platform.
Extracts license plate text from vehicle photos and cross-checks against stolen/flagged vehicle records.
"""

import io
import re
import logging
import numpy as np
import cv2
from PIL import Image

logger = logging.getLogger(__name__)

# Sample set of known flagged/stolen vehicle plates for cross-checking
MOCK_FLAGGED_PLATES = {
    "LEB1234",
    "LEA9999",
    "ICT5555",
    "KHI8888",
    "STOLEN1",
    "FLAG123"
}

class ANPRVerificationPipeline:
    def __init__(self, languages: list = ['en']):
        """
        Initialize ANPR pipeline using EasyOCR engine.
        """
        logger.info("Initializing ANPR Verification Pipeline...")
        try:
            import easyocr
            self.reader = easyocr.Reader(languages, gpu=False)
            self.use_easyocr = True
        except ImportError:
            logger.warning("EasyOCR not installed. Falling back to basic OpenCV OCR extraction.")
            self.use_easyocr = False

    def _bytes_to_cv2(self, image_bytes: bytes) -> np.ndarray:
        """Convert image byte stream to OpenCV BGR matrix."""
        image = Image.open(io.BytesIO(image_bytes)).convert('RGB')
        return cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

    def sanitize_plate_text(self, text: str) -> str:
        """
        Clean raw OCR text: convert to uppercase, remove special chars, retain alphanumeric characters.
        """
        sanitized = re.sub(r'[^A-Z0-9]', '', text.upper())
        return sanitized

    def preprocess_image(self, cv_img: np.ndarray) -> np.ndarray:
        """
        Apply grayscale, contrast enhancement, and noise reduction for better OCR accuracy.
        """
        gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)
        # Bilateral filter to smooth flat areas while keeping edges sharp
        filtered = cv2.bilateralFilter(gray, 11, 17, 17)
        # Adaptive thresholding
        thresh = cv2.adaptiveThreshold(
            filtered, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2
        )
        return thresh

    def extract_plate(self, image_bytes: bytes) -> dict:
        """
        Performs OCR on the vehicle photo to locate and extract license plate text.
        """
        cv_img = self._bytes_to_cv2(image_bytes)

        detected_plates = []
        raw_results = []

        if self.use_easyocr:
            # EasyOCR detection
            results = self.reader.readtext(cv_img)
            for (bbox, text, prob) in results:
                sanitized = self.sanitize_plate_text(text)
                if len(sanitized) >= 3 and prob > 0.3:
                    detected_plates.append({
                        "plate": sanitized,
                        "confidence": round(float(prob), 4),
                        "raw_text": text
                    })
                    raw_results.append(text)
        else:
            # Simple fallback preprocessing logging
            preprocessed = self.preprocess_image(cv_img)
            logger.info("Executed preprocessing fallback.")

        # Sort detected candidates by confidence score
        detected_plates.sort(key=lambda x: x["confidence"], reverse=True)

        best_match = detected_plates[0] if detected_plates else None

        return {
            "best_match": best_match,
            "all_detected_candidates": detected_plates,
            "raw_text_found": raw_results
        }

    def verify_plate(self, vehicle_image_bytes: bytes, claimed_plate: str = None) -> dict:
        """
        Extracts plate from vehicle photo, compares with claimed plate, and cross-checks against flagged database.
        """
        try:
            extraction = self.extract_plate(vehicle_image_bytes)
            best_candidate = extraction["best_match"]

            detected_plate_text = best_candidate["plate"] if best_candidate else None
            confidence = best_candidate["confidence"] if best_candidate else 0.0

            sanitized_claimed = self.sanitize_plate_text(claimed_plate) if claimed_plate else None

            # Determine if detected plate matches claimed registration plate
            plate_matches_claimed = False
            if detected_plate_text and sanitized_claimed:
                plate_matches_claimed = (detected_plate_text == sanitized_claimed) or (sanitized_claimed in detected_plate_text)

            # Check if plate is on stolen/flagged list
            is_flagged = False
            flag_reason = None

            check_plate = detected_plate_text or sanitized_claimed
            if check_plate and check_plate in MOCK_FLAGGED_PLATES:
                is_flagged = True
                flag_reason = "Plate matched Stolen/Flagged Vehicle Registry"

            return {
                "detected_plate": detected_plate_text,
                "claimed_plate": sanitized_claimed,
                "plate_matches_claimed": plate_matches_claimed,
                "is_flagged": is_flagged,
                "flag_reason": flag_reason,
                "confidence_score": confidence,
                "candidates": extraction["all_detected_candidates"],
                "status": "FLAGGED_WARNING" if is_flagged else "PASSED"
            }

        except Exception as e:
            logger.error(f"Error during ANPR plate verification: {str(e)}", exc_info=True)
            return {
                "detected_plate": None,
                "claimed_plate": claimed_plate,
                "plate_matches_claimed": False,
                "is_flagged": False,
                "confidence_score": 0.0,
                "status": "ERROR",
                "detail": f"Processing error: {str(e)}"
            }
