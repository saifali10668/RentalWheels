"""
Face Verification Module for RentalWheels Platform.
Compares facial embeddings between an official ID Document (CNIC/DL) and a live Selfie.
"""

import io
import logging
import numpy as np
from PIL import Image
import torch
import torchvision.transforms as transforms
from facenet_pytorch import MTCNN, InceptionResnetV1

logger = logging.getLogger(__name__)

class FaceVerificationPipeline:
    def __init__(self, match_threshold: float = 0.65):
        """
        Initialize Face Verification Pipeline using MTCNN for detection
        and InceptionResnetV1 (VGGFace2) for embedding extraction.
        """
        self.device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
        logger.info(f"Initializing FaceVerificationPipeline on device: {self.device}")

        # MTCNN for face detection and alignment
        self.mtcnn = MTCNN(
            image_size=160,
            margin=20,
            min_face_size=40,
            thresholds=[0.6, 0.7, 0.7],
            factor=0.709,
            post_process=True,
            device=self.device
        )

        # InceptionResnetV1 pretrained model for 512-dim feature embedding extraction
        self.model = InceptionResnetV1(pretrained='vggface2').eval().to(self.device)
        self.match_threshold = match_threshold

    def _bytes_to_image(self, image_bytes: bytes) -> Image.Image:
        """Convert raw image byte stream to RGB PIL Image."""
        image = Image.open(io.BytesIO(image_bytes))
        if image.mode != 'RGB':
            image = image.convert('RGB')
        return image

    def extract_embedding(self, image: Image.Image):
        """
        Detect face and extract 512-dimensional normalized embedding vector.
        Returns tensor embedding and boolean indicating face detection success.
        """
        # Detect and crop face
        face_tensor = self.mtcnn(image)
        if face_tensor is None:
            return None, False

        # Add batch dimension and move to device
        face_tensor = face_tensor.unsqueeze(0).to(self.device)

        with torch.no_grad():
            embedding = self.model(face_tensor)
            # Normalize vector to unit length for cosine distance calculation
            embedding = torch.nn.functional.normalize(embedding, p=2, dim=1)

        return embedding, True

    def compare_faces(self, id_doc_bytes: bytes, selfie_bytes: bytes) -> dict:
        """
        Compares ID document photo against live selfie.

        Returns:
            dict containing:
                - is_match (bool)
                - similarity_score (float 0.0 to 1.0)
                - id_face_detected (bool)
                - selfie_face_detected (bool)
                - confidence_level (str)
        """
        try:
            id_img = self._bytes_to_image(id_doc_bytes)
            selfie_img = self._bytes_to_image(selfie_bytes)

            id_embed, id_detected = self.extract_embedding(id_img)
            selfie_embed, selfie_detected = self.extract_embedding(selfie_img)

            if not id_detected or not selfie_detected:
                return {
                    "is_match": False,
                    "similarity_score": 0.0,
                    "id_face_detected": id_detected,
                    "selfie_face_detected": selfie_detected,
                    "confidence_level": "FAILED_FACE_DETECTION",
                    "detail": "Face could not be clearly detected in one or both images."
                }

            # Compute Cosine Similarity (dot product of normalized unit vectors)
            cosine_similarity = float(torch.mm(id_embed, selfie_embed.t()).item())

            # Map cosine similarity (-1..1) to probability score (0..1)
            similarity_score = max(0.0, min(1.0, (cosine_similarity + 1.0) / 2.0))
            is_match = cosine_similarity >= self.match_threshold

            if cosine_similarity >= 0.80:
                confidence = "HIGH"
            elif cosine_similarity >= self.match_threshold:
                confidence = "MEDIUM"
            else:
                confidence = "LOW"

            return {
                "is_match": is_match,
                "similarity_score": round(similarity_score, 4),
                "cosine_similarity": round(cosine_similarity, 4),
                "id_face_detected": True,
                "selfie_face_detected": True,
                "confidence_level": confidence,
                "detail": "Verification successful" if is_match else "Face match threshold not met"
            }

        except Exception as e:
            logger.error(f"Error during face comparison: {str(e)}", exc_info=True)
            return {
                "is_match": False,
                "similarity_score": 0.0,
                "id_face_detected": False,
                "selfie_face_detected": False,
                "confidence_level": "ERROR",
                "detail": f"Processing error: {str(e)}"
            }
