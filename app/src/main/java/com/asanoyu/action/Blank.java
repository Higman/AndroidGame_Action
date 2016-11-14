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
        canvas.drawCircle(rect.centerX(), 65, 20, paint);
        canvas.drawCircle(rect.left, 50, 10, paint);
        canvas.drawCircle(rect.right,80, 10, paint);
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
