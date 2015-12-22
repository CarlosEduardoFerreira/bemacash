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
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.BaseAddonContainerView.OnChangeListener;
import com.kaching123.tcr.fragment.modify.BaseItemModifiersFragment;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ModifierTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;

/**
 * Created by gdubina on 22.11.13.
 */
@EFragment(R.layout.quickservice_item_modifiers_fragment)
public class QuickModifyFragment extends BaseItemModifiersFragment {

    private final static int LOADER_ID = 4;
    private final Uri URI_ITEMS = ShopProvider.getContentUri(ItemExtView.URI_CONTENT);


    private static final String[] PROJECTION = new String[]{
            ItemTable.SALE_PRICE,
            ItemTable.DESCRIPTION,
            ItemTable.DISCOUNT,
            ItemTable.DISCOUNT_TYPE,
            ModifierTable.MODIFIER_GUID,
            ModifierTable.TYPE,
            ModifierTable.EXTRA_COST};

    @ViewById
    protected TextView itemTitle;
    @ViewById
    protected TextView itemPriceValue;
    @ViewById
    protected TextView itemPriceLabel;
    @ViewById
    protected TextView totalPriceValue;
    @ViewById
    protected TextView addonsPriceValue;
    @ViewById
    protected RelativeLayout modifyContainer;

    private OnCancelListener cancelListener;

    private ItemLoader itemLoader = new ItemLoader();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        modifyContainer.setOnClickListener(null);
        modifiers.setOnChangeListener(new OnChangeListener() {
            @Override
            public void onChanged() {
                selectedModifierGuid = new ArrayList<>(modifiers.getSelectedItems());
                restartLoader();
            }
        });
        addons.setOnChangeListener(new OnChangeListener() {
            @Override
            public void onChanged() {
                selectedAddonsGuids = new ArrayList<>(addons.getSelectedItems());
                restartLoader();
            }
        });

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


    public void setupParams(String itemGuid, int modifiersCount, int addonsCount, int optionalsCount, ArrayList<String>  defaultModifierGuid, OnAddonsChangedListener onAddonsChangedListener) {
        setupParams(itemGuid, modifiersCount, addonsCount, optionalsCount, defaultModifierGuid, null, null);
        setOnAddonsChangedListener(onAddonsChangedListener);
        updateInfoPanel(null);
        restartLoader();
    }

    public void setupParams(String itemGuid, int modifiersCount, int addonsCount, int optionalsCount,
                            ArrayList<String> selectedModifierGuid, ArrayList<String> selectedAddonsGuids, ArrayList<String> selectedOptionalsGuids,
                            OnAddonsChangedListener onAddonsChangedListener) {
        setupParams(itemGuid, modifiersCount, addonsCount, optionalsCount,
                selectedModifierGuid, selectedAddonsGuids, selectedOptionalsGuids);
        setOnAddonsChangedListener(onAddonsChangedListener);
        updateInfoPanel(null);
        restartLoader();
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

    public static interface OnCancelListener {
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
            itemPriceLabel.setText(getString(R.string.qs_info_panel_label_tmpl, ""));
            showPrice(itemPriceValue, BigDecimal.ZERO);
            showPrice(addonsPriceValue, BigDecimal.ZERO);
            showPrice(totalPriceValue, BigDecimal.ZERO);
            return;
        }

        itemTitle.setText(o.title);
        itemPriceLabel.setText(getString(R.string.qs_info_panel_label_tmpl, o.title));
        showPrice(itemPriceValue, o.price);
        showPrice(addonsPriceValue, o.addonsPrice);
        showPrice(totalPriceValue, o.total);
    }

    private static class ItemInfo{
        String title;
        BigDecimal price = BigDecimal.ZERO;
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
                BigDecimal extraCost = _decimal(c.getString(indexHolder.get(ModifierTable.EXTRA_COST)));

                if(modifierType == ModifierType.MODIFIER && selectedModifierGuid != null && selectedModifierGuid.equals(modifierGuid)){
                    info.price = info.price.add(extraCost);
                }else if(modifierType == ModifierType.ADDON && selectedAddonsGuids != null && selectedAddonsGuids.contains(modifierGuid)){
                    info.addonsPrice = info.addonsPrice.add(extraCost);
                }
            }

            if(info != null){
                info.total = CalculationUtil.getSubTotal(BigDecimal.ONE, info.price.add(info.addonsPrice)/*, info.discount, info.discountType*/);
            }
            return Optional.fromNullable(info);
        }
    }

}
