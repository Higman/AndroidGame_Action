package com.asanoyu.action;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by YU-YA on 2016/10/12.
 */

public class AccelerationItem extends EffectObject {
    private static final int ACCELERATION_AMOUNT = 1;  // 加算する移動する移動速度

    protected AtomicBoolean effecting;  // false:アイテム状態  true:効果付与中
    protected AtomicBoolean used;  // 使用されたかどうか

    protected int effectingTime = 5000;  // 効果付与時間 (ミリ秒)

    private static final String EFFECT_TEXT = "Speed UP!!";  // 効果付与中に表示する文字列

    private static final Paint TEXT_PAINT = new Paint();
    static { TEXT_PAINT.setColor(Color.RED); TEXT_PAINT.setTextSize(50); }

    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public AccelerationItem(int itemNumber, int left, int top) {
        super(itemNumber, left, top);
        effecting = new AtomicBoolean(false);
        used = new AtomicBoolean(false);
    }

    public AccelerationItem(int left, int top) {
        this(EffectItem.ACCELERATION_ITEM.getInt(), left, top);
    }

    //======================================================================================
    //--  描画メソッド
    //======================================================================================
    @Override
    public void draw(Canvas canvas) {
        int width = canvas.getWidth();

        if ( !effecting.get() ) {
            canvas.drawBitmap(itemDrawBitmap, srcRect, locRect, TEXT_PAINT);
        } else {
            canvas.drawText(this.EFFECT_TEXT, width/2-TEXT_PAINT.measureText(this.EFFECT_TEXT)/2, 150, TEXT_PAINT);
        }
    }

    //======================================================================================
    //--  当たり判定メソッド
    //======================================================================================
    @Override
    public boolean isHit(Player player) {
        if (super.isHit(player) && !effecting.get()) {
            return true;
        } else { return false; }
    }

    //======================================================================================
    //--  利用判定メソッド
    //======================================================================================
    @Override
    public boolean isAvailable() {
        return (super.isAvailable() || effecting.get());
    }

    //======================================================================================
    //--  描画判定メソッド
    //======================================================================================
    @Override
    public boolean isShown(int width, int height) {
        return ((super.isShown(width, height) || effecting.get()) && !used.get());
    }

    //======================================================================================
    //--  効果付与メソッド
    //======================================================================================
    @Override
    public void giveEffect(Player player) {
        ItemEffect itemEffect = new ItemEffect(player, ACCELERATION_AMOUNT, effectingTime);
        itemEffect.start();
    }

    //======================================================================================
    //======================================================================================
    //--  効果クラス
    //======================================================================================
    //======================================================================================
    public class ItemEffect extends Thread {
        private Player player;
        private int amount;
        private int waitTime;

        public ItemEffect(Player player, int amount, int waitTime) {
            this.player = player;
            this.amount = amount;
            this.waitTime = waitTime;
        }

        @Override
        public void run() {
            //--- 効果の反映
            player.setPlayerMoveToLeft(player.getPlayerMoveToLeft()+amount);
            effecting.set(true);  // 状態を効果付与中に

            synchronized ( this ) {
                try {
                    wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //--- 効果の終了
            player.setPlayerMoveToLeft(player.getPlayerMoveToLeft()-amount);
            effecting.set(false);
            used.set(true);
        }
    }
}
