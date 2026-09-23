from rest_framework import serializers
from .models import Checkin

class CheckinSerializer(serializers.ModelSerializer):
    class Meta:
        model = Checkin
        fields = (
            'id', 'booking', 'checkin_type',
            'front_photo', 'rear_photo', 'left_photo', 'right_photo',
            'odometer_reading', 'gps_latitude', 'gps_longitude',
            'timestamp'
        )
        read_only_fields = ('timestamp',)
