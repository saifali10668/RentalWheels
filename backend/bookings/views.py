from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.shortcuts import get_object_or_404
from .models import Booking
from .serializers import BookingSerializer

class BookingListCreateView(generics.ListCreateAPIView):
    serializer_class = BookingSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        user = self.request.user
        # Return bookings where user is either renter or vehicle owner
        return Booking.objects.filter(
            renter=user
        ) | Booking.objects.filter(
            vehicle__owner=user
        )

    def perform_create(self, serializer):
        serializer.save(renter=self.request.user)

class BookingDetailView(generics.RetrieveAPIView):
    serializer_class = BookingSerializer
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        user = self.request.user
        return Booking.objects.filter(
            renter=user
        ) | Booking.objects.filter(
            vehicle__owner=user
        )

class ApproveBookingView(APIView):
    permission_classes = (permissions.IsAuthenticated,)

    def patch(self, request, pk, *args, **kwargs):
        booking = get_object_or_404(Booking, pk=pk)
        if booking.vehicle.owner != request.user:
            return Response({'detail': 'Only the vehicle owner can approve this booking.'}, status=status.HTTP_403_FORBIDDEN)

        booking.status = Booking.Status.APPROVED
        booking.payment_status = Booking.PaymentStatus.ESCROW_HOLD
        booking.save()

        return Response(BookingSerializer(booking).data, status=status.HTTP_200_OK)

class CancelBookingView(APIView):
    permission_classes = (permissions.IsAuthenticated,)

    def patch(self, request, pk, *args, **kwargs):
        booking = get_object_or_404(Booking, pk=pk)
        if booking.renter != request.user and booking.vehicle.owner != request.user:
            return Response({'detail': 'Not authorized to cancel this booking.'}, status=status.HTTP_403_FORBIDDEN)

        booking.status = Booking.Status.CANCELLED
        if booking.payment_status == Booking.PaymentStatus.ESCROW_HOLD:
            booking.payment_status = Booking.PaymentStatus.REFUNDED
        booking.save()

        return Response(BookingSerializer(booking).data, status=status.HTTP_200_OK)
