from rest_framework import serializers
from .models import Dispute

class DisputeSerializer(serializers.ModelSerializer):
    raised_by_username = serializers.ReadOnlyField(source='raised_by.username')

    class Meta:
        model = Dispute
        fields = (
            'id', 'booking', 'raised_by', 'raised_by_username',
            'description', 'evidence_photo', 'status',
            'admin_notes', 'created_at', 'updated_at'
        )
        read_only_fields = ('raised_by', 'status', 'admin_notes')
