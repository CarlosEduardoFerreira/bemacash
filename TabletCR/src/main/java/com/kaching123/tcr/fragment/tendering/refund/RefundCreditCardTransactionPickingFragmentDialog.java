package com.kaching123.tcr.fragment.tendering.refund;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;

import junit.framework.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class RefundCreditCardTransactionPickingFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "PayNotificationFragmentDialog";

    protected RefundTransactionAdapter adapter;

    @ViewById
    protected ListView list;

    @ViewById
    protected TextView total;

    private ILoader listener;
    private BigDecimal amount;
    private final List<PaymentTransactionModel> transactions = new ArrayList<PaymentTransactionModel>();

    @AfterViews
    protected void init() {
        Assert.assertNotNull(transactions);
        Assert.assertNotNull(total);
        Logger.d("Hello and welcome to tendering CC list. About to set %d items to the list", transactions.size());

        adapter = new RefundTransactionAdapter(getActivity(), R.layout.refund_transaction_listrow, transactions);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.onItemClicked(adapter.getItem(i));
            }
        });
        total.setText(UiHelper.valueOf(amount));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.base_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        setCancelable(false);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.refund_transaction_listi_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.refund_CC_transactions_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_abort;
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
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                abort();
                return false;
            }
        };
    }

    private boolean abort() {
        if (listener != null) {
            listener.onCancel();
            return true;
        }
        return false;
    }

    public RefundCreditCardTransactionPickingFragmentDialog setTotal(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public RefundCreditCardTransactionPickingFragmentDialog setListener(ILoader listener) {
        this.listener = listener;
        return this;
    }

    public RefundCreditCardTransactionPickingFragmentDialog setTransactions(List<PaymentTransactionModel> transactions) {
        for (PaymentTransactionModel item : transactions) {
            if (!PaymentType.SALE.equals(item.paymentType)) {
                Logger.d("We skip this unwanted transaction - it is no SALE one");
                continue;
            }
            if (PaymentGateway.BLACKSTONE.equals(item.gateway)
                    || PaymentGateway.PAX.equals(item.gateway)
                    || PaymentGateway.PAX_DEBIT.equals(item.gateway)
                    || PaymentGateway.PAX_EBT_FOODSTAMP.equals(item.gateway)) {
                this.transactions.add(item);
            }
        }
        return this;
    }

    public static void show(FragmentActivity context, final List<PaymentTransactionModel> transactions, BigDecimal total, ILoader listener) {
        DialogUtil.show(context, DIALOG_NAME, RefundCreditCardTransactionPickingFragmentDialog_.builder().build())
                .setListener(listener).setTransactions(transactions).setTotal(total);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface ILoader {

        void onCancel();

        void onItemClicked(PaymentTransactionModel item);
    }
}
