package com.example.facedetectionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap;
import android.hardware.camera2.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.face.*;
import com.google.mlkit.vision.common.InputImage;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private TextureView textureView;
    private TextView faceCountText;
    private TextView expressionText;
    private Button startStopButton;
    private FaceOverlayView faceOverlayView;

    private boolean isDetecting = false;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraManager cameraManager;
    private String cameraId;

    private MediaPlayer mediaPlayer;
    private int previousFaceCount = 0;

    private long lastAnalyzedTime = 0;
    private static final long ANALYSIS_INTERVAL_MS = 300;

    private final FaceDetectorOptions detectorOptions =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();

    private final FaceDetector faceDetector = FaceDetection.getClient(detectorOptions);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);

        textureView = findViewById(R.id.textureView);
        faceCountText = findViewById(R.id.faceCountText);
        expressionText = findViewById(R.id.expressionText);
        startStopButton = findViewById(R.id.startStopButton);
        faceOverlayView = findViewById(R.id.overlayView);

        startStopButton.setText("التقط صورة");
        startStopButton.setOnClickListener(v -> {
            Bitmap bitmap = textureView.getBitmap();
            if (bitmap != null) {
                detectFacesFromBitmap(bitmap);
            } else {
                Toast.makeText(this, "فشل في التقاط الصورة", Toast.LENGTH_SHORT).show();
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    200);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        textureView.setSurfaceTextureListener(textureListener);
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surface.setDefaultBufferSize(width, height);  // أضف هذا السطر
            setupCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // لم نعد نحلل تلقائيًا هنا
        }
    };

    private void setupCamera() {
        try {
            cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, null);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void startCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(CameraActivity.this, "فشل بدء المعاينة", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void detectFacesFromCamera() {
        try {
            Bitmap bitmap = textureView.getBitmap();
            if (bitmap == null) return;

            InputImage image = InputImage.fromBitmap(bitmap, 0);

            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        int currentFaceCount = faces.size();

                        if (previousFaceCount == 0 && currentFaceCount > 0) {
                            mediaPlayer.start();
                        }
                        previousFaceCount = currentFaceCount;

                        updateFaceCount(currentFaceCount);
                        faceOverlayView.setFaces(faces);

                        StringBuilder expression = new StringBuilder();

                        for (Face face : faces) {
                            Float smileProb = face.getSmilingProbability();
                            Float leftEye = face.getLeftEyeOpenProbability();
                            Float rightEye = face.getRightEyeOpenProbability();

                            if (smileProb != null && smileProb > 0.7) {
                                expression.append("😊 وجه مبتسم\n");
                            } else if (leftEye != null && rightEye != null) {
                                if (leftEye < 0.2 && rightEye < 0.2) {
                                    expression.append("😴 نعسان / مغمض العينين\n");
                                } else if (leftEye > 0.9 && rightEye > 0.9) {
                                    expression.append("😲 مندهش / عينين مفتوحتين تمامًا\n");
                                } else {
                                    expression.append("😐 عادي\n");
                                }
                            } else {
                                expression.append("❓ تعبير غير معروف\n");
                            }
                        }

                        runOnUiThread(() -> expressionText.setText(expression.toString()));

                    })
                    .addOnFailureListener(e -> {
                        Log.e("FaceDetection", "فشل تحليل الصورة", e);
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFaceCount(int count) {
        runOnUiThread(() -> faceCountText.setText("عدد الوجوه: " + count));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "الكاميرا ضرورية", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void detectFacesFromBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        mediaPlayer.start(); // صوت عند وجود وجه
                    }

                    faceOverlayView.setFaces(faces);
                    updateFaceCount(faces.size());

                    StringBuilder expression = new StringBuilder();
                    for (Face face : faces) {
                        Float smileProb = face.getSmilingProbability();
                        Float leftEye = face.getLeftEyeOpenProbability();
                        Float rightEye = face.getRightEyeOpenProbability();

                        if (smileProb != null && smileProb > 0.7f) {
                            expression.append("😊 مبتسم\n");
                        } else {
                            expression.append("😐 غير مبتسم\n");
                        }

                        if (leftEye != null && rightEye != null) {
                            if (leftEye < 0.2 && rightEye < 0.2) {
                                expression.append("😴 نعسان\n");
                            }
                        }
                    }

                    runOnUiThread(() -> expressionText.setText(expression.toString()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل التحليل", Toast.LENGTH_SHORT).show();
                    Log.e("FaceDetection", "Error: ", e);
                });
    }


}
