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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity.IHoldListener;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.DefinedOnHoldModel;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.DefinedOnHoldFunction;
import com.kaching123.tcr.model.converter.SaleOrderFunction;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kaching123.tcr.fragment.UiHelper.showPhone;

/**
 * Created by mboychenko on 2/6/2017.
 */
@EFragment
public class OnHoldListDialogFragment extends BaseOnHoldDialogFragment {

    private static final String DIALOG_NAME = "onHoldListDialog";

    private static final String DIALOG_ORDER_FILTER = "DIALOG_ORDER_FILTER";

    private static final int ON_HOLD_ORDERS_LOADER_ID = 0;
    private static final int DEFINED_ON_HOLD_LOADER_ID = 1;

    @App
    protected TcrApplication app;

    @FragmentArg
    protected HoldOnAction argAction;

    @FragmentArg
    protected String argOrderGuid;

    @FragmentArg
    protected String argOrderTitle;

    @ViewById
    protected LinearLayout searchBlock;

    @ViewById
    protected EditText searchBar;

    @ViewById
    protected GridView gridView;

    @ViewById
    protected View focusGrabber;

    private IHoldListener listener;

    private Calendar calendar = Calendar.getInstance();

    private OnHoldOrdersLoader onHoldLoaderCallback = new OnHoldOrdersLoader();
    private DefinedOnHoldLoader definedOnHoldLoaderCallback = new DefinedOnHoldLoader();

    private GridAdapter gridAdapter;

    private boolean isOnHoldOrdersDefined;

    private SaleOrderModel clickedOrder;
    private DefinedOnHoldModel clickedDefinedOnHold;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isOnHoldOrdersDefined = app.getShopInfo().definedOnHold;

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.holdon_list_dlg_width),
                getResources().getDimensionPixelOffset(isOnHoldOrdersDefined ? R.dimen.holdon_list_dlg_heigth : R.dimen.holdon_list_dlg_heigth_bigger));

        gridAdapter = new GridAdapter(getContext(), isOnHoldOrdersDefined);
        gridView.setAdapter(gridAdapter);
        initViews();
    }

    private void initViews() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOnHoldOrdersDefined) {
                    clickedDefinedOnHold = (DefinedOnHoldModel) parent.getItemAtPosition(position);
                } else {
                    clickedOrder = (SaleOrderModel) parent.getItemAtPosition(position);
                }

                if (listener != null) {
                    switch (argAction) {
                        case ADD_ORDER:
                            if(clickedDefinedOnHold != null && gridAdapter.getOnHoldOrderByDefinedGuid(clickedDefinedOnHold.getGuid()) == null) {
                                printItemsToKitchen(null, false, false, false);
                                printItemToKds();
                            } else {
                                Toast.makeText(getContext(), R.string.defined_on_hold_busy, Toast.LENGTH_LONG).show();
                                return;
                            }
                            break;
                        case GET_ORDER:
                            getOnHoldOrder();
                            break;
                    }
                }
            }
        });

        if(!isOnHoldOrdersDefined) {
            searchBlock.setVisibility(View.VISIBLE);
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Bundle b = new Bundle();
                    b.putString(DIALOG_ORDER_FILTER, s.toString());
                    getLoaderManager().restartLoader(ON_HOLD_ORDERS_LOADER_ID, b, onHoldLoaderCallback);
                }
            });
            focusGrabber.requestFocus();
        }
    }

    @Override
    protected void onPositiveHandler() {
        if (listener != null && clickedDefinedOnHold != null) {
            listener.onSwap2Order(null, null, null, null, clickedDefinedOnHold.getGuid());
        }
        dismiss();
    }

    private void getOnHoldOrder(){
        SaleOrderModel saleOrderModel;
        if(isOnHoldOrdersDefined) {
            saleOrderModel = gridAdapter.getOnHoldOrderByDefinedGuid(clickedDefinedOnHold.getGuid());
            if(saleOrderModel == null) {
                Toast.makeText(getContext(), R.string.on_hold_empty_place, Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            saleOrderModel = clickedOrder;
        }

        if(saleOrderModel != null) {
            listener.onSwap2Order(saleOrderModel.getHoldName(), saleOrderModel.getHoldPhone(), saleOrderModel.getHoldStatus(), saleOrderModel.guid, saleOrderModel.getDefinedOnHoldGuid());
            dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isOnHoldOrdersDefined) {
            getLoaderManager().restartLoader(DEFINED_ON_HOLD_LOADER_ID, null, definedOnHoldLoaderCallback);
        } else {
            getLoaderManager().restartLoader(ON_HOLD_ORDERS_LOADER_ID, null, onHoldLoaderCallback);
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(syncGapReceiver, new IntentFilter(SyncCommand.ACTION_SYNC_GAP));
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.on_hold_list_dialog_fragment;
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
    protected boolean hasPositiveButton(){
        return false;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected void printItemsToKitchen(String fromPrinter, boolean skip, boolean skipPaperWarning, boolean searchByMac) {
        printToKitchenFlag = false;
        Logger.e("CEF.HoldFragmentDialog.printItemsToKitchen:printOnholdOrders " + getApp().getShopInfo().printOnholdOrders);
        /*
         *   Added if condition to print only if "Receipt Settings" configuration is seted "Print Kitchen Receipt for On Hold Orders" = enabled
         */
        if(getApp().getShopInfo().printOnholdOrders) {
            WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
        }
        PrintItemsForKitchenCommand.itComesFromPay = false;

        String title;
        if(isOnHoldOrdersDefined) {
            title =  clickedDefinedOnHold.getName();
        } else {
            title = clickedOrder.getHoldName();
        }
        PrintItemsForKitchenCommand.start(getActivity(), skipPaperWarning, searchByMac, argOrderGuid, fromPrinter, skip,
                new KitchenKitchenPrintCallback(), false, title);
    }

    @Override
    protected void printItemToKds(){
        printToKdsFlag = false;
        PrintOrderToKdsCommand.start(getActivity(), argOrderGuid, false, new KDSPrintCallback());
    }

    private class OnHoldOrdersLoader implements LoaderManager.LoaderCallbacks<List<SaleOrderModel>> {

        @Override
        public Loader<List<SaleOrderModel>> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT))
                    .where(ShopStore.SaleOrderTable.GUID + " <> ?", argOrderGuid == null ? "" : argOrderGuid)
                    .where(ShopStore.SaleOrderTable.STATUS + " = ? ", OrderStatus.HOLDON.ordinal());

            if(arg1 != null) {
                String filter = arg1.getString(DIALOG_ORDER_FILTER);
                if (!TextUtils.isEmpty(filter)) {
                    filter  = "%" + filter + "%";
                    builder.where(ShopStore.SaleOrderTable.HOLD_NAME + " LIKE ? OR " + ShopStore.SaleOrderTable.HOLD_TEL + " LIKE ? ", filter, filter);
                }
            }

            Date minCreateTime = getApp().getMinSalesHistoryLimitDateDayRounded(calendar);
            if (minCreateTime != null)
                builder.where(ShopStore.SaleOrderTable.CREATE_TIME + " >= ? ", minCreateTime.getTime());
            return builder
                    .orderBy(ShopStore.SaleOrderTable.CREATE_TIME + " desc ")
                    .transformRow(new SaleOrderFunction() {
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
            gridAdapter.setSaleOrderModels(orders);
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrderModel>> arg0) {
        }
    }

    private class DefinedOnHoldLoader implements LoaderManager.LoaderCallbacks<List<DefinedOnHoldModel>> {

        @Override
        public Loader<List<DefinedOnHoldModel>> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ShopStore.DefinedOnHoldTable.URI_CONTENT));

            return builder
                    .orderBy(ShopStore.DefinedOnHoldTable.UPDATE_TIME + " asc ")
                    .transformRow(new DefinedOnHoldFunction() {
                        @Override
                        public DefinedOnHoldModel apply(Cursor c) {
                            Logger.d("COUNT: apply");
                            return super.apply(c);
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<DefinedOnHoldModel>> arg0, List<DefinedOnHoldModel> orders) {
            gridAdapter.setDefinedOnHoldModels(orders);
            getLoaderManager().restartLoader(ON_HOLD_ORDERS_LOADER_ID, null, onHoldLoaderCallback);
        }

        @Override
        public void onLoaderReset(Loader<List<DefinedOnHoldModel>> arg0) {
        }
    }

    public void setListener(IHoldListener listener) {
        this.listener = listener;
    }

    private class GridAdapter extends BaseAdapter {

        private Context context;
        private boolean isOnHoldDefined;
        private List<SaleOrderModel> saleOrderModels = new ArrayList<>();
        private List<DefinedOnHoldModel> definedOnHoldModels = new ArrayList<>();
        private Set<String> lockedDefinedPlaces = new HashSet<>();

        public GridAdapter(Context context, boolean isOnHoldOrdersDefined) {
            this.context = context;
            isOnHoldDefined = isOnHoldOrdersDefined;
        }

        public void setDefinedOnHoldModels(List<DefinedOnHoldModel> definedOnHoldModels) {
            this.definedOnHoldModels = definedOnHoldModels;
        }

        public void setSaleOrderModels(List<SaleOrderModel> saleOrderModels) {
            this.saleOrderModels = saleOrderModels;

            lockedDefinedPlaces.clear();
            if(isOnHoldDefined) {
                for (SaleOrderModel saleOrderModel : saleOrderModels) {
                    if (saleOrderModel.getDefinedOnHoldGuid() != null) {
                        lockedDefinedPlaces.add(saleOrderModel.getDefinedOnHoldGuid());
                    }
                }
            }

            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return isOnHoldDefined ? definedOnHoldModels.size() : saleOrderModels.size();
        }

        @Override
        public Object getItem(int position) {
            return isOnHoldDefined ? definedOnHoldModels.get(position) : saleOrderModels.get(position);
        }

        private SaleOrderModel getOnHoldOrderByDefinedGuid(String definedOnHoldGuid){
            if(!lockedDefinedPlaces.contains(definedOnHoldGuid))
                return null;
            for (SaleOrderModel saleOrderModel : saleOrderModels) {
                if(definedOnHoldGuid.equals(saleOrderModel.getDefinedOnHoldGuid())) {
                    return saleOrderModel;
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View itemView = convertView;
            ViewHolder holder = null;

            if (itemView == null) {
                final LayoutInflater layoutInflater =
                        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = layoutInflater.inflate(R.layout.on_hold_item_view, parent, false);

                holder = new ViewHolder();
                holder.itemMainContainer = (RelativeLayout) itemView.findViewById(R.id.item_main_container);

                holder.baseContentHolder = (RelativeLayout) itemView.findViewById(R.id.base_content);
                holder.placeName = (TextView) itemView.findViewById(R.id.place_name);

                holder.onHoldName = (TextView) itemView.findViewById(R.id.on_hold_name);
                holder.onHoldPhone = (TextView) itemView.findViewById(R.id.on_hold_phone);
                holder.onHoldStatus = (TextView) itemView.findViewById(R.id.on_hold_status);
                itemView.setTag(holder);
            } else {
                holder = (ViewHolder) itemView.getTag();
            }
            holder.itemMainContainer.setActivated(false);

            if(isOnHoldOrdersDefined) {
                DefinedOnHoldModel model = (DefinedOnHoldModel) getItem(position);
                holder.baseContentHolder.setVisibility(View.GONE);
                holder.placeName.setVisibility(View.VISIBLE);
                holder.placeName.setText(model.getName());

                if(lockedDefinedPlaces.contains(model.getGuid())){
                    holder.itemMainContainer.setActivated(true);
                }

            } else {
                SaleOrderModel model = (SaleOrderModel) getItem(position);
                holder.onHoldName.setText(model.getHoldName());
                showPhone(holder.onHoldPhone, model.getHoldPhone());

                if(model.getHoldStatus() != OnHoldStatus.NONE) {
                    String text = "";
                    int color = 0;
                    switch (model.getHoldStatus()) {
                        case DINE_IN:
                            text = context.getString(R.string.dine_in);
                            color = context.getResources().getColor(R.color.dine_in);
                            break;
                        case TO_GO:
                            text = context.getString(R.string.to_go);
                            color = context.getResources().getColor(R.color.to_go);
                            break;
                    }
                    holder.onHoldStatus.setText(text);
                    holder.onHoldStatus.setBackgroundColor(color);
                }
            }


            return itemView;
        }

        final class ViewHolder
        {
            RelativeLayout itemMainContainer;

            RelativeLayout baseContentHolder;
            TextView placeName;

            TextView onHoldName;
            TextView onHoldPhone;
            TextView onHoldStatus;
        }

    }

    public static void show(FragmentActivity context, String orderGuid, String orderTitle, HoldOnAction action, IHoldListener listener) {
        DialogUtil.show(context, DIALOG_NAME, OnHoldListDialogFragment_.builder().argAction(action).argOrderGuid(orderGuid).argOrderTitle(orderTitle).build()).setListener(listener);
    }

    private BroadcastReceiver syncGapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("[SYNC GAP] Hold Fragment: restart orders on hold count loader");
            if(isOnHoldOrdersDefined) {
                getLoaderManager().restartLoader(DEFINED_ON_HOLD_LOADER_ID, null, definedOnHoldLoaderCallback);
            } else {
                getLoaderManager().restartLoader(ON_HOLD_ORDERS_LOADER_ID, null, onHoldLoaderCallback);
            }
        }

    };

    public enum HoldOnAction{
        ADD_ORDER,
        GET_ORDER
    }
}
