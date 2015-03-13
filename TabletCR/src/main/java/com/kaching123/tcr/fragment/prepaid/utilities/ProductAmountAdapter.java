package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidWirelessProductAmountFragment;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by gdubina on 25.11.13.
 */
public class ProductAmountAdapter extends BaseAdapter {

    private Context mContext;
    private List mData;

    private String selectedTextAmount;
    private PrepaidWirelessProductAmountFragment.AmountSelectedListener listener;

    public ProductAmountAdapter(Context context, List mData, PrepaidWirelessProductAmountFragment.AmountSelectedListener listerner) {
        super();
        this.mContext = context;
        this.mData = mData;
        this.listener = listerner;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.product_amount_item_view, parent, false);
            mViewHolder = new MyViewHolder();
            mViewHolder.amountText = (TextView) convertView.findViewById(R.id.amount_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        mViewHolder.amountText.setText(commaPriceFormat((BigDecimal) mData.get(position)));
        if (selectedTextAmount != null && selectedTextAmount.equalsIgnoreCase(commaPriceFormat((BigDecimal) mData.get(position))))
            mViewHolder.amountText.setBackgroundResource(R.drawable.amount_item_bg_pressed);
        else
            mViewHolder.amountText.setBackgroundResource(R.drawable.amount_item_bg);


        mViewHolder.amountText.setPadding(16, 8, 0, 0);

        mViewHolder.amountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tx = (TextView) v;
                selectedTextAmount = tx.getText().toString();
                String amountConverter = tx.getText().toString().substring(2);
                listener.amountSelected(new BigDecimal(amountConverter));
                notifyDataSetChanged();
            }
        });
        return convertView;
    }


    class MyViewHolder {
        TextView amountText;
    }
}
