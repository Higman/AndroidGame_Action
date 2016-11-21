package com.asanoyu.action;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by YU-YA on 2016/09/07.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap playerBitmap;
    public static final int GROUND_MOVE_TO_LEFT = 10;
    private static final int GROUND_HEIGHT = 64;
    private static final long DRAW_INTERVAL = 1000 / 80;
    private static final int ADD_GROUND_COUNT = 5;
    private static final int ITEM_SIZE_MAX = 3;
    private static final int STANDARD_GROUND_WIDTH = 1500;   // Groundの幅の基準 (最大幅)
    private static final int STANDARD_BLANK_WIDTH = 450;   // Blankの幅の基準 (最大幅)
    private static final int GROUND_WIDTH_AMPLITUDE = 200;  // Groundの幅の振幅
    private static final int BLANK_WIDTH_AMPLITUDE = 250;  // Blankの幅の振幅
    private static final int GROUND_BLOCK_HEIGHT = 100;

    private static final int EFFECT_OBJECT_PROBABILITY = 10;  // EffectObjectが配置される確率 ( 1 / n )

    private int score;   // スコア
    private static int SCORE_SIZE = 100;  // スコア単位

    private Player player;

    private Ground lastGround;

    private static final Point PLAYER_START_POINT = new Point();   // 自機の初期スタート位置

    private static final float POWER_GAUGE_HEIGHT = 30;
    private static final Paint PAINT_POWER_GAUGE = new Paint();
    static { PAINT_POWER_GAUGE.setColor(Color.RED); }

    private final Context context;

    private final List<Ground> groundList = new ArrayList<>();
    private final Random rand = new Random(System.currentTimeMillis());
    private Bitmap groundBitmap;

    private Bitmap backgroundBitmap;

    private final List<EffectObject> effectObjects = new ArrayList<>();   // 効果付与物体のリスト

    /*    fps                                */

    public static final int INTERVAL = 500;
    public static final int LIST_SIZE = 4;
    private long mTime = 0;
    private int mCount = 0;
    private LinkedList<Float> mFpsList = new LinkedList<Float>();

    /*                                       */



    private final Player.Callback playerCallback = new Player.Callback() {
        @Override
        public Player.MoveDirection getDistanceFromObstacle(Player player) {
            int distanceFromWall = Integer.MAX_VALUE;
            int distanceFromGround = Integer.MAX_VALUE;

            for ( Ground ground : groundList ) {
                if ( ground.locRect.left > player.hitRect.right ) {
                    break;
                }
                if ( ground.locRect.right < player.hitRect.left ) {
                    continue;
                }

                boolean horizontalLeft = player.hitRect.left <= ground.locRect.right && player.hitRect.left >= ground.locRect.left;
                boolean horizontalRight = player.hitRect.right <= ground.locRect.right && player.hitRect.right >= ground.locRect.left;

                if ( horizontalRight ) {
                    // playerオブジェクトの一番近くにあるGroundオブジェクトまでの距離
                    if ( ground.isSolid() && ground.locRect.top < player.hitRect.bottom ) {
                        distanceFromWall = ground.locRect.left - player.hitRect.right;
                        int local = groundList.indexOf(ground)-1;
                        ground = groundList.get(local);
                    }

                    if ( ground.isSolid() ) {
                        int distanceFromGroundRight = ground.locRect.top - player.hitRect.bottom;

                        if (distanceFromGround > distanceFromGroundRight) {
                            distanceFromGround = distanceFromGroundRight;
                        }
                    }
                    break;
                } else if ( horizontalLeft ) {
                    if ( !ground.isSolid() ) {
                        distanceFromGround = Integer.MAX_VALUE;
                    } else {
                        distanceFromGround = ground.locRect.top - player.hitRect.bottom;
                    }
                }
            }

            return new Player.MoveDirection(distanceFromWall, distanceFromGround);
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

    private GageView gageView;  // ゲージ

    private int screenWidth;   // 画面サイズ
    private int screenHeight;  // 画面サイズ

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Ground.setSizeBitmap(this.groundBitmap, this.screenWidth, this.screenHeight);

        //- 自機の初期位置の計算
        PLAYER_START_POINT.x = (int)(this.screenWidth*0.4);
        PLAYER_START_POINT.y = this.screenHeight/2;

        //- GageView
        gageView = new GageView(context);
        gageView.gageMax = (int) MAX_TOUCH_TIME;
        gageView.gageMin = 0;
        relativeLayout.addView(gageView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //- 初期化
        init();

        //- スタート
        startDrawThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        System.out.println("Destroyed");
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
        player.gameOverMove();
        player.stop();
        handler.post(new Runnable() {
            @Override
            public void run() {
//                gameOverCallback.onGameOver();
                setRetryView();
            }
        });
    }

    public GameView(Context context, int screenWidth, int screenHeight) {
        super(context);

        //--- inflaterの取得
        this.inflater = LayoutInflater.from(context);

        this.context = context;

        //--- 画面サイズ
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        //--- Groundクラス用画像の読み込み
        this.groundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ground_desert);

        //--- 背景画像の読み込み
        this.backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_blue);
        Matrix matrix = new Matrix();

        //- 画像サイズ
        int srcBgWidth = this.backgroundBitmap.getWidth();
        int srcBgHeight = this.backgroundBitmap.getHeight();

        //- 拡大縮小率
        float widthScale = this.screenWidth / (float) srcBgWidth;
        float heightScale = this.screenHeight / (float) srcBgHeight;

        //-- 背景の幅と高さのどちらに大きさを合わせるかの分岐
        if (widthScale < heightScale) {
            matrix.postScale(heightScale, heightScale);
        } else {
            matrix.postScale(widthScale, widthScale);
        }

        backgroundBitmap = Bitmap.createBitmap(backgroundBitmap, 0, 0, srcBgWidth, srcBgHeight, matrix, false);

        getHolder().addCallback(this);
    }

    protected void drawGame(Canvas canvas) {

        //---- 背景
        canvas.drawColor(Color.WHITE);

        canvas.drawBitmap(this.backgroundBitmap, 0, 0,null);

        //---- 地面
        //-- 追加
        this.createGround(screenHeight, screenWidth);

        //-- 描画
        for ( int i = 0; i < groundList.size(); i++ ) {
            Ground ground = groundList.get(i);

            if ( ground.isAvailable() || groundList.get(groundList.indexOf(ground)+1).isAvailable() ) {
                ground.move(GROUND_MOVE_TO_LEFT);
                if ( ground.isShown(screenWidth, screenHeight) ) {
                    ground.draw(canvas);
                }
            } else {
                groundList.remove(ground);
                i--;
            }
        }

        int pre_playerX = player.hitRect.centerX(); //移動前のPlayerの位置

        //---- ゲームオーバー判定
        if ( player.hitRect.top > this.screenHeight || player.hitRect.right < 0 ) { gameOver(); }

        //---- Player
        player.move();

        //-- 描画
        player.draw(canvas);

        //-- スコア計算
        if ( !this.isGameOver.get() ) { score += this.GROUND_MOVE_TO_LEFT + player.hitRect.centerX() - pre_playerX; }

        //---- エフェクトオブジェクト
        //- 効果付与
        for ( int i = 0; i < effectObjects.size(); i++ ) {
            EffectObject effObj = effectObjects.get(i);
            if ( !effObj.isShown(screenWidth, screenHeight) ) { continue; }

            if ( effObj.isHit(player) ) {
                effObj.giveEffect(player);
            }
        }

        //-- 描画
        for ( int i = 0; i < effectObjects.size(); i++ ) {
            EffectObject effObj = effectObjects.get(i);

            if ( effObj.isAvailable() ) {
                effObj.move(GROUND_MOVE_TO_LEFT);
                if ( effObj.isShown(screenWidth, screenHeight) ) {
                    effObj.draw(canvas);
                }
            } else {
                effectObjects.remove(effObj);
                i--;
            }
        }

        //---- パワーゲージバー
        float elapsedTime;
        if ( touchDownStartTime > 0 ) {
            elapsedTime = System.currentTimeMillis() - touchDownStartTime;

            // canvas.drawRect(0, 0, width * (elapsedTime / MAX_TOUCH_TIME), POWER_GAUGE_HEIGHT, PAINT_POWER_GAUGE);
        } else {
            elapsedTime = 0;
        }

        //- gageViewに値を渡す
        gageView.setGageValue((int) elapsedTime);

        //---- FPS
        long time = System.currentTimeMillis();
        Paint text = new Paint();
        text.setTextSize(30);
        if (time - mTime >= INTERVAL) {
            final float fps = mCount * 1000 / (float) (time - mTime);
            mFpsList.offer(fps);
            while (mFpsList.size() > LIST_SIZE) {
                mFpsList.remove();
            }
            mTime = time;
            mCount = 0;

        } else {
            ++mCount;
        }

        Float[] fpss = mFpsList.toArray(new Float[0]);
        Arrays.sort(fpss);
        canvas.drawText(String.format("fps : max %4.1f      min %4.1f", fpss[fpss.length - 1], fpss[0]), 0, screenHeight-20, text);
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
                jumpPlayer(time);
                touchDownStartTime = 0;
                break;
        }

        return super.onTouchEvent(event);
    }

    private void jumpPlayer(float time) {
        if ( playerCallback.getDistanceFromObstacle(player).y > 0 ) {
            return;
        }

        player.jump(Math.min(time, MAX_TOUCH_TIME) / MAX_TOUCH_TIME);
    }

    //---- 地面 Ground の作成
    private void createGround(int height, int width) {
        if ( lastGround == null ) {
            this.groundList.clear();
            int top = height - GROUND_HEIGHT;
            lastGround = new Ground(0, top, width, height);
            groundList.add(lastGround);
            int LGRight = lastGround.locRect.right;
            lastGround = new Blank(LGRight, height-1, LGRight+ player.hitRect.width()+this.GROUND_MOVE_TO_LEFT+1, height);
            groundList.add(lastGround);
        }

        if ( groundList.get(groundList.indexOf(lastGround)-1).isShown(width, height) ) {
            for ( int i = 0; i < ADD_GROUND_COUNT; i++ ) {
                int left = lastGround.locRect.right;
                int right = left + STANDARD_GROUND_WIDTH;

                int top = height - rand.nextInt(height / this.GROUND_BLOCK_HEIGHT) * this.GROUND_BLOCK_HEIGHT / 2 + this.GROUND_HEIGHT;

                int itemRangeRectBottom;  // アイテム出現範囲の矩形の下限

                if ( rand.nextInt(3) != 0 || lastGround.getKind() == "Blank" ) {
                    lastGround = new Ground(left, top, right, height);
                    lastGround.changeWidth(-rand.nextInt(GROUND_WIDTH_AMPLITUDE));
                    right = lastGround.locRect.right;
                    itemRangeRectBottom = lastGround.locRect.top;
                } else {
                    right = left + STANDARD_BLANK_WIDTH;
                    lastGround = new Blank(left, height-1, right, height);
                    lastGround.changeWidth(-rand.nextInt(BLANK_WIDTH_AMPLITUDE));
                    right = lastGround.locRect.right;
                    itemRangeRectBottom = height/2;
                }

                //--- エフェクトオブジェクトの配置
                if ( effectObjects.size() < ITEM_SIZE_MAX ) {
                    int randItem = rand.nextInt(EFFECT_OBJECT_PROBABILITY);
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
        int height = this.screenHeight;
        int width = this.screenWidth;

        //-- Playerの初期化
        this.playerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dot_tank_group);
        this.player = new Player(playerBitmap, PLAYER_START_POINT.x, PLAYER_START_POINT.y, width, playerCallback);
        this.player.setBitmapJ(BitmapFactory.decodeResource(getResources(), R.drawable.dot_tank_jump));  // ジャンプ時アニメーションの設定
        this.player.setSmoke(BitmapFactory.decodeResource(getResources(), R.drawable.tank_smoke));       // 煙の設定

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

        //-- スコアの初期化
        score = 0;

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
        final View retryView = inflater.inflate(R.layout.retry_or_end, null);

        retryView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        this.relativeLayout.addView(retryView);
        this.relativeLayout.setBackgroundColor(Color.argb(30, 0, 0, 0xFF));

        retryButton = (Button) retryView.findViewById(R.id.rt_button);

        retryEndText = (TextView) retryView.findViewById(R.id.roe_text);
        retryEndText.setText("GameOver");

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDrawThread();
                init();
                startDrawThread();
                relativeLayout.removeView(retryView);
            }
        });

        endButton = (Button) retryView.findViewById(R.id.ed_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDrawThread();
                relativeLayout.removeView(retryView);
                gameOverCallback.onGameOver();
            }
        });
    }

    //-- EffectObjectの作成
    public EffectObject createEffectObject(int left, int top, int right, int bottom) {
        EffectObject effectObject;

        Bitmap itemBitmap;

        //--- Objectの座標の決定
        int itemLeft = left + rand.nextInt(right - left - EffectObject.IMAGE_SIZE);
        int itemTop = top + rand.nextInt(bottom - EffectObject.IMAGE_SIZE);

        //--- Objectの種類の決定
        int ObjectNum = rand.nextInt(EffectObject.EffectItem.values().length);

        switch ( ObjectNum ) {
             case 0 : itemBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.item_group);
                      effectObject = new AccelerationItem(itemBitmap, itemLeft, itemTop);
                      break;
             case 1 : itemBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.item_group);
                      effectObject = new DecelerationItem(itemBitmap, itemLeft, itemTop);
                      break;
             default : effectObject = null;
        }

        return effectObject;
    }

    //======================================================================================
    //--  円ゲージクラス
    //======================================================================================
    public class GageView extends View {

        private int circleBodyRadius;   // ゲージの半径
        private Point circlePoint;  // ゲージの中心座標

        private Paint gageBodyPaint;  // ゲージの本体色
        private Paint gagePaint;      // ゲージの色

        private Paint textPaint;     //

        private int startGageAngle;   // ゲージのスタート位置（角度）
        private int endGageAngle;     // ゲージのエンド位置（角度）


        private int gageMax;   // ゲージの上限
        private int gageMin;   // ゲージの下限

        private int gageValue;  // ゲージの値

        public GageView(Context context) {
            super(context);

            init();
        }

        public GageView(Context context, AttributeSet attrs) {
            super(context, attrs);

           init();
        }

        //======================================================================================
        //--  初期化メソッド
        //======================================================================================
        public void init() {
            this.gageBodyPaint = new Paint();
            this.gageBodyPaint.setColor(Color.GRAY);
            this.gagePaint = new Paint();
            this.gagePaint.setColor(Color.RED);

            this.textPaint = new Paint();
            this.textPaint.setColor(Color.WHITE);
            this.textPaint.setTextSize(60.0f);

            this.circlePoint = new Point(0, 0);
            this.circleBodyRadius = 0;
            this.gageMax = 100;
            this.gageMin = 0;
            this.gageValue = 0;
            this.startGageAngle = 90;
            this.endGageAngle = 0;
        }

        private void setGageMax(int max) {
            this.gageMax = max;
        }

        private void setGageMin(int min) {
            this.gageMin = min;
        }

        public void setGageValue(int gageValue) {
            if ( gageValue < gageMin ) { gageValue = gageMin; }
            if ( gageValue > gageMax ) { gageValue = gageMax; }

            this.gageValue = gageValue;
        }

        public void setStartGageAngle(int startGageAngle) {
            this.startGageAngle = startGageAngle;
        }

        public void setEndGageAngle(int endGageAngle) {
            this.endGageAngle = endGageAngle;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            circleBodyRadius = h / 5;  // 半径の設定
            circlePoint.set(circleBodyRadius /4, circleBodyRadius /4);  // 中心座標の設定
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //--- ゲージの本体の描画
            canvas.drawCircle(circlePoint.x, circlePoint.y, circleBodyRadius, gageBodyPaint);

            //--- ゲージのバー描画
            //-- ゲージバー
            float gageRadius = circleBodyRadius * 3 / 4.0f;
            float left = circlePoint.x-gageRadius;
            float top = circlePoint.y-gageRadius;
            float right = circlePoint.x+gageRadius;
            float bottom = circlePoint.y+gageRadius;
            RectF rectF = new RectF(left, top, right, bottom);

            int diffMinMax = gageMax-gageMin;   // 最大値と最小値の差
            float gageRatio = gageValue / (float) diffMinMax;  // gageValueが占める割合

            int diffGageAngle = endGageAngle - startGageAngle;  // 差
            int gageAngle = (int) (diffGageAngle * gageRatio);

            gagePaint.setColor(Color.WHITE);
            canvas.drawArc(rectF, startGageAngle, diffGageAngle, true, gagePaint);
            gagePaint.setColor(Color.RED);
            canvas.drawArc(rectF, startGageAngle, gageAngle, true, gagePaint);

            //-- ゲージバーカバー
            float gageCoverRadius = gageRadius*3/4;  // ゲージカバーの半径
            canvas.drawCircle(circlePoint.x, circlePoint.y, gageCoverRadius, gageBodyPaint);

            //-- スコア
            String scoreStr = Integer.toString(score/SCORE_SIZE);
//            int lengthScore = scoreStr.length();  // 桁数

            textPaint.setTextSize(30.0f);
            canvas.drawText("Score", 5, 40, textPaint);
            textPaint.setTextSize(55.0f);
            canvas.drawText(scoreStr, 10, 100, textPaint);

            invalidate();
        }
    }
}
