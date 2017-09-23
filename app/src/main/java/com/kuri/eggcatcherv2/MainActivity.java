package com.kuri.eggcatcherv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/*
*GAME RULES:
* THE FLYING BIRD WILL DROP EGGS!, YOU HAVE TO CATCH THEM!!
* BUT REMEMBER, IT WILL DROP BLACK EGGS AS WELL. A BLACK EGG IS -1 POINT.
* GOLDEN EGGS ARE 2 POINTS AND SILVER ONES ARE 1 EACH. IF YOU DROP A GOLDEN EGG -15 POINTS!
* IF YOU DROP A SILVER EGG - 10 POINTS. DROPPING A BLACK EGG IS FINE:)
* HOVER YOUR NEST ACROSS THE EGG TO CATCH IT AND GAIN POINTS. SCORE 10 AND 20 ARE MILESTONE SCORES,
* SPEED OF THE FLYING BIRD WILL INCREASE AFTER THESE SCORES (HA HA HA HA!!)
* POINT TO NOTE: ALWAYS DOUBLE TAP YOUR NEST AFTER CATCHING AN EGG TO CONSUME IT,
* IN THIS CASE TO EMPTY THE NEST, IF YOU DON'T DO SO, YOU WONT BE ABLE TO CATCH THE OTHER
* FALLING EGGS.
* E.N.J.OY!!!!!
 */

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    Bitmap[] birdie; //bird flying images' array
    Bitmap[] eggHolder; //eggs' array
    Bitmap[] nest; //nest and nests with eggs array
    Bitmap wasteEgg;
    FrameLayout frameLayout;
    int globalX; //distance travelled along x axis by the bird
    int globalY; //distance across y = 0 (constant)
    int eggX; //location of x coordinate from which the egg will fall
    int eggY; //how fast the egg will fall, depends on this coordinate
    float nestX; //x - coordinate distance of nest
    float nestY; //y - coordinate distance of nest
    int nestDisplay; //the nest number to display a particular nest
    int birdieSelector; //the bird number for animation purpose
    float randomEggFloat; //hold random float number
    int randomEgg; //random egg to be displayed number
    int screenHeight;
    int screenWidth;
    int correction; //animation limit correction (varies)
    int score;//score keeper
    int highScore;
    int checkCount;//check on score count
    int eggFallY;
    int brokenX;
    int eggEndCorrection;
    int hitCount;
    long timeIt;
    GestureDetector gestures;
    TextView scoreText;
    TextView highScoreText;
    Paint[] eggPaint;//egg display/no display
    Paint brokenPaint; //display/no display poached egg
    Boolean[] hit;//if catch egg, hit = true, else hit = false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalY = 0;
        eggY = 20;
        eggX = 0;
        correction = 25;
        gestures = new GestureDetector(this);
        hit = new Boolean[3];
        //no catches yet
        hit[0] = false;
        hit[1] = false;
        hit[2] = false;
        birdie = new Bitmap[15];
        eggHolder = new Bitmap[3];
        nest = new Bitmap[4];
        eggPaint = new Paint[3];//initialize array
        brokenPaint = new Paint();
        for(int i = 0; i<3; i++){
            eggPaint[i] = new Paint();//initialize each element of array
        }
        //all eggs will be visible first
        eggPaint[0].setAlpha(255);
        eggPaint[1].setAlpha(255);
        eggPaint[2].setAlpha(255);
        brokenPaint.setAlpha(0);
        scoreText = (TextView)findViewById(R.id.scoreText);
        highScoreText = (TextView)findViewById(R.id.highScore);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
        BirdGo birdGo = new BirdGo(this);
        frameLayout = (FrameLayout)findViewById(R.id.graphicsIt);
        frameLayout.addView(birdGo);
        //randomly drop a egg initially
        randomEggFloat = (float)Math.random();
        randomEgg = (int)(randomEggFloat*3);//to increase randomness 3 is used
        if(randomEgg == 3){
            randomEgg = randomEgg - 1;
        }
        timeIt = 600;
        eggFallY = 110;
        eggEndCorrection  = 290;
        checkCount = 0;
        hitCount = 0;
        score = 0;
        highScore = 0;
        nestDisplay = 3;
        //animate
        globalXIncrement();
    }

    public class BirdGo extends View{
        public BirdGo(Context context){
            super(context);
            birdie[0] = BitmapFactory.decodeResource(getResources(),R.drawable.get);
            birdie[1] = BitmapFactory.decodeResource(getResources(),R.drawable.set);
            birdie[2] = BitmapFactory.decodeResource(getResources(),R.drawable.go);
            eggHolder[0] = BitmapFactory.decodeResource(getResources(),R.drawable.noeggie);
            eggHolder[1] = BitmapFactory.decodeResource(getResources(),R.drawable.goldeneggie);
            eggHolder[2] = BitmapFactory.decodeResource(getResources(),R.drawable.silvereggie);
            nest[0] = BitmapFactory.decodeResource(getResources(),R.drawable.nonest);
            nest[1] = BitmapFactory.decodeResource(getResources(),R.drawable.goldennest);
            nest[2] = BitmapFactory.decodeResource(getResources(),R.drawable.silvernest);
            nest[3] = BitmapFactory.decodeResource(getResources(),R.drawable.nest);
            wasteEgg = BitmapFactory.decodeResource(getResources(),R.drawable.wastegg);
        }

        @Override
        protected void onDraw(Canvas canvas){
            canvas.drawBitmap(birdie[birdieSelector],globalX,globalY,null);
            canvas.drawBitmap(eggHolder[randomEgg],eggX,eggY,eggPaint[randomEgg]);
            canvas.drawBitmap(nest[nestDisplay],nestX,nestY,null);
            canvas.drawBitmap(wasteEgg,brokenX,screenHeight - eggEndCorrection,brokenPaint);
            invalidate();
        }
    }

    public void moveNest(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        checkCollision(x,y);
    }

    public void resetNest(){
        nestDisplay = 3; //default nest
        checkCount = 0; // check reset
        hitCount = 0;
        hit[randomEgg] = false; //no hits
        brokenPaint.setAlpha(0);
    }

    public void checkCollision(float x, float y){
        if(nestDisplay == 3){
            if(randomEgg == 0){ //check which egg is falling first
                //collision conditions
                if(eggX <= nest[0].getWidth() + x && eggX >= x){
                    if(eggY <= nest[0].getHeight() + y && eggY >= y){
                        checkCount++;
                        nestDisplay = 0; //display the appropriate nest carrying egg
                        eggPaint[0].setAlpha(0); //caught egg, don't show it drop
                        //to stop crazy incrementing and decrementing of score
                        if(checkCount<=1){
                            score--;
                            if (score>highScore){
                                highScore = score;
                            }
                            hit[0] = true; //caught egg
                        }
                        scoreText.setText("SCORE: " + Integer.toString(score));
                        highScoreText.setText("HIGH SCORE: " + Integer.toString(highScore));
                    }
                }
            }else if(randomEgg == 1){
                if(eggX <= nest[0].getWidth() + x && eggX >= x){
                    if(eggY <= nest[0].getHeight() + y && eggY >= y){
                        checkCount++;
                        nestDisplay = 1;
                        eggPaint[1].setAlpha(0);
                        if(checkCount <= 1){
                            score += 2;
                            if(score>highScore){
                                highScore = score;
                            }
                            hit[1] = true;
                        }
                        scoreText.setText("SCORE: " + Integer.toString(score));
                        highScoreText.setText("HIGH SCORE: " + Integer.toString(highScore));
                    }
                }
            }else if(randomEgg == 2){
                if(eggX <= nest[0].getWidth() + x && eggX >= x){
                    if(eggY <= nest[0].getHeight() + y && eggY >= y){
                        checkCount++;
                        nestDisplay = 2;
                        eggPaint[2].setAlpha(0);
                        if(checkCount <= 1){
                            score += 1;
                            if(score>highScore){
                                highScore = score;
                            }
                            hit[2] = true;
                        }
                        scoreText.setText("SCORE: " + Integer.toString(score));
                        highScoreText.setText("HIGH SCORE: " + Integer.toString(highScore));
                    }
                }
            }else{
                nestDisplay = 3; //default
            }
        }
    }

    //animate egg and bird
    public void globalXIncrement(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                birdieSelector++;
                if (birdieSelector > 2) { //only 3 images in the array
                    birdieSelector = 0; //go to first image
                }
                globalX += 100; //move the bird ahead by 100
                if (globalX > screenWidth - (birdie[birdieSelector].getWidth() + correction)) { //if reaches end, restart
                    globalX = 0;
                }
                //EGG DROP, POINT DROP
                if(randomEgg == 0){//check if it is the black egg first, then check drop
                    if (eggY >= screenHeight - (eggHolder[0].getHeight() + eggEndCorrection) && !hit[0]) {//black egg
                        setBrokenX(eggX);
                        brokenPaint.setAlpha(255);
                    }
                }else if(randomEgg == 1){//check if it is the golden egg first, then check drop
                    if (eggY >= screenHeight - (eggHolder[1].getHeight() + eggEndCorrection) && !hit[1]) {//golden egg
                        hitCount++;
                        if(hitCount <=1){
                            score -= 15; //if egg reaches the end and blasts, score BLAST
                        }
                        scoreText.setText("SCORE: " + Integer.toString(score));
                        setBrokenX(eggX);
                        brokenPaint.setAlpha(255);
                    }
                }else if(randomEgg == 2){//check if it is the silver egg first, then check drop
                    if (eggY >= screenHeight - (eggHolder[2].getHeight() + eggEndCorrection) && !hit[2]) {//silver egg
                        hitCount++;
                        if(hitCount <= 1){
                            score -= 10; //if egg reaches the end and blasts, score BLAST
                        }
                        scoreText.setText("SCORE: " + Integer.toString(score));
                        setBrokenX(eggX);
                        brokenPaint.setAlpha(255);
                    }
                }
                //if egg reaches end or caught
                if (eggY >= screenHeight - (eggHolder[randomEgg].getHeight() + eggEndCorrection) || hit[randomEgg]) {
                    randomEggFloat = (float) Math.random(); //generate new egg
                    randomEgg = (int) (randomEggFloat * 3);
                    eggPaint[randomEgg].setAlpha(255); //show it
                    if (randomEgg == 3) {//only 3 elements (array[0],array[1],array[2])
                        randomEgg = randomEgg - 1;
                    }
                    eggY = 150; //start at this y
                    eggX = globalX; //egg's x same as bird's
                    hitCount = 0;
                    hit[randomEgg] = false; //to stop from showing another egg
                }
                if (eggX < 5) { //to maintain the same x position of the falling egg, till it falls to the end or is caught
                    eggX = globalX;
                }
                if (score >= 10) {
                    timeIt = 400;
                    eggFallY = 175;
                }
                if (score >= 20) {
                    timeIt = 200;
                    eggFallY = 195;
                }
                eggY += eggFallY; //how fast the egg falls
                handler.postDelayed(this,timeIt);
            }
        });
    }

    public void setBrokenX(int globalX){
        brokenX = globalX;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        resetNest();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        nestX = e2.getX(); //nest position x
        nestY = e2.getY(); //nest position y
        moveNest(e2);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}