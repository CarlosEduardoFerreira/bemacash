package com.kaching123.tcr.fragment.tendering.payment;

import android.os.Bundle;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;

/**
 * @author Ivan v. Rikhmayer
 */
public abstract class TransactionPendingFragmentDialogBase<T extends TransactionPendingFragmentDialogBase, Response extends ResponseBase> extends StyledDialogFragment {

    protected ISaleProgressListener listener;

    protected User user;
    protected Transaction transaction;
    protected CreditCard card;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        doCommand();
    }

    protected abstract void doCommand();

    @Override
    protected int getDialogContentLayout() {
        return R.layout.payment_cc_in_progress_fragment;
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

    public T setUser(User user) {
        this.user = user;
        return (T)this;
    }

    public T setTransaction(Transaction transaction) {
        this.transaction = transaction;
        return (T)this;
    }

    public T setCard(CreditCard card) {
        this.card = card;
        return (T)this;
    }

    protected boolean tryComplete(Transaction transaction, Response code, ErrorReason reason) {
        if (listener != null) {
            listener.onComplete(transaction, code, reason);
            return true;
        }
        return false;
    }

    public TransactionPendingFragmentDialogBase setListener(ISaleProgressListener<Response> listener) {
        this.listener = listener;
        return this;
    }

    public interface ISaleProgressListener<Response extends ResponseBase> {

        void onComplete(Transaction transaction, Response code, ErrorReason reason);

        void onCancel();
    }
}