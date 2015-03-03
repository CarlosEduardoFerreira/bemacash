package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.math.BigDecimal;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceProductAmountFragment extends PrepaidLongDistanceBaseBodyFragment {

    @FragmentArg
    protected WirelessItem chosenCategory;

    @ViewById
    protected ImageView icon;
    @ViewById
    protected TextView productName;
    @ViewById
    protected GridView amountItemsGrid;
    @ViewById
    protected TextView submit;
    private BigDecimal amount;
    private String selectedTextAmount;
    private BigDecimal feeAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.prepaid_long_distance_amount_fragement, container, false);
        amountItemsGrid = (GridView) view.findViewById(R.id.amount_items_grid);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    public void init() {
        productName.setText(chosenCategory.name);
        UrlImageViewHelper.setUrlDrawable(icon, chosenCategory.iconUrl, R.drawable.operator_default_icon, 60000);
        amountItemsGrid.setAdapter(new GridAdapter());
        feeAmount = new BigDecimal(chosenCategory.feeAmount);
        amountItemsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {

            }
        });
    }

    private LongDistanceProductAmount longDistanceProductAmount;

    public void setCallback(LongDistanceProductAmount callback) {
        this.longDistanceProductAmount = callback;
    }

    public interface LongDistanceProductAmount {
        void conditionSelected(BigDecimal amount, String phoneNumber, BigDecimal feeAmount);

        void headMessage(int errorCode);
    }

    @Click
    void submit() {

        if (enableFinish())
            complete();
        else
            error();
    }

    private void error() {
        longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_ZERO_ERROR);

    }

    private boolean enableFinish() {
        return (amount != null && amount != BigDecimal.ZERO);
    }

    private void complete() {
        longDistanceProductAmount.conditionSelected(amount, "",feeAmount);
        longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.PURCHASE_SUMMARY);
    }

    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chosenCategory.denominations.length;
        }

        @Override
        public Object getItem(int position) {
            return chosenCategory.denominations[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder mViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.product_amount_item_view, parent, false);
                mViewHolder = new MyViewHolder();
                mViewHolder.amountText = (TextView) convertView.findViewById(R.id.amount_text);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            mViewHolder.amountText.setText(commaPriceFormat(chosenCategory.denominations[position]));
            if (selectedTextAmount != null && selectedTextAmount.equalsIgnoreCase(commaPriceFormat(chosenCategory.denominations[position])))
                mViewHolder.amountText.setBackgroundResource(R.drawable.amount_item_bg_pressed);
            else
                mViewHolder.amountText.setBackgroundResource(R.drawable.amount_item_bg);


            mViewHolder.amountText.setPadding(20, 10, 0, 0);

            mViewHolder.amountText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tx = (TextView) v;
                    selectedTextAmount = tx.getText().toString();
                    String amountConverter = tx.getText().toString().substring(2);
                    amount = new BigDecimal(amountConverter);
                    notifyDataSetChanged();
                    longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);

                }
            });
            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
    }

    class MyViewHolder {
        TextView amountText;
    }
}
