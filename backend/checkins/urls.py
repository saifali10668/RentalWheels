from django.urls import path
from .views import CheckinCreateView, CheckinDetailView

urlpatterns = [
    path('', CheckinCreateView.as_view(), name='checkin_create'),
    path('booking/<int:booking_id>/', CheckinDetailView.as_view(), name='checkin_detail'),
]
