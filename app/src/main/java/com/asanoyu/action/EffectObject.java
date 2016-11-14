package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by YU-YA2 on 2016/10/10.
 */

public abstract class EffectObject {

    protected Paint paint = new Paint();
    protected final Rect srcRect;     // 描写元の位置・サイズ
    protected final Rect posRect;  // 画面描写先の位置・サイズ
    protected Bitmap bitmap;
    protected int OBJECT_MOVE_TO_LEFT = 10;
    public static final int IMAGE_BLOCK = 400;
    public static final int _IMAGE_BLOCK_2 = 143;  // あとで消す
    public static final int IMAGE_SIZE = 100;

    public EffectObject(Bitmap bitmap, int imageNumber, int left, int top) {
        int imageNumberOfItemX = bitmap.getWidth() / this.IMAGE_BLOCK;  // 画像の1行に存在するアイテムの数
        int imagePositionX = this.IMAGE_BLOCK * (imageNumber / imageNumberOfItemX);
        int imagePositionY = this.IMAGE_BLOCK * (imageNumber % imageNumberOfItemX);
        this.srcRect = new Rect(imagePositionX, imagePositionY,
                imagePositionX+IMAGE_BLOCK, imagePositionY+IMAGE_BLOCK);
        int bottom = top + this.IMAGE_SIZE;
        int right = left + this.IMAGE_SIZE;
        this.posRect = new Rect(left, top, right, bottom);
        this.bitmap = bitmap;
    }

    public static enum EffectItem {
        ACCELERATION_ITEM(0),
        DECELERATION_ITEM(0);

        private final int id;

        private EffectItem(final int id) {
            this.id = id;
        }

        public int getInt() {
            return this.id;
        }

    }

    public void move(int moveToLeft) {
        posRect.offset(-moveToLeft, 0);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, srcRect, posRect, paint);
    }

    public void setPosition(int top, int left) {
        this.posRect.offsetTo(top, left);
    }

    public boolean isShown(int width, int height) {
        return this.posRect.intersects(0, 0, width, height);
    }

    public boolean isAvailable() {
        return (this.posRect.right > 0);
    }

    public boolean isHit(Droid droid) { return Rect.intersects(posRect, droid.hitRect); }

    public abstract void giveEffect(Droid droid);
}
