package com.example.facedetectionapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.face.Face;

import java.util.ArrayList;
import java.util.List;

public class FaceOverlayView extends View {

    private List<Face> faces = new ArrayList<>();
    private Paint paint;

    public FaceOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(0xFFFF0000); // أحمر
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
        invalidate(); // يعيد رسم الواجهة
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            canvas.drawRect(bounds, paint);
        }
    }
}
