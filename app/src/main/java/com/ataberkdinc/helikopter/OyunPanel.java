package com.ataberkdinc.helikopter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import java.util.ArrayList;
import java.util.Random;


public class OyunPanel extends SurfaceView implements SurfaceHolder.Callback


{

    private SoundPlayer sound;
    public static final int WIDTH = 856;
    public static final int HEIGHT = 400;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long mermiStartTime;
    private MainThread thread;
    private Background bg;
    private Oyuncu oyuncu;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Mermi> mermis;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;


    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Patlama patlama;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;


    public OyunPanel(Context context) {


        super(context);


        sound = new SoundPlayer(this.getContext());
        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);

    }





    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){ }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){



        boolean retry = true;
        int counter = 0;



        while(retry && counter<1000)
        {


            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;

                SharedPreferences prefs = this.getContext().getSharedPreferences("BEST", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("key", best);
                editor.commit();


                thread = null;



            }catch(InterruptedException e){e.printStackTrace();}

        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder){


        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.fena2));
        oyuncu = new Oyuncu(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter2), 96,32, 8);
        smoke = new ArrayList<Smokepuff>();
        mermis = new ArrayList<Mermi>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime=  System.nanoTime();
        mermiStartTime = System.nanoTime();




        SharedPreferences prefs = this.getContext().getSharedPreferences("BEST", Context.MODE_PRIVATE);
        best = prefs.getInt("key", 0); //0 is the default value

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop

        thread.setRunning(true);

        thread.start();




    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {





        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!oyuncu.getPlaying() && newGameCreated && reset)
            {

                oyuncu.setPlaying(true);

                oyuncu.setUp(true);

            }
            if(oyuncu.getPlaying())
            {

                if(!started)started = true;
                reset = false;
                oyuncu.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            oyuncu.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()

    {


        if(oyuncu.getPlaying()) {


            if(botborder.isEmpty())
            {

                oyuncu.setPlaying(false);


                return;
            }
            if(topborder.isEmpty())
            {

                oyuncu.setPlaying(false);

                return;
            }

            bg.update();
            oyuncu.update();

            for(int i = 0; i<botborder.size(); i++)
            {
                if(collision(botborder.get(i), oyuncu))

                    oyuncu.setPlaying(false);

            }


            for(int i = 0; i <topborder.size(); i++)
            {
                if(collision(topborder.get(i),oyuncu))

                    oyuncu.setPlaying(false);

            }


            //add missiles on timer
            long missileElapsed = (System.nanoTime()-mermiStartTime)/1000000;
            if(missileElapsed >(2000 - oyuncu.getScore()/4)){


                //first missile always goes down the middle
                if(mermis.size()==0)
                {
                    mermis.add(new Mermi(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 45, 15, oyuncu.getScore(), 13));
                }
                else
                {

                    mermis.add(new Mermi(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, oyuncu.getScore(),13));
                }

                //reset timer
                mermiStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for(int i = 0; i<mermis.size();i++)
            {
                //update missile
                mermis.get(i).update();

                if(collision(mermis.get(i),oyuncu))
                {
                    mermis.remove(i);
                    oyuncu.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if(mermis.get(i).getX()<-100)
                {
                    mermis.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(oyuncu.getX()+15, oyuncu.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else{
            oyuncu.resetDY();
            if(!reset)
            {
                newGameCreated = false;
                reset = true;
                startReset = System.nanoTime();

                dissapear = true;
                patlama = new Patlama(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),oyuncu.getX(),
                        oyuncu.getY()-30, 100, 100, 25);
            }

            patlama.update();


            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(resetElapsed > 2500 && !newGameCreated)
            {

                newGame();



            }


        }

    }
    public boolean collision(OyunObject a, OyunObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {


            sound.playHitSound();

                  return true;



        }



        return false;


    }

    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                oyuncu.draw(canvas);
            }
            //draw smokepuffs
            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas);
            }
            //draw missiles
            for(Mermi m: mermis)
            {
                m.draw(canvas);
            }



            if(started)
            {
                patlama.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }



    public void newGame()
    {


        if (oyuncu.getScore() >  best) best = oyuncu.getScore();





        dissapear = false;


        botborder.clear();
        topborder.clear();

        mermis.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        oyuncu.resetDY();
        oyuncu.resetScore();





        if(oyuncu.getScore()>best)
        {
            best = oyuncu.getScore();

        }
        oyuncu.setY(HEIGHT/2);



        //create initial borders

        //initial top border
        for(int i = 0; i*20<WIDTH+40;i++)
        {
            //first top border create
            if(i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, 10));
            }
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, topborder.get(i-1).getHeight()+1));
            }
        }
        //initial bottom border
        for(int i = 0; i*20<WIDTH+40; i++)
        {
            //first border ever created
            if(i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick)
                        ,i*20,HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filed
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY() - 1));
            }
        }


        newGameCreated = true;






    }

    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        canvas.drawText("DISTANCE: " + (oyuncu.getScore()), 15, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if(!oyuncu.getPlaying()&&newGameCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 30, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 50, paint1);
        }
    }


}