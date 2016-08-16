package com.kaching123.tcr.fragment.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Optional;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.chart.BarChart;
import com.kaching123.tcr.component.chart.BarChart.PeriodBarChartData;
import com.kaching123.tcr.component.chart.BarChart.PeriodBarData;
import com.kaching123.tcr.component.chart.BarChart.PeriodUnit;
import com.kaching123.tcr.component.chart.BarChart.ValueUnit;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.ShiftViewModel;
import com.kaching123.tcr.model.TopItemModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.model.converter.TopItemsFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopSchema2.ReportsTopItemsView2;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.CloseManagerTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.OpenManagerTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.ShiftView2.ShiftTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionView;
import com.kaching123.tcr.store.ShopStore.ShiftView;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by pkabakov on 13.01.14.
 */
@EFragment(R.layout.reports_fragment)
public class ReportsFragment extends DateRangeFragment {

    private static final int MAX_PERIODS_COUNT = 24;

    /*public enum Mode {
        PER_SHIFT, PER_PERIOD, PER_REGISTER;
    }*/

    private static final Uri PAYMENT_TRANSACTIONS_URI = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);
    private static final Uri TOP_ITEMS_URI = ShopProvider.getContentUri(ShopStore.ReportsTopItemsView.URI_CONTENT);
    private static final Uri SHIFTS_URI = ShopProvider.getContentUri(ShiftView.URI_CONTENT);

    private static final int LOADER_PAYMENT_TRANSACTIONS_ID = 1;
    private static final int LOADER_TOP_ITEMS_ID = 2;
    private static final int LOADER_SHIFTS_ID = 3;
    private static final int LOADER_REGISTERS_ID = 3;

    @ViewById
    protected TextView modeEntitiesLabel;
    @ViewById
    protected Spinner modeEntitiesSpinner;

    @ViewById
    protected TextView totalValueLabel;
    @ViewById
    protected TextView averageValueLabel;
    @ViewById
    protected TextView transactionsValueLabel;
    @ViewById
    protected View topItemsContainer;
    @ViewById
    protected TextView topItemsValueLabel;

    @ViewById
    protected BarChart barChart;

    @ViewById
    protected TextView switchValue;
    @ViewById
    protected TextView switchCount;

    /*@FragmentArg
    protected Mode mode;*/

    private ValueUnit valueUnit = ValueUnit.COUNT;

    private ReportsFragmentListener reportsFragmentListener;

    //private ShiftsAdapter shiftsAdapter;
    private RegistersAdapter registersAdapter;

    /*private ShiftViewModel selectedShift;
    private RegisterModel selectedRegister;*/

    public static ReportsFragment instantiate(/*Mode mode*/){
        return ReportsFragment_.builder()/*.mode(mode)*/.build();
    }

    @Override
    protected boolean supportMinFromDate() {
        return true;
    }

    public long getSelectedRegisterId() {
        int selectedRegisterPos = modeEntitiesSpinner.getSelectedItemPosition();
        long resisterId = 0;
        if (selectedRegisterPos != -1) {
            resisterId = registersAdapter.getItem(selectedRegisterPos).id;
        }
        return resisterId;
    }

    @Override
    protected Date initFromDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.add(Calendar.HOUR_OF_DAY, -23);
        dateTimePickerCalendar.set(Calendar.MINUTE, 00);
        dateTimePickerCalendar.set(Calendar.SECOND, 00);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    @Override
    protected Date initToDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.MINUTE, 59);
        dateTimePickerCalendar.set(Calendar.SECOND, 59);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    /*public void setMode(Mode mode) {
        if (this.mode == mode)
            return;

        this.mode = mode;

        if (getActivity() == null)
            return;

        setModeUi();
        loadModeData();
    }

    private void setModeUi() {
        switch (mode) {
            case PER_SHIFT:
                modeEntitiesLabel.setVisibility(View.VISIBLE);
                modeEntitiesSpinner.setVisibility(View.VISIBLE);
                modeEntitiesLabel.setText(R.string.reports_mode_entity_shift);
                if (modeEntitiesSpinner.getAdapter() != getShiftsAdapter())
                    modeEntitiesSpinner.setAdapter(getShiftsAdapter());
                break;
            case PER_PERIOD:
                modeEntitiesLabel.setVisibility(View.GONE);
                modeEntitiesSpinner.setVisibility(View.GONE);
                break;
            case PER_REGISTER:
                modeEntitiesLabel.setVisibility(View.VISIBLE);
                modeEntitiesSpinner.setVisibility(View.VISIBLE);
                modeEntitiesLabel.setText(R.string.reports_mode_entity_register);
                if (modeEntitiesSpinner.getAdapter() != getRegistersAdapter())
                    modeEntitiesSpinner.setAdapter(getRegistersAdapter());
                break;
        }
    }*/

    /*private void loadModeData() {
        switch (mode) {
            case PER_SHIFT:
                stopLoadRegisters();
                clearSelectedRegister();
                loadShifts();
                break;
            case PER_PERIOD:
                stopLoadShifts();
                stopLoadRegisters();
                clearSelectedShift();
                clearSelectedRegister();
                loadData();
                break;
            case PER_REGISTER:
                stopLoadShifts();
                clearSelectedShift();
                loadRegisters();
                break;
        }
    }*/

/*    private ShiftsAdapter getShiftsAdapter() {
        if (shiftsAdapter == null)
            shiftsAdapter = new ShiftsAdapter(getActivity());
        return shiftsAdapter;
    }*/

    private RegistersAdapter getRegistersAdapter() {
        if (registersAdapter == null)
            registersAdapter = new RegistersAdapter(getActivity());
        return registersAdapter;
    }

    public void setReportsFragmentListener(ReportsFragmentListener reportsFragmentListener) {
        this.reportsFragmentListener = reportsFragmentListener;
    }

    public Date getFromDate() {
        if (fromDate == null)
            return null;
        return new Date(fromDate.getTime());
    }

    public Date getToDate() {
        if (toDate == null)
            return null;
        return new Date(toDate.getTime());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadRegisters();
    }

    protected void initViews() {
        super.initViews();

        modeEntitiesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
                /*Object item = modeEntitiesSpinner.getAdapter().getItem(position);
                setSelectedRegister((RegisterModel) item);*/
                /*switch (mode) {
                    case PER_SHIFT:
                        setSelectedShift((ShiftViewModel) item);
                        break;
                    case PER_REGISTER:
                        setSelectedRegister((RegisterModel) item);
                        break;
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        modeEntitiesSpinner.setAdapter(getRegistersAdapter());
        barChart.setValueUnit(valueUnit);
        switchValue.setActivated(valueUnit == ValueUnit.AMOUNT);
        switchCount.setActivated(valueUnit == ValueUnit.COUNT);

        //setModeUi();
    }

    private void setValueUnit(ValueUnit unit){
        if (unit == ValueUnit.AMOUNT){
            switchCount.setActivated(false);
            switchValue.setActivated(true);
        }else{
            switchCount.setActivated(true);
            switchValue.setActivated(false);
        }
        valueUnit = unit;
        barChart.setValueUnit(unit);
    }

    /*private void clearSelectedShift() {
        if (selectedShift == null)
            return;

        selectedShift = null;
    }*/

    /*private void setSelectedShift(ShiftViewModel selectedShift) {
        if (selectedShift != null && selectedShift.equals(this.selectedShift))
            return;

        this.selectedShift = selectedShift;
        loadData();
    }*/

    /*private void clearSelectedRegister() {
        if (selectedRegister == null)
            return;

        selectedRegister = null;
    }*/

    /*private void setSelectedRegister(RegisterModel selectedRegister) {
        if (selectedRegister != null && selectedRegister.equals(this.selectedRegister))
            return;

        this.selectedRegister = selectedRegister;
        loadData();
    }
*/
    @Click
    protected void topItemsContainerClicked() {
        if (reportsFragmentListener != null)
            reportsFragmentListener.onShowTopItems();
    }

    @Click
    protected void switchValueClicked(){
        if (valueUnit == ValueUnit.AMOUNT)
            return;

        setValueUnit(ValueUnit.AMOUNT);
    }

    @Click
    protected void switchCountClicked(){
        if (valueUnit == ValueUnit.COUNT)
            return;

        setValueUnit(ValueUnit.COUNT);
    }

    @Override
    protected void loadData() {
        getLoaderManager().restartLoader(LOADER_PAYMENT_TRANSACTIONS_ID, null, paymentTransactionsLoader);
        getLoaderManager().restartLoader(LOADER_TOP_ITEMS_ID, null, topItemsLoader);
    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    private void stopLoadData() {
        getLoaderManager().destroyLoader(LOADER_PAYMENT_TRANSACTIONS_ID);
        getLoaderManager().destroyLoader(LOADER_TOP_ITEMS_ID);
    }

    /*private void loadShifts() {
        getLoaderManager().restartLoader(LOADER_SHIFTS_ID, null, shiftsLoader);
    }

    private void stopLoadShifts() {
        getLoaderManager().destroyLoader(LOADER_SHIFTS_ID);
    }*/

    private void loadRegisters() {
        getLoaderManager().restartLoader(LOADER_REGISTERS_ID, null, registersLoader);
    }

    private void stopLoadRegisters() {
        getLoaderManager().destroyLoader(LOADER_REGISTERS_ID);
    }

    private void updateUI(StatisticsWithChartDataModel statisticsWithChartDataModel) {
        BarChart.PeriodBarChartData chartData = statisticsWithChartDataModel == null ? null : statisticsWithChartDataModel.chartData;
        StatisticsModel statisicsModel = statisticsWithChartDataModel == null ? null : statisticsWithChartDataModel.statisticsModel;
        final boolean hasData = statisicsModel != null;

        barChart.setData(chartData);

        UiHelper.showPrice(totalValueLabel, hasData ? statisicsModel.total : BigDecimal.ZERO);
        UiHelper.showPrice(averageValueLabel, hasData ? statisicsModel.average : BigDecimal.ZERO);
        transactionsValueLabel.setText(String.valueOf(hasData ? statisicsModel.transactions : 0));
    }


    private void updateTopItems(List<TopItemModel> topItemModels) {
        if (topItemModels == null) {
            topItemsValueLabel.setText(R.string.reports_top_items_empty);
            topItemsContainer.setEnabled(false);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (TopItemModel topItemModel : topItemModels) {
            builder.append(topItemModel.description).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        topItemsValueLabel.setText(builder);
        topItemsContainer.setEnabled(true);
    }

    @Override
    protected boolean setPeriodDate(Date date, Date newDate) {
        return super.setPeriodDate(date, newDate);
    }

    private class ShiftsAdapter extends ObjectsCursorAdapter<ShiftViewModel> {

        public ShiftsAdapter(Context context) {
            super(context);
        }

        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
            return view;
        }

        @Override
        protected View bindView(View view, int position, ShiftViewModel item) {
            StringBuilder stringBuilder = new StringBuilder(item.openEmployeeFullName);
            if(!TextUtils.isEmpty(item.closeEmployeeFullName) && !item.openManagerId.equals(item.closeManagerId)){
                stringBuilder.append(" - ").append(item.closeEmployeeFullName);
            }
            stringBuilder.append(" (").append(periodDateFormat.format(item.startTime));
            if (item.endTime != null)
                stringBuilder.append(" - ").append(periodDateFormat.format(item.endTime));
            stringBuilder.append(")");
            ((TextView) view).setText(stringBuilder);
            return view;
        }

    }

    private LoaderManager.LoaderCallbacks<Optional<StatisticsWithChartDataModel>> paymentTransactionsLoader = new LoaderManager.LoaderCallbacks<Optional<StatisticsWithChartDataModel>>() {

        @Override
        public Loader<Optional<StatisticsWithChartDataModel>> onCreateLoader(int id, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder
                    .forUri(PAYMENT_TRANSACTIONS_URI)
                    .projection(PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME, PaymentTransactionView2.PaymentTransactionTable.AMOUNT, PaymentTransactionView2.PaymentTransactionTable.ORDER_GUID, EmployeeTipsTable.AMOUNT)
                    .where(PaymentTransactionView2.PaymentTransactionTable.STATUS + " != ?", PaymentTransactionModel.PaymentStatus.FAILED.ordinal())
                    .where(EmployeeTipsTable.PARENT_GUID + " IS NULL")
                    .where(PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME + " >= ?", fromDate.getTime())
                    .where(PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME + " <= ?", toDate.getTime())
                    .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                    .orderBy(PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME);
            /*if (Mode.PER_SHIFT == mode && selectedShift != null)
                builder.where(PaymentTransactionView2.PaymentTransactionTable.SHIFT_GUID + " = ?", selectedShift.guid);*/
            //if (Mode.PER_REGISTER == mode && selectedRegister != null)
            long selectedRegisterId = getSelectedRegisterId();
            if(selectedRegisterId > 0){
                builder.where(PaymentTransactionView2.SaleOrderTable.REGISTER_ID + " = ?", selectedRegisterId);
            }

            return builder
                    .wrap(new PaymentTransactionsFunction(fromDate, toDate, inDays))
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Optional<StatisticsWithChartDataModel>> loader, Optional<StatisticsWithChartDataModel> result) {
            updateUI(result.orNull());
        }

        @Override
        public void onLoaderReset(Loader<Optional<StatisticsWithChartDataModel>> loader) {
            updateUI(null);
        }
    };

    private LoaderManager.LoaderCallbacks<Optional<List<TopItemModel>>> topItemsLoader = new LoaderManager.LoaderCallbacks<Optional<List<TopItemModel>>>() {

        @Override
        public Loader<Optional<List<TopItemModel>>> onCreateLoader(int id, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder
                    .forUri(TOP_ITEMS_URI)
                    .where(ReportsTopItemsView2.SaleOrderTable.CREATE_TIME + " >= ?", fromDate.getTime())
                    .where(ReportsTopItemsView2.SaleOrderTable.CREATE_TIME + " <= ?", toDate.getTime());
            /*if (Mode.PER_SHIFT == mode && selectedShift != null)
                builder.where(ReportsTopItemsView2.SaleOrderTable.SHIFT_GUID + " = ?", selectedShift.guid);*/
            //if (Mode.PER_REGISTER == mode && selectedRegister != null)
            long selectedRegisterId = getSelectedRegisterId();
            if(selectedRegisterId > 0){
                builder.where(ReportsTopItemsView2.SaleOrderTable.REGISTER_ID + " = ?", selectedRegisterId);
            }
            return builder
                    .wrap(new TopItemsFunction())
                    .build(getActivity());

        }

        @Override
        public void onLoadFinished(Loader<Optional<List<TopItemModel>>> loader, Optional<List<TopItemModel>> result) {
            updateTopItems(result.orNull());
        }

        @Override
        public void onLoaderReset(Loader<Optional<List<TopItemModel>>> loader) {
            updateTopItems(null);
        }
    };

   /* private LoaderManager.LoaderCallbacks<List<ShiftViewModel>> shiftsLoader = new LoaderManager.LoaderCallbacks<List<ShiftViewModel>>() {

        @Override
        public Loader<List<ShiftViewModel>> onCreateLoader(int id, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(SHIFTS_URI)
                    .orderBy(ShiftView2.ShiftTable.START_TIME + " DESC")
                    .transform(new ShiftsFunction())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ShiftViewModel>> loader, List<ShiftViewModel> result) {
            ((ShiftsAdapter) modeEntitiesSpinner.getAdapter()).changeCursor(result);
            if (modeEntitiesSpinner.getCount() == 0)
                setSelectedShift(null);
        }

        @Override
        public void onLoaderReset(Loader<List<ShiftViewModel>> loader) {
            getShiftsAdapter().changeCursor(null);
        }
    };*/

    private LoaderManager.LoaderCallbacks<List<RegisterModel>> registersLoader = new RegistersLoader(){

        @Override
        public void onLoadFinished(Loader<List<RegisterModel>> loader, List<RegisterModel> result) {
            ArrayList<RegisterModel> arrayList = new ArrayList<RegisterModel>(result.size() + 1);
            arrayList.add(new RegisterModel(0, null, getString(R.string.register_label_all), null, 0, 0));
            arrayList.addAll(result);
            ((RegistersAdapter) modeEntitiesSpinner.getAdapter()).changeCursor(arrayList);
            /*if (modeEntitiesSpinner.getCount() == 0)
                setSelectedShift(null);*/
        }

        @Override
        public void onLoaderReset(Loader<List<RegisterModel>> loader) {
            getRegistersAdapter().changeCursor(null);
        }

        @Override
        protected Context getLoaderContext() {
            return getActivity();
        }
    };

    private static class ShiftsFunction extends ListConverterFunction<ShiftViewModel> {

        @Override
        public ShiftViewModel apply(Cursor cursor) {
            super.apply(cursor);
            ShiftViewModel shiftViewModel = new ShiftViewModel(cursor.getString(indexHolder.get(ShiftView2.ShiftTable.GUID)),
                    _nullableDate(cursor, indexHolder.get(ShiftView2.ShiftTable.START_TIME)),
                    _nullableDate(cursor, indexHolder.get(ShiftView2.ShiftTable.END_TIME)),
                    cursor.getString(indexHolder.get(ShiftTable.OPEN_MANAGER_ID)),
                    cursor.getString(indexHolder.get(ShiftTable.CLOSE_MANAGER_ID)),
                    cursor.getLong(indexHolder.get(ShiftView2.ShiftTable.REGISTER_ID)),
                    _decimal(cursor, indexHolder.get(ShiftView2.ShiftTable.OPEN_AMOUNT), BigDecimal.ZERO),
                    _decimal(cursor, indexHolder.get(ShiftView2.ShiftTable.CLOSE_AMOUNT), BigDecimal.ZERO),
                    cursor.getString(indexHolder.get(OpenManagerTable.FIRST_NAME)) + " " + cursor.getString(indexHolder.get(OpenManagerTable.LAST_NAME)),
                    cursor.getString(indexHolder.get(CloseManagerTable.FIRST_NAME)) + " " + cursor.getString(indexHolder.get(CloseManagerTable.LAST_NAME)),
                    cursor.getString(indexHolder.get(RegisterTable.TITLE)));
            return shiftViewModel;
        }
    }

    private static class PaymentTransactionsFunction extends ListConverterFunction<Optional<StatisticsWithChartDataModel>> {

        private final boolean inDays;
        private final Date fromDate;
        private final Date toDate;
        private final Calendar calendar = Calendar.getInstance();

        public PaymentTransactionsFunction(Date fromDate, Date toDate, boolean inDays) {
            this.fromDate = new Date(fromDate.getTime());
            this.toDate = new Date(toDate.getTime());
            this.inDays = inDays;
        }

        @Override
        public Optional<StatisticsWithChartDataModel> apply(Cursor cursor) {
            super.apply(cursor);

            LinkedHashMap<Long, PeriodsTotalMapValue> periodsTotalMap = new LinkedHashMap<Long, PeriodsTotalMapValue>();
            initPeriodsTotalMap(periodsTotalMap);

            if (!cursor.moveToFirst()) {
                StatisticsWithChartDataModel result = new StatisticsWithChartDataModel(getChartData(periodsTotalMap));
                return Optional.fromNullable(result);
            }

            HashSet<String> ordersSet = new HashSet<String>();
            BigDecimal total = BigDecimal.ZERO;
            do {

                final Date paymentTransactionCreateTime = new Date(cursor.getLong(indexHolder.get(PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME)));
                BigDecimal paymentTransactionAmount = _decimal(cursor, indexHolder.get(PaymentTransactionView2.PaymentTransactionTable.AMOUNT), BigDecimal.ZERO);
                final String paymentTransactionOrderGuid = cursor.getString(indexHolder.get(PaymentTransactionView2.PaymentTransactionTable.ORDER_GUID));

                if (!cursor.isNull(indexHolder.get(EmployeeTipsTable.AMOUNT))) {
                    paymentTransactionAmount = paymentTransactionAmount.subtract(_decimal(cursor, indexHolder.get(EmployeeTipsTable.AMOUNT), BigDecimal.ZERO));
                }

                calendar.setTimeInMillis(paymentTransactionCreateTime.getTime());
                if (inDays)
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                final long periodTimestamp = calendar.getTimeInMillis();

                PeriodsTotalMapValue periodsTotalMapValue = periodsTotalMap.get(periodTimestamp);

                BigDecimal periodTotal = periodsTotalMapValue.amount;
                periodTotal = periodTotal.add(paymentTransactionAmount);
                periodsTotalMapValue.amount = periodTotal;

                periodsTotalMapValue.ordersSet.add(paymentTransactionOrderGuid);

                ordersSet.add(paymentTransactionOrderGuid);

                total = total.add(paymentTransactionAmount);
            } while (cursor.moveToNext());

            final int transactions = ordersSet.size();
            BigDecimal average = CalculationUtil.divide(total, new BigDecimal(transactions));

            StatisticsWithChartDataModel result = new StatisticsWithChartDataModel(getChartData(periodsTotalMap), new StatisticsModel(total, average, transactions));
            return Optional.fromNullable(result);
        }

        private void initPeriodsTotalMap(LinkedHashMap<Long, PeriodsTotalMapValue> periodsTotalMap) {
            Calendar fromCalendar = Calendar.getInstance();
            Calendar toCalendar = Calendar.getInstance();
            fromCalendar.setTimeInMillis(fromDate.getTime());
            toCalendar.setTimeInMillis(toDate.getTime());
            if (inDays)
                fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            fromCalendar.set(Calendar.MILLISECOND, 0);
            if (inDays)
                toCalendar.set(Calendar.HOUR_OF_DAY, 0);
            toCalendar.set(Calendar.MINUTE, 0);
            toCalendar.set(Calendar.SECOND, 0);
            toCalendar.set(Calendar.MILLISECOND, 0);
            do {
                periodsTotalMap.put(fromCalendar.getTimeInMillis(), new PeriodsTotalMapValue(BigDecimal.ZERO, new HashSet<String>()));
                fromCalendar.add(inDays ? Calendar.DAY_OF_YEAR : Calendar.HOUR, 1);
            } while (fromCalendar.compareTo(toCalendar) <= 0);
        }

        private PeriodBarChartData getChartData(LinkedHashMap<Long, PeriodsTotalMapValue> periodsTotalMap) {
            PeriodBarData[] barData = new PeriodBarData[periodsTotalMap.size()];
            int i = 0;
            for (Entry<Long, PeriodsTotalMapValue> entry : periodsTotalMap.entrySet()) {
                barData[i++] = new PeriodBarData(entry.getKey(), entry.getValue().amount, entry.getValue().ordersSet.size());
            }
            PeriodUnit periodUnit = inDays ? PeriodUnit.DAY : PeriodUnit.HOUR;
            return new PeriodBarChartData(barData, periodUnit);
        }

        private class PeriodsTotalMapValue {
            BigDecimal amount;
            HashSet<String> ordersSet;

            private PeriodsTotalMapValue(BigDecimal amount, HashSet<String> ordersSet) {
                this.amount = amount;
                this.ordersSet = ordersSet;
            }
        }

    }

    private static class StatisticsWithChartDataModel {

        BarChart.PeriodBarChartData chartData;
        StatisticsModel statisticsModel;

        private StatisticsWithChartDataModel(BarChart.PeriodBarChartData chartData) {
            this.chartData = chartData;
        }

        private StatisticsWithChartDataModel(BarChart.PeriodBarChartData chartData, StatisticsModel statisticsModel) {
            this.chartData = chartData;
            this.statisticsModel = statisticsModel;
        }
    }

    private static class StatisticsModel {
        final BigDecimal total;
        final BigDecimal average;
        final int transactions;

        private StatisticsModel(BigDecimal total, BigDecimal average, int transactions) {
            this.total = total;
            this.average = average;
            this.transactions = transactions;
        }
    }

    public static interface ReportsFragmentListener {
        public void onShowTopItems();
    }
}
