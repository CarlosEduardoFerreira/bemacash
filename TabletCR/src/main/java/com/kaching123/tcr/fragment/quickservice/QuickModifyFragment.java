package com.kaching123.tcr.fragment.quickservice;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Optional;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.CollectModifiersCommand;
import com.kaching123.tcr.component.ModifierContainerView;
import com.kaching123.tcr.fragment.modify.ItemModifiersFragment;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ModifierTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by gdubina on 22.11.13.
 */
@EFragment(R.layout.quickservice_item_modifiers_fragment_bra)
public class QuickModifyFragment extends ItemModifiersFragment {

    private final static int LOADER_ID = 4;
    private final Uri URI_ITEMS = ShopProvider.contentUriGroupBy(ItemExtView.URI_CONTENT, ModifierTable.MODIFIER_GUID);

    private static final String[] PROJECTION = new String[]{
          ItemTable.SALE_PRICE,
          ItemTable.DESCRIPTION,
          ItemTable.DISCOUNT,
          ItemTable.DISCOUNT_TYPE,
          ModifierTable.MODIFIER_GUID,
          ModifierTable.TYPE,
          ModifierTable.EXTRA_COST,
          ModifierTable.ITEM_SUB_QTY,
          ShopSchema2.ItemExtView2.ModifierSubItemTable.SALE_PRICE
  };


    @ViewById
    protected TextView itemTitle;
    @ViewById
    protected TextView itemPriceValue;
    @ViewById
    protected TextView modifiersPriceValue;
    @ViewById
    protected TextView totalPriceValue;

    @ViewById
    protected TextView addonsPriceValue;
    @ViewById
    protected RelativeLayout modifyContainer;

    private OnCancelListener cancelListener;

    private ItemLoader itemLoader = new ItemLoader();

    private Set<String> selectedModifiersGuids = new HashSet<>();
    private Set<String> selectedAddonsGuids = new HashSet<>();


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        modifyContainer.setOnClickListener(null);
        updateInfoPanel(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
    }

    private void restartLoader(){
        if(TextUtils.isEmpty(itemGuid)){
            return;
        }
        getLoaderManager().restartLoader(LOADER_ID, null, itemLoader);
    }


    public void setupParams(String itemGuid, OnAddonsChangedListener onAddonsChangedListener) {
        setupParams(itemGuid, null, onAddonsChangedListener);
    }

    public void setupParams(String itemGuid, String saleItemGuid, OnAddonsChangedListener onAddonsChangedListener) {
        this.itemGuid = itemGuid;
        this.saleItemGuid = saleItemGuid;
        setOnAddonsChangedListener(onAddonsChangedListener);
        updateInfoPanel(null);
        restartLoader();
        collectModifiers();
    }

    @Override
    protected void fillViewWithContainers(Map<String, List<CollectModifiersCommand.SelectedModifierExModel>> groupedItems) {
        super.fillViewWithContainers(groupedItems);
        recollectSelectedItems();
        restartLoader();

        int n = holder.getChildCount();
        for (int i = 0; i < n; i++){
            final ModifierContainerView view = (ModifierContainerView) holder.getChildAt(i);
            if (view.getModifierType() == ModifierType.OPTIONAL) //optionals don't have price
                continue;

            view.setOnChangeListener(new ModifierContainerView.OnChangeListener() {
                @Override
                public void onChanged() {
                    recollectSelectedItems();
                    restartLoader();
                }
            });
        }
    }

    private void recollectSelectedItems() {
        int n = holder.getChildCount();

        selectedModifiersGuids.clear();
        selectedAddonsGuids.clear();

        for (int i = 0; i < n; i++){
            final ModifierContainerView view = (ModifierContainerView) holder.getChildAt(i);
            if (view.getModifierType() == ModifierType.OPTIONAL) //optionals don't have price
                continue;

            if (view.getModifierType() == ModifierType.MODIFIER){
                selectedModifiersGuids.addAll(view.getSelectedItems());
            }else {
                selectedAddonsGuids.addAll(view.getSelectedItems());
            }
        }
    }

    @Click
    protected void btnConfirmClicked() {
        onConfirm();
        getLoaderManager().destroyLoader(LOADER_ID);
    }

    @Click
    protected void btnCancelClicked() {
        getLoaderManager().destroyLoader(LOADER_ID);
        if (cancelListener == null)
            return;
        cancelListener.onFragmentCanceled();
    }

    public void setCancelListener(OnCancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public interface OnCancelListener {
        void onFragmentCanceled();
    }

    private class ItemLoader implements LoaderCallbacks<Optional<ItemInfo>> {

        @Override
        public Loader<Optional<ItemInfo>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(URI_ITEMS)
                    .projection(PROJECTION)
                    .where(ItemTable.GUID + " = ?", itemGuid == null ? "" : itemGuid)
                    .wrap(new ItemWithModifierWrapFunction())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Optional<ItemInfo>> loader, Optional<ItemInfo> o) {
            updateInfoPanel(o.orNull());
        }

        @Override
        public void onLoaderReset(Loader<Optional<ItemInfo>> loader) {
            updateInfoPanel(null);
        }
    }

    private void updateInfoPanel(ItemInfo o) {
        if(itemTitle == null)
            return;
        if(o == null){
            itemTitle.setText("");
            showPrice(itemPriceValue, BigDecimal.ZERO);
            showPrice(modifiersPriceValue, BigDecimal.ZERO);
            showPrice(addonsPriceValue, BigDecimal.ZERO);
            showPrice(totalPriceValue, BigDecimal.ZERO);
            return;
        }

        itemTitle.setText(o.title);
        showPrice(itemPriceValue, o.price);
        showPrice(modifiersPriceValue, o.modifiersPrice);
        showPrice(addonsPriceValue, o.addonsPrice);
        showPrice(totalPriceValue, o.total);
    }

    private static class ItemInfo{
        String title;
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal modifiersPrice = BigDecimal.ZERO;
        BigDecimal addonsPrice = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        BigDecimal discount;
        DiscountType discountType;
    }

    private class ItemWithModifierWrapFunction extends ListConverterFunction<Optional<ItemInfo>> {

        @Override
        public Optional<ItemInfo> apply(Cursor c) {
            super.apply(c);

            ItemInfo info = null;

            while(c.moveToNext()){
                if(info == null){
                    info = new ItemInfo();

                    info.price = _decimal(c.getString(indexHolder.get(ItemTable.SALE_PRICE)));
                    info.title = c.getString(indexHolder.get(ItemTable.DESCRIPTION));

                    info.discount = _decimal(c.getString(indexHolder.get(ItemTable.DISCOUNT)));
                    info.discountType = _discountType(c, indexHolder.get(ItemTable.DISCOUNT_TYPE));

                }

                String modifierGuid = c.getString(indexHolder.get(ModifierTable.MODIFIER_GUID));


                if(TextUtils.isEmpty(modifierGuid)){
                    continue;
                }

                ModifierType modifierType = _modifierType(c, indexHolder.get(ModifierTable.TYPE));

                BigDecimal subItemQty = _decimalQty(c.getString(indexHolder.get(ModifierTable.ITEM_SUB_QTY)));
                BigDecimal subItemCost = _decimal(c.getString(indexHolder.get(ShopSchema2.ItemExtView2.ModifierSubItemTable.SALE_PRICE)));
                BigDecimal modifierCost = _decimal(c.getString(indexHolder.get(ModifierTable.EXTRA_COST)));

                BigDecimal extraCost = subItemQty != null && BigDecimal.ZERO.compareTo(subItemQty) != 0 ? getSubTotal(subItemQty, subItemCost) : modifierCost;

                if(modifierType == ModifierType.MODIFIER && selectedModifiersGuids != null && selectedModifiersGuids.contains(modifierGuid)){
                    info.modifiersPrice = info.modifiersPrice.add(extraCost);
                }else if(modifierType == ModifierType.ADDON && selectedAddonsGuids != null && selectedAddonsGuids.contains(modifierGuid)){
                    info.addonsPrice = info.addonsPrice.add(extraCost);
                }
            }

            if(info != null){
                info.total = getSubTotal(BigDecimal.ONE, info.price.add(info.addonsPrice).add(info.modifiersPrice)/*, info.discount, info.discountType*/);
            }
            return Optional.fromNullable(info);

        }
    }

}
