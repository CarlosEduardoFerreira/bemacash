package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductAccessNumbersFragment extends PrepaidLongDistanceBaseBodyFragment {

    @FragmentArg
    protected WirelessItem chosenCategory;
    @ViewById
    protected ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_long_distance_product_access_numbers_fragment, container, false);
    }

    public void setCallback(closeLayoutCallback callback) {
        this.callback = callback;
    }

    @Click
    void closeLayout() {
        callback.pageSelected(PrepaidLongDistanceProductInfoMenuFragment.ACCESS_NUMBERS);
    }

    @AfterViews
    public void init() {
        listview.setAdapter(new AccessNumbersAdapter());
    }

    class AccessNumbersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chosenCategory.productAccessPhones.length;
        }

        @Override
        public Object getItem(int position) {
            return chosenCategory.productAccessPhones[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder mViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.access_numbers_item, parent, false);
                mViewHolder = new MyViewHolder();
                mViewHolder.accessNumber = (TextView) convertView.findViewById(R.id.access_number);
                mViewHolder.state = (TextView) convertView.findViewById(R.id.state);
                mViewHolder.city = (TextView) convertView.findViewById(R.id.city);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            if (chosenCategory.productAccessPhones[position] != null) {
                mViewHolder.accessNumber.setText(chosenCategory.productAccessPhones[position].phoneNumber != null ? chosenCategory.productAccessPhones[position].phoneNumber : "");
                mViewHolder.state.setText(chosenCategory.productAccessPhones[position].state != null ? chosenCategory.productAccessPhones[position].state : "");
                mViewHolder.city.setText(chosenCategory.productAccessPhones[position].city != null ? chosenCategory.productAccessPhones[position].city : "");
            }


            return convertView;
        }
    }

    class MyViewHolder {
        TextView accessNumber;
        TextView state;
        TextView city;
    }
}
