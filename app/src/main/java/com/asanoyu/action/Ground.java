package com.asanoyu.action;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by YU-YA on 2016/09/07.
 */
public class Ground {

    private int COLOR = Color.rgb(153, 76, 0);  // 茶色
    private Paint paint = new Paint();
    final Rect rect;

    public Ground(int left, int top, int right, int bottom) {
        rect = new Rect(left, top, right, bottom);

        paint.setColor(COLOR);
        paint.setColor(Color.rgb(5+new Random().nextInt(251), 5+new Random().nextInt(251), 5+new Random().nextInt(251)));
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(rect, paint);

        canvas.drawCircle(rect.centerX(), 65, 20, paint);
        canvas.drawCircle(rect.left, 50, 10, paint);
        canvas.drawCircle(rect.right, 80, 10, paint);
    }

    public void changeWidth(int amplitude) {  // 幅の変更
        rect.inset(-amplitude/2, 0);
        rect.offset(amplitude/2, 0);
    }

    public void move(int moveToLeft) {
        rect.offset(-moveToLeft, 0);
    }

    public boolean isShown(int width, int height) {
        return rect.intersects(0, 0, width, height);
    }

    public boolean isAvailable() {
        return (rect.right > 0);
    }

    public boolean isSolid() {
        return true;
    }

    public String getKind() {
        return "Ground";
    }
}
