package com.vizor.metronome;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MetronomeActivity extends Activity {
	
	private TextView TVbpmLive;
	private TextView TVbpmLabel;
	private ImageView IVplay;
	private ImageView tickerView;   
	private int BPM = 100;
	private GestureDetector mGestureDetector;
	private SoundPool soundPool;
    private HashMap<Integer, Integer> soundsMap;
    private int SOUND1 = 1; 
    private int SOUND2 = 2; 
    private int soundPlaying = 1; 
    private Boolean isPlaying = false;
    private Boolean dblTap = false;

	@SuppressLint("UseSparseArrays")
	@Override
	protected void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TVbpmLive = (TextView) findViewById(R.id.textView1);
		TVbpmLabel = (TextView) findViewById(R.id.textView2);
		IVplay = (ImageView) findViewById(R.id.ImageViewPlay);
		tickerView = (ImageView) findViewById(R.id.ImageViewNeedle);
		updateBpm(Integer.toString(BPM));
		mGestureDetector = createGestureDetector(this);	
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundsMap = new HashMap<Integer, Integer>();
        soundsMap.put(SOUND2, soundPool.load(this, R.raw.beat1, 1));
        soundsMap.put(SOUND1, soundPool.load(this, R.raw.beat2, 1));
	}
	@Override
	public void onPause() {
	    super.onPause();
	    stopSound();
		animateNeedleStop();
	}
	protected void updateBpm(String num) {
		TVbpmLive.setText(num);
	}
	//GESURE METHODS ****************************************************************
	private GestureDetector createGestureDetector(final Context context) {
	    GestureDetector gestureDetector = new GestureDetector(context);
        final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	    //finger tap and press listeners
	    gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
	        @Override
	        public boolean onGesture(Gesture gesture) {
	        	if (gesture == Gesture.TAP) {
	        		if (isPlaying == false) {
	        			playSound(soundPlaying,2000,(bpmToFSpeed(BPM)));
	        			animateNeedle((int) bpmToAnim(BPM));
	        		}
	        		else if (isPlaying == true) {
		        		stopSound();
		        		animateNeedleStop();
	        		}
	        	}
	        	if (gesture == Gesture.LONG_PRESS) {
	        		stopSound();
	        		animateNeedleStop();
	        		if (soundPlaying == 1) {
	        			soundPlaying = 2;
	        			TVbpmLabel.setText("tock");
	        		}
	        		else if (soundPlaying == 2) {
	        			soundPlaying = 1;
	        			TVbpmLabel.setText("tick");
	        		}
	        		playSound(soundPlaying,100,(bpmToFSpeed(BPM)));
	        		animateNeedle((int) bpmToAnim(BPM));
	        	}
	        	if (gesture == Gesture.TWO_TAP) {
	        		if (dblTap == false) {
	        			BPM = 208;
			            updateBpm(Integer.toString(BPM));
			            audio.playSoundEffect(Sounds.TAP);
			            stopSound();
			            animateNeedleStop();
			            dblTap = true;
	        		}
	        		else if (dblTap == true) {
	        			BPM = 60;
			            updateBpm(Integer.toString(BPM));
			            audio.playSoundEffect(Sounds.TAP);
			            stopSound();
			            animateNeedleStop();
			            dblTap = false;
	        		}
	            } 
	            return false;
	        }
	    });
	    //one finger scroll listener
	    gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
            	if (displacement > 0) {
            		BPM = BPM + 1;
            		if (BPM > 208) {
                		BPM = 208;
                		audio.playSoundEffect(Sounds.DISALLOWED);
            		}
            		else
            		audio.playSoundEffect(Sounds.SELECTED);
            	}
            	else if (displacement < 0) {
            		BPM = BPM - 1;
            		if (BPM < 60) {
                		BPM = 60;
                		audio.playSoundEffect(Sounds.DISALLOWED);
            		}
            		else
            		audio.playSoundEffect(Sounds.SELECTED);
            	}
            	updateBpm(Integer.toString(BPM));
            	stopSound();
            	animateNeedleStop();
            	return true;
            }
        });
	    return gestureDetector;
	} 
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
	    if (mGestureDetector != null) {
	        return mGestureDetector.onMotionEvent(event);
	    }
	    return false;
	}
	//SOUND METHODS ****************************************************************
	public void playSound(int sound, int loop, float fSpeed) {
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;  
        soundPool.play(soundsMap.get(sound), volume, volume, 1, loop, fSpeed);
        isPlaying = true;
		IVplay.setImageResource(R.drawable.pause);
	}
	public void stopSound() {
		soundPool.autoPause();
    	isPlaying = false;
    	IVplay.setImageResource(R.drawable.play);
	}
	public float bpmToFSpeed(int bpm) {
		float old_value = bpm;
		float new_value = 0;
		float old_max = 208.0f;
		float old_min = 40.0f;
		float new_max = 2.1f;
		float new_min = 0.1f;
		new_value = ( (old_value - old_min) / (old_max - old_min) ) * (new_max - new_min) + new_min;
		System.out.println("sound:"+" "+new_value);
		return new_value;
	}
	//ANIMATION METHODS ****************************************************************
	public void animateNeedle(final int dur) {
		RotateAnimation tickRight = new RotateAnimation (0, 30, 25, 245);
		tickRight.setDuration(10);
		tickerView.startAnimation(tickRight);
		tickRight.setAnimationListener(new Animation.AnimationListener() {
	        @Override
	        public void onAnimationEnd(Animation animation) {
	        	RotateAnimation tick = new RotateAnimation (30, -30, 25, 245);
	    		tick.setDuration(dur);
	        	tickerView.startAnimation(tick);
	        	tick.setRepeatMode(Animation.REVERSE);
	        	tick.setRepeatCount(Animation.INFINITE);
	        }
	        @Override
	        public void onAnimationRepeat(Animation animation) {}
	        @Override
	        public void onAnimationStart(Animation animation) {}
	    });
	}
	public void animateNeedleStop() {
		tickerView.clearAnimation();
	}
	public float bpmToAnim(int bpm) {
		float old_value = bpm;
		float new_value = 0;
		float old_max = 208;
		float old_min = 40;
		float new_max = 10;
		float new_min = 250;
		new_value = ( (old_value - old_min) / (old_max - old_min) ) * (new_max - new_min) + new_min;
		System.out.println("anim:"+" "+new_value);
		return new_value;
	}
}
