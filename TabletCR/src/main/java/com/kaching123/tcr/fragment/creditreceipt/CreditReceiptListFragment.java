package com.kaching123.tcr.fragment.creditreceipt;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.creditreceipt.CreditReceiptFilterFragment.CreditReceiptFilterListener;
import com.kaching123.tcr.fragment.filter.CashierFilterSpinnerAdapter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptExView2.CashierTable;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptExView2.CreditReceiptTable;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptExView2.PaymentTable;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptExView2.RegisterTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptExView;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 27/02/14.
 */
@EFragment(R.layout.creditreceipt_list_fragment)
public class CreditReceiptListFragment extends SuperBaseFragment implements CreditReceiptFilterListener {

    private final static Uri URI_CREDIT_RECEIPT = ShopProvider.getContentUri(CreditReceiptExView.URI_CONTENT);

    private final static int CREDIT_RECEIPTS_LOADER_ID = 0;
    private final static int CREDIT_RECEIPT_INFO_LOADER_ID = 1;

    @ViewById(android.R.id.list)
    protected ListView listView;

    @ViewById
    protected TextView totalAmount;
    @ViewById
    protected TextView totalUsedAmount;

    private String registerTitle;
    private int printNumber;
    private String cashierGuid;
    private Date from;
    private Date to;

    private ReceiptsAdapter adapter;
    Calendar calendar = Calendar.getInstance();
    //private IListRefreshedListener listener;

    private CreditReceiptsLoader creditReceiptsLoader = new CreditReceiptsLoader();
    private CreditReceiptInfoLoader creditReceiptInfoLoader = new CreditReceiptInfoLoader();

    @AfterViews
    public void onCreate(){
        adapter = new ReceiptsAdapter(getActivity());
        listView.setAdapter(adapter);
    }

    @ItemClick(android.R.id.list)
    protected void listViewItemClicked(int pos){
        String receiptGuid = adapter.getGuid(pos);
        PrintCreditReceiptFragmentDialog.show(getActivity(), receiptGuid);
    }

    private Loader getLoader(int loaderId) {
        CursorLoaderBuilder builder = CursorLoaderBuilder
                .forUri(URI_CREDIT_RECEIPT);

        if (loaderId == CREDIT_RECEIPTS_LOADER_ID) {
            builder.projection("0 as _id",
                    CreditReceiptTable.CREATE_TIME,
                    RegisterTable.TITLE,
                    CreditReceiptTable.PRINT_NUMBER,
                    CashierTable.FIRST_NAME,
                    CashierTable.LAST_NAME,
                    CreditReceiptTable.AMOUNT,
                    PaymentTable.CREATE_TIME,
                    PaymentTable.AMOUNT,
                    CreditReceiptTable.EXPIRE_TIME,
                    CreditReceiptTable.GUID);
        } else {
            builder.projection(CreditReceiptTable.AMOUNT,
                    PaymentTable.CREATE_TIME,
                    PaymentTable.AMOUNT);
        }

        if (from != null && to != null) {
            builder.where(CreditReceiptTable.CREATE_TIME + " >= ? and " + CreditReceiptTable.CREATE_TIME + " <= ?", from.getTime(), to.getTime());
        }
        if (!TextUtils.isEmpty(registerTitle)) {
            builder.where(RegisterTable.TITLE + " = ?", registerTitle);
        }
        if (printNumber > 0) {
            builder.where(CreditReceiptTable.PRINT_NUMBER + " = ?", printNumber);
        }
        if (!TextUtils.isEmpty(cashierGuid) && !CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(cashierGuid)) {
            builder.where(CreditReceiptTable.CASHIER_GUID + " = ?", cashierGuid);
        }
        builder.orderBy(CreditReceiptTable.CREATE_TIME + " DESC");

        if (loaderId == CREDIT_RECEIPT_INFO_LOADER_ID) {
            return builder.wrap(new CreditReceiptInfoFunction()).build(getActivity());
        }

        return builder.build(getActivity());
    }

    private void setTotal(CreditReceiptsInfo creditReceiptsInfo) {
        showPrice(totalAmount, creditReceiptsInfo.total);
        showPrice(totalUsedAmount, creditReceiptsInfo.usedTotal);
    }

    @Override
    public void onFilter(Date fromDate, Date toDate, String registerTitle, int printNumber, String cashierGuid) {
        this.from = fromDate;
        this.to = toDate;
        this.registerTitle = registerTitle;
        this.printNumber = printNumber;
        this.cashierGuid = cashierGuid;
        getLoaderManager().restartLoader(CREDIT_RECEIPTS_LOADER_ID, null, creditReceiptsLoader);
        getLoaderManager().restartLoader(CREDIT_RECEIPT_INFO_LOADER_ID, null, creditReceiptInfoLoader);
    }

    private class CreditReceiptsLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return getLoader(CREDIT_RECEIPTS_LOADER_ID);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            adapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            adapter.changeCursor(null);
        }
    }

    private class CreditReceiptInfoLoader implements LoaderCallbacks<CreditReceiptsInfo> {

        @Override
        public Loader<CreditReceiptsInfo> onCreateLoader(int i, Bundle bundle) {
            return getLoader(CREDIT_RECEIPT_INFO_LOADER_ID);
        }

        @Override
        public void onLoadFinished(Loader<CreditReceiptsInfo> creditReceiptsInfoLoader, CreditReceiptsInfo creditReceiptsInfo) {
            setTotal(creditReceiptsInfo);
        }

        @Override
        public void onLoaderReset(Loader<CreditReceiptsInfo> creditReceiptsInfoLoader) {

        }
    }

    private static class CreditReceiptInfoFunction implements Function<Cursor, CreditReceiptsInfo> {

        @Override
        public CreditReceiptsInfo apply(Cursor cursor) {
            CreditReceiptsInfo creditReceiptsInfo = new CreditReceiptsInfo();
            if (!cursor.moveToFirst()) {
                return creditReceiptsInfo;
            }

            do {
                BigDecimal amount = _decimal(cursor, cursor.getColumnIndex(CreditReceiptTable.AMOUNT));
                long usedTime = cursor.getLong(cursor.getColumnIndex(PaymentTable.CREATE_TIME));
                BigDecimal usedAmount = _decimal(cursor, cursor.getColumnIndex(PaymentTable.AMOUNT));

                if (usedTime <= 0)
                    creditReceiptsInfo.total = creditReceiptsInfo.total.add(amount);
                else
                    creditReceiptsInfo.usedTotal = creditReceiptsInfo.usedTotal.add(usedAmount);
            } while (cursor.moveToNext());

            return creditReceiptsInfo;
        }

    }

    private static class CreditReceiptsInfo {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal usedTotal = BigDecimal.ZERO;
    }

    private class ReceiptsAdapter extends ResourceCursorAdapter {

        public ReceiptsAdapter(Context context) {
            super(context, R.layout.creditreceipt_list_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor c, ViewGroup parent) {
            View v = super.newView(context, c, parent);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.create_time),
                    (TextView) v.findViewById(R.id.number),
                    (TextView) v.findViewById(R.id.cashier),
                    (TextView) v.findViewById(R.id.amount),
                    (TextView) v.findViewById(R.id.expire_time),
                    (TextView) v.findViewById(R.id.used_time),
                    (TextView) v.findViewById(R.id.used_amount)
            ));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            UiHolder holder = (UiHolder) view.getTag();
            long createTime = c.getLong(c.getColumnIndex(CreditReceiptTable.CREATE_TIME));
            holder.createTime.setText(DateUtils.dateOnlyFormat(createTime));

            calendar.setTimeInMillis(createTime);
            calendar.add(Calendar.DATE, c.getInt(c.getColumnIndex(CreditReceiptTable.EXPIRE_TIME)));

            holder.expireTime.setText(DateUtils.dateOnlyFormat(calendar.getTime()));
            holder.number.setText(c.getString(c.getColumnIndex(RegisterTable.TITLE)) + "-" + c.getString(c.getColumnIndex(CreditReceiptTable.PRINT_NUMBER)));
            holder.cashier.setText(c.getString(c.getColumnIndex(CashierTable.FIRST_NAME)) + " " + c.getString(c.getColumnIndex(CashierTable.LAST_NAME)));
            showPrice(holder.amount, _decimal(c, c.getColumnIndex(CreditReceiptTable.AMOUNT)));

            long usedTime = c.getLong(c.getColumnIndex(PaymentTable.CREATE_TIME));
            if(usedTime > 0){
                holder.usedTime.setText(DateUtils.dateOnlyFormat(usedTime));
                showPrice(holder.usedAmount, _decimal(c, c.getColumnIndex(PaymentTable.AMOUNT)));
            }else{
                holder.usedTime.setText(null);
                holder.usedAmount.setText(null);
            }

            view.setAlpha(usedTime > 0 ? 0.3f : 1f);
        }

        public String getGuid(int pos) {
            Cursor c = (Cursor)getItem(pos);
            return c.getString(c.getColumnIndex(CreditReceiptTable.GUID));
        }

        public BigDecimal getAmount(int pos) {
            Cursor c = (Cursor)getItem(pos);
            return _decimal(c, c.getColumnIndex(CreditReceiptTable.AMOUNT));
        }

        public BigDecimal getUsedAmount(int pos) {
            Cursor c = (Cursor)getItem(pos);
            long usedTime = c.getLong(c.getColumnIndex(PaymentTable.CREATE_TIME));
            return usedTime > 0 ? _decimal(c, c.getColumnIndex(PaymentTable.AMOUNT)) : null;
        }
    }

    private static class UiHolder {
        final TextView createTime;
        final TextView number;
        final TextView cashier;
        final TextView amount;
        final TextView expireTime;
        final TextView usedTime;
        final TextView usedAmount;

        private UiHolder(TextView createTime, TextView number, TextView cashier, TextView amount, TextView expireTime, TextView usedTime, TextView usedAmount) {
            this.createTime = createTime;
            this.number = number;
            this.cashier = cashier;
            this.amount = amount;
            this.expireTime = expireTime;
            this.usedTime = usedTime;
            this.usedAmount = usedAmount;
        }
    }

}
