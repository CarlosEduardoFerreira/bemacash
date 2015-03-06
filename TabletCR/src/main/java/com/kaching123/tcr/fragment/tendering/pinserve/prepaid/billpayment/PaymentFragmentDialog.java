package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.saleorder.AddBillPaymentOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.AddBillPaymentOrderCommand.BaseAddBillPaymentOrderCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class PaymentFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "PaymentFragmentDialog";

    protected BaseAddBillPaymentOrderCallback callback;

    @FragmentArg
    Boolean isFixed;
    @FragmentArg
    BigDecimal amount;
    @FragmentArg
    String prepaidDescription;
    @FragmentArg
    PrepaidType prepaidType;
    @FragmentArg
    Broker broker;
    @FragmentArg
    BigDecimal transactionFee;


    public void setCallback(BaseAddBillPaymentOrderCallback callback) {
        this.callback = callback;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cc_in_progress_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_progress_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
    }

    @AfterViews
    protected void init() {
        AddBillPaymentOrderCommand.start(getActivity(), isFixed.booleanValue(), amount, prepaidDescription, prepaidType, broker, transactionFee,callback);
    }

    public static void show(FragmentActivity context,
                            Boolean isFixed,
                            BigDecimal amount,
                            String prepaidDescription,
                            PrepaidType prepaidType,
                            Broker broker,
                            BigDecimal transactionFee,
                            BaseAddBillPaymentOrderCallback callback) {
        PaymentFragmentDialog dialog = PaymentFragmentDialog_.builder()
                .isFixed(isFixed)
                .amount(amount)
                .prepaidDescription(prepaidDescription)
                .prepaidType(prepaidType)
                .broker(broker)
                .transactionFee(transactionFee)
                .build();
        dialog.setCallback(callback);
        DialogUtil.show(context, DIALOG_NAME, dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
