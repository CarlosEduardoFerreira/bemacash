package com.kaching123.tcr.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;
import com.kaching123.tcr.websvc.api.prepaid.VectorPaymentOption;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by teli.yin on 6/6/2014.
 */
public class BillPaymentOptionsAdapter extends BaseAdapter {

    private FragmentActivity mContext;

    private VectorPaymentOption mPymentOption;

    private BillPaymentOptionsListener mListener;

    private int selectedPosition;

    public BillPaymentOptionsAdapter(FragmentActivity context, VectorPaymentOption paymentOptions, BillPaymentOptionsListener listener) {
        this.mContext = context;
        this.mPymentOption = paymentOptions;
        this.mListener = listener;
        this.selectedPosition = -1;
    }

    private boolean InCutOffTime() {
        boolean InCutOffTime = false;
        try {
            String pattern = "HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date cutOffTime = sdf.parse("17:00");
            String UTCTime = sdf.format(new Date());
            Date UTCTimes = sdf.parse(UTCTime);
            InCutOffTime = (UTCTimes.compareTo(cutOffTime) < 0);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return InCutOffTime;
    }

    @Override
    public int getCount() {
        return mPymentOption.size();
    }

    @Override
    public Object getItem(int i) {
        return mPymentOption.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.bill_payment_options_listview_item, null);

        TextView fee = (TextView) view.findViewById(R.id.fee);
        fee.setText(getString(R.string.Dollar_Ampersand) + mPymentOption.get(i).feeAmount);
        TextView type = (TextView) view.findViewById(R.id.type);
        type.setText(mPymentOption.get(i).paymentType);
        TextView description = (TextView) view.findViewById(R.id.description);
        String sDescription = null;

        if (mPymentOption.get(i).paymentType.equalsIgnoreCase(getString(R.string.SAME_DAY)))
            sDescription = InCutOffTime() == true ? (getString(R.string.SAME_DAY) + getString(R.string.SAME_DAY_IN_CUTOFFTIME_HOURS)) : (getString(R.string.SAME_DAY) + getString(R.string.SAME_DAY_OVER_CUTOFFTIME_HOURS));
        else if (mPymentOption.get(i).paymentType.equalsIgnoreCase(getString(R.string.NEXT_DAY)))
            sDescription = InCutOffTime() == true ? (getString(R.string.NEXT_DAY) + getString(R.string.NEXT_DAY_IN_CUTOFFTIME_HOURS)) : (getString(R.string.NEXT_DAY) + getString(R.string.NEXT_DAY_OVER_CUTOFFTIME_HOURS));
        else if (mPymentOption.get(i).paymentType.equalsIgnoreCase(getString(R.string.STANDARD)))
            sDescription = InCutOffTime() == true ? (getString(R.string.STANDARD) + getString(R.string.STANDARD_IN_CUTOFFTIME_HOURS)) : (getString(R.string.STANDARD) + getString(R.string.STANDARD_OVER_CUTOFFTIME_HOURS));


        description.setText(sDescription);

        if (i == selectedPosition) {
            fee.setTextColor(mContext.getResources().getColor(R.color.prepaid_dialog_white));
            type.setTextColor(mContext.getResources().getColor(R.color.prepaid_dialog_white));
            description.setTextColor(mContext.getResources().getColor(R.color.prepaid_dialog_white));
            view.setBackgroundColor(mContext.getResources().getColor(R.color.prepaid_dialog_buttons_background_color));
        }
        view.setOnClickListener(listener(i, mPymentOption.get(i)));

        return view;
    }

    private String getString(int name) {
        return mContext.getResources().getString(name);
    }

    public interface BillPaymentOptionsListener {
        void OnClicked(int position, PaymentOption paymentOption);
    }

    protected View.OnClickListener listener(final int position, final PaymentOption paymentOption) {

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = position;
                mListener.OnClicked(position, paymentOption);
                notifyDataSetChanged();
            }
        };
    }


}
