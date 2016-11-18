package com.asanoyu.action;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.zip.Inflater;

public class MainActivity extends Activity implements GameView.GameOverCallback {

    private Title title;
    private GameView gameView;
    RelativeLayout relativeLayout;

    @Override
    public void onGameOver() {
        //Toast.makeText(this, "GameOver", Toast.LENGTH_LONG).show();
        new Title(relativeLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        relativeLayout = new RelativeLayout(this);

        this.inflater = LayoutInflater.from(this);
        setContentView(relativeLayout);

        title = new Title(relativeLayout);
    }

    protected void startGame() {
        gameView = new GameView(this, title.view.getWidth(), title.view.getHeight());
        gameView.setCallback(this);

        gameView.setFlame(relativeLayout);

        relativeLayout.addView(gameView);

        relativeLayout.removeViews(0, relativeLayout.getChildCount()-1);  // gameViewを残して、他のviewをListから削除
    }

    LayoutInflater inflater;

    public class Title {
        private Button startButton;

        public final View view;

        public Title(RelativeLayout relativeLayout) {
            view = inflater.inflate(R.layout.title, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            relativeLayout.addView(view);

            startButton = (Button) findViewById(R.id.t_button);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame();
                }
            });
        }
    }
}
