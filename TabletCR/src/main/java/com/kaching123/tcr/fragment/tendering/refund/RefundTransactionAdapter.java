package com.kaching123.tcr.fragment.tendering.refund;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.util.StringUtils;

import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
public class RefundTransactionAdapter extends ArrayAdapter<PaymentTransactionModel> {

    private Activity context;

    private final List<PaymentTransactionModel> list;

    public RefundTransactionAdapter(Activity context, int resource, List<PaymentTransactionModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.list = objects;
    }

    static class ViewHolder {
        protected TextView name;
        protected TextView total;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.refund_transaction_listrow, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.message);
            viewHolder.total = (TextView) view.findViewById(R.id.total);

            view.setTag(viewHolder);
            viewHolder.total.setTag(list.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).total.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(context.getString(R.string.blackstone_pay_transaction_constructor, String.valueOf(position + 1)
                .concat(StringUtils.getNumericPostfix(position + 1)).concat(" ")));
        PaymentTransactionModel current = list.get(position);
        holder.total.setText(UiHelper.valueOf(current.availableAmount));
        return view;
    }
}