package com.asanoyu.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by YU-YA on 2016/09/07.
 */
public class Ground {

    private int COLOR = Color.rgb(153, 76, 0);  // 茶色
    private Paint paint = new Paint();
    public final Rect srcRect;  // 描画元
    public final Rect locRect;  // 描画先

    public static Bitmap groundSrcBitmap;    // ソース
    public static Bitmap groundDrawBitmap;   // 描画用
    public static final int GROUND_ONE_BLOCK_SIZE = 16;  // グラウンドの1ブロックのサイズ

    public Ground(int left, int top, int right, int bottom) {
        srcRect = new Rect(0, 0, right-left, bottom-top);
        locRect = new Rect(left, top, right, bottom);

        paint.setColor(COLOR);
        paint.setColor(Color.rgb(5+new Random().nextInt(251), 5+new Random().nextInt(251), 5+new Random().nextInt(251)));
    }

    public static synchronized void setSizeBitmap(Bitmap bitmap, int bW, int bH) {
        groundSrcBitmap = bitmap;
        groundDrawBitmap = Bitmap.createBitmap(bW, bH, Bitmap.Config.ARGB_8888);

        // Canvasの作成:描画先のBitmapを与える
        Canvas canvas = new Canvas(groundDrawBitmap);

        for (int y = 0; y < bH; y += GROUND_ONE_BLOCK_SIZE) {
            for (int x = 0; x < bW; x += GROUND_ONE_BLOCK_SIZE) {
                canvas.drawBitmap(groundSrcBitmap, x, y, null);
            }
        }
    }

    public void draw(Canvas canvas) {
        if ( this.groundSrcBitmap != null && this.groundDrawBitmap != null ) {
            canvas.drawBitmap(this.groundDrawBitmap, srcRect, locRect, null);
        } else {
            canvas.drawCircle(locRect.centerX(), 65, 20, paint);
            canvas.drawCircle(locRect.left, 50, 10, paint);
            canvas.drawCircle(locRect.right, 80, 10, paint);
        }
    }

    public void changeWidth(int amplitude) {  // 幅の変更
        srcRect.inset(-amplitude/2, 0);
        srcRect.offset(amplitude/2, 0);
        locRect.inset(-amplitude/2, 0);
        locRect.offset(amplitude/2, 0);
    }

    public void move(int moveToLeft) {
        locRect.offset(-moveToLeft, 0);
    }

    public void groundSetTo(int newLeft, int newTop) {
        locRect.offsetTo(newLeft, newTop);
    }

    public boolean isShown(int width, int height) {
        if ( locRect.top > height ) { srcRect.bottom = this.GROUND_ONE_BLOCK_SIZE*3;  locRect.top = height - this.GROUND_ONE_BLOCK_SIZE*3; }  // 画面回転時に縦状態の幅・高さが設定されるのでその補正
        return locRect.intersects(0, 0, width, height);
    }

    public boolean isAvailable() {
        return (locRect.right > 0);
    }

    public boolean isSolid() {
        return true;
    }

    public String getKind() {
        return "Ground";
    }
}
