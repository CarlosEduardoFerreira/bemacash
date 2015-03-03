package com.kaching123.tcr.fragment.creditreceipt;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.CustomEditBox.IKeyboardSupport;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.OrderNumberFormatInputFilter;
import com.kaching123.tcr.fragment.filter.CashierFilterSpinnerAdapter;
import com.kaching123.tcr.fragment.reports.DateRangeFragment;

import java.util.Calendar;
import java.util.Date;

import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by gdubina on 27/02/14.
 */
@EFragment(R.layout.creditreceipt_filter_fragment)
public class CreditReceiptFilterFragment extends DateRangeFragment implements IKeyboardSupport {

    private static final int MAX_PERIODS_COUNT = 31;
    private static final int CASHIER_LOADER_ID = 0;

    @ViewById
    protected KeyboardView keyboard;

    @ViewById
    protected Spinner cashier;

    @ViewById
    protected CustomEditBox creditReceiptNum;

    private CashierFilterSpinnerAdapter cashierAdapter;

    private CreditReceiptFilterListener listener;

    public void setCreditReceiptNum(String creditReceiptNum) {
		if (this.creditReceiptNum != null)
        	this.creditReceiptNum.setText(creditReceiptNum);
    }

    @AfterViews
    public void onCreate(){
        creditReceiptNum.setFilters(new InputFilter[]{new OrderNumberFormatInputFilter()});
        creditReceiptNum.setKeyboardSupportConteiner(this);

        creditReceiptNum.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                filterClicked();
                return false;
            }
        });

        cashierAdapter = new CashierFilterSpinnerAdapter(getActivity());
        cashier.setAdapter(cashierAdapter);

        keyboard.setDotEnabled(false);
        keyboard.setMinusVisible(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(CASHIER_LOADER_ID, null, cashierAdapter);
    }

    protected Date initFromDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 00);
        dateTimePickerCalendar.set(Calendar.MINUTE, 00);
        dateTimePickerCalendar.set(Calendar.SECOND, 00);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        dateTimePickerCalendar.add(Calendar.DATE, -MAX_PERIODS_COUNT);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    @Click
    protected void filterClicked() {
        String cashierGuid = cashierAdapter.getGuid(cashier.getSelectedItemPosition());
        String creditReceipt = creditReceiptNum.getText().toString();
        String register = null;
        String number = null;
        if (!TextUtils.isEmpty(creditReceipt)) {
            String[] ar = creditReceipt.split("-");
            if (ar.length > 0) {
                register = ar[0];
            }
            if (ar.length > 1) {
                number = ar[1];
            }
        }
        if (listener != null) {
            listener.onFilter(fromDate, toDate, register, toInt(number, 0), cashierGuid);
        }
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    public void setListener(CreditReceiptFilterListener listener) {
        this.listener = listener;
        filterClicked();
    }

    public static interface CreditReceiptFilterListener {
        void onFilter(Date from, Date to, String registerTitle, int printNumber, String cashierGuid);
    }
}
