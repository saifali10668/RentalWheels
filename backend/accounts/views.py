import requests
from django.conf import settings
from rest_framework import generics, status, permissions
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView
from django.contrib.auth import get_user_model

from .serializers import UserSignUpSerializer, UserProfileSerializer, IDVerificationSerializer

User = get_user_model()

class SignUpView(generics.CreateAPIView):
    queryset = User.objects.all()
    permission_classes = (permissions.AllowAny,)
    serializer_class = UserSignUpSerializer

class ProfileView(generics.RetrieveUpdateAPIView):
    permission_classes = (permissions.IsAuthenticated,)
    serializer_class = UserProfileSerializer

    def get_object(self):
        return self.request.user

class VerifyIDView(APIView):
    permission_classes = (permissions.IsAuthenticated,)

    def post(self, request, *args, **kwargs):
        serializer = IDVerificationSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        user = request.user
        data = serializer.validated_data

        # Save documents on User instance
        user.cnic_number = data['cnic_number']
        user.license_number = data['license_number']
        user.id_document = data['id_document']
        user.selfie_photo = data['selfie_photo']
        user.verification_status = User.VerificationStatus.PENDING
        user.save()

        # Call AI Microservice for Face Verification
        ai_url = f"{settings.AI_SERVICE_URL}/api/v1/ai/verify-face"
        try:
            files = {
                'id_document': user.id_document.open('rb'),
                'selfie': user.selfie_photo.open('rb')
            }
            response = requests.post(ai_url, files=files, timeout=10)

            if response.status_code == 200:
                ai_result = response.json()
                is_match = ai_result.get('is_match', False)
                similarity_score = ai_result.get('similarity_score', 0.0)

                user.verification_score = similarity_score
                if is_match:
                    user.is_id_verified = True
                    user.verification_status = User.VerificationStatus.VERIFIED
                else:
                    user.is_id_verified = False
                    user.verification_status = User.VerificationStatus.REJECTED

                user.save()

                return Response({
                    'message': 'Identity verification completed.',
                    'is_id_verified': user.is_id_verified,
                    'verification_status': user.verification_status,
                    'verification_score': user.verification_score,
                    'ai_detail': ai_result.get('detail', '')
                }, status=status.HTTP_200_OK)
            else:
                return Response({
                    'message': 'AI verification service returned an error.',
                    'detail': response.text
                }, status=status.HTTP_502_BAD_GATEWAY)

        except Exception as e:
            # Fallback to pending state if AI service is offline
            return Response({
                'message': 'ID documents submitted for manual admin review.',
                'verification_status': user.verification_status,
                'detail': f"AI service unavailable: {str(e)}"
            }, status=status.HTTP_202_ACCEPTED)
