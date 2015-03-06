package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.other.OtherSaleCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

@EFragment
public class PayOtherFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = PayOtherFragmentDialog.class.getSimpleName();

    protected ISaleProgressListener listener;

    protected Transaction transaction;

    @FragmentArg
    protected Type type;

    public enum Type {
        OFFLINE_CREDIT, CHECK
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_other_fragment;
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


    public PayOtherFragmentDialog setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public PayOtherFragmentDialog setListener(ISaleProgressListener listener) {
        this.listener = listener;
        return this;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        setCancelable(false);

        doCommand();
    }

    protected void doCommand() {
        IPaymentGateway paymentGateway;
        switch (type) {
            case OFFLINE_CREDIT:
                paymentGateway = PaymentGateway.OFFLINE_CREDIT.gateway();
                break;
            case CHECK:
                paymentGateway = PaymentGateway.CHECK.gateway();
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
        paymentGateway.sale(getActivity(), this, null, null, transaction);
    }

    @OnSuccess(OtherSaleCommand.class)
    public void onSuccess() {
        listener.onComplete();
    }

    @OnFailure(OtherSaleCommand.class)
    public void onFailure() {
        Logger.e("OtherSaleCommand.onPayFail()");
        listener.onFail();
    }


    public interface ISaleProgressListener {

        void onComplete();
        void onFail();

    }

    public static void show(FragmentActivity context,
                            final Transaction transaction,
                            ISaleProgressListener listener,
                            Type type) {
        DialogUtil.show(context, DIALOG_NAME, PayOtherFragmentDialog_.builder().type(type).build())
                .setListener(listener).setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}