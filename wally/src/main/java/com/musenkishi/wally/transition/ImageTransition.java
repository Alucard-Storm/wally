package com.musenkishi.wally.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ImageTransition extends Transition {
    private static final String PROPNAME_BOUNDS = "wally:imageBounds";

    public ImageTransition() {
    }

    public ImageTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        Rect bounds = new Rect();
        view.getGlobalVisibleRect(bounds);
        transitionValues.values.put(PROPNAME_BOUNDS, bounds);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                 TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);

        if (startBounds == null || endBounds == null) {
            return null;
        }

        View view = endValues.view;
        float startScale = Math.min((float) startBounds.width() / endBounds.width(),
                (float) startBounds.height() / endBounds.height());
        
        float endScale = 1f;

        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, startScale, endScale);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, startScale, endScale);
        
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(300);

        return animatorSet;
    }
}
