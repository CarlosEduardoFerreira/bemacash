package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.reports.EmployeeReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.reports.PayrollReportQuery;
import com.kaching123.tcr.reports.PayrollReportQuery.EmployeePayrollInfo;
import com.kaching123.tcr.util.DateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 04.02.14.
 */
@EFragment (R.layout.reports_payroll_fragment)
public class PayrollReportFragment extends SalesBaseFragment<EmployeePayrollInfo> implements IDetailsFragment {

    @FragmentArg
    protected String employeeGuid;

    @ViewById
    protected TextView commissionHeader;

    @ViewById
    protected View commissionColumn;

    private boolean isCommissionEnabled;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        isCommissionEnabled = ((TcrApplication)getActivity().getApplicationContext()).isCommissionsEnabled();

        super.onActivityCreated(savedInstanceState);
        int visibility =  isCommissionEnabled ? View.VISIBLE : View.GONE;
        commissionHeader.setVisibility(visibility);
        commissionColumn.setVisibility(visibility);
    }

    @Override
    public void updateData(long startTime, long endTime, String employeeGuid) {
        this.employeeGuid = employeeGuid;
        super.updateData(startTime, endTime, 0);
    }
    @Override
    protected ObjectsCursorAdapter<EmployeePayrollInfo> createAdapter() {
        return new PayrollAdapter(getActivity());
    }

    @Override
    public Loader<List<EmployeePayrollInfo>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<EmployeePayrollInfo>>(getActivity()){

            @Override
            public List<EmployeePayrollInfo> loadInBackground() {
                Collection<EmployeePayrollInfo> infos = PayrollReportQuery.getItems(getActivity(), startTime, endTime, employeeGuid);
                return new ArrayList<EmployeePayrollInfo>(infos);
            }
        };
    }

    private class PayrollAdapter extends ObjectsCursorAdapter<EmployeePayrollInfo>{

        public PayrollAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.reports_payroll_item_view, null, false);

            ViewHolder holder = new ViewHolder(
                    (TextView)convertView.findViewById(R.id.employee_name),
                    (TextView)convertView.findViewById(R.id.total_hours),
                    (TextView)convertView.findViewById(R.id.hourly_rate),
                    (TextView)convertView.findViewById(R.id.total_due),
                    (TextView)convertView.findViewById(R.id.commission)
            );

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, EmployeePayrollInfo item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if(item == null)
                return convertView;

            holder.name.setText(item.name);
            holder.totalHrs.setText(DateUtils.formatMins(item.totalMins));
            showPrice(holder.hRate, item.hRate);
            showPrice(holder.totalDue, isCommissionEnabled ? item.totalDue.add(item.commission) : item.totalDue);
            if (isCommissionEnabled){
                holder.commission.setVisibility(View.VISIBLE);
                showPrice(holder.commission, item.commission);
            }else{
                holder.commission.setVisibility(View.GONE);
            }

            return convertView;
        }

        private class ViewHolder{
            TextView name;
            TextView totalHrs;
            TextView hRate;
            TextView totalDue;
            TextView commission;

            private ViewHolder(TextView name, TextView totalHrs, TextView hRate, TextView totalDue, TextView commission) {
                this.name = name;
                this.totalHrs = totalHrs;
                this.hRate = hRate;
                this.totalDue = totalDue;
                this.commission = commission;
            }
        }
    }

    public static PayrollReportFragment instance(long startTime, long endTime, long resisterId) {
        return PayrollReportFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }
}
