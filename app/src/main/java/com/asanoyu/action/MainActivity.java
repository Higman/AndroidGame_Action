package com.asanoyu.action;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements GameView.GameOverCallback {

    private Title title;
    private GameView gameView;
    RelativeLayout relativeLayout;

    @Override
    public void onGameOver() {
        //Toast.makeText(this, "GameOver", Toast.LENGTH_LONG).show();
        new Title(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        relativeLayout = new RelativeLayout(this);

        this.inflater = LayoutInflater.from(this);
        setContentView(relativeLayout);

        title = new Title(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    protected void startGame() {
        gameView = new GameView(this, title.getWidth(), title.getHeight());
        gameView.setCallback(this);

        gameView.setFlame(relativeLayout);

        relativeLayout.addView(gameView);

        relativeLayout.removeViews(0, relativeLayout.getChildCount()-1);  // gameViewを残して、他のviewをListから削除
    }

    LayoutInflater inflater;

    public class Title extends FrameLayout implements Player.Callback {
        private Button startButton;

        private Player player;

        private final View view;

        public Title(Context context) {
            super(context);
            view = inflater.inflate(R.layout.title, this);
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            relativeLayout.addView(this);

            //-- 自機
            Bitmap bitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dot_tank_group);
            player = new Player(bitmap, -10, 100, Integer.MAX_VALUE, this);
            this.player.setBitmapJ(BitmapFactory.decodeResource(getResources(), R.drawable.dot_tank_jump));  // ジャンプ時アニメーションの設定
            this.player.setSmoke(BitmapFactory.decodeResource(getResources(), R.drawable.tank_smoke));       // 煙の設定

            player.setPlayerMoveToLeft(3);  // 移動速度の設定

            startButton = (Button) findViewById(R.id.t_button);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame();
                }
            });
        }


        //======================================================================================
        //--  描画メソッド
        //======================================================================================
        private boolean directionFlag = true;         // 走る位置 true : 上向き  false : 下向き
        private boolean rotateFlag = false;  // 走る位置を変えるか否か true : 変える  false : 変えない

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);

            //---- 自機
            //-- 方向転換
            if ( getWidth() + 200 < player.hitRect.left ) {
                directionFlag = false;
                player.setLocation(-200, Math.round(player.rect.top));

                if ( rotateFlag == true ) {
                    directionFlag = true;
                    rotateFlag = false;
                }
            }

            if ( directionFlag == false ) {
                canvas.rotate(180, getWidth()/2, getHeight()/2);
                rotateFlag = true;
            }

            //- 自機画像の反転

            //- 移動
            player.move();

            //- 描画
            player.draw(canvas);

            invalidate();
        }

        @Override
        public Player.MoveDirection getDistanceFromObstacle(Player player) {
            int width = view.getWidth();
            int height = view.getHeight();

            return new Player.MoveDirection(Integer.MAX_VALUE, height-player.hitRect.bottom);
        }
    }
}
