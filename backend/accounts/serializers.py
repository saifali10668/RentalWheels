from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

class UserSignUpSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=8)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'phone_number', 'password', 'role')

    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data.get('email', ''),
            phone_number=validated_data.get('phone_number'),
            password=validated_data['password'],
            role=validated_data.get('role', User.Role.RENTER)
        )
        return user

class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = (
            'id', 'username', 'email', 'phone_number', 'role',
            'cnic_number', 'license_number', 'is_id_verified',
            'verification_status', 'verification_score', 'id_document', 'selfie_photo'
        )
        read_only_fields = ('is_id_verified', 'verification_status', 'verification_score')

class IDVerificationSerializer(serializers.Serializer):
    cnic_number = serializers.CharField(max_length=20, required=True)
    license_number = serializers.CharField(max_length=30, required=True)
    id_document = serializers.ImageField(required=True)
    selfie_photo = serializers.ImageField(required=True)
