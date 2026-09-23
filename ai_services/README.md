# RentalWheels AI Security Microservice

This microservice houses the AI and Computer Vision pipelines for the **RentalWheels Car Rental Security Platform**.

## Core Features

1. **Face Verification Pipeline (`face_verifier.py`)**:
   - Uses **MTCNN** for face detection, bounding box extraction, and alignment.
   - Extracts 512-dimensional facial feature embeddings using **InceptionResnetV1** pretrained on VGGFace2.
   - Calculates **Cosine Similarity** between official ID Document (CNIC / Driver's License) and a live Selfie.
   - Determines verification result based on confidence threshold (`similarity >= 0.65`).

2. **ANPR & Vehicle Verification Pipeline (`anpr_verifier.py`)**:
   - Locates and extracts license plate text from vehicle photos using **EasyOCR** & **OpenCV**.
   - Sanitizes raw text (cleans spaces, special characters).
   - Cross-checks plate strings against stolen/flagged vehicle registries.

---

## Setup & Running Locally

### 1. Create Virtual Environment & Install Dependencies
```bash
cd ai_services
python -m venv venv
# On Windows:
venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
```

### 2. Start the FastAPI Service
```bash
uvicorn service:app --host 0.0.0.0 --port 8001 --reload
```
The microservice will start at: `http://localhost:8001`
Interactive Swagger API documentation is available at: `http://localhost:8001/docs`

---

## API Endpoints Summary

### `POST /api/v1/ai/verify-face`
* **Content-Type**: `multipart/form-data`
* **Form Fields**:
  - `id_document`: File (CNIC or Driver's License image)
  - `selfie`: File (User live selfie image)
* **Sample Response**:
```json
{
  "is_match": true,
  "similarity_score": 0.892,
  "cosine_similarity": 0.784,
  "id_face_detected": true,
  "selfie_face_detected": true,
  "confidence_level": "HIGH",
  "detail": "Verification successful"
}
```

### `POST /api/v1/ai/verify-plate`
* **Content-Type**: `multipart/form-data`
* **Form Fields**:
  - `vehicle_image`: File (Photo of vehicle license plate)
  - `claimed_plate`: String (Optional expected plate, e.g. "LEB1234")
* **Sample Response**:
```json
{
  "detected_plate": "LEB1234",
  "claimed_plate": "LEB1234",
  "plate_matches_claimed": true,
  "is_flagged": false,
  "flag_reason": null,
  "confidence_score": 0.941,
  "status": "PASSED"
}
```

---

## Django DRF Integration Example

From your Django backend (`backend/accounts/views.py` or `backend/vehicles/views.py`), call this microservice asynchronously or via `requests` / `httpx`:

```python
import requests

def verify_user_identity(id_image_bytes, selfie_image_bytes):
    response = requests.post(
        "http://localhost:8001/api/v1/ai/verify-face",
        files={
            "id_document": ("id.jpg", id_image_bytes, "image/jpeg"),
            "selfie": ("selfie.jpg", selfie_image_bytes, "image/jpeg")
        }
    )
    return response.json()
```
