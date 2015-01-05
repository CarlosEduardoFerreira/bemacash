package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.ResourseUtils;
import com.kaching123.tcr.util.StringUtils;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * @author Ivan v. Rikhmayer
 */
@EViewGroup(R.layout.tendering_history_payment_transaction_item_view)
public class TransactionHistoryMiniItemView extends FrameLayout {

    @ViewById
    protected TextView message;
    @ViewById
    protected TextView total;
    @ViewById
    protected ImageView icon;
    @ViewById
    protected ImageView status;
    private PaymentTransactionModel transaction;


    public TransactionHistoryMiniItemView(Context context) {
        super(context);
    }

    public void bind(int position,
                     PaymentTransactionModel transaction) {
        this.transaction = transaction;
        showPrice(this.total, CalculationUtil.value(transaction.availableAmount));
        this.message.setText(getContext().getString(R.string.blackstone_pay_transaction_constructor, String.valueOf(position + 1)
                .concat(StringUtils.getNumericPostfix(position + 1)).concat(" ")));
        this.icon.setImageResource(ResourseUtils.getMiniIconForTransactionType(transaction.gateway));
        int imgResource = ResourseUtils.getMiniIconForTransactionType(transaction.paymentType);
        if (imgResource == 0) {
            this.status.setVisibility(GONE);
        } else {
            this.status.setVisibility(VISIBLE);
            this.status.setImageResource(ResourseUtils.getMiniIconForTransactionType(transaction.paymentType));
        }
    }

}