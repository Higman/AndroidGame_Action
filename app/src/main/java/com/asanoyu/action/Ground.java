package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by YU-YA on 2016/09/07.
 */
public class Ground {

    private int COLOR = Color.rgb(153, 76, 0);  // 茶色
    private Paint paint = new Paint();
    final Rect rect;

    public final Bitmap groundSrcBitmap;    // ソース
    public Bitmap groundDrawBitmap;   // 描画用
    public static final int GROUND_ONE_BLOCK_SIZE = 16;  // グラウンドの1ブロックのサイズ

    public Ground(Bitmap bitmap, int left, int top, int right, int bottom) {
        rect = new Rect(left, top, right, bottom);

        groundSrcBitmap = bitmap;

        if ( bitmap != null ) {
            int bWidth = right - left;
            int bHeight = bottom - top;
            this.groundDrawBitmap = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888);

            // Canvasの作成:描画先のBitmapを与える
            Canvas canvas = new Canvas(this.groundDrawBitmap);

            for (int y = 0; y < bHeight; y += GROUND_ONE_BLOCK_SIZE) {
                for (int x = 0; x < bWidth; x += GROUND_ONE_BLOCK_SIZE) {
                    canvas.drawBitmap(bitmap, x, y, null);
                }
            }
        }

        paint.setColor(COLOR);
        paint.setColor(Color.rgb(5+new Random().nextInt(251), 5+new Random().nextInt(251), 5+new Random().nextInt(251)));
    }

    public void draw(Canvas canvas) {
        if ( this.groundSrcBitmap != null ) {
            canvas.drawBitmap(this.groundDrawBitmap, rect.left, rect.top, null);
        } else {
            canvas.drawCircle(rect.centerX(), 65, 20, paint);
            canvas.drawCircle(rect.left, 50, 10, paint);
            canvas.drawCircle(rect.right, 80, 10, paint);
        }
    }

    public void changeWidth(int amplitude) {  // 幅の変更
        rect.inset(-amplitude/2, 0);
        rect.offset(amplitude/2, 0);

        //-- 画像作り直し
        if ( groundSrcBitmap != null ) {
            int bWidth = rect.right - rect.left;
            int bHeight = rect.bottom - rect.top;
            this.groundDrawBitmap = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888);

            // Canvasの作成:描画先のBitmapを与える
            Canvas canvas = new Canvas(this.groundDrawBitmap);

            for (int y = 0; y < bHeight; y += GROUND_ONE_BLOCK_SIZE) {
                for (int x = 0; x < bWidth; x += GROUND_ONE_BLOCK_SIZE) {
                    canvas.drawBitmap(groundSrcBitmap, x, y, null);
                }
            }
        }
    }

    public void move(int moveToLeft) {
        rect.offset(-moveToLeft, 0);
    }

    public boolean isShown(int width, int height) {
        if ( rect.top > height ) { rect.top = height - 10; }  // 画面回転時に縦状態の幅・高さが設定されるのでその補正
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

    //======================================================================================
    //--  Groundインスタンス生成クラス
    //======================================================================================
    private static class GroundFactory implements Runnable {
        private final ArrayList<Ground> groundStockList = new ArrayList<Ground>();
        private int maxStockSize = 5;  // Groundインスタンスの最大保持数

        private int maxWidth, minWidth;     // 幅
        private int maxHeight, minHeight;   // 高さ

        private Bitmap groundBitmap;

        //======================================================================================
        //--  コンスタンス
        //======================================================================================
        public GroundFactory(Bitmap bitmap, int maxW, int minW, int maxH, int minH) {
            this.maxWidth = maxW;
            this.minWidth = minW;
            this.maxHeight = maxH;
            this.minHeight = minH;

            this.groundBitmap = bitmap;
        }

        //======================================================================================
        //--  Thread
        //======================================================================================
        Random rand = new Random(System.currentTimeMillis());

        @Override
        public void run() {
            if ( this.groundStockList.size() < this.maxStockSize ) {
                int groundHeight = rand.nextInt(maxHeight-minHeight) + minHeight;
                //-- 高さをGround.GROUND_ONE_BLOCK_SIZEの倍数に補正
                groundHeight /= Ground.GROUND_ONE_BLOCK_SIZE;
                groundHeight *= Ground.GROUND_ONE_BLOCK_SIZE;

                int groundWidth = rand.nextInt(maxWidth-minWidth) + minWidth;
                //-- 幅をGround.GROUND_ONE_BLOCK_SIZEの倍数に補正
                groundWidth /= Ground.GROUND_ONE_BLOCK_SIZE;
                groundWidth *= Ground.GROUND_ONE_BLOCK_SIZE;

                groundStockList.add(new Ground(groundBitmap, 0, 0, groundWidth, groundHeight));
            }
        }

        //======================================================================================
        //--  Groundインスタンスの所得
        //======================================================================================
        public synchronized Ground getGround() {
            Ground ground = groundStockList.get(0);
            groundStockList.remove(ground);
            return ground;
        }

    }
}
