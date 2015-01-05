package com.kaching123.tcr.fragment.saleorder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.commands.display.DisplaySaleItemCommand;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.commands.store.saleorder.DiscountSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.DiscountSaleOrderItemCommand.BaseDiscountSaleOrderItemCallback;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdatePriceSaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdatePriceSaleOrderItemCommand.BaseUpdatePriceSaleOrderItemCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateQtySaleOrderItemCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateQtySaleOrderItemCommand.BaseUpdateQtySaleOrderItemCallback;
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
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EFragment
public class OrderItemListFragment extends ListFragment implements LoaderCallbacks<List<SaleOrderItemViewModel>>, BarcodeListenerHolder.BarcodeListener {

    protected String orderGuid;

    protected ItemsAdapter adapter;

    private IItemsListHandlerHandler itemsListHandler;

    private boolean need2ScrollList = false;

    private boolean firstLoad;

    private boolean isCreateReturnOrder;

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

        adapter = !isCreateReturnOrder ? new ItemsAdapter(getActivity()) : new ReturnItemsAdapter(getActivity());

        adapter.setItemRemoveListener(new ItemView.OnItemRemoveClick() {
            @Override
            public void onRemoveClicked(View v, final int pos) {
                getListView().closeOpenedItems();

                if (adapter.getCount() == 1) {
                    if (itemsListHandler != null) {
                        itemsListHandler.onRemoveLastItem();
                    }
                    return;
                }

                RemoveSaleOrderItemCommand.start(getActivity(), adapter.getSaleItemGuid(pos), OrderItemListFragment.this);
            }

            @Override
            public void onCancelClicked(View v, int pos) {
                getListView().closeOpenedItems();
            }

            @Override
            public void onQtyClicked(final View v, final int pos) {
                final SaleOrderItemViewModel model = adapter.getItem(pos);
                if (!model.isSerializable) {
                    final String saleItemGuid = adapter.getSaleItemGuid(pos);
                    QtyEditFragment.show(getActivity(), saleItemGuid, adapter.getItemQty(pos), adapter.isPcsUnit(pos), new OnEditQtyListener() {
                        @Override
                        public void onConfirm(BigDecimal value) {
                            highlightedColumn(saleItemGuid, Type.QTY);

                            UpdateQtySaleOrderItemCommand.start(getActivity(), saleItemGuid, value, updateQtySaleOrderItemCallback);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.cashier_msg_error_changing_qty, Toast.LENGTH_LONG).show();
                }
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

                final String saleItemGuid = adapter.getSaleItemGuid(pos);
                final boolean isOrderDiscounted = ((BaseCashierActivity) getActivity()).isOrderDiscounted();
                if (!isOrderDiscounted) {
                    SaleItemDiscountEditFragment.show(getActivity(), saleItemGuid, adapter.getItemPrice(pos), adapter.getItemDiscount(pos), adapter.getItemDiscountType(pos), new OnEditSaleItemDiscountListener() {
                        @Override
                        public void onConfirm(BigDecimal value, DiscountType type) {
                            highlightedColumn(saleItemGuid, Type.DISCOUNT);

                            DiscountSaleOrderItemCommand.start(getActivity(), saleItemGuid, value, type, discountSaleOrderItemCallback);
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

    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    private void handleOnTitleClick(int pos) {
        SaleOrderItemViewModel item = adapter.getItem(pos);
        int modifiersCount = item.modifiersCount;
        int addonsCount = item.addonsCount;
        int optionalsCount = item.optionalsCount;
        boolean hasModifiers = modifiersCount > 0 || addonsCount > 0 || optionalsCount > 0;
        if (!hasModifiers) {
            return;
        }

        String selectedModifierGuid = null;
        if (item.getModifier() != null) {
            selectedModifierGuid = item.getModifier().addon.addonGuid;
        }

        ArrayList<String> selectedAddonsGuids = new ArrayList<String>();
        ArrayList<String> selectedOptionalsGuids = new ArrayList<String>();
        if (item.getAddons() != null) {
            for (SaleOrderItemViewModel.AddonInfo addonInfo : item.getAddons()) {
                if (addonInfo.addon.type == ModifierType.ADDON) {
                    selectedAddonsGuids.add(addonInfo.addon.addonGuid);
                } else {
                    selectedOptionalsGuids.add(addonInfo.addon.addonGuid);
                }
            }
        }

        if (itemsListHandler != null) {
            //getListView().setItemChecked(pos, true);
            itemsListHandler.onEditItemModifiers(
                    item.itemModel.saleItemGuid,
                    item.itemModel.itemGuid,
                    modifiersCount,
                    addonsCount,
                    optionalsCount,
                    selectedModifierGuid,
                    selectedAddonsGuids,
                    selectedOptionalsGuids);
        }

    }

    public void setItemsListHandler(IItemsListHandlerHandler itemsListHandler) {
        this.itemsListHandler = itemsListHandler;
    }

    @Override
    public SwipeListView getListView() {
        return (SwipeListView) super.getListView();
    }

    @Override
    public Loader<List<SaleOrderItemViewModel>> onCreateLoader(int loaderId, Bundle args) {
        return SaleOrderItemViewModelWrapFunction.createLoader(getActivity(), orderGuid);
    }

    @Override
    public void onLoadFinished(Loader<List<SaleOrderItemViewModel>> loader, List<SaleOrderItemViewModel> list) {
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

        void onEditItemModifiers(String saleItemGuid, String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, String selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids);

        void onRemoveLastItem();

        void onOrderLoaded(SaleOrderItemViewModel lastItem);
    }

    private void needScrollToTheEnd() {
        getListView().setSelection(adapter.getCount() - 1);
    }

}
