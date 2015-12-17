package com.kaching123.tcr.fragment.tendering.history;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.edit.RefundQtyEditFragment;
import com.kaching123.tcr.fragment.tendering.history.CheckBoxHeader.ICheckBoxListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemView.IQtyListener;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.fragment.wireless.UnitsSearchFragment;
import com.kaching123.tcr.fragment.wireless.UnitsSearchHistoryFragment;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.HistoryOrderItemViewModelWrapFunction;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.processor.UnitItemCache;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class HistoryDetailedOrderItemListFragment extends ListFragment implements LoaderCallbacks<List<HistoryDetailedOrderItemModel>>, ICheckBoxListener, IQtyListener {

    @ViewById
    protected CheckBoxHeader header;

    @Bean
    protected HistoryDetailedOrderItemAdapter adapter;


    private Map<String, List<Unit>> scannedUnits = new HashMap<String, List<Unit>>();

    public String guid;

    private OnCheckedChangeListener checkBoxListener;

    private IRefundAmountListener refundAmountListener;

    private boolean firstLoad;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tendering_history_items_list_fragment, container, false);
    }

    @Override
    public void onListItemClick(ListView l, final View v, int position, long id) {
        HistoryDetailedOrderItemModel item = adapter.getItem(position);
        if (item.saleItemModel.isSerializable && !item.wanted && item.availableQty.intValue() > 0) {
            processUnits(item, item.saleItemModel.itemModel.itemGuid, guid, 1, true, new UnitManagerCallback() {
                @Override
                public void onUnitScannedSuccessfully() {
                    ((HistoryDetailedOrderItemView) v).enableCheckboxPolitics();
                }

                @Override
                public void onUnitsExist() {
                    ((HistoryDetailedOrderItemView) v).enableCheckboxPolitics();
                }
            });
            return;
        }
    }

    @AfterViews
    public void afterViews() {
        setListAdapter(adapter);
        adapter.setWatcher(this);
        adapter.setItemListener(this);
        checkBoxListener = new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.setAllChecked(isChecked);
                fireAmountChanged();
            }
        };
        Logger.d("checkChange initVoid");
        header.checkbox.setOnCheckedChangeListener(checkBoxListener);
    }

    public void updateList() {
        getLoaderManager().restartLoader(0, null, this);
//        Logger.d("ECHO update list");
    }

    public void onHide() {
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<List<HistoryDetailedOrderItemModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("HistoryDetailedOrderItemListFragment.onCreateLoader()");
        return HistoryOrderItemViewModelWrapFunction.createHistoryLoader(getActivity(), guid);
    }

    @Override
    public void onLoadFinished(Loader<List<HistoryDetailedOrderItemModel>> listLoader, List<HistoryDetailedOrderItemModel> saleOrderModels) {
        Logger.d("HistoryDetailedOrderItemListFragment.onLoadFinished(): count " + (saleOrderModels == null ? 0 : saleOrderModels.size()));
        adapter.changeCursor(saleOrderModels);
        if (firstLoad)
            header.checkbox.setChecked(true);
        firstLoad = false;
        fireAmountChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<HistoryDetailedOrderItemModel>> listLoader) {
        Logger.d("HistoryDetailedOrderItemListFragment.onLoaderReset()");
        firstLoad = false;
        if (getActivity() == null)
            return;
        adapter.changeCursor(null);
    }

    public void init(String guid) {
        Logger.d("HistoryDetailedOrderItemListFragment.init(): guid = " + guid);
        this.guid = guid;
        if (guid == null) {
            adapter.changeCursor(null);
            if (getActivity() != null) {
                getLoaderManager().destroyLoader(0);
            }
            return;
        }

        firstLoad = true;
        getLoaderManager().restartLoader(0, null, this);
    }



    public void setRefundAmountListener(IRefundAmountListener refundAmountListener) {
        this.refundAmountListener = refundAmountListener;
    }

    public RefundAmount getReturnAmount() {
        BigDecimal pickedValue = BigDecimal.ZERO;
        ArrayList<SaleOrderItemViewModel> orderItems = new ArrayList<SaleOrderItemViewModel>(adapter.getCount());
        ArrayList<RefundSaleItemInfo> refundItems = new ArrayList<RefundSaleItemInfo>(adapter.getCount());

        for (int i = 0; i < adapter.getCount(); i++) {
            HistoryDetailedOrderItemModel historyItem = adapter.getItem(i);
            SaleOrderItemViewModel saleOrderItem = historyItem.saleItemModel;
            orderItems.add(saleOrderItem);
            if (!historyItem.wanted) {
                Logger.d("not wanted");
                continue;
            }
            final RefundSaleItemInfo info = new RefundSaleItemInfo(saleOrderItem.itemModel.saleItemGuid, historyItem.wantedQty);
            refundItems.add(info);
            pickedValue = pickedValue.add(CalculationUtil.getSubTotal(historyItem.wantedQty, historyItem.saleItemModel.finalPrice));
        }

        final RefundAmount refundAmount = new RefundAmount(pickedValue, BigDecimal.ZERO);
        OrderTotalPriceCalculator.calculate(orderItems, null, new Handler() {


            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty, BigDecimal itemPriceWithAddons, BigDecimal itemSubTotal, BigDecimal itemTotal, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {

            }

            @Override
            public void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue) {
                refundAmount.orderValue = totalOrderPrice;
            }

        });
        refundAmount.itemsInfo = refundItems;
        return refundAmount;
    }

    private BigDecimal calcRefundAmount() {
        BigDecimal pickedValue = BigDecimal.ZERO;
        for (int i = 0; i < adapter.getCount(); i++) {
            HistoryDetailedOrderItemModel historyItem = adapter.getItem(i);
            if (!historyItem.wanted) {
                Logger.d("not wanted");
                continue;
            }
            pickedValue = pickedValue.add(CalculationUtil.getSubTotal(historyItem.wantedQty, historyItem.saleItemModel.finalPrice));
        }
        Logger.d("HistoryDetailedOrderItemListFragment.calcRefundAmount(): pickedValue " + pickedValue);
        return pickedValue;
    }

    @Override
    public void onCheckChange(HistoryDetailedOrderItemModel item, BigDecimal qty, boolean check) {
        if (item.saleItemModel.isSerializable) {
            if (check) {
                return;
            }
            List<Unit> units = scannedUnits.get(item.saleItemModel.itemModel.itemGuid);
            if (units != null && units.size() > 0) {
                item.wantedQty = BigDecimal.ZERO;
                item.scannedQty = BigDecimal.ZERO;
                units.clear();
                onCheckChanged(check);
            }
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                if (adapter.getItem(i).saleItemModel.getSaleItemGuid().equals(item.saleItemModel.getSaleItemGuid())) {
                    View v = getListView().getChildAt(i);
                    if (v != null && v instanceof HistoryDetailedOrderItemView) {
                        ((HistoryDetailedOrderItemView) v).disableCheckboxPolitics();
                    }
                    break;
                }
            }
        } else {
            onCheckChanged(check);
        }

    }

    private void onCheckChanged(boolean check) {
        if (!check && header.checkbox.isChecked()) {
            header.checkbox.setOnCheckedChangeListener(null);
            header.checkbox.setChecked(false);
            header.checkbox.setOnCheckedChangeListener(checkBoxListener);
        }
        fireAmountChanged();
    }

    private void processUnits(final HistoryDetailedOrderItemModel model,
                              final String itemId,
                              final String orderId,
                              final int qty,
                              final boolean check,
                              final UnitManagerCallback callback) {
        List<Unit> units = scannedUnits.get(itemId);
        if (units == null) {
            units = new ArrayList<Unit>();
            scannedUnits.put(itemId, units);
        }
        final int qtyScanned = units.size();
        if (qtyScanned == qty) {
            if (callback != null) {
                callback.onUnitsExist();
            }
            onCheckChanged(check);
            return;
        }

        if (qtyScanned > qty) {
            model.wantedQty = BigDecimal.ZERO;
            model.scannedQty = BigDecimal.ZERO;
            units.clear();
        }

        if (!check) {
            return;
        }

        UnitsSearchHistoryFragment.show(getActivity(),
                orderId, itemId, new UnitsSearchFragment.UnitCallback() {
            @Override
            public void handleSuccess(String serialCode, ArrayList<Unit> unitList, ArrayList<SaleOrderViewModel> orderList) {
                final Unit unit = unitList.get(0);
                SaleOrderViewModel order = orderList.get(0);
                final List<Unit> units = scannedUnits.get(itemId);
                for (Unit scannedUnit : units) {
                    if (itemId.equals(scannedUnit.itemId)
                        && orderId.equals(scannedUnit.orderId)
                        && unit.serialCode.equals(scannedUnit.serialCode)) {

                        // already scanned
                        Toast.makeText(getActivity(), "Item was already scanned.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (!itemId.equals(unit.itemId) || !orderId.equals(unit.orderId)) {
                    Toast.makeText(getActivity(), R.string.unit_history_serial_wrong, Toast.LENGTH_LONG).show();
                    return;
                }

                Long warrantyOutdatedDays = checkUnitWarranty(unit, order);
                boolean warrantyOutdated = warrantyOutdatedDays != null;
                if (warrantyOutdated) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.warning_dialog_title,
                            getResources().getQuantityString(R.plurals.warning_message_warranty_outdated, warrantyOutdatedDays.intValue(), warrantyOutdatedDays), R.string.btn_proceed,
                            new OnDialogClickListener() {
                                @Override
                                public boolean onClick() {
                                    if (!TcrApplication.get().hasPermission(Permission.WARRANTY_EXPIRATION_OVERRIDE)) {
                                        PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                                            @Override
                                            public void onLoginComplete() {
                                                super.onLoginComplete();
                                                setUnitChecked(unit, units, model, callback, check);
                                                hide();
                                            }
                                        }, Permission.WARRANTY_EXPIRATION_OVERRIDE);
                                        return true;
                                    }
                                    setUnitChecked(unit, units, model, callback, check);
                                    hide();
                                    return true;
                                }
                            }, null);
                    return;
                }

                setUnitChecked(unit, units, model, callback, check);
                hide();
            }

            @Override
            public void handleError(String serialCode, String message) {
                Toast.makeText(getActivity(), R.string.unit_history_serial_not_found, Toast.LENGTH_LONG).show();
                hide();
            }

            @Override
            public void handleCancel() {

            }

            @Override
            public void handleClear() {

            }

            @Override
            public void handleServerSearch(String serialCode) {

            }

            private void hide() {
                UnitsSearchHistoryFragment.hide(getActivity());
            }
        });
    }

    private Long checkUnitWarranty(Unit unit, SaleOrderViewModel order) {
        Date createTime = order.createTime;
        Date now = new Date();
        long diff = now.getTime() - createTime.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days == unit.warrantyPeriod) {
            Toast.makeText(getActivity(), R.string.warning_message_warranty_ends_today, Toast.LENGTH_LONG).show();
            return null;
        }
        if (days < unit.warrantyPeriod) {
            return null;
        }
        return days - unit.warrantyPeriod;
    }

    private void setUnitChecked(Unit unit, List<Unit> units, HistoryDetailedOrderItemModel model, UnitManagerCallback callback, boolean check) {
        units.add(unit);
        model.wantedQty = new BigDecimal(units.size());
        model.scannedQty = new BigDecimal(units.size());
        adapter.notifyDataSetChanged();
        if (callback != null) {
            callback.onUnitScannedSuccessfully();
        }
        onCheckChanged(check);
    }

    private void fireAmountChanged() {
        Logger.d("HistoryDetailedOrderItemListFragment.fireAmountChanged()");
        if (refundAmountListener == null)
            return;
        refundAmountListener.onRefundAmountChanged(calcRefundAmount());
    }

    @Override
    public void onRefundQtyClicked(View v, final HistoryDetailedOrderItemModel position) {
        RefundQtyEditFragment.show(getActivity(), position, position.saleItemModel.isPcsUnit, new RefundQtyEditFragment.OnRefundResultListener() {

            @Override
            public void onComplete(HistoryDetailedOrderItemModel item, BigDecimal qty) {
                if (!item.saleItemModel.isSerializable) {
                    adapter.notifyDataSetChanged();
                    fireAmountChanged();
                } else {
                    List<Unit> units = scannedUnits.get(item.saleItemModel.itemModel.itemGuid);
                    if (units == null || units.isEmpty() || units.size() < qty.intValue()) {
                        processUnits(item, item.saleItemModel.itemModel.itemGuid, guid, qty.intValue(), true, new UnitManagerCallback() {
                            @Override
                            public void onUnitScannedSuccessfully() {
                                adapter.notifyDataSetChanged();
                                fireAmountChanged();
                            }

                            @Override
                            public void onUnitsExist() {
//                                adapter.notifyDataSetChanged();
//                                fireAmountChanged();
                            }
                        });
                    } else {
                        adapter.notifyDataSetChanged();
                        fireAmountChanged();
                    }
                }

            }
        });
    }

    public void updateUnitsBeforeRefund() {
        UnitItemCache.get().reset();
        for (String key : scannedUnits.keySet()) {
            List<Unit> units = scannedUnits.get(key);
            List<Unit> unitsCopy = new ArrayList<Unit>(units.size());
            for (Unit unit : units) {
                unitsCopy.add(unit);
            }
            UnitItemCache.get().add(key, unitsCopy);
        }
    }

    public static class RefundAmount {
        public BigDecimal pickedValue;
        public BigDecimal orderValue;
        public ArrayList<RefundSaleItemInfo> itemsInfo;
        public boolean refundTips;

        public RefundAmount(BigDecimal pickedValue, BigDecimal orderValue) {
            this.pickedValue = pickedValue;
            this.orderValue = orderValue;
        }

        public RefundAmount(BigDecimal pickedValue, BigDecimal orderValue, ArrayList<RefundSaleItemInfo> itemsInfo) {
            this.pickedValue = pickedValue;
            this.orderValue = orderValue;
            this.itemsInfo = itemsInfo;
        }

        public RefundAmount(BigDecimal pickedValue, BigDecimal orderValue, ArrayList<RefundSaleItemInfo> itemsInfo, boolean refundTips) {
            this.pickedValue = pickedValue;
            this.orderValue = orderValue;
            this.itemsInfo = itemsInfo;
            this.refundTips = refundTips;
        }
    }

    public static interface IRefundAmountListener {
        void onRefundAmountChanged(BigDecimal amount);
    }

    private interface UnitManagerCallback {
        void onUnitScannedSuccessfully();
        void onUnitsExist();
    }
}
