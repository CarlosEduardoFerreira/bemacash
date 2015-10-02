package com.kaching123.tcr.fragment.tendering.history;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BaseCustomersAutocompleteAdapter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.CustomEditBox.IKeyboardSupport;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.filter.CashierFilterSpinnerAdapter;
import com.kaching123.tcr.fragment.reports.DateRangeFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryOrderListFragment.ILoader;
import com.kaching123.tcr.fragment.wireless.UnitsSearchFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment(R.layout.tendering_history_orders_filter_fragment)
@OptionsMenu(R.menu.tendering_history_orders_filter_fragment)
public class HistoryOrderFragment extends DateRangeFragment implements IKeyboardSupport, ILoader, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MAX_PERIODS_COUNT = 31;

    private static final BigDecimal MAX_VALUE = new BigDecimal("99999.99");
    private static final Uri CASHIER_URI = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);
    private static final Uri CUSTOMER_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    private static final int CASHIER_LOADER_ID = 0;

    @ViewById
    protected KeyboardView keyboard;

    @ViewById
    protected CheckBox serverSearchCheckbox;


    @ViewById
    protected Button filter;

    @ViewById
    protected Spinner merchant;

    @ViewById
    protected AutoCompleteTextView customer;

    @ViewById
    protected View transactionsStatusContainer;

    @ViewById
    protected Spinner transactionsStatus;

    @ViewById
    protected CustomEditBox orderNumber;

    private MenuItem unitAction;

    private CashierFilterSpinnerAdapter cashierAdapter;
    private CustomersAdapter customerAdapter;
    private IFilterRequestListener callback;
    private ISettlementListener settlementListener;

    private String customerGuid;

    private ArrayList<String> sequences = new ArrayList<String>();

    private boolean isShown = true;

    private String unitSerial;

    public void setOrderNumber(String orderNumber) {
        String orderNumberFilted = orderNumber.replace("\n", "").replace("\r", "");
        if (this.orderNumber != null && orderNumber != null && this.orderNumber.isEnabled())
            this.orderNumber.setText(orderNumberFilted);

        Logger.d("HistoryOrderFragment setOrderNumber: " + ",Thread, " + Thread.currentThread().getId());
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        MenuItem actionSettlement = menu.findItem(R.id.action_settlement);
        unitAction = menu.findItem(R.id.action_unit);

//        actionSettlement.setVisible(getApp().isTipsEnabled());
        unitAction.setVisible(isShown && getApp().getShopPref().acceptSerializableItems().get());
    }

//    @OptionsItem
//    protected void actionSettlementSelected() {
//        if (settlementListener != null)
//            settlementListener.onSettlementRequested();
//    }

    @OptionsItem
    protected void actionUnitSelected() {
        onBarcodeReceived();
    }

    public void onHide() {
        isShown = false;
        unitAction.setVisible(false);
    }

    public void onShow() {
        isShown = true;
        unitAction.setVisible(getApp().getShopPref().acceptSerializableItems().get());
    }

    public void onBarcodeReceived() {
        boolean forceServerSearch = serverSearchCheckbox.isChecked();
        UnitsSearchFragment.show(getActivity(), unitSerial, forceServerSearch, new UnitsSearchFragment.UnitCallback() {


            @Override
            public void handleSuccess(String serialCode, ArrayList<Unit> unit, ArrayList<SaleOrderViewModel> order) {
                if (getActivity() == null)
                    return;

                hide();

                ArrayList<String> sequences = new ArrayList<String>(order.size());
                for (int i = 0; i < order.size(); i++) {
                    sequences.add(order.get(i).registerTitle + "-" + order.get(i).printSeqNum);
                }

                if (unitSerial != null && unitSerial.equals(serialCode)
                        && HistoryOrderFragment.this.sequences.equals(sequences)) {
                    return;
                }

                HistoryOrderFragment.this.sequences.clear();
                HistoryOrderFragment.this.sequences.addAll(sequences);
                unitSerial = serialCode;

                orderNumber.setText(null);

                setFilterFieldsEnabled(false);
                setServerSearchCheckboxEnabled(true);

                requestFilter(true);
            }

            @Override
            public void handleError(String serialCode, String message) {
                if (getActivity() == null)
                    return;

                Toast.makeText(getActivity(), R.string.unit_history_serial_not_found, Toast.LENGTH_LONG).show();
                hide();

                callback.onSearchOrderByUnitFailed(serialCode);
            }

            @Override
            public void handleCancel() {
            }

            @Override
            public void handleClear() {
                if (getActivity() == null)
                    return;

                hide();

                if (unitSerial == null) {
                    return;
                }

                sequences.clear();
                unitSerial = null;

                setFilterFieldsEnabled(true);
                setServerSearchCheckboxEnabled(false);

                requestFilter(true);
            }

            @Override
            public void handleServerSearch(String serialCode) {
                if (getActivity() == null)
                    return;

                hide();

                callback.onSearchOrderByUnitOnServer(serialCode);
            }

            private void hide() {
                UnitsSearchFragment.hide(getActivity());
            }
        });
    }

    private void setFilterFieldsEnabled(boolean isEnabled) {
        setFilterFieldsEnabled(isEnabled, false);
    }

    private void setFilterFieldsEnabled(boolean isEnabled, boolean excludeOrderNumber) {
        merchant.setEnabled(isEnabled);
        customer.setEnabled(isEnabled);
        transactionsStatus.setEnabled(isEnabled);
        if (!excludeOrderNumber) {
            orderNumber.setEnabled(isEnabled);
            keyboard.setEnabled(isEnabled);
        }
        fromEdit.setEnabled(isEnabled);
        toEdit.setEnabled(isEnabled);
        if (!excludeOrderNumber && isEnabled) {
            keyboard.setDotEnabled(false);
            keyboard.setMinusVisible(true);
            orderNumber.requestFocus();
        }
    }

    private void setServerSearchCheckboxEnabled(boolean isEnabled) {
        serverSearchCheckbox.setEnabled(isEnabled);
        if (!isEnabled)
            serverSearchCheckbox.setChecked(false);
    }

    @Click
    public void filterClicked() {
        requestFilter(true);
    }

    private void requestFilter() {
        requestFilter(false);
    }

    private void requestFilter(boolean isManual) {
        // http://194.79.22.58:8080/browse/ACR-211
//        if (!toValue.hasFocus())
//            onToTextChanges(toValue.getText());
//        if (!fromValue.hasFocus())
//            onFromTextChanges(fromValue.getText());
        String cashierGuid = cashierAdapter.getGuid(merchant.getSelectedItemPosition());
        String customerGuid = this.customerGuid == null ? "-1" : this.customerGuid;
        TransactionsState transactionsState = (TransactionsState) transactionsStatus.getSelectedItem();
        /*BigDecimal min = TextUtils.isEmpty(fromValue.getText()) ? null : new BigDecimal(String.valueOf(fromValue.getText()));
        BigDecimal max = TextUtils.isEmpty(toValue.getText()) ? null : new BigDecimal(String.valueOf(toValue.getText()));*/
        boolean forceServerSearch = serverSearchCheckbox.isChecked();

        String creditReceipt = orderNumber.getText().toString();
        ArrayList<String> regs;
        ArrayList<String> seqs;
        if (!TextUtils.isEmpty(creditReceipt)) {
            Pair<String, String> seq = getSeq(creditReceipt);
            regs = new ArrayList<String>(Arrays.asList(seq.first));
            seqs = new ArrayList<String>(Arrays.asList(seq.second));
        } else {
            regs = new ArrayList<String>();
            seqs = new ArrayList<String>();

            for (String s : sequences) {
                Pair<String, String> seq = getSeq(s);
                regs.add(seq.first);
                seqs.add(seq.second);
            }
        }

        if (isManual && !forceServerSearch
                && regs.isEmpty() && seqs.isEmpty() && !TextUtils.isEmpty(unitSerial)) {
            onBarcodeReceived();
            return;
        }

        callback.onFilterRequested(fromDate,
                toDate,
                cashierGuid,
                customerGuid,
                transactionsState,
                regs,
                seqs,
                unitSerial,
                isManual,
                forceServerSearch);

    }

    private Pair<String, String> getSeq(String s) {
        String reg = null;
        String seq = null;
        String[] ar = s.split("-");
        if (ar.length > 0) {
            reg = ar[0];
        }
        if (ar.length > 1) {
            seq = ar[1];
        }
        return new Pair<String, String>(reg, seq);
    }

    @AfterViews
    public void onCreate() {

//        orderNumber.setFilters(new InputFilter[]{new OrderNumberFormatInputFilter()});
        orderNumber.setKeyboardSupportConteiner(this);
        orderNumber.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                requestFilter(true);
                return false;
            }
        });

        orderNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Toast.makeText(getActivity(), orderNumber.getText(), Toast.LENGTH_SHORT).show();
                    filterClicked();
                    return false;
                }
                return false;
            }
        });
        orderNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Boolean ignore = (Boolean) orderNumber.getTag();
                if (ignore != null && ignore) {
                    orderNumber.setTag(null);
                    return;
                }
                Pair<String, String> seq = TextUtils.isEmpty(s) ? null : getSeq(s.toString());
                if (seq != null && !TextUtils.isEmpty(seq.first) && !TextUtils.isEmpty(seq.second)) {
                    setFilterFieldsEnabled(false, true);
                    setServerSearchCheckboxEnabled(true);
                } else {
                    setFilterFieldsEnabled(true, true);
                    setServerSearchCheckboxEnabled(false);
                }
            }
        });
        cashierAdapter = new CashierFilterSpinnerAdapter(getActivity());
        merchant.setAdapter(cashierAdapter);

        serverSearchCheckbox.setVisibility(getApp().isTrainingMode() ? View.GONE : View.VISIBLE);
        setServerSearchCheckboxEnabled(false);

        boolean isTipsEnabled = getApp().isTipsEnabled();
        transactionsStatusContainer.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        transactionsStatus.setAdapter(new TransactionsStateAdapter(getActivity()));

        customerAdapter = new CustomersAdapter(getActivity());
        customer.setAdapter(customerAdapter);
        customer.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCustomer(customerAdapter.getItem(position).guid);
            }
        });


        getLoaderManager().initLoader(CASHIER_LOADER_ID, null, this);
    }

    @Override
    protected Date initFromDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 00);
        dateTimePickerCalendar.set(Calendar.MINUTE, 00);
        dateTimePickerCalendar.set(Calendar.SECOND, 00);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        dateTimePickerCalendar.add(Calendar.DATE, -MAX_PERIODS_COUNT);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    private void setCustomer(String customerGuid) {
        this.customerGuid = customerGuid;
        final boolean customerSet = customerGuid != null;
        customer.setCompoundDrawablesWithIntrinsicBounds(0, 0, !customerSet ? 0 : R.drawable.check_gray, 0);
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        if (v == orderNumber) {
            if (keyboard.isNumPadEnabled()) {
                keyboard.setDotEnabled(false);
                keyboard.setMinusVisible(true);
            }
        } else {
            if (keyboard.isNumPadEnabled()) {
                keyboard.setDotEnabled(true);
                keyboard.setMinusVisible(false);
            }
        }
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    public void onLoad(int count, BigDecimal min, BigDecimal max) {
        /*minValue = min;
        maxValue = max;*/
        //totalValue.setText(String.valueOf(count));
    }

    @Override
    public void onItemClicked(String ignore0, BigDecimal ignore1, Date ignore2, String ignore3, String numText, OrderType type, boolean isOrderTipped) {

    }

    @Override
    public boolean onLoadedFromServer(String unitSerial) {
        boolean isSearchByUnitSerial = !TextUtils.isEmpty(unitSerial);
        if (!isSearchByUnitSerial)
            return false;

        sequences.clear();
        this.unitSerial = unitSerial;

        orderNumber.setTag(true);
        orderNumber.setText(null);

        setFilterFieldsEnabled(false);
        setServerSearchCheckboxEnabled(true);

        requestFilter();

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(CASHIER_URI)
                .projection(new String[]{EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME, EmployeeTable.GUID})
                .where(EmployeeTable.IS_MERCHANT + " = ?", 0)
                .build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cashierAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cashierAdapter.changeCursor(null);
    }

    public void setTransactionsState(TransactionsState transactionState) {
        if (transactionsStatus != null && transactionState != null)
            transactionsStatus.setSelection(transactionState.ordinal());
    }

    public HistoryOrderFragment setCallback(IFilterRequestListener callback) {
        this.callback = callback;
        requestFilter();
        return this;
    }

    public void setSettlementListener(ISettlementListener listener) {
        this.settlementListener = listener;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    private class CustomersAdapter extends BaseCustomersAutocompleteAdapter {

        public CustomersAdapter(Context context) {
            super(context);
        }

        @Override
        protected void publishResults(FluentIterable<CustomerModel> cursor) {
            setCustomer(null);
        }

    }

    public interface ISettlementListener {

        void onSettlementRequested();

    }

    private static class TransactionsStateAdapter extends BaseAdapter {

        private Context context;

        public TransactionsStateAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return TransactionsState.values().length;
        }

        @Override
        public Object getItem(int position) {
            return TransactionsState.values()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = newView(parent, position);
            bindView(parent, position, convertView);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = newDropDownView(parent, position);
            bindView(parent, position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent, int position) {
            return LayoutInflater.from(context).inflate(R.layout.spinner_item_filter, parent, false);
        }

        private View newDropDownView(ViewGroup parent, int position) {
            return LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        private void bindView(ViewGroup parent, int position, View view) {
            TransactionsState transactionsState = (TransactionsState) getItem(position);
            ((TextView) view).setText(getTransactionStateLabel(transactionsState));
        }

        private int getTransactionStateLabel(TransactionsState transactionsState) {
            switch (transactionsState) {
                case OPEN:
                    return R.string.order_transactions_status_filter_open;
                case CLOSED:
                    return R.string.order_transactions_status_filter_closed;
                default:
                    return R.string.order_transactions_status_filter_all;
            }
        }

    }
}
