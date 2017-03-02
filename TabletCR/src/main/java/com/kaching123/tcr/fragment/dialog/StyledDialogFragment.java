package com.kaching123.tcr.fragment.dialog;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.util.KeyboardUtils;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Created by gdubina on 20/11/13.
 */
@EFragment
public abstract class StyledDialogFragment extends SimpleDialogFragment{

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int disabledBtnColor;

    @ColorRes(R.color.dlg_text_green)
    protected int greenBtnColor;

    @ColorRes(R.color.dlg_btn_text_color)
    protected int normalBtnColor;

    @SystemService
    protected LayoutInflater layoutInflater;

    @Override
    protected Builder build(Builder builder) {
        builder = super.build(builder);

        builder.setTitle(getDialogTitle());
        builder.setTitleTextColor(getTitleTextColor());
        builder.setIcon(getTitleIcon());
        builder.setTitleGravity(getTitleGravity());
        builder.setTitleTextBackgroundColor(getTitleTextBackgroundColor());
        builder.setView(createDialogContentView());
        builder.setButtonsBackgroundColor(getButtonsBackgroundColor());
        builder.setTitleViewBackgroundColor(getTitleViewBackgroundColor());
        builder.setSeparatorColor(getSeparatorColor());

        if(hasTitleTextTypeface())
            builder.setTitleTypeface(getTitleTextTypeface());
        if(hasNegativeButtonTextTypeface())
            builder.setNegativeButtonTypeface(getNegativeButtonTextTypeface());

        if(hasNegativeButton()){
            builder.setNegativeButton(getNegativeButtonTitle(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callInternalListener(getNegativeButtonListener());
                }
            });
        }
        if(hasPositiveButton()){
            builder.setPositiveButton(getPositiveButtonTitle(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callInternalListener(getPositiveButtonListener());
                }
            });

        }
        if(hasSkipButton()){
            builder.setNeutralButton(getSkipButtonTitle(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callInternalListener(getSkipButtonListener());
                }
            });
        }
        return builder;
    }


    @Override
    public void dismiss() {
        KeyboardUtils.hideKeyboard(getActivity(), getView());
        super.dismiss();
    }
    protected int getSeparatorColor(){return 0;}
    protected int getTitleTextColor(){return 0;}
    protected int getTitleViewBackgroundColor(){return 0;}
    protected int getButtonsBackgroundColor(){return 0;}
    protected int getTitleTextBackgroundColor(){return 0;}

    protected int getTitleTextTypeface(){return Typeface.NORMAL; }

    protected int getNegativeButtonTextTypeface(){return Typeface.NORMAL; }

    protected boolean hasTitleTextTypeface(){return false;}

    protected boolean hasNegativeButtonTextTypeface(){return false;}

    protected boolean hasSkipButton(){
        return false;
    }

    protected boolean hasNegativeButton(){
        return true;
    }

    protected boolean hasPositiveButton(){
        return true;
    }

    protected View createDialogContentView(){
        return layoutInflater.inflate(getDialogContentLayout(), null, false);
    }

    protected  int getTitleIcon(){return 0;};

    protected  int getTitleGravity(){return Gravity.CENTER;};

    protected abstract int getDialogContentLayout();

    protected abstract int getDialogTitle();

    protected  int getSkipButtonTitle(){
        return R.string.btn_skip;
    }

    protected abstract int getNegativeButtonTitle();

    protected abstract int getPositiveButtonTitle();

    protected abstract OnDialogClickListener getPositiveButtonListener();

    protected OnDialogClickListener getNegativeButtonListener() { return null; }

    protected OnDialogClickListener getSkipButtonListener() { return null; }

    protected void callInternalListener(OnDialogClickListener listener) {
        boolean dismiss = true;
        if (listener != null) {
            dismiss = listener.onClick();
        }
        if (dismiss) {
            dismiss();
        }
    }

    protected void enablePositiveButtons(boolean enable) {
        enablePositiveButton(enable, normalBtnColor);
    }

    protected void enableSkipButtons(boolean enable) {
        enableButton(getNegativeButton(), enable, normalBtnColor);
    }

    protected void enablePositiveButton(boolean enable, int normalColor) {
        enableButton(getPositiveButton(), enable, normalColor);
    }

    protected void enableButton(Button button, boolean enable, int normalColor) {
        button.setEnabled(enable);
        button.setTextColor(enable ? normalColor : disabledBtnColor);
    }

    public static interface OnDialogClickListener {
        boolean onClick();
    }

    protected TcrApplication getApp(){
        if(getActivity() == null)
            throw new IllegalStateException("getActivity returned null");
        return (TcrApplication)getActivity().getApplicationContext();
    }
}
