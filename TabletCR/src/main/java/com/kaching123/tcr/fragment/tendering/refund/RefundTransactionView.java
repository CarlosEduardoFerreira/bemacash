package com.kaching123.tcr.fragment.tendering.refund;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.StringUtils;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * @author Ivan v. Rikhmayer
 */
@EViewGroup(R.layout.refund_transaction_listrow)
public class RefundTransactionView extends FrameLayout {

    @ViewById
    protected TextView message;

    @ViewById
    protected TextView total;

    public PaymentTransactionModel transaction;


    public RefundTransactionView(Context context) {
        super(context);
    }

    public void bind(int position,
                     PaymentTransactionModel transaction) {
        this.transaction = transaction;
        showPrice(this.total, CalculationUtil.value(transaction.availableAmount));
        this.message.setText(getContext().getString(R.string.blackstone_pay_transaction_constructor, String.valueOf(position + 1)
                .concat(StringUtils.getNumericPostfix(position + 1)).concat(" ")));
    }

}