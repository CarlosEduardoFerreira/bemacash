package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showAddonPrice;

/**
 * Created by vkompaniets on 19.11.13.
 */
@EViewGroup (R.layout.modify_button)
public class ModifyButton extends FrameLayout {

    @ViewById
    protected TextView title;

    @ViewById
    protected TextView price;

    private String guid;

    public ModifyButton(Context context) {
        super(context);
        init();
    }

    public ModifyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModifyButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        setClickable(true);
    }

    public void bind(String guid, String title, BigDecimal cost, boolean isSelected) {
        this.guid = guid;
        this.title.setText(title);
        showAddonPrice(this.price, cost);
        setActivated(isSelected);
    }

    public String getGuid() {
        return guid;
    }
}
