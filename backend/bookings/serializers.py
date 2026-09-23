from rest_framework import serializers
from .models import Booking
from vehicles.serializers import VehicleSerializer

class BookingSerializer(serializers.ModelSerializer):
    renter_username = serializers.ReadOnlyField(source='renter.username')
    renter_verified = serializers.ReadOnlyField(source='renter.is_id_verified')
    vehicle_details = VehicleSerializer(source='vehicle', read_only=True)

    class Meta:
        model = Booking
        fields = (
            'id', 'renter', 'renter_username', 'renter_verified',
            'vehicle', 'vehicle_details',
            'start_date', 'end_date', 'total_amount',
            'status', 'payment_status',
            'created_at', 'updated_at'
        )
        read_only_fields = ('renter', 'status', 'payment_status')
