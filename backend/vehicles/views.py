import requests
from django.conf import settings
from rest_framework import generics, permissions, status
from rest_framework.response import Response
from .models import Vehicle
from .serializers import VehicleSerializer

class VehicleListCreateView(generics.ListCreateAPIView):
    queryset = Vehicle.objects.all().order_by('-created_at')
    serializer_class = VehicleSerializer

    def get_permissions(self):
        if self.request.method == 'GET':
            return [permissions.AllowAny()]
        return [permissions.IsAuthenticated()]

    def perform_create(self, serializer):
        vehicle = serializer.save(owner=self.request.user)

        # Trigger ANPR plate check if vehicle photo is provided
        if vehicle.vehicle_photo:
            ai_url = f"{settings.AI_SERVICE_URL}/api/v1/ai/verify-plate"
            try:
                files = {'vehicle_image': vehicle.vehicle_photo.open('rb')}
                data = {'claimed_plate': vehicle.plate_number}
                response = requests.post(ai_url, files=files, data=data, timeout=10)

                if response.status_code == 200:
                    result = response.json()
                    is_flagged = result.get('is_flagged', False)
                    plate_matches = result.get('plate_matches_claimed', False)

                    vehicle.is_flagged_stolen = is_flagged
                    if is_flagged:
                        vehicle.status = Vehicle.Status.SUSPENDED
                        vehicle.is_verified = False
                    elif plate_matches:
                        vehicle.is_verified = True

                    vehicle.save()
            except Exception as e:
                # Log error and retain default state
                pass

class VehicleDetailView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Vehicle.objects.all()
    serializer_class = VehicleSerializer

    def get_permissions(self):
        if self.request.method == 'GET':
            return [permissions.AllowAny()]
        return [permissions.IsAuthenticated()]
