package com.musenkishi.wally.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class SpringImageView extends AppCompatImageView {
    
    private SpringAnimation scaleXAnimation;
    private SpringAnimation scaleYAnimation;
    private float downX, downY;
    
    public SpringImageView(Context context) {
        super(context);
        init();
    }
    
    public SpringImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Set up spring animations for scaling
        scaleXAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_X, 1f);
        scaleYAnimation = new SpringAnimation(this, DynamicAnimation.SCALE_Y, 1f);
        
        // Configure spring behavior
        SpringForce springForce = new SpringForce()
            .setStiffness(SpringForce.STIFFNESS_MEDIUM)
            .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            
        scaleXAnimation.setSpring(springForce);
        scaleYAnimation.setSpring(springForce);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                // Scale down slightly on press
                scaleXAnimation.animateToFinalPosition(0.95f);
                scaleYAnimation.animateToFinalPosition(0.95f);
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Spring back to original size
                scaleXAnimation.animateToFinalPosition(1f);
                scaleYAnimation.animateToFinalPosition(1f);
                break;
                
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                
                // If finger moves too far, cancel the press effect
                if (Math.abs(moveX - downX) > 20 || Math.abs(moveY - downY) > 20) {
                    scaleXAnimation.animateToFinalPosition(1f);
                    scaleYAnimation.animateToFinalPosition(1f);
                }
                break;
        }
        return true;
    }
}
