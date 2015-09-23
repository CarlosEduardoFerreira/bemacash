package com.kaching123.tcr.fragment.saleorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.BaseKitchenPrintCallback;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper.IKitchenPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.SaleOrderFunction;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by gdubina on 13/11/13.
 */
@EFragment
public class HoldFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "confirmationDialog";

    @ViewById
    protected EditText orderTitle;

    @ViewById
    protected CheckBox printBox;

    @ViewById(android.R.id.list)
    protected ListView listView;

    @FragmentArg
    protected String argOrderGuid;

    @FragmentArg
    protected String argOrderTitle;

    @FragmentArg
    protected boolean hasKitchenPrintable;

    private IHoldListener listener;
    private OrdersNavigationAdapter adapter;
    private NavigationLoader loaderCallback = new NavigationLoader();

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final boolean hasOrderTitle = !TextUtils.isEmpty(argOrderGuid);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(hasOrderTitle ? R.dimen.holdon_dlg_expanded_heigth: R.dimen.holdon_dlg_heigth));

        printBox.setVisibility(hasKitchenPrintable && hasOrderTitle && getApp().getShopInfo().printOnholdOrders ? View.VISIBLE : View.GONE);
        iniTitle();
        initListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, loaderCallback);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(syncGapReceiver, new IntentFilter(SyncCommand.ACTION_SYNC_GAP));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(syncGapReceiver);
        super.onPause();
    }

    private void iniTitle() {
        if (TextUtils.isEmpty(argOrderGuid)) {
            orderTitle.setVisibility(View.GONE);
        } else {
            orderTitle.setText(argOrderTitle);
            orderTitle.selectAll();
        }
        orderTitle.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE == actionId){
                    onPositiveHandler();
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    private void initListView() {
        listView.addFooterView(new View(getActivity()));
        listView.addHeaderView(new View(getActivity()));
        listView.setAdapter(adapter = new OrdersNavigationAdapter(getActivity()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    SaleOrderModel item = (SaleOrderModel) parent.getItemAtPosition(position);
                    listener.onSwap2Order(orderTitle.getText().toString(), item.guid);
                }
                dismiss();
            }
        });
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.saleorder_hold_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_hold_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_new_order;
    }

    @Override
    protected boolean hasPositiveButton() {
        return argOrderGuid != null;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (printBox.isChecked()){
                    printItemsToKitchen(null, false, false, false);
                    return false;
                }
                onPositiveHandler();
                return true;
            }
        };
    }

    private void onPositiveHandler(){
        if (listener != null) {
            listener.onSwap2Order(orderTitle.getText().toString(), null);
        }
    }

    private class OrdersNavigationAdapter extends ObjectsCursorAdapter<SaleOrderModel> {

        public OrdersNavigationAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.saleorder_hold_order_item_view, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, SaleOrderModel item) {
            TextView t = (TextView) convertView;
            t.setText(item.getHoldName());
            return t;
        }
    }

    private class NavigationLoader implements LoaderManager.LoaderCallbacks<List<SaleOrderModel>> {

        @Override
        public Loader<List<SaleOrderModel>> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT))
//                    .where(ShopStore.SaleOrderTable.OPERATOR_GUID + " = ?", getApp().getOperatorGuid())
                    .where(ShopStore.SaleOrderTable.GUID + " <> ?", argOrderGuid == null ? "" : argOrderGuid)
                    .where(ShopStore.SaleOrderTable.STATUS + " = ? ", OrderStatus.HOLDON.ordinal());

            Date minCreateTime = getApp().getMinSalesHistoryLimitDateDayRounded(calendar);
            if (minCreateTime != null)
                builder.where(ShopStore.SaleOrderTable.CREATE_TIME + " >= ? ", minCreateTime.getTime());
            return builder
                    .orderBy(ShopStore.SaleOrderTable.CREATE_TIME + " desc ")
                    .transform(new SaleOrderFunction() {
                        @Override
                        public SaleOrderModel apply(Cursor c) {
                            Logger.d("COUNT: apply");
                            return super.apply(c);
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<SaleOrderModel>> arg0, List<SaleOrderModel> orders) {
            adapter.changeCursor(orders);
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrderModel>> arg0) {
            adapter.changeCursor(null);
        }
    }

    private void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        PrintItemsForKitchenCommand.start(getActivity(), skipPaperWarning, searchByMac, argOrderGuid, fromPrinter, skip, new KitchenKitchenPrintCallback(), false, orderTitle.getText().toString());
    }

    private class KitchenKitchenPrintCallback extends BaseKitchenPrintCallback {

        private IKitchenPrintCallback skipListener = new IKitchenPrintCallback() {

            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, false, ignorePaperEnd, searchByMac);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                printItemsToKitchen(fromPrinter, true, ignorePaperEnd, searchByMac);
            }
        };

        @Override
        protected void onPrintSuccess() {
            WaitDialogFragment.hide(getActivity());
            onPositiveHandler();

            dismiss();
        }

        @Override
        protected void onPrintError(PrinterError error, String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrintError(getActivity(), error, fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterNotConfigured(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterDisconnected(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterIPnotFound(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(getActivity(), fromPrinter, aliasTitle, skipListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd(String fromPrinter, String aliasTitle) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(getActivity(), fromPrinter, aliasTitle, skipListener);
        }
    }

    public void setListener(IHoldListener listener) {
        this.listener = listener;
    }

    public static interface IHoldListener {
        void onSwap2Order(String holdName, String nextOrderGuid);
        //void onCancelHold();
    }

    public static void show(FragmentActivity context, String orderGuid, String holdTitle, boolean hasKitchenPrintable, IHoldListener listener) {
        DialogUtil.show(context, DIALOG_NAME, HoldFragmentDialog_.builder().argOrderGuid(orderGuid).argOrderTitle(holdTitle).hasKitchenPrintable(hasKitchenPrintable).build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    private BroadcastReceiver syncGapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("[SYNC GAP] Hold Fragment: restart orders on hold count loader");
            getLoaderManager().restartLoader(0, null, loaderCallback);
        }

    };
}
