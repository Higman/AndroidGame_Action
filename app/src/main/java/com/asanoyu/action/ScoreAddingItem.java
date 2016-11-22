package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by YU-YA on 2016/10/12.
 */

public class ScoreAddingItem extends EffectObject {
    private static final int SCORE_ADDING_AMOUNT = 10000;  // 加算するスコア

    protected AtomicBoolean effecting;  // false:アイテム状態  true:効果付与中
    protected AtomicBoolean used;  // 使用されたかどうか

    protected int textTime = 2000;  // テキスト表示時間

    private static final String EFFECT_TEXT = "Score +" + Integer.toString(SCORE_ADDING_AMOUNT/GameView.SCORE_SIZE);  // 効果付与中に表示する文字列

    private static final Paint TEXT_PAINT = new Paint();
    static { TEXT_PAINT.setColor(Color.YELLOW); TEXT_PAINT.setTextSize(50); }

    //======================================================================================
    //--  コンストラクタ
    //======================================================================================
    public ScoreAddingItem(Bitmap bitmap, int imageNumber, int left, int top) {
        super(bitmap, imageNumber, left, top);
        effecting = new AtomicBoolean(false);
        used = new AtomicBoolean(false);
    }

    public ScoreAddingItem(Bitmap bitmap, int left, int top) {
        this(bitmap, EffectItem.SCORE_ADDING_ITEM.getInt(), left, top);
    }

    //======================================================================================
    //--  描画メソッド
    //======================================================================================
    @Override
    public void draw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        if ( !effecting.get() ) {
            canvas.drawBitmap(bitmap, srcRect, posRect, null);
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
    private final int effectingTime = 2000;  // 効果付与時間

    @Override
    public void giveEffect(Player player) {
        effecting.set(true);  // 状態を効果付与中に
        GameView.addScore(this.SCORE_ADDING_AMOUNT);  // スコアの加算
        Thread th = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(effectingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                effecting.set(false);
            }
        };
        th.start();
    }
}
