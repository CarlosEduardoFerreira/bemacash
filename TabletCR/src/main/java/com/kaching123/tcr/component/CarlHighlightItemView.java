package com.kaching123.tcr.component;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.saleorder.ItemView;
import com.kaching123.tcr.fragment.saleorder.ItemsAdapter;

import java.util.concurrent.TimeUnit;

/**
 * Created by ferre on 29/12/2016.
 */

public class CarlHighlightItemView {

    private View lastView;
    private View listFragment;
    private ItemsAdapter itemsAdapter;
    public String saleItemGuid = null;
    private static final int DEFAULT_ANIMATION_TIME = 100;


    public CarlHighlightItemView(View listFragment, final ItemsAdapter itemsAdapter, String saleItemGuid) {

        this.listFragment = listFragment;
        this.itemsAdapter = itemsAdapter;
        this.saleItemGuid = saleItemGuid;

        /*
        listFragment.postDelayed(new Runnable() {
            @Override
            public void run() {
                itemsAdapter.notifyDataSetChanged();
            }
        }, DEFAULT_ANIMATION_TIME);
        /**/

    }


    public void CarlHighlightItemViewRun(View view) {
        if(!saleItemGuid.equals("")) {
            lastView = view;
            String IdAsString = lastView.getResources().getResourceName(lastView.getId());

            ((View) lastView.getParent()).setBackgroundColor(Color.WHITE);

            final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(
                    lastView,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    0xff78c5f9, 0xFFFFFFFF);
            backgroundColorAnimator.setDuration(1000);
            backgroundColorAnimator.start();

            listFragment.postDelayed(new Runnable() {
                @Override
                public void run() {
                    itemsAdapter.notifyDataSetChanged();
                    saleItemGuid = "";
                }
            }, DEFAULT_ANIMATION_TIME);
            /**/
        }
    }


}