package com.asanoyu.action;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by YU-YA on 2016/09/07.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap droidBitmap;
    public static final int GROUND_MOVE_TO_LEFT = 10;
    private static final int GROUND_HEIGHT = 50;
    private static final long DRAW_INTERVAL = 1000 / 80;
    private static final int ADD_GROUND_COUNT = 5;
    private static final int ITEM_SIZE_MAX = 3;
    private static final int STANDARD_GROUND_WIDTH = 600;   // Groundの幅の基準 (最大幅)
    private static final int STANDARD_BLANK_WIDTH = 400;   // Blankの幅の基準 (最大幅)
    private static final int GROUND_WIDTH_AMPLITUDE = 200;  // Groundの幅の振幅
    private static final int BLANK_WIDTH_AMPLITUDE = 300;  // Blankの幅の振幅
    private static final int GROUND_BLOCK_HEIGHT = 100;

    private Droid droid;
    private Ground lastGround;

    private static final Point DROID_START_POINT = new Point();   // 自機の初期スタート位置

    private static final float POWER_GAUGE_HEIGHT = 30;
    private static final Paint PAINT_POWER_GAUGE = new Paint();
    static { PAINT_POWER_GAUGE.setColor(Color.RED); }

    private final List<Ground> groundList = new ArrayList<>();
    private final Random rand = new Random(System.currentTimeMillis());

    private final List<EffectObject> effectObjects = new ArrayList<>();   // 効果付与物体のリスト

    private final Droid.Callback droidCallback = new Droid.Callback() {
        @Override
        public Droid.MoveDirection getDistanceFromObstacle(Droid droid) {
            int distanceFromWall = Integer.MAX_VALUE;
            int distanceFromGround = Integer.MAX_VALUE;

            for ( Ground ground : groundList ) {
                if ( ground.rect.left > droid.hitRect.right ) {
                    break;
                }
                if ( ground.rect.right < droid.hitRect.left ) {
                    continue;
                }

                boolean horizontalLeft = droid.hitRect.left <= ground.rect.right && droid.hitRect.left >= ground.rect.left;
                boolean horizontalRight = droid.hitRect.right <= ground.rect.right && droid.hitRect.right >= ground.rect.left;

                if ( horizontalRight ) {
                    // droidオブジェクトの一番近くにあるGroundオブジェクトまでの距離
                    if ( ground.isSolid() && ground.rect.top < droid.hitRect.bottom ) {
                        distanceFromWall = ground.rect.left - droid.hitRect.right;
                        int local = groundList.indexOf(ground)-1;
                        ground = groundList.get(local);
                    }

                    if ( ground.isSolid() ) {
                        int distanceFromGroundRight = ground.rect.top - droid.hitRect.bottom;

                        if (distanceFromGround > distanceFromGroundRight) {
                            distanceFromGround = distanceFromGroundRight;
                        }
                    }
                    break;
                } else if ( horizontalLeft ) {
                    if ( !ground.isSolid() ) {
                        distanceFromGround = Integer.MAX_VALUE;
                    } else {
                        distanceFromGround = ground.rect.top - droid.hitRect.bottom;
                    }
                }
            }

            return new Droid.MoveDirection(distanceFromWall, distanceFromGround);
        }
    };


    private class DrawThread extends Thread {
        private final AtomicBoolean isFinished = new AtomicBoolean(false);

        public void finish() { isFinished.set(true); }

        @Override
        public void run() {
            SurfaceHolder holder = getHolder();

            while ( !isFinished.get() ) {
                if ( holder.isCreating() ) {
                    continue;
                }

                Canvas canvas = holder.lockCanvas();
                if ( canvas == null ) {
                    continue;
                }
                long startTime = System.currentTimeMillis();

                drawGame(canvas);

                holder.unlockCanvasAndPost(canvas);

                long waitTime = DRAW_INTERVAL - (System.currentTimeMillis() - startTime);
                if ( waitTime > 0 ) {
                    synchronized (this) {
                        try {
                            wait(waitTime);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }
        }
    }

    private DrawThread drawThread;

    public void startDrawThread() {
        stopDrawThread();

        drawThread = new DrawThread();
        drawThread.start();
    }

    public boolean stopDrawThread() {
        if ( drawThread == null ) {
            return false;
        }
        drawThread.finish();
        drawThread = null;
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //- 自機の初期位置の計算
        DROID_START_POINT.x = (int)(getWidth()*0.6);
        DROID_START_POINT.y = getHeight()/2;

        //- 初期化
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startDrawThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }


    private final Handler handler = new Handler();

    public interface GameOverCallback {
        void onGameOver();
    }

    private GameOverCallback gameOverCallback;

    public void setCallback(GameOverCallback callback) {
        gameOverCallback = callback;
    }

    private final AtomicBoolean isGameOver = new AtomicBoolean();

    private void gameOver() {
        if ( isGameOver.get() ) {
            return;
        }

        isGameOver.set(true);
        droid.gameOverMove();
        droid.stop();
        handler.post(new Runnable() {
            @Override
            public void run() {
//                gameOverCallback.onGameOver();
                setRetryView();
            }
        });
    }

    public GameView(Context context) {
        super(context);

        // inflaterの取得
        this.inflater = LayoutInflater.from(context);

        getHolder().addCallback(this);
    }

    protected void drawGame(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //---- 地面
        //-- 追加
        this.createGround(height, width);

        //-- 描画
        for ( int i = 0; i < groundList.size(); i++ ) {
            Ground ground = groundList.get(i);

            if ( ground.isAvailable() || groundList.get(groundList.indexOf(ground)+1).isAvailable() ) {
                ground.move(GROUND_MOVE_TO_LEFT);
                if ( ground.isShown(width, height) ) {
                    ground.draw(canvas);
                }
            } else {
                groundList.remove(ground);
                i--;
            }
        }

        //---- アンドロイドロイド君
        droid.move();
        //-- ゲームオーバー判定
        if ( droid.hitRect.top > canvas.getHeight() || droid.hitRect.right < 0 ) { gameOver(); }

        //-- 描画
        droid.draw(canvas);

        //---- エフェクトオブジェクト
        //- 効果付与
        for ( int i = 0; i < effectObjects.size(); i++ ) {
            EffectObject effObj = effectObjects.get(i);
            if ( !effObj.isShown(width, height) ) { continue; }

            if ( effObj.isHit(droid) ) {
                effObj.giveEffect(droid);
            }
        }

        //-- 描画
        for ( int i = 0; i < effectObjects.size(); i++ ) {
            EffectObject effObj = effectObjects.get(i);

            if ( effObj.isAvailable() ) {
                effObj.move(GROUND_MOVE_TO_LEFT);
                if ( effObj.isShown(width, height) ) {
                    effObj.draw(canvas);
                }
            } else {
                effectObjects.remove(effObj);
                i--;
            }
        }

        //---- パワーゲージバー
        if ( touchDownStartTime > 0 ) {
            float elapsedTime = System.currentTimeMillis() - touchDownStartTime;
            canvas.drawRect(0, 0, width * (elapsedTime / MAX_TOUCH_TIME), POWER_GAUGE_HEIGHT, PAINT_POWER_GAUGE);
        }
    }

    private static final long MAX_TOUCH_TIME = 500;  // ミリ秒
    private long touchDownStartTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN :
                if ( this.isGameOver.get() ) { break; }
                touchDownStartTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP :
                float time = System.currentTimeMillis() - touchDownStartTime;
                jumpDroid(time);
                touchDownStartTime = 0;
                break;
        }

        return super.onTouchEvent(event);
    }

    private void jumpDroid(float time) {
        if ( droidCallback.getDistanceFromObstacle(droid).y > 0 ) {
            return;
        }

        droid.jump(Math.min(time, MAX_TOUCH_TIME) / MAX_TOUCH_TIME);
    }

    //---- 地面 Ground の作成
    private void createGround(int height, int width) {
        if ( lastGround == null ) {
            this.groundList.clear();
            int top = height - GROUND_HEIGHT;
            lastGround = new Ground(0, top, width, height);
            groundList.add(lastGround);
            int LGRight = lastGround.rect.right;
            lastGround = new Blank(LGRight, height-1, LGRight+droid.hitRect.width(), height);
            groundList.add(lastGround);
        }

        if ( groundList.get(groundList.indexOf(lastGround)-1).isShown(width, height) ) {
            for ( int i = 0; i < ADD_GROUND_COUNT; i++ ) {
                int left = lastGround.rect.right;
                int right;

                int groundHeight = rand.nextInt(height / GROUND_BLOCK_HEIGHT) * GROUND_BLOCK_HEIGHT / 2 + GROUND_HEIGHT;
                int top = height - groundHeight;

                int itemRangeRectBottom;  // アイテム出現範囲の矩形の下限

                if ( rand.nextInt(3) != 0 || lastGround.getKind() == "Blank" ) {
                    right = left + STANDARD_GROUND_WIDTH;
                    lastGround = new Ground(left, top, right, height);
                    if ( rand.nextInt(3) == 0 ) { lastGround.changeWidth(-rand.nextInt(GROUND_WIDTH_AMPLITUDE)); }
                    itemRangeRectBottom = top;
                } else {
                    right = left + STANDARD_BLANK_WIDTH;
                    lastGround = new Blank(left, height-1, right, height);
                    lastGround.changeWidth(-rand.nextInt(BLANK_WIDTH_AMPLITUDE));
                    itemRangeRectBottom = height/2;
                }

                //--- エフェクトオブジェクトの配置
                if ( effectObjects.size() < ITEM_SIZE_MAX ) {
                    int randItem = rand.nextInt(10);
                    if ( randItem == 0 ) {
                        // オブジェクトの作成・追加
                        effectObjects.add(createEffectObject(left, 0, right, itemRangeRectBottom));
                    }
                }
                groundList.add(lastGround);
            }
        }
    }

    //-- 各フィールドをゲーム開始時の状態にするメソッド
    public void init() {
        int height = getHeight();
        int width = getWidth();

        //-- Droidの初期化
        this.droidBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droidtwins);
        this.droid = new Droid(droidBitmap, DROID_START_POINT.x, DROID_START_POINT.y, width, droidCallback);

        //-- 地面オブジェクト
        this.groundList.removeAll(this.groundList);

        // EffectObjectの削除
        this.effectObjects.removeAll(this.effectObjects);

        // 初期化
        this.lastGround = null;
        // 地面の作成
        createGround(height, width);

        //-- 時間の初期化
        this.touchDownStartTime = 0;

        //-- GameOver関連
        this.isGameOver.set(false);
    }

    //-- リトライ・終了確認画面の表示
    private RelativeLayout relativeLayout;
    private LayoutInflater inflater;
    private TextView retryEndText;
    private Button retryButton;
    private Button endButton;

    public void setFlame(RelativeLayout relativeLayout) {
        this.relativeLayout = relativeLayout;
    }

    private void setRetryView() {
        final View view = inflater.inflate(R.layout.retry_or_end, null);

        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        this.relativeLayout.addView(view);
        this.relativeLayout.setBackgroundColor(Color.argb(30, 0, 0, 0xFF));

        retryButton = (Button) view.findViewById(R.id.rt_button);

        retryEndText = (TextView) view.findViewById(R.id.roe_text);
        retryEndText.setText("GameOver");

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDrawThread();
                init();
                startDrawThread();

                relativeLayout.removeView(view);
            }
        });

        endButton = (Button) view.findViewById(R.id.ed_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDrawThread();
                gameOverCallback.onGameOver();
            }
        });
    }

    //-- EffectObjectの作成
    private static final int NUMBER_OF_EFFECT_OBJECT = EffectObject.EffectItem.values().length;

    public EffectObject createEffectObject(int left, int top, int right, int bottom) {
        EffectObject effectObject;

        Bitmap itemBitmap;

        //--- Objectの座標の決定
        int itemLeft = left + rand.nextInt(right - left - EffectObject.IMAGE_BLOCK);
        int itemTop = top + rand.nextInt(bottom - EffectObject._IMAGE_BLOCK_2);

        //--- Objectの種類の決定
        int ObjectNum = rand.nextInt(NUMBER_OF_EFFECT_OBJECT);

        switch ( ObjectNum ) {
             case 0 : itemBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droid);
                      effectObject = new AccelerationItem(itemBitmap, itemLeft, itemTop);
                      break;
             default : effectObject = null;
        }

        return effectObject;
    }

//    //---- X軸のleft ～ rightの範囲にEffectObjectを作成
//    private EffectObject createEffectObject(int left, int right) {
//        EffectObject effectObject;
//        Bitmap itemBitmap;
//
//        //--- Objectの種類の決定
//        int ObjectNum = rand.nextInt(NUMBER_OF_EFFECT_OBJECT);
//
//        switch ( ObjectNum ) {
//            case 0 : itemBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droid);
//                     effectObject = new AccelerationItem(itemBitmap, 0, 0);
//        }
//
//        //--- 位置の決定
//        //-- X軸
//        int itemLeft = left + rand.nextInt(right - left);
//        int itemRight = itemLeft + effectObject.positionRect.right;
//
//        //-- Y軸
//        //- 上限の決定
//        int itemBottom = getHeight();
//        for ( Ground ground : groundList ) {
//            boolean horizontalLeft = itemLeft <= ground.rect.right && itemLeft >= ground.rect.left;
//            boolean horizontalRight = itemRight <= ground.rect.right && itemRight >= ground.rect.left;
//
//            if ( ground.isSolid() ) {
//                if ( horizontalRight ) {
//                    if ( itemBottom > ground.rect.top ) {
//                        itemBottom = ground.rect.top;
//                    }
//                    break;
//                } else if ( horizontalLeft ) {
//                    if ( itemBottom > ground.rect.top ) {
//                        itemBottom = ground.rect.top;
//                    }
//                }
//            }
//        }
//    }
}
