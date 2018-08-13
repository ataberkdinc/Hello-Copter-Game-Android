package com.ataberkdinc.helikopter;

import android.graphics.Rect;

/**
 * Created by ataberkdnc on 30.10.2017.
 */

public abstract class OyunObject {
    protected int x;
    protected int y;
    protected float dy;
    protected int dx;
    protected int width;
    protected int height;


    public void setX(int x)
    {
        this.x = x;
    }
    public void setY(int y)
    {
        this.y = y;
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;

    }
    public int getHeight()
    {
        return height;
    }
    public int getWidth()

    {
        return width;
    }
    public Rect getRectangle()
    {
        return new Rect(x,y,x+width,y+height);
    }
}
