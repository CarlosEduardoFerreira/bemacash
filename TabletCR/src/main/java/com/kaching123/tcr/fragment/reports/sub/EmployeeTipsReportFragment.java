package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.reports.EmployeeReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery.EmployeeTipsInfo;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery.ShiftTipsInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;
import static com.kaching123.tcr.fragment.UiHelper.showDecimal;

/**
 * Created by vkompaniets on 18.06.2014.
 */
@EFragment (R.layout.reports_employee_tips_fragment)
public class EmployeeTipsReportFragment extends SuperBaseFragment implements LoaderCallbacks<List<Object>>, IDetailsFragment {

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    private String employeeGuid;

    @ViewById(android.R.id.list)
    protected ListView list;
    @ViewById
    protected TextView totalCash;
    @ViewById
    protected TextView totalCredit;
    @ViewById
    protected TextView total;

    private TipsReportAdapter adapter;

    private BigDecimal totalCashTips;
    private BigDecimal totalCreditTips;

    @Override
    public void updateData(long startTime, long endTime, String employeeGuid) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.employeeGuid = employeeGuid;

        if (getActivity() != null){
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    @AfterViews
    protected void init(){
        adapter = new TipsReportAdapter(getActivity());
        list.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private static class ShiftRow {
        long start;
        long end;

        private ShiftRow(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    @Override
    public Loader<List<Object>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Object>>(getActivity()) {
            @Override
            public List<Object> loadInBackground() {
                totalCashTips = BigDecimal.ZERO;
                totalCreditTips = BigDecimal.ZERO;

                Collection<ShiftTipsInfo> items = EmployeeTipsReportQuery.getItems(getContext(), startTime, endTime, employeeGuid);
                ArrayList<Object> rows = new ArrayList<Object>();
                for (ShiftTipsInfo item : items){
                    if (item.zeroTips())
                        continue;

                    rows.add(new ShiftRow(item.start, item.end));

                    for (EmployeeTipsInfo tips : item.employeeTipsInfos){
                        if (tips.zeroTips())
                            continue;

                        totalCashTips = totalCashTips.add(tips.cashTips);
                        totalCreditTips = totalCreditTips.add(tips.creditTips);
                        rows.add(tips);
                    }
                }
                return rows;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Object>> loader, List<Object> data) {
        adapter.changeCursor(data);
        updateTotal();
    }

    @Override
    public void onLoaderReset(Loader<List<Object>> loader) {
        adapter.changeCursor(null);
        totalCashTips = BigDecimal.ZERO;
        totalCreditTips = BigDecimal.ZERO;
        updateTotal();
    }

    private void updateTotal() {
        setAmount(totalCash, totalCashTips);
        setAmount(totalCredit, totalCreditTips);
        showDecimal(total, totalCashTips.add(totalCreditTips));
    }

    private class TipsReportAdapter extends ObjectsCursorAdapter<Object> {

        public TipsReportAdapter(Context context) {
            super(context);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Class clazz = getItem(position).getClass();
            if (clazz == ShiftRow.class){
                return 0;
            }else {
                return 1;
            }
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            int type = getItemViewType(position);
            View view;
            if (type == 0){
                view = View.inflate(getContext(), R.layout.reports_employee_tips_item0_view, null);
                ViewHolderShift holder = new ViewHolderShift(
                        (TextView) view.findViewById(R.id.shift)
                );
                view.setTag(holder);
            }else {
                view = View.inflate(getContext(), R.layout.reports_employee_tips_item1_view, null);
                ViewHolderEmployee holder = new ViewHolderEmployee(
                        (TextView) view.findViewById(R.id.name),
                        (TextView) view.findViewById(R.id.cash),
                        (TextView) view.findViewById(R.id.credit),
                        (TextView) view.findViewById(R.id.total)
                );
                view.setTag(holder);
            }
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, Object item) {
            int type = getItemViewType(position);
            if (type == 0){
                ViewHolderShift holder = (ViewHolderShift) convertView.getTag();
                ShiftRow shiftRow = (ShiftRow) item;
                holder.shift.setText(EmployeeTipsReportQuery.getShift2PeriodString(shiftRow.start, shiftRow.end, false));
            }else {
                ViewHolderEmployee holder = (ViewHolderEmployee) convertView.getTag();
                EmployeeTipsInfo tipsRow = (EmployeeTipsInfo) item;
                holder.name.setText(tipsRow.fullName);
                setAmount(holder.cashTips, tipsRow.cashTips);
                setAmount(holder.creditTips, tipsRow.creditTips);
                showDecimal(holder.totalTips, tipsRow.cashTips.add(tipsRow.creditTips));
            }
            return convertView;
        }

    }

    private static void setAmount (TextView view, BigDecimal amount){
        view.setText(amount.compareTo(BigDecimal.ZERO) == 0 ? null : priceFormat(amount));
    }

    private static class ViewHolderShift {
        TextView shift;

        private ViewHolderShift(TextView shift) {
            this.shift = shift;
        }
    }

    private static class ViewHolderEmployee {
        TextView name;
        TextView cashTips;
        TextView creditTips;
        TextView totalTips;

        private ViewHolderEmployee(TextView name, TextView cashTips, TextView creditTips, TextView totalTips) {
            this.name = name;
            this.cashTips = cashTips;
            this.creditTips = creditTips;
            this.totalTips = totalTips;
        }
    }

    public static EmployeeTipsReportFragment instance(long startTime, long endTime){
        return EmployeeTipsReportFragment_.builder().startTime(startTime).endTime(endTime).build();
    }
}
