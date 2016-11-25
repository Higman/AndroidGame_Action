package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;


/**
 * Created by YU-YA on 2016/09/07.
 */
public class Player {
    private final Paint paint = new Paint();

    private static final int IMAGE_SIZE = 200;
    private static final int SMOKE_IMAGE_SIZE = 150;
    private static final int BLOCK_SIZE = 64;

    private BitmapAnimation bitmapA;
    private Bitmap srcBitmapJ;           // ジャンプ時の画像の元画像
    private Bitmap drawBitmapJ;          // 描画用
    private static final Rect PLAYER_JUMPING_RECT = new Rect(0, 0, BLOCK_SIZE, BLOCK_SIZE);

    private BitmapAnimation bitmapA_smoke;  // 煙アニメーション
    private final RectF smokeRect = new RectF();

    private static final int HIT_MARGIN_LEFT = 4;
    private static final int HIT_MARGIN_RIGHT = 2;
    private static final int HIT_MARGIN_TOP = 17;
    private static final int HIT_MARGIN_BOTTOM = 16;

    private static final int HIT_JUMPING_MARGIN_LEFT = 3;
    private static final int HIT_JUMPING_MARGIN_RIGHT = 6;
    private static final int HIT_JUMPING_MARGIN_TOP = 12;
    private static final int HIT_JUMPING_MARGIN_BOTTOM = 16;

    private static final float GRAVITY = 0.8f;
    private static final float WEIGHT = GRAVITY * 40;

    private int animationInterval = 750;   // アニメーション間隔

    private float velocity = 0;

    final RectF rect;
    final Rect hitRect;

    final Rect hitRectN;
    final Rect hitRectJ;

    public static final int DEFAULT_PLAYER_MOVE_LEFT = 0;
    private int playerMoveToLeft;  // 自機が画面上を移動するX軸上の速度

    private int rightEnd;

    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public Player(Bitmap bitmap, int left, int top, int rightEnd, Callback callback) {
        int right = left + this.IMAGE_SIZE;
        int bottom = top + this.IMAGE_SIZE;
        double imageRatio = this.IMAGE_SIZE / (double)this.BLOCK_SIZE;  // 拡大比率

        //--- 描画位置・当たり判定矩形
        this.hitRectN = new Rect(left, top, right, bottom);
        this.hitRectN.left += Math.round(HIT_MARGIN_LEFT * imageRatio);
        this.hitRectN.right -= Math.round(HIT_MARGIN_RIGHT * imageRatio);
        this.hitRectN.top += Math.round(HIT_MARGIN_TOP * imageRatio);
        this.hitRectN.bottom -= Math.round(HIT_MARGIN_BOTTOM * imageRatio);

        this.hitRectJ = new Rect(left, top, right, bottom);
        this.hitRectJ.left += Math.round(HIT_JUMPING_MARGIN_LEFT * imageRatio);
        this.hitRectJ.right -= Math.round(HIT_JUMPING_MARGIN_RIGHT * imageRatio);
        this.hitRectJ.top += Math.round(HIT_JUMPING_MARGIN_TOP * imageRatio);
        this.hitRectJ.bottom -= Math.round(HIT_JUMPING_MARGIN_BOTTOM * imageRatio);

        this.rect = new RectF(left, top, right, bottom);
        this.hitRect = new Rect();
        this.hitRect.set(hitRectN);

        //--- アニメーション関連
        this.bitmapA = new BitmapAnimation(bitmap, this.BLOCK_SIZE);
        this.bitmapA.setInterval(this.animationInterval);  // アニメーション間隔の設定

        this.rightEnd = rightEnd;
        this.callback = callback;
        this.playerMoveToLeft = DEFAULT_PLAYER_MOVE_LEFT;  // 移動
    }

    public interface Callback {
        Player.MoveDirection getDistanceFromObstacle(Player player);
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

    //======================================================================================
    //--  描画メソッド
    //======================================================================================
    public void draw(Canvas canvas) {
        //--- 煙の描画
        if ( bitmapA_smoke != null ) {
            bitmapA_smoke.drawAnimation(canvas, smokeRect, paint);
        }

        //--- 自機の描画
        if ( srcBitmapJ != null && velocity != 0 ) {
            canvas.drawBitmap(srcBitmapJ, this.PLAYER_JUMPING_RECT, this.rect, paint);
        } else {
            bitmapA.drawAnimation(canvas, this.rect, paint);
        }

//        paint.setColor(Color.argb(100, 100, 0, 100));
//        canvas.drawRect(rect, paint);
//        paint.setColor(Color.argb(100, 100, 100, 40));
//        canvas.drawRect(hitRect, paint);
//        paint.setColor(Color.argb(100, 100, 00, 00));
//        canvas.drawRect(hitRectN, paint);
//        paint.setColor(Color.argb(100, 0, 100, 00));
//        canvas.drawRect(hitRectJ, paint);
//        paint.setColor(Color.argb(255, 0, 0, 0));

//        paint.setColor(Color.CYAN);
//        canvas.drawLine(srcRect.left, srcRect.centerY(), srcRect.left, srcRect.centerY()-100, paint);
//        canvas.drawLine(srcRect.right, srcRect.centerY(), srcRect.right, srcRect.centerY()-100, paint);
//
//        paint.setColor(Color.BLUE);
//        canvas.drawLine(hitRect.left, srcRect.centerY(), hitRect.left, srcRect.centerY()-100, paint);
//        canvas.drawLine(hitRect.right, srcRect.centerY(), hitRect.right, srcRect.centerY()-100, paint);
    }

    public void setSrcBitmapJ(Bitmap bitmap) {
        this.srcBitmapJ = bitmap;

        //-- 画像加工
        Matrix matrix = new Matrix();
        //- 拡大率の計算
        int width = this.srcBitmapJ.getWidth();
        int height = this.srcBitmapJ.getHeight();
        float scale = this.SMOKE_IMAGE_SIZE / height;
        matrix.postScale(scale, scale);

        this.drawBitmapJ = Bitmap.createBitmap(this.srcBitmapJ, 0, 0, width, height, matrix, false);  // 作成
    }

    public void setSmoke(Bitmap bitmap) {
        this.bitmapA_smoke = new BitmapAnimation(bitmap, BLOCK_SIZE);
        this.bitmapA_smoke.setInterval(animationInterval);
        this.smokeRect.set(hitRectN.left-this.SMOKE_IMAGE_SIZE, hitRectN.bottom-this.SMOKE_IMAGE_SIZE, hitRectN.left, hitRectN.bottom);
    }

    public void jump(float power) { velocity = (power * WEIGHT); }

    public void stop() { velocity = 0; }

    private void moveRectAll(int dx, int dy) {
        rect.offset(dx, dy);
        hitRectN.offset(dx, dy);
        hitRectJ.offset(dx, dy);
        smokeRect.offset(dx, dy);
    }

    public void move() {

        MoveDirection moveDirection = callback.getDistanceFromObstacle(this);
        int distanceFromGround = moveDirection.y;
        int distanceFromWall = moveDirection.x;

        if ( velocity < 0 && velocity < -distanceFromGround ) {
            velocity = -distanceFromGround;
        }

        //-- 移動
        moveRectAll(this.playerMoveToLeft, Math.round(-1*velocity));

        //-- 補正
        if ( distanceFromWall < 0 ) {
            moveRectAll(distanceFromWall, 0);
        }

        if ( hitRect.right > rightEnd ) {
            int endMoveLeft = rightEnd - hitRect.right;
            moveRectAll(endMoveLeft, 0);
        }

        if ( distanceFromGround == 0 ) {
            hitRect.set(hitRectN);
            return;
        } else if ( distanceFromGround < 0 ) {
            moveRectAll(0, distanceFromGround);
            return;
        }

        hitRect.set(hitRectJ);

        velocity -= GRAVITY;
    }

    public void setLocation(int newLeft, int newTop) {
        int dx = newLeft - Math.round(rect.left);
        int dy = newTop - Math.round(rect.top);

        rect.offset(dx, dy);
        hitRectN.offset(dx, dy);
        hitRectJ.offset(dx, dy);
        smokeRect.offset(dx, dy);
    }

    public void gameOverMove() {
        rect.offset(Integer.MAX_VALUE, Integer.MAX_VALUE);
        hitRectN.offset(Integer.MAX_VALUE, Integer.MAX_VALUE);
        hitRectJ.offset(Integer.MAX_VALUE, Integer.MAX_VALUE);
        hitRect.offset(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    //----- setter/getter
    public void setPlayerMoveToLeft(int playerMoveToLeft) {
        //--- 補正
        if ( playerMoveToLeft < -10 ) { playerMoveToLeft = -10; }
        if ( playerMoveToLeft > 10 ) { playerMoveToLeft = 10; }

        //--- 処理
        this.playerMoveToLeft = playerMoveToLeft;
    }

    public int getPlayerMoveToLeft() {
        return playerMoveToLeft;
    }

    public class BitmapAnimation {

        private Bitmap srcBitmap;   // 元画像
        private Bitmap drawBitmap;  // 描画画像
        private final int IMAGE_BLOCK;  // 1つのアニメーションの大きさ
        private final int DRAW_SIZE;  // 画像内にあるアニメーション枚数

        private ArrayList<Rect> srcRectList = new ArrayList<Rect>();

        private int drawInterval = 1;    // アニメーションの間隔

        //======================================================================================
        //--  コンストラクタ
        //======================================================================================
        public BitmapAnimation(Bitmap srcBitmap, int imageBlock) {
            this.srcBitmap = srcBitmap;
            this.IMAGE_BLOCK = imageBlock;
            int width = this.srcBitmap.getWidth();
            int height = this.srcBitmap.getHeight();
            this.DRAW_SIZE = width / imageBlock;

            //-- 画像加工
            Matrix matrix = new Matrix();
            //- 拡大率の計算
            float scale = this.IMAGE_BLOCK / height;
            matrix.postScale(scale, scale);

            this.drawBitmap = Bitmap.createBitmap(this.srcBitmap, 0, 0, width, height, matrix, false);  // 作成

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
        public boolean setInterval(int itv) {
            //-- 判定
            if ( itv < 0 ) { return false; }  // 無効値

            this.drawInterval = itv + 1;
            return true;
        }

        //======================================================================================
        //--  アニメーション
        //======================================================================================
        private int draw_num = 0;  // 描画対象の画像番号
        private long previousAnimationTime = System.currentTimeMillis();  // 以前描画番号draw_numを変更した時間

        public void drawAnimation(Canvas canvas, RectF locRect, Paint paint) {
            canvas.drawBitmap(this.drawBitmap, srcRectList.get(draw_num), locRect, paint);

            long numTime = System.currentTimeMillis();
            if (numTime - this.previousAnimationTime >= this.drawInterval) {
                this.previousAnimationTime = numTime;  // 時間の更新
                this.draw_num++;  this.draw_num %= this.DRAW_SIZE;   // 番号の更新
            }
        }
    }
}
