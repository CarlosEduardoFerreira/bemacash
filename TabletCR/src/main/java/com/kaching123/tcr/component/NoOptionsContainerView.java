package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;

import org.androidannotations.annotations.EViewGroup;
import com.kaching123.tcr.R;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup(R.layout.modify_container)
public class NoOptionsContainerView extends AddonsContainerView {

    public NoOptionsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
