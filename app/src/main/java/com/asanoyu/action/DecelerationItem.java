package com.asanoyu.action;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by YU-YA on 2016/11/14.
 */

public class DecelerationItem extends AccelerationItem {

    private static final int DECELERATION_AMOUNT = -1;  // 加算する移動する移動速度

    private static final String EFFECT_TEXT = "Speed Down!!";  // 効果付与中に表示する文字列

    private static final Paint TEXT_PAINT = new Paint();
    static { TEXT_PAINT.setColor(Color.BLUE); TEXT_PAINT.setTextSize(50); }

    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public DecelerationItem(int left, int top) {
        super(EffectItem.DECELERATION_ITEM.getInt(), left, top);
        effectingTime = 2000;
    }

    //======================================================================================
    //--  描画メソッド
    //======================================================================================
    @Override
    public void draw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        if ( !effecting.get() ) {
            canvas.drawBitmap(itemDrawBitmap, srcRect, locRect, TEXT_PAINT);
        } else {
            canvas.drawText(this.EFFECT_TEXT, width/2-TEXT_PAINT.measureText(this.EFFECT_TEXT)/2, 150, TEXT_PAINT);
        }
    }

    //======================================================================================
    //--  効果付与メソッド
    //======================================================================================
    @Override
    public void giveEffect(Player player) {
        ItemEffect itemEffect = new ItemEffect(player, this.DECELERATION_AMOUNT, effectingTime);
        itemEffect.start();
    }
}
