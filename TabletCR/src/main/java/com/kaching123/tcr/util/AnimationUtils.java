package com.kaching123.tcr.util;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.kaching123.tcr.R;

/**
 * @author Ivan v. Rikhmayer
 */
public class AnimationUtils {

    private static final int BLINKING_DURATION = 100;
    private static final int BLINKING_OFFSET = 40;

    private AnimationUtils() {}

    public void flipCard(FragmentActivity context, Fragment one, Fragment two) {

        context.getFragmentManager()
                .beginTransaction()

                        // Replace the default fragment animations with animator resources representing
                        // rotations when switching to the back of the card, as well as animator
                        // resources representing rotations when flipping back to the front (e.g. when
                        // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                        // Replace any fragments currently in the container view with a fragment
                        // representing the next page (indicated by the just-incremented currentPage
                        // variable).
                .replace(R.id.container, two)

                        // Add this transaction to the back stack, allowing users to press Back
                        // to get to the front of the card.
                .addToBackStack(null)

                        // Commit the transaction.
                .commit();
    }

    /**
     * starting view blinking
     */
    public static void applyBlinkingEffect(View view) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(BLINKING_DURATION); //You can manage the time of the blink with this parameter
        anim.setStartOffset(BLINKING_OFFSET);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    /**
     * starting view flipping
     */
    public static void applyFlippingEffect(Context context, ViewFlipper view) {
        view.setInAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.flip_in));
        view.setOutAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.flip_out));
    }

    public static void applySmoothEffect(ProgressBar progressBar, float from, float to) {
        ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, from, to);
        anim.setDuration(1000);
        progressBar.startAnimation(anim);
    }

    public static void applySmoothEffect(ProgressBar progressBar, float by) {
        ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, by);
        anim.setDuration(1000);
        progressBar.startAnimation(anim);
    }

    private static class ProgressBarAnimation extends Animation {

        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float by) {
            super();
            this.progressBar = progressBar;
            this.from = progressBar.getProgress();
            this.to = from + by;
        }

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }
    }
}