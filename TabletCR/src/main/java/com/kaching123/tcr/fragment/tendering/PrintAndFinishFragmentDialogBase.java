package com.kaching123.tcr.fragment.tendering;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.CheckBox;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseCashierActivity.SaleOrderViewResult;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorHelloCommand;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by pkabakov on 26.12.13.
 */
@EFragment
public abstract class PrintAndFinishFragmentDialogBase extends StyledDialogFragment {

    protected IFinishConfirmListener listener;

    @ViewById
    protected CheckBox printBox;

    @FragmentArg
    protected String orderGuid;

    protected SaleOrderModel order;
    protected ArrayList<SaleOrderItemViewModel> items;
    protected ArrayList<String> itemsPrinterAlias = new ArrayList<>();
    protected CustomerModel customer;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.base_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        setCancelable(false);
        getLoaderManager().initLoader(0, null, new SaleOrderViewLoader());
    }

    protected abstract BigDecimal calcTotal();

    protected boolean enableSignatureCheckbox() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_abort;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_finish;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {

        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                onConfirm();
                return false;
            }
        };
    }

    private class SaleOrderViewLoader implements LoaderCallbacks<SaleOrderViewResult> {

        @Override
        public Loader<SaleOrderViewResult> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(SaleOrderView.URI_CONTENT))
                    .where(SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid == null ? "" : orderGuid)
                    .transform(new Function<Cursor, SaleOrderViewResult>() {
                        @Override
                        public SaleOrderViewResult apply(Cursor cursor) {
                            if (!cursor.moveToFirst()){
                                return new SaleOrderViewResult(null, null);
                            }else{
                                SaleOrderModel order = SaleOrderModel.fromView(cursor);
                                CustomerModel customer = null;
                                if (!cursor.isNull(cursor.getColumnIndex(SaleOrderView2.SaleOrderTable.CUSTOMER_GUID))){
                                    customer = CustomerModel.fromOrderView(cursor);
                                }
                                return new SaleOrderViewResult(order, customer);
                            }
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<SaleOrderViewResult> loader, SaleOrderViewResult data) {
            order = data.order;
            customer = data.customer;
            getLoaderManager().initLoader(1, null, new SaleOrderItemsModelLoader());
        }

        @Override
        public void onLoaderReset(Loader<SaleOrderViewResult> loader) {
            order = null;
            customer = null;
        }
    }

    private class SaleOrderItemsModelLoader implements LoaderCallbacks<List<SaleOrderItemViewModel>> {

        @Override
        public Loader<List<SaleOrderItemViewModel>> onCreateLoader(int loaderId, Bundle args) {
            return SaleOrderItemViewModelWrapFunction.createLoader(getActivity(), orderGuid);
        }

        @Override
        public void onLoadFinished(Loader<List<SaleOrderItemViewModel>> loader, final List<SaleOrderItemViewModel> list) {
            items = new ArrayList<>(list);
            HashSet<String> printerAliases = new HashSet<>(items.size());
            for (SaleOrderItemViewModel item : items) {
                printerAliases.add(item.kitchenPrinterGuid);
            }
            itemsPrinterAlias = new ArrayList<>(printerAliases);
            handler.sendEmptyMessage(MSG_ORDER_DATA_LOADED);
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrderItemViewModel>> c) {
            items.clear();
            items = new ArrayList<>();
        }
    }

    public static final int MSG_ORDER_DATA_LOADED = 123;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_ORDER_DATA_LOADED)
                onOrderDataLoaded();
        }
    };

    protected void onOrderDataLoaded(){

    }

    protected boolean onConfirm() {
        if (!getApp().isBlackstonePax() && getApp().isPaxConfigured())
            PaxProcessorHelloCommand.start(getActivity(), PaxModel.get(), helloCallBack);
        if (printBox.isChecked()) {
            printOrder(false, false);
        } else {
            completeProcess();
        }
        return false;
    }

    public PaxProcessorHelloCommand.PaxHelloCommandBaseCallback helloCallBack = new PaxProcessorHelloCommand.PaxHelloCommandBaseCallback() {

        @Override
        protected void handleSuccess(String details) {

        }

        @Override
        protected void handleError(String error) {

        }
    };

    protected void completeProcess() {
        if (listener != null) {
            listener.onConfirmed();
        }
        dismiss();
    }

    public PrintAndFinishFragmentDialogBase setListener(IFinishConfirmListener listener) {
        this.listener = listener;
        return this;
    }

    public static interface IFinishConfirmListener {

        void onConfirmed();
    }

    protected abstract void printOrder(boolean skipPaperWarning, boolean searchByMac);

}
