package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.Settings;


/**
 * Created by YU-YA on 2016/09/07.
 */
public class Droid {
    private final Paint paint = new Paint();
    private Bitmap bitmap;

    private static final int HIT_MARGIN_LEFT = 30;
    private static final int HIT_MARGIN_RIGHT = 10;
    private static final float GRAVITY = 0.8f;
    private static final float WEIGHT = GRAVITY * 40;

    private static final int BLOCK_SIZE = 153;
    private static final Rect BITMAP_SRC_RUNNING = new Rect(0, 0, BLOCK_SIZE, BLOCK_SIZE);
    private static final Rect BITMAP_SEC_JUMPING = new Rect(BLOCK_SIZE, 0, BLOCK_SIZE*2, BLOCK_SIZE);

    private float velocity = 0;

    final RectF rect;
    final Rect hitRect;

    public static final int DEFAULT_DROID_MOVE_LEFT = 0;
    private int droidMoveToLeft;  // 自機が画面上を移動するX軸上の速度

    private int rightEnd;

    public interface Callback {
        Droid.MoveDirection getDistanceFromObstacle(Droid droid);
    }

    public static class MoveDirection {
        public int x;
        public int y;

        MoveDirection() { this.x = 0;  this.y = 0; }
        MoveDirection(int x, int y) { this.x = x;  this.y = y; }

        public void setDirection(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private final Callback callback;

    public Droid(Bitmap bitmap, int left, int top, int rightEnd, Callback callback) {
        int right = left + BLOCK_SIZE;
        int bottom = top + bitmap.getHeight();
        this.rect = new RectF(left, top, right, bottom);
        this.hitRect = new Rect(left, top, right, bottom);
        this.hitRect.left += HIT_MARGIN_LEFT;
        this.hitRect.right -= HIT_MARGIN_RIGHT;
        this.bitmap = bitmap;
        this.rightEnd = rightEnd;
        this.callback = callback;
        this.droidMoveToLeft = DEFAULT_DROID_MOVE_LEFT;
    }

    public void draw(Canvas canvas) {
        Rect src = BITMAP_SRC_RUNNING;
        if ( velocity != 0 ) {
            src = BITMAP_SEC_JUMPING;
        }

        canvas.drawBitmap(bitmap, src, rect, paint);

        paint.setColor(Color.argb(100, 100, 100, 40));
        canvas.drawRect(hitRect.left, hitRect.top, hitRect.right, hitRect.bottom, paint);
//
//        paint.setColor(Color.CYAN);
//        canvas.drawLine(rect.left, rect.centerY(), rect.left, rect.centerY()-100, paint);
//        canvas.drawLine(rect.right, rect.centerY(), rect.right, rect.centerY()-100, paint);
//
//        paint.setColor(Color.BLUE);
//        canvas.drawLine(hitRect.left, rect.centerY(), hitRect.left, rect.centerY()-100, paint);
//        canvas.drawLine(hitRect.right, rect.centerY(), hitRect.right, rect.centerY()-100, paint);
    }

    public void jump(float power) { velocity = (power * WEIGHT); }

    public void stop() { velocity = 0; }

    public void move() {
        MoveDirection moveDirection = callback.getDistanceFromObstacle(this);
        int distanceFromGround = moveDirection.y;
        int distanceFromWall = moveDirection.x;

        if ( velocity < 0 && velocity < -distanceFromGround ) {
            velocity = -distanceFromGround;
        }

        //-- 移動
        rect.offset(this.droidMoveToLeft, Math.round(-1*velocity));
        hitRect.offset(this.droidMoveToLeft, Math.round(-1*velocity));

        //-- 補正
        if ( distanceFromWall < 0 ) {
            rect.offset(distanceFromWall, 0);
            hitRect.offset(distanceFromWall, 0);
        }

        if ( hitRect.right > rightEnd ) {
            int endMoveLeft = rightEnd - hitRect.right;
            rect.offset(endMoveLeft, 0);
            hitRect.offset(endMoveLeft, 0);
        }

        if ( distanceFromGround == 0 ) {
            return;
        } else if ( distanceFromGround < 0 ) {
            rect.offset(0, distanceFromGround);
            hitRect.offset(0, distanceFromGround);
            return;
        }

        velocity -= GRAVITY;
    }

    public void gameOverMove() {
        rect.offset(-Integer.MAX_VALUE/2, 0);
        hitRect.offset(-Integer.MAX_VALUE/2, 0);
    }

    //----- setter/getter
    public void setDroidMoveToLeft(int droidMoveToLeft) {
        //--- 補正
        if ( droidMoveToLeft < -10 ) { droidMoveToLeft = -10; }
        if ( droidMoveToLeft > 10 ) { droidMoveToLeft = 10; }

        //--- 処理
        this.droidMoveToLeft = droidMoveToLeft;
    }

    public int getDroidMoveToLeft() {
        return droidMoveToLeft;
    }
}
