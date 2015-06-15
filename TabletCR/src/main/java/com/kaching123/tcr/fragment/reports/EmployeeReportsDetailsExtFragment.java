package com.kaching123.tcr.fragment.reports;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by vkompaniets on 05.02.14.
 */
@EFragment(R.layout.reports_details_spinner_ext_fragment)
public class EmployeeReportsDetailsExtFragment extends EmployeeReportsDetailsFragment {

    private enum Period {
        WEEK("Week"), MONTH("Month"), FLOAT("Float");

        private final String label;

        Period(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private ArrayAdapter<Period> periodAdapter;

    @ViewById
    protected Spinner periodSpinner;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        periodAdapter = new ArrayAdapter<Period>(getActivity(), R.layout.spinner_item_light, Period.values());
        periodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        periodSpinner.setAdapter(periodAdapter);
        periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onPeriodSelected(periodAdapter.getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

    }

    private void onPeriodSelected(Period period) {
        switch (period){
            case WEEK:
                fromDate.setTime(addTimeToDate(fromDate, toDate, Calendar.DATE, -7));
                break;
            case MONTH:
                fromDate.setTime(addTimeToDate(fromDate, toDate, Calendar.MONTH, -1));
                break;
        }

        Date minFromDate = getMinFromDate();
        if (fromDate.getTime() < minFromDate.getTime())
            fromDate.setTime(minFromDate.getTime());

        setPeriodDates();
        loadData();
    }


    @Override
    protected boolean validatePeriodDates(Date date, Date newDate) {
        boolean isFromDate = date == this.fromDate;
        Date fromDate = isFromDate ? newDate : this.fromDate;
        Date toDate = !isFromDate ? newDate : this.toDate;

        switch((Period)periodSpinner.getSelectedItem()){
            case WEEK:
                if (isFromDate){
                    toDate.setTime(addTimeToDate(toDate, newDate, Calendar.DATE, 7));
                }else{
                    fromDate.setTime(addTimeToDate(fromDate, newDate , Calendar.DATE, -7));
                    Date minFromDate = getMinFromDate();
                    if (fromDate.getTime() < minFromDate.getTime())
                        fromDate.setTime(minFromDate.getTime());
                }
                break;
            case MONTH:
                if (isFromDate){
                    toDate.setTime(addTimeToDate(toDate, newDate, Calendar.MONTH, 1));
                }else{
                    fromDate.setTime(addTimeToDate(fromDate, newDate , Calendar.MONTH, -1));
                    Date minFromDate = getMinFromDate();
                    if (fromDate.getTime() < minFromDate.getTime())
                        fromDate.setTime(minFromDate.getTime());
                }
                break;
        }

        if (!fromDate.before(toDate)) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.reports_error_from_to_dates));
            return false;
        }

        final boolean inDays = isPeriodInDays(fromDate, toDate);
        final int periodsCount = Math.round((toDate.getTime() - fromDate.getTime()) / (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS) + 0.5f);

        int maxPeriod = getMaxPeriod();
        if (periodsCount > maxPeriod + 1) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(inDays ? R.string.reports_error_period_too_large_days : R.string.reports_error_period_too_large_hrs, maxPeriod));
            cropPeriodDates(fromDate, toDate, inDays, isFromDate, maxPeriod);
        }
        return true;
    }

    private static long addTimeToDate(Date date1, Date date2, int field, int amount){
        Calendar c1 = new GregorianCalendar();
        c1.setTime(date1);
        Calendar c2 = new GregorianCalendar();
        c2.setTime(date2);

        int hours = c1.get(Calendar.HOUR_OF_DAY);
        int minutes = c1.get(Calendar.MINUTE);
        int seconds = c1.get(Calendar.SECOND);

        try {
            c2.add(field, amount);
            c2.set(Calendar.HOUR_OF_DAY, hours);
            c2.set(Calendar.MINUTE, minutes);
            c2.set(Calendar.SECOND, seconds);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return c2.getTimeInMillis();
    }


    public static EmployeeReportsDetailsExtFragment instance(ReportType reportType) {
        return EmployeeReportsDetailsExtFragment_.builder().type(reportType).build();
    }
}
