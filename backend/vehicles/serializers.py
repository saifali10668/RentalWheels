from rest_framework import serializers
from .models import Vehicle

class VehicleSerializer(serializers.ModelSerializer):
    owner_username = serializers.ReadOnlyField(source='owner.username')
    owner_verified = serializers.ReadOnlyField(source='owner.is_id_verified')

    class Meta:
        model = Vehicle
        fields = (
            'id', 'owner', 'owner_username', 'owner_verified',
            'make', 'model', 'year', 'plate_number',
            'is_verified', 'is_flagged_stolen',
            'registration_doc', 'vehicle_photo',
            'price_per_day', 'location', 'status',
            'created_at', 'updated_at'
        )
        read_only_fields = ('owner', 'is_verified', 'is_flagged_stolen')
