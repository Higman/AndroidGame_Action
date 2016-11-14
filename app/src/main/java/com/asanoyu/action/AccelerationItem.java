package com.asanoyu.action;

import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by YU-YA on 2016/10/12.
 */

public class AccelerationItem extends EffectObject {
    private static final int ACCELERATION_AMOUNT = 1;  // 加算する移動する移動速度

    private AtomicBoolean effecting;  // false:アイテム状態  true:効果付与中
    private AtomicBoolean used;  // 使用されたかどうか

    private static final String EFFECT_TEXT = "Speed UP!!";  // 効果付与中に表示する文字列

    private static final Paint TEXT_PAINT = new Paint();
    static { TEXT_PAINT.setColor(Color.RED); TEXT_PAINT.setTextSize(50); }

    public AccelerationItem(Bitmap bitmap, int left, int top) {
        super(bitmap, EffectItem.ACCELERATION_ITEM.getInt(), left, top);
        effecting = new AtomicBoolean(false);
        used = new AtomicBoolean(false);
    }

    @Override
    public void draw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        if ( !effecting.get() ) {
            super.draw(canvas);
            canvas.drawRect(positionRect, TEXT_PAINT);
        } else {
            canvas.drawText(this.EFFECT_TEXT, width/2-TEXT_PAINT.measureText(this.EFFECT_TEXT)/2, 150, TEXT_PAINT);
        }
    }

    @Override
    public boolean isHit(Droid droid) {
        if (super.isHit(droid) && !effecting.get()) {
            return true;
        } else { return false; }
    }

    @Override
    public boolean isAvailable() {
        return (super.isAvailable() || effecting.get());
    }

    @Override
    public boolean isShown(int width, int height) {
        return ((super.isShown(width, height) || effecting.get()) && !used.get());
    }

    @Override
    public void giveEffect(Droid droid) {
        ItemEffect itemEffect = new ItemEffect(droid);
        itemEffect.start();
    }

    public class ItemEffect extends Thread {
        Droid droid;

        public ItemEffect(Droid droid) {
            this.droid = droid;
        }

        @Override
        public void run() {
            //--- 効果の反映
            droid.setDroidMoveToLeft(droid.getDroidMoveToLeft()+ACCELERATION_AMOUNT);
            effecting.set(true);  // 状態を効果付与中に

            synchronized ( this ) {
                try {
                    wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //--- 効果の終了
            droid.setDroidMoveToLeft(droid.getDroidMoveToLeft()-ACCELERATION_AMOUNT);
            effecting.set(false);
            used.set(true);
        }
    }
}
