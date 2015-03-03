package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.reports.sub.SalesByTenderTypesFragment.TenderInfo;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.reports.SalesByTenderTypeQuery;
import com.kaching123.tcr.reports.SalesByTenderTypeQuery.PaymentStat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamsterksu on 28.01.14.
 */
@EFragment(R.layout.reports_sales_by_tender_types_list_fragment)
public class SalesByTenderTypesFragment extends SalesBaseFragment<TenderInfo> {

    @Override
    protected ObjectsCursorAdapter<TenderInfo> createAdapter() {
        return new ItemAdapter(getActivity());
    }

    @Override
    public Loader<List<TenderInfo>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<TenderInfo>>(getActivity()) {
            @Override
            public List<TenderInfo> loadInBackground() {
                PaymentStat stat = SalesByTenderTypeQuery.getItems(getContext(), startTime, endTime, resisterId);
                ArrayList<TenderInfo> result = new ArrayList<TenderInfo>();
                result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_cash), stat.cash));
                //result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_gift), stat.gift));
                result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_credit), stat.creditCard));
                result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_credit_receipt), stat.creditReceipt));
                result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_offline_credit), stat.offlineCredit));
                result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_check), stat.check));
                totalValue = BigDecimal.ZERO.add(stat.cash)/*.add(stat.gift)*/.add(stat.creditCard).add(stat.creditReceipt).add(stat.offlineCredit).add(stat.check);
                ShopInfo shopInfo = ((TcrApplication)getContext().getApplicationContext()).getShopInfo();
                if (shopInfo.acceptEbtCards){
                    result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_ebt_cash), stat.ebtCash));
                    result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_ebt_foodstamp), stat.ebtFoodstamp));
                    totalValue = totalValue.add(stat.ebtCash).add(stat.ebtFoodstamp);
                }
                if (shopInfo.acceptDebitCards){
                    result.add(new TenderInfo(getString(R.string.report_sales_by_tender_types_debit), stat.debit));
                    totalValue = totalValue.add(stat.debit);
                }
                return result;
            }
        };
    }

    public static class TenderInfo{
        private String title;
        private BigDecimal amaount;

        public TenderInfo(String title, BigDecimal amaount) {
            this.title = title;
            this.amaount = amaount;
        }
    }

    private class ItemAdapter extends ObjectsCursorAdapter<TenderInfo> {

        public ItemAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.reports_sales_by_tender_type_item_view, null);
            view.setTag(new UiHolder(
                    (TextView) view.findViewById(R.id.description),
                    (TextView) view.findViewById(R.id.price)
            ));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, TenderInfo item) {
            UiHolder holder = (UiHolder) convertView.getTag();
            holder.description.setText(item.title);
            UiHelper.showPrice(holder.price, item.amaount);
            return convertView;
        }

    }

    private static class UiHolder {
        TextView description;
        TextView price;

        private UiHolder(TextView description, TextView price) {
            this.description = description;
            this.price = price;
        }
    }

    public static SalesByTenderTypesFragment instance(long startTime, long endTime, long resisterId) {
        return SalesByTenderTypesFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }
}
