package com.kaching123.tcr.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.user.LoginFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by alboyko on 08.08.2016.
 */
@EFragment
public class EBTPaymentTypeChooserDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "EBTPaymentTypeChooserDialogFragment";
    private EBTTypeChooseListener listener;

    public void setListener(EBTTypeChooseListener listener) {
        this.listener = listener;
    }

    public interface EBTTypeChooseListener {
        void onEBTCashTypeChosen();

        void onEBTFoodStampTypeChosen();
    }

    @ViewById
    protected Button btnPaxEbtFoodstamp;

    @ViewById
    protected Button btnPaxEbtCash;

    @AfterViews
    protected void init() {
        btnPaxEbtFoodstamp.setVisibility(getApp().getShopInfo().ebtFoodStampPaymentEnabled ? View.VISIBLE : View.GONE);
        btnPaxEbtCash.setVisibility(getApp().getShopInfo().ebtCashPaymentButtonEnabled ? View.VISIBLE : View.GONE);
    }

    @Click
    protected void btnPaxEbtFoodstampClicked() {
        if (listener != null) {
            listener.onEBTFoodStampTypeChosen();
        }
    }

    @Click
    protected void btnPaxEbtCashClicked() {
        if (listener != null) {
            listener.onEBTCashTypeChosen();
        }
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.ebt_chooser_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.ebt_type_chooser_dlg_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener()
    {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        };
    }
    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                Toast.makeText(getActivity(), "OnDialogClickListener", Toast.LENGTH_SHORT).show();
                return false;
            }
        };
    }

    public static void show(FragmentActivity activity, EBTTypeChooseListener listener) {
        DialogUtil.show(activity, DIALOG_NAME, EBTPaymentTypeChooserDialogFragment_.builder().build())
                .setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }
}
