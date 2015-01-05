package com.kaching123.tcr.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.ResourseUtils;
import com.kaching123.tcr.util.StringUtils;

import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
public class TransactionTenderAdapter extends ArrayAdapter<Transaction> {

    private final List<Transaction> list;
    private final Activity context;

    public TransactionTenderAdapter(Activity context, List<Transaction> list) {
        super(context, R.layout.tendering_history_payment_transaction_item_view, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView text;
        protected TextView total;
        protected ImageView icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.tendering_history_payment_transaction_item_view, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.message);
            viewHolder.total = (TextView) view.findViewById(R.id.total);
            viewHolder.icon = (ImageView) view.findViewById(R.id.icon);

            view.setTag(viewHolder);
            viewHolder.total.setTag(list.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).total.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(context.getString(R.string.blackstone_pay_transaction_constructor, String.valueOf(position + 1)
                .concat(StringUtils.getNumericPostfix(position + 1)).concat(" ")));
        Transaction current = list.get(position);
        holder.total.setText(UiHelper.valueOf(current.getAmount()));
        holder.icon.setImageResource(ResourseUtils.getMiniIconForTransactionType(current.getType()));
        return view;

    }
}