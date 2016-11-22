package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by YU-YA2 on 2016/10/10.
 */

public abstract class EffectObject {

    protected Paint paint = new Paint();
    protected final Rect srcRect;     // 描写元の位置・サイズ
    protected final Rect locRect;  // 画面描写先の位置・サイズ
    protected static Bitmap itemSrcBitmap = null;   // ソース
    protected static Bitmap itemDrawBitmap = null;  // 描画用画像

    public static final int IMAGE_BLOCK = 64;   // 描画元の画像のサイズ(1オブジェクトあたり)
    public static final int IMAGE_SIZE = 100;    // 描画するサイズ


    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public EffectObject(int itemNumber, int left, int top) {
        int bottom = top + this.IMAGE_SIZE;
        int right = left + this.IMAGE_SIZE;
        this.locRect = new Rect(left, top, right, bottom);
        this.srcRect = new Rect();
        if ( itemDrawBitmap != null ) {
            int imageNumberOfItemX = itemDrawBitmap.getWidth() / this.IMAGE_SIZE;  // 画像の1行に存在するアイテムの数
            int imagePositionX = this.IMAGE_SIZE * (itemNumber % imageNumberOfItemX);
            int imagePositionY = this.IMAGE_SIZE * (itemNumber / imageNumberOfItemX);
            this.srcRect.set(imagePositionX, imagePositionY,
                    imagePositionX+this.IMAGE_SIZE, imagePositionY+this.IMAGE_SIZE);
        }
    }

    //======================================================================================
    //--  アイテム判定用列挙体
    //======================================================================================
    public static enum EffectItem {
        ACCELERATION_ITEM(0),
        DECELERATION_ITEM(1),
        SCORE_ADDING_ITEM(2);

        private final int id;

        private EffectItem(final int id) {
            this.id = id;
        }

        public int getInt() {
            return this.id;
        }
    }

    //======================================================================================
    //--  移動メソッド
    //======================================================================================
    public void move(int moveToLeft) {
        locRect.offset(-moveToLeft, 0);
    }

    //======================================================================================
    //--  描画メソッド
    //======================================================================================
    public void draw(Canvas canvas) {
        if ( itemDrawBitmap != null ) {
            canvas.drawBitmap(itemDrawBitmap, srcRect, locRect, null);
        } else {
            canvas.drawRect(locRect, paint);
        }
    }

    //======================================================================================
    //--  アイテム画像指定メソッド
    //======================================================================================
    public static void setItemDrawBitmap(Bitmap bitmap) {
        itemSrcBitmap = bitmap;

        Matrix matrix = new Matrix();
        matrix.postScale(IMAGE_SIZE/(float) IMAGE_BLOCK, IMAGE_SIZE/(float) IMAGE_BLOCK);
        itemDrawBitmap = Bitmap.createBitmap(itemSrcBitmap, 0, 0, itemSrcBitmap.getWidth(), itemSrcBitmap.getHeight(), matrix, false);
    }

    //======================================================================================
    //--  位置修正メソッド
    //======================================================================================
    public void setPosition(int top, int left) {
        this.locRect.offsetTo(top, left);
    }

    //======================================================================================
    //--  描画判定メソッド
    //======================================================================================
    public boolean isShown(int width, int height) {
        return this.locRect.intersects(0, 0, width, height);
    }

    //======================================================================================
    //--  利用判定メソッド
    //======================================================================================
    public boolean isAvailable() {
        return (this.locRect.right > 0);
    }

    //======================================================================================
    //--  当たり判定メソッド
    //======================================================================================
    public boolean isHit(Player player) { return Rect.intersects(locRect, player.hitRect); }

    //======================================================================================
    //--  効果付与メソッド
    //======================================================================================
    public abstract void giveEffect(Player player);
}
