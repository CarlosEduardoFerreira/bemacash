package com.kaching123.tcr.fragment.tendering.payment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.util.StringUtils;

/**
 * Created by pkabakov on 29.05.2014.
 */
@EViewGroup(R.layout.close_transaction_item)
public class CloseTransactionView extends RelativeLayout {

    @ViewById
    protected TextView label;

    @ViewById
    protected TextView value;

    public CloseTransactionView(Context context) {
        super(context);
    }

    public CloseTransactionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CloseTransactionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bind(int position, PaymentTransactionModel item) {
        label.setText(getContext().getString(R.string.blackstone_pay_transaction_constructor, String.valueOf(position + 1).concat(StringUtils.getNumericPostfix(position + 1)).concat(" ")));
        UiHelper.showPrice(value, item.availableAmount);
    }
}
