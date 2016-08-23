package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EViewGroup;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup(R.layout.modify_container)
public class NoOptionsContainerView extends AddonsContainerView {

    public NoOptionsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
