package com.example.mihailtodorov.pong;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class Game extends Activity {

    ScreenView screenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screenView = new ScreenView(this);
        setContentView(screenView);

    }

    class ScreenView extends SurfaceView implements Runnable {

        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;
        boolean paused = true;
        Canvas canvas;
        Paint paint;
        long fps;

        private long timeThisFrame;

        int screenX;
        int screenY;

        Racket racket;

        Ball ball;

        Bricks[] bricks = new Bricks[200];
        int numBricks = 0;
        int BricksInPlay = 0;

        int score = 0;
        int level = 1;
        int lives = 3;

        public ScreenView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            racket = new Racket(screenX, screenY);

            ball = new Ball(screenX, screenY);

            createBricksAndRestart();

        }

        public void createBricksAndRestart(){
            ball.reset(screenX, screenY);

            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;

            numBricks = 0;
            for(int column = 0; column < 8; column ++ ){
                for(int row = 0; row < 3; row ++ ){
                    bricks[numBricks] = new Bricks(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

            BricksInPlay = numBricks;


            if(lives == 0){
                score = 0;
                lives = 3;
                level = 1;
            }
        }

        @Override
        public void run(){
            while (playing) {

                long startFrameTime = System.currentTimeMillis();

                if(!paused){
                    update();
                }

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;

                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        public void update() {

            racket.update(fps);

            ball.update(fps);

            for(int i = 0; i < numBricks; i++){
                if (bricks[i].getVisibility()){
                    if(RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + 10;
                        BricksInPlay =- 1;
                    }
                }
            }

            if(RectF.intersects(racket.getRect(), ball.getRect())) {
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(racket.getRect().top - 2);
            }

            if(ball.getRect().bottom > screenY){
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);
                lives --;

                if(lives == 0){
                    paused = true;
                    createBricksAndRestart();
                }
            }

            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
            }
            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
            }

            if(ball.getRect().right > screenX - 10){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
            }

            if(BricksInPlay==0){
                level =+ 1;
                createBricksAndRestart();
                changeSpeed();
            }

        }

        public void changeSpeed(){
            if (level>0 && level<5){
                ball.xVelocity = 300;
                ball.yVelocity = -500;
            }else if (level>4 && level<9){
                ball.xVelocity = 350;
                ball.yVelocity = -550;
            }else if (level>8 && level<13){
                ball.xVelocity = 400;
                ball.yVelocity = -600;
            }else if (level>12 && level<17){
                ball.xVelocity = 450;
                ball.yVelocity = -650;
            }else if (level>16 && level<21){
                ball.xVelocity = 500;
                ball.yVelocity = -700;
            }else if (level>20){
                ball.xVelocity = 600;
                ball.yVelocity = -800;
            }
        }

        public void getColor(){
            if (level>0 && level<5){
                canvas.drawColor(Color.argb(255,  255, 255, 0));
            }else if (level>4 && level<9){
                canvas.drawColor(Color.argb(255,  255, 128, 0));
            }else if (level>8 && level<13){
                canvas.drawColor(Color.argb(255,  255, 0, 0));
            }else if (level>12 && level<17){
                canvas.drawColor(Color.argb(255,  128, 0, 0));
            }else if (level>16 && level<21){
                canvas.drawColor(Color.argb(255,  128, 0, 128));
            }else if (level>20){
                canvas.drawColor(Color.argb(255,  0, 0, 0));
            }
        }

        public void draw() {

            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.argb(255,  192, 192, 192));
                paint.setColor(Color.argb(255,  255, 255, 255));
                canvas.drawRect(racket.getRect(), paint);
                canvas.drawRect(ball.getRect(), paint);
                paint.setColor(Color.argb(255,  249, 129, 0));

                // Draw the bricks if visible
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                paint.setColor(Color.argb(255,  255, 255, 255));
                paint.setTextSize(40);
                canvas.drawText("Резултат: " + score + "   Животи: " + lives+ "   Ниво: " + level, 10,screenY-50, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }


        public void pause() {
            playing = false;
            try {
                gameThread.join();
            }
            catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }


        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if(motionEvent.getX() > screenX / 2){


                        racket.setMovementState(racket.RIGHT);
                    }
                    else{
                        racket.setMovementState(racket.LEFT);
                    }

                    break;

                case MotionEvent.ACTION_UP:

                    racket.setMovementState(racket.STOPPED);
                    break;
            }
            return true;
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        screenView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenView.pause();
    }

}