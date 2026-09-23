from rest_framework import generics, permissions, status
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from .models import Checkin
from .serializers import CheckinSerializer
from bookings.models import Booking

class CheckinCreateView(generics.CreateAPIView):
    serializer_class = CheckinSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def perform_create(self, serializer):
        booking_id = self.request.data.get('booking')
        booking = get_object_or_404(Booking, pk=booking_id)

        checkin = serializer.save(booking=booking)

        # Update Booking state based on checkin type
        if checkin.checkin_type == Checkin.Type.PICKUP:
            booking.status = Booking.Status.ACTIVE
            booking.save()
        elif checkin.checkin_type == Checkin.Type.DROPOFF:
            booking.status = Booking.Status.COMPLETED
            booking.payment_status = Booking.PaymentStatus.RELEASED
            booking.save()

class CheckinDetailView(generics.ListAPIView):
    serializer_class = CheckinSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        booking_id = self.kwargs['booking_id']
        return Checkin.objects.filter(booking_id=booking_id)
