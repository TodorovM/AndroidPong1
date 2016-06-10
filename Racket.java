package com.example.mihailtodorov.pong;


import android.graphics.RectF;

public class Racket {

    private RectF rect;

    public float length;
    public float height;

    public float x;

    public float y;

    private float paddleSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int paddleMoving = STOPPED;

    public Racket(int screenX, int screenY){
        length = 130;
        height = 40;

        x = screenX / 2;
        y = screenY - 60;

        rect = new RectF(x, y, x + length, y + height);

        paddleSpeed = 850;
    }

    public RectF getRect(){
        return rect;
    }

    public void setMovementState(int state){
        paddleMoving = state;
    }

    public void update(long fps){
        if(paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }
}
