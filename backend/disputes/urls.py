from django.urls import path
from .views import DisputeListCreateView, DisputeDetailView

urlpatterns = [
    path('', DisputeListCreateView.as_view(), name='dispute_list_create'),
    path('<int:pk>/', DisputeDetailView.as_view(), name='dispute_detail'),
]
