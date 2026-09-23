from rest_framework import generics, permissions, status
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from .models import Dispute
from .serializers import DisputeSerializer
from bookings.models import Booking

class DisputeListCreateView(generics.ListCreateAPIView):
    serializer_class = DisputeSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        user = self.request.user
        return Dispute.objects.filter(
            raised_by=user
        ) | Dispute.objects.filter(
            booking__vehicle__owner=user
        )

    def perform_create(self, serializer):
        booking_id = self.request.data.get('booking')
        booking = get_object_or_404(Booking, pk=booking_id)

        dispute = serializer.save(raised_by=self.request.user, booking=booking)

        # Update Booking status
        booking.status = Booking.Status.DISPUTED
        booking.save()

class DisputeDetailView(generics.RetrieveAPIView):
    serializer_class = DisputeSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        user = self.request.user
        return Dispute.objects.filter(
            raised_by=user
        ) | Dispute.objects.filter(
            booking__vehicle__owner=user
        )
