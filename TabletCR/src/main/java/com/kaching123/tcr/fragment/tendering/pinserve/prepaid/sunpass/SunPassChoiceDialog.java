package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;


@EFragment
public class SunPassChoiceDialog extends StyledDialogFragment {
    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;
    @ViewById(R.id.iv_transponder)
    protected ImageView ivTransponder;
    @ViewById(R.id.iv_pay_your_document)
    protected ImageView ivPayYourDocument;

    @Click
    public void ivTransponderClicked() {
        callback.onTrensponder(mContext);

    }

    @Click
    public void ivPayYourDocumentClicked() {
        callback.onPayYourDocument(mContext);
    }

    private FragmentActivity mContext;
    private static final String DIALOG_NAME = "SunPassChoiceDialog";

    SunPassChoiceCallback callback;


    public void setCallback(SunPassChoiceCallback callback) {
        this.callback = callback;
    }


    public void setContext(FragmentActivity context) {
        this.mContext = context;
    }

    public interface SunPassChoiceCallback {

        void onTrensponder(FragmentActivity context);

        void onPayYourDocument(FragmentActivity context);

        void cancel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getNegativeButton().setTypeface(Typeface.DEFAULT_BOLD);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.prepaid_dlg_heigth));
    }


    @Override
    protected int getTitleIcon() {
        return R.drawable.icon_sun_pass;
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleViewBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_title_background_color);
    }

    @Override
    protected int getButtonsBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_buttons_background_color);
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    ;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.sunpass_choices;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.sunpass_prepaid_dialog_credentials_title;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {

        callback.cancel();
        return null;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }


    public static void show(FragmentActivity context,
                            SunPassChoiceCallback callback) {
        SunPassChoiceDialog dialog = SunPassChoiceDialog_.builder()
                .build();
        dialog.setCallback(callback);
        dialog.setContext(context);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
