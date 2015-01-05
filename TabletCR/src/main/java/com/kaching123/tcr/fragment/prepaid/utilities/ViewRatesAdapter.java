package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.websvc.api.prepaid.ProductRate;
import com.kaching123.tcr.websvc.api.prepaid.VectorProductRate;

/**
 * Created by teli.yin on 11/10/2014.
 */
public class ViewRatesAdapter extends BaseAdapter {
    private ViewRatesClass[] productRate;
    private Context context;

    public ViewRatesAdapter(Context context, VectorProductRate productRate) {
        this.context = context;
        this.productRate = getRates(productRate);

    }

    @Override
    public int getCount() {
        return productRate.length;
    }

    @Override
    public Object getItem(int position) {
        return productRate[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.view_rates_item, parent, false);
            mViewHolder = new MyViewHolder();
            mViewHolder.init(mViewHolder, convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        if (productRate[position] != null) {
            mViewHolder.toll.setText(productRate[position].toll != null ? productRate[position].toll : "");
            mViewHolder.country.setText(productRate[position].country != null ? productRate[position].country : "");
            mViewHolder.city.setText(productRate[position].city != null ? productRate[position].city : "");
            mViewHolder.toll_rate.setText(productRate[position].toll_rate != null ? productRate[position].toll_rate : "");
        }


        return convertView;
    }

    private ViewRatesClass[] getRates(VectorProductRate productRates) {
        ViewRatesClass[] viewRates = new ViewRatesClass[productRates.size() * 2];
        for (int i = 0; i < productRates.size() * 2; i++) {
            ViewRatesClass rate = null;
            ProductRate productRate = productRates.get(i / 2);
            String[] locations = productRate.country.split(" ");
            if (i % 2 == 0) {
                rate = new ViewRatesClass(context.getString(R.string.prepaid_view_rates_local), locations[0], locations.length > 1 ? locations[1] : "", productRate.localRate + "");
            } else {
                rate = new ViewRatesClass(context.getString(R.string.prepaid_view_rates_toll_free), locations[0], locations.length > 1 ? locations[1] : "", productRate.tollFreeRate + "");
            }
            viewRates[i] = rate;
        }
        return viewRates;
    }
}


class ViewRatesClass {
    String toll;
    String country;
    String city;
    String toll_rate;

    public ViewRatesClass(String toll, String country, String city, String toll_rate) {
        this.toll = toll;
        this.country = country;
        this.city = city;
        this.toll_rate = toll_rate;
    }

}

class MyViewHolder {
    TextView toll;
    TextView country;
    TextView city;
    TextView toll_rate;

//        TextView amount_three;
//        TextView amount_five;
//        TextView amount_ten;
//        TextView amount_twenty;

    void init(MyViewHolder mViewHolder, View convertView) {
        mViewHolder.country = (TextView) convertView.findViewById(R.id.country);
        mViewHolder.toll = (TextView) convertView.findViewById(R.id.toll);
        mViewHolder.city = (TextView) convertView.findViewById(R.id.city);
        mViewHolder.toll_rate = (TextView) convertView.findViewById(R.id.toll_rate);
//            mViewHolder.amount_three = (TextView) convertView.findViewById(R.id.amount_three);
//            mViewHolder.amount_five = (TextView) convertView.findViewById(R.id.amount_five);
//            mViewHolder.amount_ten = (TextView) convertView.findViewById(R.id.amount_ten);
//            mViewHolder.amount_twenty = (TextView) convertView.findViewById(R.id.amount_twenty);
    }


}
