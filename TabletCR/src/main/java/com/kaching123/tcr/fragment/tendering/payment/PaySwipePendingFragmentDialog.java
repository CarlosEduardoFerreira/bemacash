package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.data.CardData;
import com.kaching123.tcr.fragment.data.MsrDataFragment;
import com.kaching123.tcr.fragment.data.MsrDataFragment.SwipeCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.util.KeyboardUtils;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PaySwipePendingFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "PaySwipePendingFragmentDialog";
    public static final int CARD_NUMBER_MIN_LEN = 16;
    public static final int CARD_CVN_MIN_LEN = 3;
    public static final int CARD_EXPDATE_MIN_LEN = 4;
    public static final int CARD_ZIP_MIN_LEN = 4;


    private ISaleSwipeListener listener;

    @ViewById
    protected View errorIcon;

    @ViewById
    protected View btnTryAgain;

    @ViewById
    protected TextView msg;

    @ViewById
    protected View swipeLayer;

    @ViewById
    protected View manualLayer;

    @ViewById
    protected EditText cardNumber;

    @ViewById
    protected EditText cardExpDate;

    @ViewById
    protected EditText cardCvn;

    @ViewById
    protected EditText cardZip;

    @FragmentArg
    protected boolean isManualAvailable;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.swipe_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        hideError();
        trySwipe();
        setCancelable(false);

        cardZip.setImeOptions(EditorInfo.IME_ACTION_DONE);
        cardZip.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_DONE == i) {
                    btnManualSubmitClicked();
                    return true;
                }
                return false;
            }
        });
    }

    private void showError(int err){
        msg.setText(err);
        errorIcon.setVisibility(View.VISIBLE);
        btnTryAgain.setVisibility(View.VISIBLE);
    }

    private void hideError(){
        msg.setText(R.string.blackstone_pay_swipe_body);
        errorIcon.setVisibility(View.GONE);
        btnTryAgain.setVisibility(View.GONE);
    }

    @Click
    protected void btnTryAgainClicked(){
        hideError();
        trySwipe();
    }

    @AfterViews
    protected void init() {

    }

    private void trySwipe(){
        if (getMsr() == null) {
            showError(R.string.swipe_card_error_not_ready_msg);
            return;
        }
        boolean isOk = getMsr().swipeCard(msrSwipeCallback);
        if(!isOk){
            showError(R.string.swipe_card_error_not_ready_msg);
        }
    }

    protected MsrDataFragment getMsr() {
        return (MsrDataFragment) getFragmentManager().findFragmentByTag(MsrDataFragment.FTAG);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cc_swipe_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_swipe_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_manual;
    }

    @Override
    protected int getSkipButtonTitle() {
        return R.string.btn_simulate;
    }

    @Override
    protected boolean hasSkipButton() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected boolean hasPositiveButton() {
        return isManualAvailable;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                simulate();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                switchLayers(manualLayer.getVisibility() == View.GONE);
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                stopSwipe();
                tryCancel();
                return false;
            }
        };
    }

    private void stopSwipe() {
        getMsr().stopSwipeCard();
    }

    @Click
    protected void btnManualSubmitClicked(){
        String sCardNum = cardNumber.getText().toString();
        String sExpDate = cardExpDate.getText().toString();
        String sCvn = cardCvn.getText().toString();
        String sZip = cardZip.getText().toString();

        if (!validateForm(sCardNum, sExpDate, sCvn, sZip)) {
            return;
        }
        if(listener != null){
            listener.onCardInfo(sCardNum, sExpDate, sCvn, sZip);
        }
    }

    private boolean validateForm(String cardNum, String expDate, String cvn, String zip){
        if(TextUtils.isEmpty(cardNum)){
            showAlert(R.string.swipe_manual_card_number_emty_msg);
            return false;
        }

        if(cardNum.length() < CARD_NUMBER_MIN_LEN){
            showAlert(R.string.swipe_manual_card_number_invalid_msg);
            return false;
        }

        if(TextUtils.isEmpty(expDate)){
            showAlert(R.string.swipe_manual_card_expdate_empty_msg);
            return false;
        }

        if(expDate.length() < CARD_EXPDATE_MIN_LEN){
            showAlert(R.string.swipe_manual_card_expdate_invalid_msg);
            return false;
        }

        if(TextUtils.isEmpty(cvn) && getApp().getShopInfo().cvnMandatory){
            showAlert(R.string.swipe_manual_card_cvn_empty_msg);
            return false;
        }

        if(!TextUtils.isEmpty(cvn) && cvn.length() < CARD_CVN_MIN_LEN){
            showAlert(R.string.swipe_manual_card_cvn_invalid_msg);
            return false;
        }

        if (TextUtils.isEmpty(zip) && getApp().getShopInfo().zipMandatory){
            showAlert(R.string.swipe_manual_card_zip_empty_msg);
            return false;
        }

        if(!TextUtils.isEmpty(zip) && zip.length() < CARD_ZIP_MIN_LEN){
            showAlert(R.string.swipe_manual_card_zip_invalid_msg);
            return false;
        }

        return true;
    }

    private void showAlert(int msg){
        AlertDialogFragment.showAlert(getActivity(), R.string.swipe_manual_alert_title, getString(msg));
    }

    private void simulate() {
        if (listener != null) {
            listener.onSwiped("%B378750803111032^OTTMANN/R^1601101120101518?;378750803111032=160110112010151800000?");
        }
    }

    private boolean tryCancel() {
        if (listener != null) {
            listener.onCancel();
            return true;
        }
        return false;
    }

    protected void switchLayers(boolean showManual){
        swipeLayer.setVisibility(showManual ? View.GONE : View.VISIBLE);
        manualLayer.setVisibility(showManual ? View.VISIBLE : View.GONE);
        if (hasPositiveButton())
            getPositiveButton().setText(showManual ? R.string.btn_swipe : R.string.btn_manual);
        if (showManual){
            cardNumber.requestFocus();
            KeyboardUtils.showKeyboard(getActivity(), cardNumber);
        }
    }

    private SwipeCallback msrSwipeCallback = new SwipeCallback() {

        @Override
        public void onSwipeReady() {

        }

        @Override
        public void onSwipeCompleted(CardData result) {
            if (listener != null) {
                listener.onSwiped(result.getT2DataAscii());
            }
        }

        @Override
        public void onSwipeError(int index, String error) {
            showError(R.string.swipe_card_error_msg);
        }
    };

    public PaySwipePendingFragmentDialog setListener(ISaleSwipeListener listener) {
        this.listener = listener;
        return this;
    }

    public static interface ISaleSwipeListener {

        void onSwiped(String track);

        void onCardInfo(String number, String expireDate, String cvn, String zip);

        void onCancel();
    }

    public static void show(FragmentActivity context, boolean isManualAvailable, ISaleSwipeListener listener) {
        DialogUtil.show(context, DIALOG_NAME, PaySwipePendingFragmentDialog_.builder().isManualAvailable(isManualAvailable).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}