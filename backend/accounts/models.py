from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    class Role(models.TextChoices):
        RENTER = 'RENTER', 'Renter'
        OWNER = 'OWNER', 'Owner (Lister)'
        BOTH = 'BOTH', 'Renter & Owner'
        ADMIN = 'ADMIN', 'Admin'

    class VerificationStatus(models.TextChoices):
        UNVERIFIED = 'UNVERIFIED', 'Unverified'
        PENDING = 'PENDING', 'Pending Review'
        VERIFIED = 'VERIFIED', 'Verified'
        REJECTED = 'REJECTED', 'Verification Rejected'

    phone_number = models.CharField(max_length=20, unique=True, null=True, blank=True)
    role = models.CharField(max_length=10, choices=Role.choices, default=Role.RENTER)

    # Security & Verification Fields
    cnic_number = models.CharField(max_length=20, blank=True, null=True)
    license_number = models.CharField(max_length=30, blank=True, null=True)
    is_id_verified = models.BooleanField(default=False)
    verification_status = models.CharField(
        max_length=20,
        choices=VerificationStatus.choices,
        default=VerificationStatus.UNVERIFIED
    )

    id_document = models.ImageField(upload_to='id_documents/', null=True, blank=True)
    selfie_photo = models.ImageField(upload_to='selfies/', null=True, blank=True)
    verification_score = models.FloatField(default=0.0)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.username} ({self.get_role_display()}) - Verified: {self.is_id_verified}"
