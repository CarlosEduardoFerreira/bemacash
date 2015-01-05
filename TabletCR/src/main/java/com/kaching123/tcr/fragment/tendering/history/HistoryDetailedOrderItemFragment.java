package com.kaching123.tcr.fragment.tendering.history;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.commands.store.user.ClockInCommand;
import com.kaching123.tcr.commands.store.user.ClockInCommand.BaseClockInCallback;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment.RefundAmount;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.user.TimesheetFragment;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.function.ReadTipsFunction;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PrepaidOrderView2;
import com.kaching123.tcr.store.ShopSchema2.PrepaidOrderView2.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopSchema2.PrepaidOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleItemExDelView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleItemExDelView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.PrepaidOrderView;
import com.kaching123.tcr.store.ShopStore.SaleItemExDelView;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment(R.layout.tendering_history_total_cost_fragment)
public class HistoryDetailedOrderItemFragment extends SuperBaseFragment {

    private static final Uri URI_SHIFTS = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);
    private static final Uri PRINTER_ALIAS_URI = ShopProvider.getContentUri(SaleItemExDelView.URI_CONTENT);
    private static final Uri PREPAID_ORDER_URI = ShopProvider.getContentUri(PrepaidOrderView.URI_CONTENT);

    private static final int SHIFTS_LOADER_ID = 0;
    private static final int KITCHEN_PRINTING_LOADER_ID = 1;
    private static final int PREPAID_ORDER_LOADER_ID = 2;
    private static final int PREAUTH_TRANSACTIONS_LOADER_ID = 3;
    private static final int TIPS_LOADER_ID = 4;

    @ViewById
    protected TextView num;

    @ViewById
    protected View subtotalContainer;

    @ViewById
    protected TextView subtotal;

    @ViewById
    protected View tipsContainer;

    @ViewById
    protected TextView tips;

    @ViewById
    protected TextView total;

    @ViewById
    protected TextView date;

    @ViewById
    protected TextView cashier;

    @ViewById
    protected Button btnReturn;

    @ViewById
    protected Button btnReprintKitchen;

    @ViewById
    protected Button btnClose;

    @ViewById
    protected TextView refundAmount;

    private MenuItem actionRefundTips;

    private String orderGuid;
    private BigDecimal totalAmount;
    private Date orderDate;
    private BigDecimal orderRefundAmount;
    private boolean shiftIsOpened;
    private OrderType type;
    private PrepaidType prepaidType;
    private boolean isPrepaidFailed;
    private boolean isOrderTipped;

    private boolean isTipsEnabled;

    private ArrayList<PaymentTransactionModel> preauthTransactions;

    private BigDecimal tipsAmount = BigDecimal.ZERO;

    private HistoryDetailedOrderItemListener listener;

    private boolean isShown;

    public OrderType getOrderType() {
        return type;
    }

    public PrepaidType getPrepaidType() {
        return prepaidType;
    }

    public RefundAmount getTipsRefundAmount() {
        return new RefundAmount(tipsAmount, BigDecimal.ZERO, new ArrayList<RefundSaleItemInfo>(), true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @AfterViews
    protected void initViews() {
        isTipsEnabled = getApp().isTipsEnabled();
        btnClose.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);

        subtotalContainer.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);
        tipsContainer.setVisibility(isTipsEnabled ? View.VISIBLE : View.GONE);

        if (getApp().getShiftGuid() == null) {
            setShift(null);
        } else {
            getLoaderManager().initLoader(SHIFTS_LOADER_ID, null, shiftsLoader);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tendering_history_total_cost_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

        actionRefundTips = menu.findItem(R.id.action_refund_tips);
        actionRefundTips.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!getApp().isOperatorClockedIn() && getApp().getShopInfo().clockinRequired4Sales){
                    try2ClockIn();
                    return true;
                }
                boolean tipsPermitted = getApp().hasPermission(Permission.TIPS);
                if (!tipsPermitted) {
                    PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                        @Override
                        public void onLoginComplete() {
                            super.onLoginComplete();
                            if (listener != null)
                                listener.onTipsRefundClick();
                        }
                    }, Permission.TIPS);
                    return true;
                }
                if (listener != null)
                    listener.onTipsRefundClick();
                return true;
            }
        });

        actionRefundTips.setVisible(isShown && getApp().isTipsEnabled());
        boolean hasTips = BigDecimal.ZERO.compareTo(tipsAmount) < 0;
        actionRefundTips.setEnabled(hasTips && shiftIsOpened && !getApp().getShopInfo().useCreditReceipt);
    }

    public void update(String orderGuid, BigDecimal totalAmount, Date date, String cashierText, String numText, OrderType type, boolean isTipped) {
        this.orderGuid = orderGuid;
        this.totalAmount = totalAmount;
        orderDate = new Date(date.getTime());
        orderRefundAmount = null;

        this.type = type;
        num.setText(numText);
        UiHelper.showPrice(total, totalAmount);
        this.date.setText(DateUtils.formatFull(date));
        cashier.setText(cashierText);
        btnReturn.setEnabled(false);
        btnClose.setEnabled(false);
        getLoaderManager().restartLoader(KITCHEN_PRINTING_LOADER_ID, null, kitchenPrintingLoader);
        if (type == OrderType.PREPAID)
            getLoaderManager().restartLoader(PREPAID_ORDER_LOADER_ID, null, prepaidOrderLoader);
        if (isTipsEnabled) {
            getLoaderManager().restartLoader(PREAUTH_TRANSACTIONS_LOADER_ID, null, preauthTransactionsLoader);
            getLoaderManager().restartLoader(TIPS_LOADER_ID, null, tipsLoader);
        }
        this.isOrderTipped = isTipped;
    }

    public void onHide() {
        isShown = false;
        actionRefundTips.setVisible(false);
    }

    public void onShow() {
        isShown = true;
        actionRefundTips.setVisible(getApp().isTipsEnabled());
    }

    public void updateRefundAmount(BigDecimal amount) {
        orderRefundAmount = amount;

        showPrice(refundAmount, amount);
        showRefund();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    //TODO: use only one way to determine current shift state
    private void setShift(ShiftModel shift) {
        shiftIsOpened = shift != null && shift.endTime == null;

        showRefund();
    }

    private void setPrepaidType(Cursor cursor) {
        if (cursor.moveToFirst()) {
            prepaidType = ContentValuesUtil._prepaidType(cursor, 0);
            isPrepaidFailed = ContentValuesUtil._bool(cursor, 1);
        }
        showRefund();
    }

    private void showRefund(){
        btnReturn.setEnabled(isEligibleForReturn());
        refundAmount.setVisibility(isEligibleForReturn() ? View.VISIBLE : View.GONE);
    }

    private void setTipsAmount(BigDecimal tipsAmount) {
        this.tipsAmount = tipsAmount;

        boolean hasTips = BigDecimal.ZERO.compareTo(tipsAmount) < 0;
        actionRefundTips.setEnabled(hasTips && shiftIsOpened && !getApp().getShopInfo().useCreditReceipt);

        UiHelper.showPrice(subtotal, totalAmount);
        UiHelper.showPrice(tips, tipsAmount);
        UiHelper.showPrice(total, tipsAmount.add(totalAmount));
    }

    private boolean isEligibleForReturn() {
        return (type == OrderType.SALE || prepaidType == PrepaidType.WIRELESS_PIN || isPrepaidFailed) && shiftIsOpened
                && ((orderRefundAmount != null && orderRefundAmount.compareTo(BigDecimal.ZERO) > 0))
                && (orderDate != null);
    }

    @Click
    protected void btnReturnClicked() {
        assert listener != null;

        if (!getApp().isOperatorClockedIn() && getApp().getShopInfo().clockinRequired4Sales){
            try2ClockIn();
            return;
        }

        listener.onReturnClick();
    }

    @Click
    protected void btnReprintClicked() {
        assert listener != null;
        ReprintOrderReceiptDialog.show(getActivity(), orderGuid, listener);
    }

    @Click
    protected void btnEmailClicked() {
        assert listener != null;
        listener.onEmailClick();
    }

    @Click
    protected void btnReprintKitchenClicked() {
        assert listener != null;
        listener.onReprintKitchenClick();
    }

    @Click
    protected void btnCloseClicked() {
        if (!getApp().isOperatorClockedIn() && getApp().getShopInfo().clockinRequired4Sales){
            try2ClockIn();
            return;
        }
        boolean tipsPermitted = getApp().hasPermission(Permission.TIPS);
        if (!tipsPermitted) {
            PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    if (listener != null)
                        listener.onCloseClick(preauthTransactions, isOrderTipped);
                }
            }, Permission.TIPS);
            return;
        }
        if (listener != null)
            listener.onCloseClick(preauthTransactions, isOrderTipped);
    }

    private void try2ClockIn() {
        TimesheetFragment.show(getActivity(), TimesheetFragment.Type.CLOCK_IN, getString(R.string.timesheet_dialog_clarify_message), new TimesheetFragment.OnTimesheetListener() {
            @Override
            public void onCredentialsEntered(String login/*, String password*/) {
                TimesheetFragment.hide(getActivity());
                WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_clock_in));
                ClockInCommand.start(getActivity(), login/*, password*/, new BaseClockInCallback() {
                    @Override
                    protected void onClockIn(String guid, String fullName, Date time) {
                        WaitDialogFragment.hide(getActivity());
                        if (guid.equals(getApp().getOperatorGuid())) {
                            getApp().setOperatorClockedIn(true);
                        }
                        AlertDialogFragment.showComplete(getActivity(), R.string.btn_clock_in,
                                getString(R.string.dashboard_clock_in_msg, fullName, DateUtils.timeOnlyAttendanceFormat(time)));
                    }

                    @Override
                    protected void onClockInError(ClockInCommand.ClockInOutError error) {
                        WaitDialogFragment.hide(getActivity());
                        int messageId = R.string.error_message_timesheet;
                        switch (error) {
                            case ALREADY_CLOCKED_IN:
                                messageId = R.string.error_message_already_clocked_in;
                                break;
                            case USER_DOES_NOT_EXIST:
                                messageId = R.string.error_message_employee_does_not_exist;
                                break;
                            case EMPLOYEE_NOT_ACTIVE:
                                messageId = R.string.error_message_employee_not_active;
                                break;
                        }
                        AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(messageId));
                    }
                });
            }
        });
    }

    public HistoryDetailedOrderItemFragment setListener(HistoryDetailedOrderItemListener listener) {
        this.listener = listener;
        return this;
    }

    private void setPreauthTransactions(ArrayList<PaymentTransactionModel> transactions) {
        this.preauthTransactions = transactions;
        btnClose.setEnabled(
                getApp().isShiftOpened() &&
                        transactions != null &&
                        !transactions.isEmpty()
        );
    }

    private LoaderCallbacks<Optional<ShiftModel>> shiftsLoader = new LoaderCallbacks<Optional<ShiftModel>>() {

        @Override
        public Loader<Optional<ShiftModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(URI_SHIFTS)
                    .where(ShiftTable.GUID + " = ?", getApp().getShiftGuid())
                    .wrap(new Function<Cursor, Optional<ShiftModel>>() {
                        @Override
                        public Optional<ShiftModel> apply(Cursor cursor) {
                            ShiftModel model = null;
                            if (cursor.moveToFirst())
                                model = new ShiftModel(cursor);
                            return Optional.fromNullable(model);
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Optional<ShiftModel>> loader, Optional<ShiftModel> optionalModel) {
            setShift(optionalModel.orNull());
        }

        @Override
        public void onLoaderReset(Loader<Optional<ShiftModel>> loader) {

        }
    };

    private LoaderCallbacks<Cursor> kitchenPrintingLoader = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(PRINTER_ALIAS_URI)
                    .projection(SaleItemTable.SALE_ITEM_GUID)
                    .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                    .where(ItemTable.PRINTER_ALIAS_GUID + " IS NOT NULL")
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            btnReprintKitchen.setEnabled(cursor.getCount() != 0);
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    };

    private LoaderCallbacks<Cursor> prepaidOrderLoader = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(PREPAID_ORDER_URI)
                    .projection(PrepaidOrderView2.BillPaymentDescriptionTable.TYPE, BillPaymentDescriptionTable.IS_FAILED)
                    .where(SaleOrderTable.GUID + " = ?", orderGuid)
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            setPrepaidType(cursor);
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderCallbacks<ArrayList<PaymentTransactionModel>> preauthTransactionsLoader = new LoaderCallbacks<ArrayList<PaymentTransactionModel>>() {

        @Override
        public Loader<ArrayList<PaymentTransactionModel>> onCreateLoader(int id, Bundle args) {
            return ReadPaymentTransactionsFunction.createLoaderOnlyOpenedPreauth(getActivity(), orderGuid);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PaymentTransactionModel>> loader, ArrayList<PaymentTransactionModel> list) {
            setPreauthTransactions(list);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PaymentTransactionModel>> loader) {

        }
    };

    private LoaderCallbacks<BigDecimal> tipsLoader = new LoaderCallbacks<BigDecimal>() {

        @Override
        public Loader<BigDecimal> onCreateLoader(int id, Bundle args) {
            return ReadTipsFunction.createLoader(getActivity(), orderGuid);
        }

        @Override
        public void onLoadFinished(Loader<BigDecimal> loader, BigDecimal tipsAmount) {
            setTipsAmount(tipsAmount);
        }

        @Override
        public void onLoaderReset(Loader<BigDecimal> loader) {

        }
    };

    public interface HistoryDetailedOrderItemListener {

        void onReturnClick();

        void onTipsRefundClick();

        void onReprintClick(boolean printOrder, boolean printRefund);

        void onEmailClick();

        void onReprintKitchenClick();

        void onCloseClick(ArrayList<PaymentTransactionModel> preauthTransactions, boolean isOrderTipped);
    }
}
