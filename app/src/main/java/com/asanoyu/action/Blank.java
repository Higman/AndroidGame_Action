package com.asanoyu.action;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Created by YU-YA2 on 2016/09/10.
 */
public class Blank extends Ground {
    public Blank(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);

        paint.setColor(Color.rgb(5+new Random().nextInt(251), 5+new Random().nextInt(251), 5+new Random().nextInt(251)));
    }

    private Paint paint = new Paint();

    @Override
    public void draw(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawCircle(locRect.centerX(), 65, 20, paint);
        canvas.drawCircle(locRect.left, 50, 10, paint);
        canvas.drawCircle(locRect.right,80, 10, paint);

        canvas.drawLine(locRect.left, locRect.top, locRect.left, 0, paint);
        canvas.drawLine(locRect.right, locRect.top, locRect.right, 0, paint);
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public String getKind() {
        return "Blank";
    }
}
