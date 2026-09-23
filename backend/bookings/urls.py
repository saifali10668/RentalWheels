from django.urls import path
from .views import BookingListCreateView, BookingDetailView, ApproveBookingView, CancelBookingView

urlpatterns = [
    path('', BookingListCreateView.as_view(), name='booking_list_create'),
    path('<int:pk>/', BookingDetailView.as_view(), name='booking_detail'),
    path('<int:pk>/approve/', ApproveBookingView.as_view(), name='approve_booking'),
    path('<int:pk>/cancel/', CancelBookingView.as_view(), name='cancel_booking'),
]
