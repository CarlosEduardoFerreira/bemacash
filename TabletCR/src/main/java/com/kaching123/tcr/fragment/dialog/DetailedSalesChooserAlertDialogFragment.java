package com.kaching123.tcr.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.picker.DateTimePickerFragment;
import com.kaching123.tcr.fragment.reports.DetailedSalesRegistersAdapter;
import com.kaching123.tcr.fragment.reports.RegistersLoader;
import com.kaching123.tcr.model.RegisterModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mboychenko on 24.08.2016.
 */
@EFragment
public class DetailedSalesChooserAlertDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "DetailedSalesReportFilterFragment";
    private static final int MAX_PERIODS_COUNT = 31;
    private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    private static final SimpleDateFormat periodDateFormat = new SimpleDateFormat("h:mm a  dd MMM");

    @ViewById
    protected Spinner registerSpinner;
    @ViewById
    protected EditText fromEdit, toEdit;

    private RegisterModel registerModel;
    private DetailedSalesRegistersAdapter registersAdapter;
    private Calendar dateTimePickerCalendar = Calendar.getInstance();
    private Date fromDate;
    private Date toDate;
    private boolean inDays;
    private Dialog filterDialog;

    public interface DetailedReportFilterListener {
        void onDetailedReportFiltered(long registerId, long fromDate, long toDate);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.detailed_sales_filter_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.detailed_report_chooser_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    private DetailedReportFilterListener detailedReportChooseListener;

    @Override
    protected StyledDialogFragment.OnDialogClickListener getPositiveButtonListener() {
        return new StyledDialogFragment.OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (detailedReportChooseListener != null) {
                    detailedReportChooseListener.onDetailedReportFiltered(getSelectedRegisterId(), fromDate.getTime(), toDate.getTime());
                }
                return true;
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            detailedReportChooseListener = ((DetailedReportFilterListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " or parent fragments must implement DetailedReportFilterListener");
        }
    }

    @AfterViews
    protected void initViews() {
        initPeriodDates();
        setPeriodDates();

        fromEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.hide();
                showDateTimePicker(fromDate);
            }
        });
        toEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.hide();
                showDateTimePicker(toDate);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        filterDialog = getDialog();
        filterDialog.getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.detailed_filter_dialog_width),
                getDialog().getWindow().getAttributes().height);

        registersAdapter = new DetailedSalesRegistersAdapter(getContext());
        registerSpinner.setAdapter(registersAdapter);
        registerModel = getApp().getRegisterModel();
        getLoaderManager().restartLoader(0, null, registersLoader);
    }

    private long getSelectedRegisterId() {
        int selectedRegisterPos = registerSpinner.getSelectedItemPosition();
        long resisterId = 0;
        if (selectedRegisterPos != -1) {
            resisterId = registersAdapter.getItem(selectedRegisterPos).id;
        }
        return resisterId;
    }

    private void initPeriodDates() {
        fromDate = initFromDate(dateTimePickerCalendar);
        toDate = initToDate(dateTimePickerCalendar);
        inDays = isPeriodInDays(fromDate, toDate);
    }

    private Date initFromDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 00);
        dateTimePickerCalendar.set(Calendar.MINUTE, 00);
        dateTimePickerCalendar.set(Calendar.SECOND, 00);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    private Date initToDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 23);
        dateTimePickerCalendar.set(Calendar.MINUTE, 59);
        dateTimePickerCalendar.set(Calendar.SECOND, 59);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    private void showDateTimePicker(final Date date) {
        dateTimePickerCalendar.setTimeInMillis(date.getTime());
        DateTimePickerFragment.show(getActivity(), date, getMinFromDate(), new DateTimePickerFragment.OnDateTimeActionsListener() {
            @Override
            public boolean onDateTimeSet(Date dateTime) {
                dateTimePickerCalendar.setTime(dateTime);
                final boolean isFromDate = date == fromDate;
                dateTimePickerCalendar.set(Calendar.SECOND, isFromDate ? 0 : 59);
                dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
                filterDialog.show();
                return setPeriodDate(date, dateTimePickerCalendar.getTime());
            }

            @Override
            public boolean onCancel() {
                filterDialog.show();
                return true;
            }
        });
    }

    private void setPeriodDates() {
        fromEdit.setText(periodDateFormat.format(fromDate).toUpperCase());
        toEdit.setText(periodDateFormat.format(toDate).toUpperCase());
    }

    private Date getMinFromDate() {
        return getApp().getMinSalesHistoryLimitDateDayRounded(dateTimePickerCalendar);
    }

    private boolean setPeriodDate(Date date, Date newDate) {
        if (date.getTime() == newDate.getTime())
            return true;

        if (!validatePeriodDates(date, newDate))
            return false;

        date.setTime(newDate.getTime());
        inDays = isPeriodInDays(fromDate, toDate);
        setPeriodDates();

        return true;
    }

    private boolean validatePeriodDates(Date date, Date newDate) {
        boolean isFromDate = date == this.fromDate;
        Date fromDate = isFromDate ? newDate : this.fromDate;
        Date toDate = !isFromDate ? newDate : this.toDate;
        if (!fromDate.before(toDate)) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.reports_error_from_to_dates));
            return false;
        }

        final boolean inDays = isPeriodInDays(fromDate, toDate);
        final int periodsCount = Math.round((toDate.getTime() - fromDate.getTime()) / (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS) + 0.5f);

        int maxPeriod = MAX_PERIODS_COUNT;
        if (periodsCount > maxPeriod) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(inDays ? R.string.reports_error_period_too_large_days : R.string.reports_error_period_too_large_hrs, maxPeriod));
            cropPeriodDates(fromDate, toDate, inDays, isFromDate, maxPeriod);
        }

        checkAndFixMinDate(fromDate, toDate);

        return true;
    }

    private boolean isPeriodInDays(Date fromDate, Date toDate) {
        return (toDate.getTime() - fromDate.getTime()) > DAY_IN_MILLIS;
    }

    private static void cropPeriodDates(Date fromDate, Date toDate, boolean inDays, boolean fromDateChanged, int maxPeriod) {
        if (fromDateChanged)
            toDate.setTime(fromDate.getTime() + (maxPeriod - 1) * (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS));
        else
            fromDate.setTime(toDate.getTime() - (maxPeriod - 1) * (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS));
    }

    private void checkAndFixMinDate(Date fromDate, Date toDate) {
        Date minFromDate = getMinFromDate();
        if (minFromDate == null)
            return;

        if (fromDate.getTime() < minFromDate.getTime()) {
            Logger.w("DateRangeFragment: set date is out of sales history limit - fixing");
            fromDate.setTime(minFromDate.getTime());

            if (toDate.before(fromDate)) {
                toDate.setTime(fromDate.getTime());
            }
        }

    }

    private LoaderManager.LoaderCallbacks<List<RegisterModel>> registersLoader = new RegistersLoader() {

        @Override
        public void onLoadFinished(Loader<List<RegisterModel>> loader, List<RegisterModel> result) {
            ArrayList<RegisterModel> arrayList = new ArrayList<RegisterModel>(result.size() + 1);
            arrayList.add(new RegisterModel(0, null, null, getString(R.string.register_label_all), null, 0, 0, null));
            arrayList.addAll(result);
            registersAdapter.changeCursor(arrayList);
            for (int i = 0; i < arrayList.size() - 1; i++) {
                if (arrayList.get(i).equals(registerModel)) {
                    registerSpinner.setSelection(i);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<List<RegisterModel>> loader) {
            registersAdapter.changeCursor(null);
        }

        @Override
        protected Context getLoaderContext() {
            return getActivity();
        }
    };

    public static void show(FragmentActivity activity) {
        DialogUtil.show(activity, DIALOG_NAME, DetailedSalesChooserAlertDialogFragment_.builder().build());
    }
}
