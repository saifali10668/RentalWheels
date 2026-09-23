from django.db import models
from django.conf import settings
from bookings.models import Booking

class Dispute(models.Model):
    class Status(models.TextChoices):
        OPEN = 'OPEN', 'Open'
        UNDER_REVIEW = 'UNDER_REVIEW', 'Under Admin Review'
        RESOLVED_RENTER = 'RESOLVED_RENTER', 'Resolved (Renter Favor)'
        RESOLVED_OWNER = 'RESOLVED_OWNER', 'Resolved (Owner Favor)'

    booking = models.ForeignKey(Booking, on_delete=models.CASCADE, related_name='disputes')
    raised_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)

    description = models.TextField()
    evidence_photo = models.ImageField(upload_to='dispute_evidence/', null=True, blank=True)

    status = models.CharField(max_length=20, choices=Status.choices, default=Status.OPEN)
    admin_notes = models.TextField(blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"Dispute #{self.id} for Booking #{self.booking.id} ({self.get_status_display()})"
