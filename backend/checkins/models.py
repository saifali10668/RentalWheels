from django.db import models
from bookings.models import Booking

class Checkin(models.Model):
    class Type(models.TextChoices):
        PICKUP = 'PICKUP', 'Pickup Inspection'
        DROPOFF = 'DROPOFF', 'Drop-off Inspection'

    booking = models.ForeignKey(Booking, on_delete=models.CASCADE, related_name='checkins')
    checkin_type = models.CharField(max_length=10, choices=Type.choices)

    # 4-Sided Vehicle Photos
    front_photo = models.ImageField(upload_to='checkin_photos/')
    rear_photo = models.ImageField(upload_to='checkin_photos/')
    left_photo = models.ImageField(upload_to='checkin_photos/')
    right_photo = models.ImageField(upload_to='checkin_photos/')

    # Inspection Metadata
    odometer_reading = models.IntegerField()
    gps_latitude = models.DecimalField(max_digits=9, decimal_places=6)
    gps_longitude = models.DecimalField(max_digits=9, decimal_places=6)

    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.get_checkin_type_display()} for Booking #{self.booking.id}"
