package com.kaching123.tcr.fragment.saleorder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.commands.store.saleorder.ApplyMultipleDiscountCommand;
import com.kaching123.tcr.commands.store.saleorder.CheckIsItemComposerCommand;
import com.kaching123.tcr.commands.store.saleorder.CompositionItemsCalculationCommand;
import com.kaching123.tcr.commands.store.saleorder.DiscountSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.DiscountSaleOrderItemCommand.BaseDiscountSaleOrderItemCallback;
import com.kaching123.tcr.commands.store.saleorder.ItemsNegativeStockTrackingCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdatePriceSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdatePriceSaleOrderItemCommand.BaseUpdatePriceSaleOrderItemCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateQtySaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateQtySaleOrderItemCommand.BaseUpdateQtySaleOrderItemCallback;
import com.kaching123.tcr.component.CarlHighlightItemView;
import com.kaching123.tcr.fragment.KitchenPrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.ComposerOverrideQtyDialog;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.edit.NotesEditFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment;
import com.kaching123.tcr.fragment.edit.PriceEditFragment.OnEditPriceListener;
import com.kaching123.tcr.fragment.edit.QtyEditFragment;
import com.kaching123.tcr.fragment.edit.QtyEditFragment.OnEditQtyListener;
import com.kaching123.tcr.fragment.edit.SaleItemDiscountEditFragment;
import com.kaching123.tcr.fragment.edit.SaleItemDiscountEditFragment.OnEditSaleItemDiscountListener;
import com.kaching123.tcr.fragment.saleorder.ItemsAdapter.HighlightedColumn.Type;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.BarcodeListenerHolder;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.UnitUtil;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.EXTRA_ALIAS_TITLE;
import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.EXTRA_PRINTER;

@EFragment
public class OrderItemListFragment extends ListFragment implements LoaderCallbacks<List<SaleOrderItemViewModel>>, BarcodeListenerHolder.BarcodeListener {

    protected String orderGuid;

    protected ItemsAdapter adapter;

    private IItemsListHandlerHandler itemsListHandler;

    private boolean need2ScrollList = false;

    private boolean firstLoad;

    private boolean isCreateReturnOrder;

    private HashMap<String, BigDecimal> qtyBefore = new HashMap<>();
    private boolean checkIsNewItemComposerInProcess;
    private boolean qtyChanged;
    private boolean newItem;
    private SaleOrderItemViewModel qtyChangedItem;
    private SaleOrderItemViewModel newItemInOrder;
    private boolean ignorReculc;
    private TcrApplication app;
    @ViewById
    protected EditText usbScannerInput;

    private static final Uri ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private int position;

    private boolean isReturn = false;

    @AfterTextChange
    protected void usbScannerInputAfterTextChanged(Editable s) {
        String newline = System.getProperty("line.separator");
        boolean hasNewline = s.toString().contains(newline);
        if (hasNewline) {
            Logger.d("OrderItemListFragment usbScannerInputAfterTextChanged hasNewline: " + s.toString());
            String result = s.toString().replace("\n", "").replace("\r", "");
            itemsListHandler.onBarcodeReceivedFromUSB(result);
            s.clear();
        }
    }

    public SaleOrderItemViewModel getLastItem() {
        if (adapter == null || adapter.getCount() == 0)
            return null;

        return adapter.getItem(adapter.getCount() - 1);
    }

    public void setCreateReturnOrder(boolean isCreateReturnOrder) {
        this.isCreateReturnOrder = isCreateReturnOrder;
    }

    public boolean hasKitchenItems() {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).isKitchenPrintable)
                return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.saleorder_items_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        app = TcrApplication.get();
        if(app.getQtyBefore() == null || app.getQtyBefore().isEmpty()){
            app.setQtyBefore(qtyBefore);
        } else {
            qtyBefore = app.getQtyBefore();
        }
        adapter = !isCreateReturnOrder ? new ItemsAdapter(getActivity()) : new ReturnItemsAdapter(getActivity());
        isReturn = true;

        adapter.setItemRemoveListener(new ItemView.OnItemRemoveClick() {
            @Override
            public void onRemoveClicked(View v, final int pos) {
                //adapter.itemRemoved = true;
                SaleOrderItemViewModel orderItemForRemove = adapter.getItem(pos);
                if (orderItemForRemove.isLocked()) {
                    return;
                }
                orderItemForRemove.setLocked(true);

                position = pos;
                isVoidNeedPermission();

                BigDecimal subValue = app.getOrderItemsQty().get(orderItemForRemove.itemModel.itemGuid);
                subValue = subValue != null ? subValue.subtract(orderItemForRemove.getQty()) : subValue;

                if(subValue!=null) {
                    if (subValue.compareTo(BigDecimal.ZERO) == 0) {
                        app.getOrderItemsQty().remove(orderItemForRemove.itemModel.itemGuid);
                    } else {
                        app.getOrderItemsQty().put(orderItemForRemove.itemModel.itemGuid, subValue);
                    }
                }

                if(orderItemForRemove.hasModifiers()){
                    ItemsNegativeStockTrackingCommand.start(getActivity(),orderItemForRemove.itemModel.itemGuid, orderItemForRemove.getQty(), orderItemForRemove.modifiers, ItemsNegativeStockTrackingCommand.ItemType.REMOVE);
                }
            }

            @Override
            public void onCancelClicked(View v, int pos) {
                getListView().closeOpenedItems();
            }

            @Override
            public void onQtyClicked(final View v, final int pos) {
                final SaleOrderItemViewModel model = adapter.getItem(pos);
                if (model.isPrepaidItem || model.isGiftCard)
                    return;
                if (model.itemModel.discountBundleId != null){
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_qty_multiple_discount, Toast.LENGTH_LONG).show();
                    return;
                }
                if (!model.isSerializable) {
                    final String saleItemGuid = adapter.getSaleItemGuid(pos);
                    if (model.itemModel.priceType == PriceType.UNIT_PRICE) {
                        if (getOperatorPermissions().contains(Permission.CHANGE_QTY)) {
                            QtyEditFragment.show(getActivity(), saleItemGuid, adapter.getItemQty(pos), adapter.isPcsUnit(pos), new OnEditQtyListener() {
                                @Override
                                public void onConfirm(final BigDecimal value) {
                                    checkAvailableQty(model, value, saleItemGuid);
                                }
                            });
                        } else {
                            PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                                @Override
                                public void onLoginComplete() {
                                    super.onLoginComplete();
                                    QtyEditFragment.show((FragmentActivity) getActivity(), saleItemGuid, adapter.getItemQty(pos), adapter.isPcsUnit(pos), new OnEditQtyListener() {
                                        @Override
                                        public void onConfirm(final BigDecimal value) {
                                            checkAvailableQty(model, value, saleItemGuid);
                                        }
                                    });
                                }
                            }, Permission.CHANGE_QTY);
                        }
                    } else {
                        QtyEditFragment.show(getActivity(), saleItemGuid, adapter.getItemQty(pos), adapter.isPcsUnit(pos), new OnEditQtyListener() {
                            @Override
                            public void onConfirm(final BigDecimal value) {
                                checkAvailableQty(model, value, saleItemGuid);
                            }
                        });
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_qty, Toast.LENGTH_LONG).show();
                }
            }

            private void checkAvailableQty(SaleOrderItemViewModel model, final BigDecimal value, final String saleItemGuid){
                ItemsNegativeStockTrackingCommand.start(getActivity(), orderGuid, model.itemModel.itemGuid, model.itemModel.qty,
                        value, model.modifiers, ItemsNegativeStockTrackingCommand.ItemType.CHANGE_QTY,
                        new ItemsNegativeStockTrackingCommand.NegativeStockTrackingCallback() {
                            @Override
                            protected void handleSuccess(boolean result) {
                                if(!result){
                                    Toast.makeText(getActivity(), R.string.item_qty_lower_zero, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                highlightedColumn(saleItemGuid, Type.QTY);
                                UpdateQtySaleOrderItemCommand.start(getActivity(), saleItemGuid, value, updateQtySaleOrderItemCallback);
                            }
                        });
            }

            @Override
            public void onPriceClicked(View v, int pos) {
                final String saleItemGuid = adapter.getSaleItemGuid(pos);
                final BigDecimal itemDiscount = adapter.getItemDiscount(pos);
                if (itemDiscount.equals(BigDecimal.ZERO)) {
                    PriceEditFragment.show(getActivity(), saleItemGuid, adapter.getItemPrice(pos), new OnEditPriceListener() {
                        @Override
                        public void onConfirm(BigDecimal value) {
                            highlightedColumn(saleItemGuid, Type.PRICE);

                            UpdatePriceSaleOrderItemCommand.start(getActivity(), saleItemGuid, value, updatePriceSaleOrderItemCallback);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_price, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDiscountClicked(final View v, final int pos) {
                boolean saleOrderDiscountPermitted = ((SuperBaseActivity) getActivity()).getApp().hasPermission(Permission.SALES_DISCOUNTS);
                if (!saleOrderDiscountPermitted) {
                    PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                        @Override
                        public void onLoginComplete() {
                            super.onLoginComplete();
                            onDiscountClicked(v, pos);
                        }
                    }, Permission.SALES_DISCOUNTS);
                    return;
                }

                if (adapter.getItem(pos).itemModel.discountBundleId != null){
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_discount_multiple_discount, Toast.LENGTH_LONG).show();
                    return;
                }

                final String saleItemGuid = adapter.getSaleItemGuid(pos);
                final boolean isOrderDiscounted = ((BaseCashierActivity) getActivity()).isOrderDiscounted();
                if (!isOrderDiscounted) {
                    SaleItemDiscountEditFragment.show(getActivity(), saleItemGuid, adapter.getItemPrice(pos), adapter.getItemDiscount(pos), adapter.getItemDiscountType(pos), new OnEditSaleItemDiscountListener() {
                        @Override
                        public void onConfirm(BigDecimal value, DiscountType type) {
                            highlightedColumn(saleItemGuid, Type.DISCOUNT);

                            DiscountSaleOrderItemCommand.start(getActivity(), saleItemGuid, value, type, null, discountSaleOrderItemCallback);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_discount, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onTitleClicked(View v, int pos) {
                handleOnTitleClick(pos);
            }

            @Override
            public void onNotesClicked(View v, int pos) {
                final String saleItemGuid = adapter.getSaleItemGuid(pos);
                final String saleItemNotes = adapter.getSaleItemNotes(pos);
                NotesEditFragment.show(getActivity(), saleItemNotes, saleItemGuid);
            }
        });
        setListAdapter(adapter);
    }

    private void showChangeQtyFragment() {
    }

    private boolean isVoidNeedPermission() {
        Cursor c = ProviderAction.query(ORDER_URI)
                .projection(
                        ShopStore.SaleOrderTable.GUID,
                        ShopStore.SaleOrderTable.STATUS,
                        ShopStore.SaleOrderTable.KITCHEN_PRINT_STATUS
                )
                .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid == null ? "" : orderGuid)
                .perform(getActivity());
        SaleOrderPrintInfo saleOrderPrintInfo = null;
        if (c.moveToFirst()) {
            saleOrderPrintInfo = new SaleOrderPrintInfo(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            );
        }
        if (saleOrderPrintInfo!=null && saleOrderPrintInfo.kitchenPrintStatus != OrderStatus.COMPLETED.name() && saleOrderPrintInfo.Guid != null)
            if ((saleOrderPrintInfo.kitchenPrintStatus == PrintItemsForKitchenCommand.KitchenPrintStatus.PRINTED.name()) || getOperatorPermissions().contains(Permission.VOID_SALES))
                doRemoceClickLine();
            else {
                PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                    @Override
                    public void onLoginComplete() {
                        super.onLoginComplete();
                        doRemoceClickLine();
                    }
                }, Permission.VOID_SALES);
                adapter.getItem(position).setLocked(false);
            }
        c.close();

        return false;
    }

    class SaleOrderPrintInfo {
        String Guid;
        String status;
        String kitchenPrintStatus;

        public SaleOrderPrintInfo(String Guid, String status, String kitchenPrintStatus) {
            this.Guid = Guid;
            this.status = status;
            this.kitchenPrintStatus = kitchenPrintStatus;
        }
    }


    private void showErrorVoidMessage() {
        AlertDialogFragment.showAlert(getActivity(),
                R.string.dlg_void_title,
                getString(R.string.dlg_void_forbidden_msg),
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        return true;
                    }
                }
        );
    }

    public void cleanAll(){
        checkIsNewItemComposerInProcess= false;
        qtyChanged = false;
        newItem = false;
        ignorReculc = false;
        qtyChangedItem = null;
        newItemInOrder = null;
        qtyBefore.clear();
        app.clearIgnorComposerItems();
        app.setSalesOnScreenTmpSize(0);
        app.clearQtyBefore();
    }

    private void doRemoceClickLine() {
        ignorReculc = true;
        getListView().closeOpenedItems();
        itemsListHandler.onTotolQtyUpdated(getRemoveQty(adapter.getSaleItemGuid(position)), true, null);
        if (adapter.getCount() == 1) {
            adapter.getItem(position).setLocked(false);
            cleanAll();
            if (itemsListHandler != null) {
                itemsListHandler.onRemoveLastItem();
            }
            return;
        }

        RemoveSaleOrderItemCommand.start(getActivity(), adapter.getSaleItemGuid(position), RemoveSaleOrderItemCommand.ActionType.REMOVE, OrderItemListFragment.this);

    }

    private static final Uri SALE_ITEM_ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleItemTable.URI_CONTENT);

    private String getRemoveQty(String saleItemGuid) {
        BigDecimal saleItemAmount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(SALE_ITEM_ORDER_URI)
                .where(ShopStore.SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemGuid)
                .perform(getActivity());
        BigDecimal itemQty = BigDecimal.ZERO;
        if (c.moveToFirst()) {

            itemQty = ContentValuesUtil._decimal(c.getString(c.getColumnIndex(ShopStore.SaleItemTable.QUANTITY)), BigDecimal.ZERO);
            saleItemAmount = saleItemAmount.add(itemQty);

            c.close();
        }
        return saleItemAmount.toString();
    }


    public void doRemoceClickLine(String guid) {
        ignorReculc = true;
        getListView().closeOpenedItems();
        if (adapter.getCount() == 1) {
            cleanAll();
            if (itemsListHandler != null) {
                itemsListHandler.onRemoveLastItem();
            }
            return;
        }

        RemoveSaleOrderItemCommand.start(getActivity(), guid, RemoveSaleOrderItemCommand.ActionType.REMOVE, OrderItemListFragment.this);
    }



    private void highlightedColumn(String saleItemGuid, Type type) {
        adapter.highlightedColumn(saleItemGuid, type);
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, ItemsAdapter.DEFAULT_ANIMATION_TIME + 100);
    }



    @OnSuccess(RemoveSaleOrderItemCommand.class)
    public void onItemRemovedCallback() {
        getListView().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, 350);

        if (getDisplayBinder() != null)
            getDisplayBinder().startCommand(new DisplayWelcomeMessageCommand());
    }

    @OnFailure(RemoveSaleOrderItemCommand.class)
    public void onItemRemovedFailCallback(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterCommand.PrinterError printerError,
                                          @Param(EXTRA_PRINTER) String fromPrinter,
                                          @Param(EXTRA_ALIAS_TITLE) String aliasTitle) {

        adapter.getItem(position).setLocked(false);

        KitchenPrintCallbackHelper.IKitchenPrintCallback callback = new KitchenPrintCallbackHelper.IKitchenPrintCallback() {
            @Override
            public void onRetry(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                RemoveSaleOrderItemCommand.start(getActivity(), adapter.getSaleItemGuid(position), RemoveSaleOrderItemCommand.ActionType.REMOVE, OrderItemListFragment.this);
            }

            @Override
            public void onSkip(String fromPrinter, boolean ignorePaperEnd, boolean searchByMac) {
                RemoveSaleOrderItemCommand.start(getActivity(), adapter.getSaleItemGuid(position), RemoveSaleOrderItemCommand.ActionType.REMOVE, OrderItemListFragment.this, true);
            }
        };

        if (printerError != null && printerError == PrinterCommand.PrinterError.DISCONNECTED) {
            KitchenPrintCallbackHelper.onPrinterDisconnected(getActivity(), fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.IP_NOT_FOUND) {
            KitchenPrintCallbackHelper.onPrinterIPnotfound(getActivity(), fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.NOT_CONFIGURED) {
            KitchenPrintCallbackHelper.onPrinterNotConfigured(getActivity(), fromPrinter, aliasTitle, callback);
            return;
        }
        if (printerError != null && printerError == PrinterCommand.PrinterError.PAPER_IS_NEAR_END) {
            KitchenPrintCallbackHelper.onPrinterPaperNearTheEnd(getActivity(), fromPrinter, aliasTitle, callback);
            return;
        }
        KitchenPrintCallbackHelper.onPrintError(getActivity(), printerError, fromPrinter, aliasTitle, callback);
    }


    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    private void handleOnTitleClick(int pos) {
        SaleOrderItemViewModel item = adapter.getItem(pos);
        if (!item.hasModifiers()) {
            return;
        }
        SaleOrderModel saleOrder = ((OrderDelivery)getActivity()).getOrder();
        if (item.isKitchenPrintable && saleOrder != null && saleOrder.orderStatus == OrderStatus.HOLDON) {
            Toast.makeText(getContext(), R.string.kitchen_printable_modifier_cant_be_changed, Toast.LENGTH_LONG).show();
            return;
        }

        if (itemsListHandler != null) {
            itemsListHandler.onEditItemModifiers(
                    item.itemModel.saleItemGuid,
                    item.itemModel.itemGuid
            );
        }

    }

    public void setItemsListHandler(IItemsListHandlerHandler itemsListHandler) {
        this.itemsListHandler = itemsListHandler;
    }

    public void setIgnorRecalc(boolean ignorRecaulc){
        this.ignorReculc = ignorReculc;
    }

    @Override
    public SwipeListView getListView() {
        return (SwipeListView) super.getListView();
    }

    private void checkAddedItemsOnComposerEnoughQty(final List<SaleOrderItemViewModel> list){
        CompositionItemsCalculationCommand.start(getContext(), orderGuid, new CompositionItemsCalculationCommand.CompositionItemsCalculationCommandCallback() {
            @Override
            protected void onSuccess(HashMap<String, List<CompositionItemsCalculationCommand.CantSaleComposerModel>> itemsCantBeSold) {
                if (itemsCantBeSold != null && !itemsCantBeSold.isEmpty()) {
                    ComposerOverrideQtyDialog.show(getActivity(), itemsCantBeSold, list, getCompositionDialogButtonListener(list));
                } else {
                    refreshOldQty(list);
                    checkIsNewItemComposerInProcess = false;
                }
            }
        });
    }
    private void checkIsNewItemComposer(final List<SaleOrderItemViewModel> list, boolean checkQty){
        qtyChanged = false;
        String guidToCheck = null;
        if(!checkIsNewItemComposerInProcess) {
            checkIsNewItemComposerInProcess = true;
            if (checkQty) {
                newItem = false;
                for (SaleOrderItemViewModel saleOrderItemViewModel : list) {
                    BigDecimal qty = qtyBefore.get(saleOrderItemViewModel.getSaleItemGuid());
                    if (qty != null && saleOrderItemViewModel.itemModel.qty.compareTo(qty) != 0) {
                        guidToCheck = saleOrderItemViewModel.itemModel.itemGuid;
                        qtyChangedItem = saleOrderItemViewModel;
                        qtyChanged = true;
                        break;
                    }
                }
                if(guidToCheck == null){                                                            // if return on screen or discount
                    checkIsNewItemComposerInProcess = false;
                    return;
                }
            } else {
                newItem = true;
                long maxSequence = 0;
                for (SaleOrderItemViewModel saleOrderItemViewModel : list) {
                    if (saleOrderItemViewModel.itemModel.sequence > maxSequence) {
                        maxSequence = saleOrderItemViewModel.itemModel.sequence;
                        guidToCheck = saleOrderItemViewModel.itemModel.itemGuid;
                        newItemInOrder = saleOrderItemViewModel;
                    }
                }
            }
            adapter.carlHighlightDoIt = checkIsNewItemComposerInProcess;
            CheckIsItemComposerCommand.start(getContext(), guidToCheck, new CheckIsItemComposerCommand.IsItemComposerCommandCallback() {
                @Override
                protected void onSuccess(boolean isItemComposer) {
                    if (isItemComposer) {
                        checkAddedItemsOnComposerEnoughQty(list);
                    } else {
                        refreshOldQty(list);
                        checkIsNewItemComposerInProcess = false;
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), getString(R.string.composer_processing_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private ComposerOverrideQtyDialog.DialogButtonListener getCompositionDialogButtonListener(final List<SaleOrderItemViewModel> list){
        return new ComposerOverrideQtyDialog.DialogButtonListener() {
            @Override
            public void onCancelButtonPress(Set<String> listItems) {
                HashSet<String> saleOrderItemViewGuids = new HashSet<>();

                if(newItem){
                    saleOrderItemViewGuids.add(newItemInOrder.getSaleItemGuid());
                    qtyChanged = false;
                } else if(qtyChanged) {
                    saleOrderItemViewGuids.add(qtyChangedItem.getSaleItemGuid());
                } else {
                    for (SaleOrderItemViewModel saleOrderItemViewModel : list) {
                        if (listItems.contains(saleOrderItemViewModel.itemModel.itemGuid)) {
                            saleOrderItemViewGuids.add(saleOrderItemViewModel.getSaleItemGuid());
                        }
                    }
                }

                ignorReculc = true;
                checkIsNewItemComposerInProcess = false;

                if (!saleOrderItemViewGuids.isEmpty()) {
                    for (SaleOrderItemViewModel saleOrderItemViewModel : list) {
                        if (saleOrderItemViewGuids.contains(saleOrderItemViewModel.getSaleItemGuid())) {
                            BigDecimal qty = qtyBefore.get(saleOrderItemViewModel.getSaleItemGuid());
                            if (qtyChanged) {
                                UpdateQtySaleOrderItemCommand.start(getActivity(),
                                        saleOrderItemViewModel.getSaleItemGuid(), qty != null ? qty : BigDecimal.ONE, updateQtySaleOrderItemCallback);
                                qtyChanged = false;
                            } else if(qty != null && qty.compareTo(saleOrderItemViewModel.itemModel.qty) == -1){
                                UpdateQtySaleOrderItemCommand.start(getActivity(),
                                        saleOrderItemViewModel.getSaleItemGuid(), qty, updateQtySaleOrderItemCallback);
                                qtyChanged = false;
                            } else {
                                doRemoceClickLine(saleOrderItemViewModel.getSaleItemGuid());
                                qtyBefore.remove(saleOrderItemViewModel.getSaleItemGuid());
                            }
                        }
                    }
                }
            }

            @Override
            public void onOverrideButtonPress(Set<String> listItems) {
                TcrApplication.get().addIgnorComposerItem(listItems);
                refreshOldQty(list);
                qtyChanged = false;
                checkIsNewItemComposerInProcess = false;
            }
        };
    }

    private void refreshOldQty(final List<SaleOrderItemViewModel> list){
        if(!list.isEmpty()) {
            qtyBefore.clear();
            for (SaleOrderItemViewModel saleOrderItemViewModel : list) {
                qtyBefore.put(saleOrderItemViewModel.getSaleItemGuid(), saleOrderItemViewModel.itemModel.qty);
            }
        }
    }

    @Override
    public Loader<List<SaleOrderItemViewModel>> onCreateLoader(int loaderId, Bundle args) {
        return SaleOrderItemViewModelWrapFunction.createLoader(getActivity(), orderGuid);
    }

    @Override
    public void onLoadFinished(Loader<List<SaleOrderItemViewModel>> loader, final List<SaleOrderItemViewModel> list) {

        adapter.carlHighlightDoIt = false;

        boolean dontAnimate = false;
        if(!list.isEmpty()) {
            if (!ignorReculc) {
                if ((list.size() - app.getSalesOnScreenTmpSize()) == 1) {
                    checkIsNewItemComposer(list, false);
                } else if (list.size() == app.getSalesOnScreenTmpSize()) {           //chg qty or discount or come back
                    checkIsNewItemComposer(list, true);
                    if((!qtyChanged && firstLoad) || (!qtyChanged && !need2ScrollList)) {
                        dontAnimate = true;
                    }
                } else {                                                           //new order, hold on
                    dontAnimate = true;
                    newItem = false;
                    checkAddedItemsOnComposerEnoughQty(list);
                }
            } else {
                ignorReculc = false;
            }
        }
        app.setSalesOnScreenTmpSize(list.size());

        itemsListHandler.onTotolQtyUpdated(getCount(list), false, null);
        //Collections.sort(list, SaleOrderItemViewModel.filterOrderItem); // do not sort - BEMA-1149 Register screen in ABC order
        adapter.changeCursor(list);

        if (need2ScrollList) {
            getListView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    needScrollToTheEnd();
                }
            }, 150);
        }
        need2ScrollList = false;

        if (firstLoad) {
            firstLoad = false;
            if (itemsListHandler != null)
                itemsListHandler.onOrderLoaded(getLastItem());
        }


        // CarlHighlightItemView
        if(dontAnimate)
            return;
        if (getListView().getCount() > 0 && !isReturn){
            if(adapter.carlHighlightItemView == null) {
                CarlHighlightItemView carlHighlightItemView = new CarlHighlightItemView(getView(), adapter, getLastItem().getSaleItemGuid());
                adapter.carlHighlightItemView = carlHighlightItemView;
            }else{
                    adapter.carlHighlightDoIt = checkIsNewItemComposerInProcess;
                    if(adapter.highlightedColumn == null && adapter.carlHighlightDoIt) {
                        adapter.carlHighlightItemView.saleItemGuid = getLastItem().getSaleItemGuid();
                    }
            }
        }else{
            isReturn = false;
        }

    }

    private String getCount(List<SaleOrderItemViewModel> list) {
        BigDecimal count = BigDecimal.ZERO;
        for (SaleOrderItemViewModel model : list) {
            if(model.unitsLabel !=null &&!UnitUtil.isUnitLbs(model.unitsLabel))
                count = count.add(model.itemModel.qty);
            else
                count = count.add(BigDecimal.ONE);
        }
        return count.toString();
    }

    @Override
    public void onLoaderReset(Loader<List<SaleOrderItemViewModel>> c) {
        if (getActivity() == null)
            return;
        adapter.changeCursor(null);
    }

    public void setOrderGuid(String orderGuid) {
        this.orderGuid = orderGuid;
        if (orderGuid == null) {
            adapter.changeCursor(null);
            if (getActivity() != null) {
                getLoaderManager().destroyLoader(0);
            }
            return;
        }
        //getListView().closeOpenedItems();
        firstLoad = true;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void setNeed2ScrollList(boolean need2ScrollList) {
        this.need2ScrollList = need2ScrollList;
    }

    private BaseUpdateQtySaleOrderItemCallback updateQtySaleOrderItemCallback = new BaseUpdateQtySaleOrderItemCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            if (getDisplayBinder() != null)
                getDisplayBinder().startCommand(new DisplaySaleItemCommand(saleItemGuid));
            Log.d("BemaCarl15", "OrderItemListFragment 799");
            ApplyMultipleDiscountCommand.start(getActivity(), orderGuid, null);
        }
    };

    private BaseUpdatePriceSaleOrderItemCallback updatePriceSaleOrderItemCallback = new BaseUpdatePriceSaleOrderItemCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            if (getDisplayBinder() != null)
                getDisplayBinder().startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    private BaseDiscountSaleOrderItemCallback discountSaleOrderItemCallback = new BaseDiscountSaleOrderItemCallback() {

        @Override
        protected void onSuccess(String saleItemGuid) {
            if (getDisplayBinder() != null)
                getDisplayBinder().startCommand(new DisplaySaleItemCommand(saleItemGuid));
        }
    };

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    private class ReturnItemsAdapter extends ItemsAdapter {

        public ReturnItemsAdapter(Context context) {
            super(context);
        }

        @Override
        protected ItemView instantiateView() {
            return ReturnItemView_.build(getContext());
        }
    }

    public static interface IItemsListHandlerHandler {

        //void onBarcodeSearched(ItemExModel item, String barcode);

        void onEditItemModifiers(String saleItemGuid,
                                 String itemGuid);/*
                                 int modifiersCount,
                                 int addonsCount,
                                 int optionalsCount,
                                 String selectedModifierGuid,
                                 ArrayList<String> selectedAddonsGuids,
                                 ArrayList<String> selectedOptionalsGuids);*/

        void onRemoveLastItem();

        void onOrderLoaded(SaleOrderItemViewModel lastItem);

        void onBarcodeReceivedFromUSB(String barcode);

        void onTotolQtyUpdated(String qty, boolean remove,List<SaleOrderItemViewModel> list);
    }

    private void needScrollToTheEnd() {
        getListView().setSelection(adapter.getCount() - 1);
    }

    private Set<Permission> getOperatorPermissions() {
        return ((TcrApplication) getActivity().getApplication()).getOperatorPermissions();
    }

    public interface OrderDelivery {
        SaleOrderModel getOrder();
    }

}
