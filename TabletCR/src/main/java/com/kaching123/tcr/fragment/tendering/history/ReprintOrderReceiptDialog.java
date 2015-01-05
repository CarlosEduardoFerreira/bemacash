package com.kaching123.tcr.fragment.tendering.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemFragment.HistoryDetailedOrderItemListener;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;

/**
 * Created by vkompaniets on 16.05.2014.
 */
@EFragment (R.layout.reprint_order_receipt_dialog_fragment)
public class ReprintOrderReceiptDialog extends StyledDialogFragment implements LoaderCallbacks<Cursor>{

    private static final String DIALOG_NAME = ReprintOrderReceiptDialog.class.getSimpleName();

    @FragmentArg
    protected String orderGuid;

    @ViewById
    protected CheckBox orderReceiptChkbx;

    @ViewById
    protected CheckBox refundReceiptChkbx;

    private HistoryDetailedOrderItemListener listener;

    private boolean printOrder = true;
    private boolean printRefund = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.reprint_order_receipt_dialog_width),
                getResources().getDimensionPixelOffset(R.dimen.reprint_order_receipt_dialog_height));

        orderReceiptChkbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                printOrder = isChecked;
                updatePositiveBtn();
            }
        });

        refundReceiptChkbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                printRefund = isChecked;
                updatePositiveBtn();
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    public ReprintOrderReceiptDialog setListener(HistoryDetailedOrderItemListener listener) {
        this.listener = listener;
        return this;
    }

    private void updatePositiveBtn(){
        Button positiveBtn = getPositiveButton();
        if (!printOrder && !printRefund){
            positiveBtn.setEnabled(false);
            positiveBtn.setTextColor(disabledBtnColor);
        }else{
            positiveBtn.setEnabled(true);
            positiveBtn.setTextColor(normalBtnColor);
        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.reprint_order_receipt_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.reprint_order_receipt_dialog_name;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_print;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null){
                    listener.onReprintClick(printOrder, printRefund);
                }
                return true;
            }
        };
    }

    public static void show(FragmentActivity context, String orderGuid, HistoryDetailedOrderItemListener listener) {
        DialogUtil.show(context, DIALOG_NAME, ReprintOrderReceiptDialog_.builder().orderGuid(orderGuid).build().setListener(listener));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT))
                .projection(SaleOrderTable.GUID)
                .where(SaleOrderTable.PARENT_ID + " = ?", orderGuid)
                .build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        refundReceiptChkbx.setEnabled(data.getCount() > 0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
