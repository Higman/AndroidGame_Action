package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;


/**
 * Created by YU-YA on 2016/09/07.
 */
public class Droid {
    private final Paint paint = new Paint();
    private BitmapAnimation bitmapA;

    private static final int HIT_MARGIN_LEFT = 4;
    private static final int HIT_MARGIN_RIGHT = 2;
    private static final int HIT_MARGIN_TOP = 17;
    private static final int HIT_MARGIN_BOTTOM = 16;

    private static final float GRAVITY = 0.8f;
    private static final float WEIGHT = GRAVITY * 40;

    private static final int IMAGE_SIZE = 200;
    private static final int BLOCK_SIZE = 64;
    private int animationInterval = 750;   // アニメーション間隔

    private float velocity = 0;

    final RectF rect;
    final Rect hitRect;

    public static final int DEFAULT_DROID_MOVE_LEFT = 0;
    private int droidMoveToLeft;  // 自機が画面上を移動するX軸上の速度

    private int rightEnd;

    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public Droid(Bitmap bitmap, int left, int top, int rightEnd, Callback callback) {
        int right = left + this.IMAGE_SIZE;
        int bottom = top + this.IMAGE_SIZE;
        double imageRate = this.IMAGE_SIZE / (double)this.BLOCK_SIZE;  // 拡大比率

        //--- 描画位置・当たり判定矩形
        this.rect = new RectF(left, top, right, bottom);
        this.hitRect = new Rect(left, top, right, bottom);
        this.hitRect.left += Math.round(HIT_MARGIN_LEFT * imageRate);
        this.hitRect.right -= Math.round(HIT_MARGIN_RIGHT * imageRate);
        this.hitRect.top += Math.round(HIT_MARGIN_TOP * imageRate);
        this.hitRect.bottom -= Math.round(HIT_MARGIN_BOTTOM * imageRate);


        //--- アニメーション関連
        this.bitmapA = new BitmapAnimation(bitmap, this.BLOCK_SIZE);
        this.bitmapA.setInterval(this.animationInterval);  // アニメーション間隔の設定

        this.rightEnd = rightEnd;
        this.callback = callback;
        this.droidMoveToLeft = DEFAULT_DROID_MOVE_LEFT;
    }

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

    public void draw(Canvas canvas) {

        bitmapA.drawAnimation(canvas, this.rect, paint);

//        paint.setColor(Color.argb(100, 100, 100, 40));
//        canvas.drawRect(hitRect.left, hitRect.top, hitRect.right, hitRect.bottom, paint);
        paint.setColor(Color.argb(255, 0, 0, 0));
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

    public class BitmapAnimation {

        private Bitmap bitmap;  // 表示画像
        private final int IMAGE_BLOCK;  // 1つのアニメーションの大きさ
        private final int DRAW_SIZE;  // 画像内にあるアニメーション枚数

        private ArrayList<Rect> srcRectList = new ArrayList<Rect>();

        private int drawInterval = 1;    // アニメーションの間隔

        //======================================================================================
        //--  コンストラクタ
        //======================================================================================
        public BitmapAnimation(Bitmap bitmap, int imageBlock) {
            this.bitmap = bitmap;
            this.IMAGE_BLOCK = imageBlock;
            this.DRAW_SIZE = bitmap.getWidth() / imageBlock;

            //-- 描画範囲の格納
            int left = 0;
            int top = 0;
            int bottom = top + this.IMAGE_BLOCK;
            for ( int i = 0; i < this.DRAW_SIZE; i++ ) {
                int right = left + this.IMAGE_BLOCK;
                setRect(new Rect(left, top, right, bottom));  // 格納
                left = right;  // 更新
            }
        }

        //======================================================================================
        //--  描画範囲格納
        //======================================================================================
        public boolean setRect(Rect rect) {
            //-- 判定
            if ( this.srcRectList.size() > this.DRAW_SIZE ) { return false; }  // リストが満杯

            this.srcRectList.add(rect);  // 格納

            return true;
        }

        //======================================================================================
        //--  アニメーション間隔の設定
        //======================================================================================
        public boolean setInterval(int itb) {
            //-- 判定
            if ( itb < 0 ) { return false; }  // 無効値

            this.drawInterval = itb + 1;
            return true;
        }

        //======================================================================================
        //--  アニメーション
        //======================================================================================
        private int draw_num = 0;  // 描画対象の画像番号
        private long previousAnimationTime = System.currentTimeMillis();  // 以前描画番号draw_numを変更した時間

        public void drawAnimation(Canvas canvas, RectF locRect, Paint paint) {
            canvas.drawBitmap(this.bitmap, srcRectList.get(draw_num), locRect, paint);

            long numTime = System.currentTimeMillis();
            if (numTime - this.previousAnimationTime >= this.drawInterval) {
                this.previousAnimationTime = numTime;  // 時間の更新
                this.draw_num++;  this.draw_num %= this.DRAW_SIZE;   // 番号の更新
            }
        }
    }
}
