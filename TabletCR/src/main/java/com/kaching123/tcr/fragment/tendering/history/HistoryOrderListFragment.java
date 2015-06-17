package com.kaching123.tcr.fragment.tendering.history;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.CashierActivity;
import com.kaching123.tcr.activity.QuickServiceActivity;
import com.kaching123.tcr.commands.rest.sync.DownloadOldOrdersCommand;
import com.kaching123.tcr.commands.rest.sync.DownloadOldOrdersCommand.BaseDownloadOldOrdersCommandCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filter.CashierFilterSpinnerAdapter;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TenderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.OperatorTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.TipsTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTipsQuery;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
@OptionsMenu(R.menu.history_activity)
public class HistoryOrderListFragment extends ListFragment implements IFilterRequestListener, LoaderCallbacks<List<SaleOrderTipsViewModel>> {

    private static final Uri URI_ORDERS_WITH_TIPS = ShopProvider.getContentUri(SaleOrderTipsQuery.URI_CONTENT);

    @ViewById
    protected View gratuityDivider;
    @ViewById
    protected View transactionsStatusDivider;
    @ViewById
    protected View gratuityHeader;
    @ViewById
    protected View transactionsStatusHeader;

    private List<ILoader> loaderCallback = new ArrayList<ILoader>();

    protected HistoryOrderAdapter adapter;

    private Date from;
    private Date to;
    private String cashierGUID = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID;
    private String customerGUID = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID;
    private TransactionsState transactionsState;
    private ArrayList<String> registerTitle;
    private ArrayList<String> seqNum;
    private String unitSerial;

    private String[] loadedOrderGuids;

    private boolean isTipsEnabled;

    private Calendar calendar = Calendar.getInstance();

    private boolean firstLoad;
    private boolean isSearchingOrder;

    private static final Handler handler = new Handler();

    protected void setFilterValues(Date from, Date to, String cashierGUID, String customerGUID,
                                   TransactionsState transactionsState, ArrayList<String> registerTitle, ArrayList<String> seqNum, String unitSerial) {
        this.from = from;
        this.to = to;
        this.cashierGUID = cashierGUID;
        this.customerGUID = customerGUID;
        this.transactionsState = transactionsState;
        this.registerTitle = registerTitle;
        this.seqNum = seqNum;
        this.unitSerial = unitSerial;

        this.loadedOrderGuids = null;

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tendering_history_orders_list_fragment, container, false);
    }

    @AfterViews
    protected void init() {
        firstLoad = true;
        isSearchingOrder = false;
        loadedOrderGuids = null;
        isTipsEnabled = ((TcrApplication) getActivity().getApplicationContext()).isTipsEnabled();
        setListAdapter(adapter = new HistoryOrderAdapter(getActivity(), isTipsEnabled));
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                SaleOrderViewModel model = adapter.getItem(pos);
                Logger.d("clicking" + model.guid);
                for (ILoader loader : loaderCallback) {

                    loader.onItemClicked(model.guid,
                            model.tmpTotalPrice,
                            model.createTime,
                            model.operatorName,
                            model.registerTitle + "-" + model.printSeqNum,
                            model.type,
                            model.isTipped);
                }
            }
        });

        gratuityDivider.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        transactionsStatusDivider.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        gratuityHeader.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        transactionsStatusHeader.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!firstLoad && from != null)
            getLoaderManager().restartLoader(0, null, this);
        firstLoad = false;
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(syncGapReceiver, new IntentFilter(SyncCommand.ACTION_SYNC_GAP));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(syncGapReceiver);
        isSearchingOrder = false;
        loadedOrderGuids = null;
        super.onPause();
    }

    @Override
    public Loader<List<SaleOrderTipsViewModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("onCreateLoader");
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ORDERS_WITH_TIPS);

        if (loadedOrderGuids != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < loadedOrderGuids.length; i++) {
                if (i > 0)
                    sb.append(',');
                sb.append('?');
            }

            loader.where(SaleOrderTable.GUID + " IN (" + sb.toString() + ")", loadedOrderGuids);

            return loader.orderBy(SaleOrderTable.CREATE_TIME + " desc ")
                    .transform(new SaleOrderTipsViewFunction()).build(getActivity());
        }

        if (isTipsEnabled) {
            if (TransactionsState.OPEN.equals(transactionsState)) {
                loader.where(SaleOrderTipsQuery.HAS_OPENED_TRANSACTIONS + " = 1 ");
            } else if (TransactionsState.CLOSED.equals(transactionsState)) {
                loader.where(SaleOrderTipsQuery.HAS_PREAUTH_TRANSACTIONS + " = 1 and " + SaleOrderTipsQuery.HAS_OPENED_TRANSACTIONS + " = 0");
            }
        }

        if (!TextUtils.isEmpty(cashierGUID) && !CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(cashierGUID)) {
            loader.where(SaleOrderTable.OPERATOR_GUID + " = ?", cashierGUID);
        }

        if (!TextUtils.isEmpty(customerGUID) && !CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(customerGUID)) {
            loader.where(SaleOrderTable.CUSTOMER_GUID + " = ?", customerGUID);
        }

        if (from != null && to != null) {
            loader.where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", from.getTime(), to.getTime());
        }

        if (registerTitle != null && registerTitle.size() > 0) {
            StringBuilder sb = null;
            String[] content = new String[registerTitle.size()];
            boolean corrupted = false;
            for (int i = 0; i < registerTitle.size(); i++) {
                if (sb == null) {
                    sb = new StringBuilder("?");
                } else {
                    sb.append(",?");
                }
                String s = registerTitle.get(i);
                if (s != null) {
                    content[i] = s;
                } else {
                    corrupted = true;
                }
            }
            if (sb != null && !corrupted) {
                loader.where(RegisterTable.TITLE + " IN (" + sb.toString() + ")", content);
            }
        }

        if (seqNum != null && seqNum.size() > 0) {
            StringBuilder sb = null;
            String[] content = new String[seqNum.size()];
            boolean corrupted = false;
            for (int i = 0; i < seqNum.size(); i++) {
                if (sb == null) {
                    sb = new StringBuilder("?");
                } else {
                    sb.append(",?");
                }
                String s = seqNum.get(i);
                if (s != null) {
                    content[i] = s;
                } else {
                    corrupted = true;
                }
            }
            if (sb != null && !corrupted) {
                loader.where(SaleOrderTable.PRINT_SEQ_NUM + " IN (" + sb.toString() + ")", content);
            }
        }

        TcrApplication app = (TcrApplication) getActivity().getApplicationContext();
        Date  minCreateTime = app.getMinSalesHistoryLimitDateDayRounded(calendar);
        if (minCreateTime != null)
            loader.where("(" + SaleOrderTable.CREATE_TIME + " >= " + minCreateTime.getTime() + " OR " + SaleOrderView2.TipsTable.CREATE_TIME + " >= " + minCreateTime.getTime() + " OR " + SaleOrderTipsQuery.MAX_REFUND_CREATE_TIME + " >= " + minCreateTime.getTime() + ")");

        return loader.orderBy(SaleOrderTable.CREATE_TIME + " desc ")
                .transform(new SaleOrderTipsViewFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<SaleOrderTipsViewModel>> listLoader, List<SaleOrderTipsViewModel> saleOrderModels) {
        adapter.changeCursor(saleOrderModels);

        boolean shouldSearchOnServer = false;
        if (isSearchingOrder) {
            isSearchingOrder = false;
            if ((saleOrderModels == null || saleOrderModels.isEmpty()) && isSearchingOrderFilters())
                shouldSearchOnServer = true;
        }

        if (getActivity() == null)
            return;

        if (!shouldSearchOnServer || TcrApplication.get().isTrainingMode() || TcrApplication.get().getSalesHistoryLimit() == null)
            return;

        final String registerTitle = this.registerTitle == null || this.registerTitle.isEmpty() ? null : this.registerTitle.get(0);
        final String printSeqNum = this.seqNum == null || this.seqNum.isEmpty() ? null : this.seqNum.get(0);
        final String employeeGuid = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(this.cashierGUID) ? null : this.cashierGUID;
        final String customerGuid = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(this.customerGUID) ? null : this.customerGUID;
        final Boolean isStatusOpened;
        if (isTipsEnabled && transactionsState != null && !transactionsState.equals(TransactionsState.NA)) {
            isStatusOpened = TransactionsState.OPEN.equals(transactionsState);
        } else {
            isStatusOpened = null;
        }
        final Date from = this.from;
        final Date to = this.to;
        final String unitSerial = this.unitSerial;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                AlertDialogFragment.showConfirmation(getActivity(), R.string.order_searching_title, getString(R.string.order_searching_message), new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
                        DownloadOldOrdersCommand.start(getActivity(), registerTitle, printSeqNum, employeeGuid, customerGuid, isStatusOpened, from, to, unitSerial, downloadOrderCallback);
                        return true;
                    }
                });
            }
        });

        /*Pair<BigDecimal, BigDecimal> priceRange = getPriceRange(saleOrderModels);
        for (ILoader loader : loaderCallback) {
            loader.onLoad(adapter.getCount(), priceRange.first, priceRange.second);
        }*/
        for (ILoader loader : loaderCallback) {
            loader.onSearchFinish();
        }

    }

    private boolean isSearchingOrderFilters() {
        return loadedOrderGuids == null &&
                (!TextUtils.isEmpty(this.unitSerial) || (
                (registerTitle != null && !registerTitle.isEmpty()
                    && seqNum != null && !seqNum.isEmpty() && seqNum.get(0) != null)
                    && from != null && to != null
                    && (TextUtils.isEmpty(cashierGUID) || cashierGUID.equals(CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID))
                    && (TextUtils.isEmpty(customerGUID) || customerGUID.equals(CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID))
                    && (!isTipsEnabled || transactionsState == null || transactionsState.equals(TransactionsState.NA))));
    }

    private Pair<BigDecimal, BigDecimal> getPriceRange(List<SaleOrderTipsViewModel> saleOrderModels) {
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        for (SaleOrderTipsViewModel model : saleOrderModels) {
            if (min.compareTo(model.tmpTotalPrice) > 0) {
                min = model.tmpTotalPrice;
            }
            if (max.compareTo(model.tmpTotalPrice) < 0) {
                max = model.tmpTotalPrice;
            }
        }
        return new Pair<BigDecimal, BigDecimal>(min, max);
    }

    @Override
    public void onLoaderReset(Loader<List<SaleOrderTipsViewModel>> listLoader) {
        Logger.d("onLoaderReset");
        if (getActivity() == null)
            return;
        adapter.changeCursor(null);
    }

    public void addListener(ILoader callback) {
        loaderCallback.add(callback);
    }

    @Override
    public void onFilterRequested(Date from, Date to, String cashierGUID, String customerGUID,
                                  TransactionsState transactionsState, ArrayList<String> registerNum, ArrayList<String> seqNum, String unitSerial, boolean isManual) {
        Logger.d("We are up to filter the list with %s, %s, %s, %s, %s, %s", from, to, cashierGUID, customerGUID, registerNum, seqNum);
        isSearchingOrder = isManual;
        setFilterValues(from, to, cashierGUID, customerGUID, transactionsState, registerNum, seqNum, unitSerial);
    }

    @Override
    public void onSearchByUnitFailed(String serialCode) {
        boolean shouldSearchOnServer = !TextUtils.isEmpty(unitSerial);

        if (!shouldSearchOnServer || TcrApplication.get().isTrainingMode() || TcrApplication.get().getSalesHistoryLimit() == null)
            return;

        final String employeeGuid = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(this.cashierGUID) ? null : this.cashierGUID;
        final String customerGuid = CashierFilterSpinnerAdapter.DEFAULT_ITEM_GUID.equals(this.customerGUID) ? null : this.customerGUID;
        final Boolean isStatusOpened;
        if (isTipsEnabled && transactionsState != null && !transactionsState.equals(TransactionsState.NA)) {
            isStatusOpened = TransactionsState.OPEN.equals(transactionsState);
        } else {
            isStatusOpened = null;
        }
        final Date from = this.from;
        final Date to = this.to;
        final String unitSerial = serialCode;

        AlertDialogFragment.showConfirmation(getActivity(), R.string.order_searching_title, getString(R.string.order_searching_message), new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                WaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
                DownloadOldOrdersCommand.start(getActivity(), null, null, employeeGuid, customerGuid, isStatusOpened, from, to, unitSerial, downloadOrderCallback);
                return true;
            }
        });
    }

    @OptionsItem
    protected void actionAddSelected() {
        if (((TcrApplication) getActivity().getApplicationContext()).getStartView() == ShopInfoViewJdbcConverter.ShopInfo.ViewType.QUICK_SERVICE) {
            QuickServiceActivity.start4Return(getActivity());
        } else {
            CashierActivity.start4Return(getActivity());
        }
    }

    public interface ILoader {

        void onLoad(int count, BigDecimal min, BigDecimal max);

        void onItemClicked(String guid, BigDecimal totalAmount, Date dateText, String cashierText, String numText, OrderType type, boolean isTipped);

        void onSearchFinish();
    }

    private class SaleOrderTipsViewFunction extends ListConverterFunction<SaleOrderTipsViewModel> {

        @Override
        public SaleOrderTipsViewModel apply(Cursor c) {
            super.apply(c);

            boolean hasPreauthTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.HAS_PREAUTH_TRANSACTIONS)) > 0;
            boolean hasOpenedTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.HAS_OPENED_TRANSACTIONS)) > 0;

            boolean hasCashTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.CASH_TRANSACTION_CNT)) > 0;
            boolean hasCreditTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.CREDIT_TRANSACTION_CNT)) > 0;
            boolean hasDebitTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.DEBIT_TRANSACTION_CNT)) > 0;
            boolean hasEbtTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.EBT_TRANSACTION_CNT)) > 0;
            boolean hasOtherTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.OTHER_TRANSACTION_CNT)) > 0;

            TenderType tenderType;
            ArrayList<TenderType> tenderTypes = new ArrayList<TenderType>();
            if (hasCashTransactions) {
                tenderTypes.add(TenderType.CASH);
            }
            if (hasCreditTransactions) {
                tenderTypes.add(TenderType.CREDIT_CARD);
            }
            if (hasDebitTransactions) {
                tenderTypes.add(TenderType.DEBIT_CARD);
            }
            if (hasEbtTransactions) {
                tenderTypes.add(TenderType.EBT);
            }
            if (hasOtherTransactions) {
                tenderTypes.add(TenderType.OTHER);
            }

            if (tenderTypes.isEmpty()) {
                tenderType = null;
            } else if (tenderTypes.size() == 1) {
                tenderType = tenderTypes.get(0);
            } else {
                tenderType = TenderType.MULTIPLE;
            }

            TransactionsState transactionState;
            if (hasOpenedTransactions)
                transactionState = TransactionsState.OPEN;
            else
                transactionState = TransactionsState.CLOSED;

            return new SaleOrderTipsViewModel(
                    c.getString(indexHolder.get(SaleOrderTable.GUID)),
                    new Date(c.getLong(indexHolder.get(SaleOrderTable.CREATE_TIME))),
                    c.getString(indexHolder.get(SaleOrderTable.OPERATOR_GUID)),
                    c.getString(indexHolder.get(SaleOrderTable.SHIFT_GUID)),
                    c.getString(indexHolder.get(SaleOrderTable.CUSTOMER_GUID)),
                    _decimal(c, indexHolder.get(SaleOrderTable.DISCOUNT)),
                    _discountType(c, indexHolder.get(SaleOrderTable.DISCOUNT_TYPE)),
                    _orderStatus(c, indexHolder.get(SaleOrderTable.STATUS)),
                    c.getString(indexHolder.get(SaleOrderTable.HOLD_NAME)),
                    _bool(c, indexHolder.get(SaleOrderTable.TAXABLE)),
                    _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_PRICE)),
                    _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_TAX)),
                    _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_DISCOUNT)),
                    c.getInt(indexHolder.get(SaleOrderTable.PRINT_SEQ_NUM)),
                    c.getInt(indexHolder.get(SaleOrderTable.REGISTER_ID)),
                    c.getString(indexHolder.get(SaleOrderTable.PARENT_ID)),
                    _orderType(c, indexHolder.get(SaleOrderTable.ORDER_TYPE)),
                    _bool(c, indexHolder.get(SaleOrderTable.IS_TIPPED)),
                    concatFullname(c.getString(indexHolder.get(OperatorTable.FIRST_NAME)), c.getString(indexHolder.get(OperatorTable.LAST_NAME))),
                    c.getString(indexHolder.get(RegisterTable.TITLE)),
                    concatFullname(c.getString(indexHolder.get(CustomerTable.FISRT_NAME)), c.getString(indexHolder.get(CustomerTable.LAST_NAME))),
                    c.getString(indexHolder.get(CustomerTable.PHONE)),
                    c.getString(indexHolder.get(CustomerTable.EMAIL)),
                    _decimal(c, indexHolder.get(TipsTable.AMOUNT)),
                    transactionState,
                    tenderType,
                    _kitchenPrintStatus(c, indexHolder.get(SaleOrderTable.KITCHEN_PRINT_STATUS)),
                    _decimal(c, indexHolder.get(SaleOrderTable.TRANSACTION_FEE))
            );
        }


    }

    private BroadcastReceiver syncGapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (from == null)
                return;
            Logger.d("[SYNC GAP] History Order List Fragment: restart orders loader");
            getLoaderManager().restartLoader(0, null, HistoryOrderListFragment.this);
        }

    };


    private BaseDownloadOldOrdersCommandCallback downloadOrderCallback = new BaseDownloadOldOrdersCommandCallback() {

        @Override
        protected void onSuccess(String[] guids) {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            loadedOrderGuids = guids;
            getLoaderManager().restartLoader(0, null, HistoryOrderListFragment.this);
        }

        @Override
        protected void onNotFoundError() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.order_searching_error_message_not_found));
        }

        @Override
        protected void onSyncLockedError() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.error_message_sync_locked));
        }

        @Override
        protected void onFailure() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.order_searching_error_message));
        }
    };

}
