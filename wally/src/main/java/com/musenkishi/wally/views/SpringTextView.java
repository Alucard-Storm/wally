package com.musenkishi.wally.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class SpringTextView extends TextView {
    
    private SpringAnimation scaleXAnimation;
    private SpringAnimation scaleYAnimation;
    
    public SpringTextView(Context context) {
        super(context);
        init();
    }
    
    public SpringTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        scaleXAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_X, 1f);
        scaleYAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_Y, 1f);
        
        SpringForce springForce = new SpringForce()
            .setStiffness(SpringForce.STIFFNESS_LOW)
            .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            
        scaleXAnimation.setSpring(springForce);
        scaleYAnimation.setSpring(springForce);
        
        setElevation(4f); // Add some elevation for material design feel
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scaleXAnimation.animateToFinalPosition(0.9f);
                scaleYAnimation.animateToFinalPosition(0.9f);
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scaleXAnimation.animateToFinalPosition(1f);
                scaleYAnimation.animateToFinalPosition(1f);
                performClick();
                break;
        }
        return true;
    }
    
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
