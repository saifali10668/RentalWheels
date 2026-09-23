from django.db import models
from django.conf import settings

class Vehicle(models.Model):
    class Status(models.TextChoices):
        AVAILABLE = 'AVAILABLE', 'Available'
        RENTED = 'RENTED', 'Rented'
        MAINTENANCE = 'MAINTENANCE', 'In Maintenance'
        SUSPENDED = 'SUSPENDED', 'Suspended / Flagged'

    owner = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='vehicles'
    )
    make = models.CharField(max_length=50)
    model = models.CharField(max_length=50)
    year = models.IntegerField()
    plate_number = models.CharField(max_length=20, unique=True)

    # Verification & Media
    is_verified = models.BooleanField(default=False)
    is_flagged_stolen = models.BooleanField(default=False)
    registration_doc = models.ImageField(upload_to='vehicle_docs/', null=True, blank=True)
    vehicle_photo = models.ImageField(upload_to='vehicle_photos/', null=True, blank=True)

    # Rental details
    price_per_day = models.DecimalField(max_digits=10, decimal_places=2)
    location = models.CharField(max_length=255)
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.AVAILABLE)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.year} {self.make} {self.model} ({self.plate_number})"
