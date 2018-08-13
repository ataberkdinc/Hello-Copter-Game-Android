package com.ataberkdinc.helikopter;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by ataberkdnc on 2.11.2017.
 */

public class SoundPlayer {


    private static SoundPool soundPool;
    private static int hitSound;
    private static int heliSound;

    public SoundPlayer(Context context)
    {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
            hitSound = soundPool.load(context,R.raw.explodemini,1);


    }

    public void playHitSound()
    {
        soundPool.play(hitSound,1.0f,1.0f,1,0,1.0f);

    }
    public void playHeliSound()
    {
        soundPool.play(heliSound,1.0f,1.0f,1,0,1.0f);

    }


}
