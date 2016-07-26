package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditReferenceItemCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantSubItemsCountView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by aakimov on 27/04/15.
 */

@EActivity(R.layout.inventory_reference_item_activity)
@OptionsMenu(R.menu.items_actions)
public class EditReferenceItemActivity extends BaseReferenceItemActivity {

    private static final Uri URI_VARIANT_SUB_ITEMS_COUNT = ShopProvider.contentUri(VariantSubItemsCountView.URI_CONTENT);
    private final static int LOADER_TAG = 0x00000100;

    @ViewById
    protected ListView variantsList;
    protected VariantsAdapter variantsAdapter;

    @AfterViews
    @Override
    protected void init() {
        super.init();
        fillFields();
        variantsList.setAdapter(variantsAdapter = new VariantsAdapter(this));
        getSupportLoaderManager().restartLoader(LOADER_TAG, Bundle.EMPTY, variantsLoaderCallbacks);

    }

    @Override
    protected void callCommand(ItemModel model) {
        EditReferenceItemCommand.start(this, model);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_serial).setVisible(false);
        menu.findItem(R.id.action_composer).setVisible(false);
        modifier.setVisible(false);
        return true;
    }

    private void fillFields() {

        description.setText(model.description);

        active.setChecked(model.isActiveStatus);
        serializationType.setSelection(getIndexForType(model.codeType));

        //stockTrackingFlag.setChecked(model.isStockTracking);
        showPrice(salesPrice, model.price);
        discountable.setChecked(model.isDiscountable);
//        taxable.setChecked(model.isTaxable);
        showPrice(cost, model.cost);

        commissionsEligible.setChecked(model.commissionEligible);
        showPrice(commissions, model.commission);

        if (model.discountType != null) {
            int dType = 0;
            switch (model.discountType) {
                case PERCENT:
                    discountType.setSelection(INDEX_DISCOUNT_PERCENT);
                    break;
                case VALUE:
                    discountType.setSelection(INDEX_DISCOUNT_VALUE);
                    break;
            }
        }


        showPrice(discount, model.discount);

        buttonView.getBackground().setLevel(model.btnView);

        hasNotes.setChecked(model.hasNotes);

        showInteger(this.loyaltyPoints, model.loyaltyPoints);
        useLoyaltyPoints.setChecked(model.useLoyaltyPopints);

        setFieldsChangeListeners();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        super.onLoadFinished(cursorLoader, cursor);
        switch (cursorLoader.getId()) {
            case DEPARTMENT_LOADER_ID:
                final int depPos = departmentAdapter.getPosition4Id(model.departmentGuid);
                department.setSelection(depPos);
                break;
            case CATEGORY_LOADER_ID:
                final int catPos = categoryAdapter.getPosition4Id(model.categoryId);
                category.setSelection(catPos);
                category.setOnItemSelectedListener(new SpinnerChangeListener(catPos));
                break;
        }
    }

    @OptionsItem
    protected void actionRemoveSelected() {
        AlertDialogFragment.show(
                this,
                AlertDialogFragment.DialogType.CONFIRM_NONE,
                R.string.item_activity_hide_item_dialog_title,
                getString(R.string.item_activity_hide_item_dialog_message),
                R.string.btn_confirm,
                new StyledDialogFragment.OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteItemCommand.start(EditReferenceItemActivity.this, model.guid);
                        finish();
                        return true;
                    }
                }
        );

    }

    @Click
    protected void variantsChangeClicked() {
        VariantsActivity.start(this, model);
    }

    @Override
    protected void setPriceType() {
        int pType = 0;
        switch (model.priceType) {
            case FIXED:
                priceType.setSelection(INDEX_FIXED_PRICE);
                pType = INDEX_FIXED_PRICE;
                break;
            case OPEN:
                priceType.setSelection(INDEX_OPEN_PRICE);
                pType = INDEX_OPEN_PRICE;
                break;
            case UNIT_PRICE:
                priceType.setSelection(INDEX_UNIT_PRICE);
                pType = INDEX_UNIT_PRICE;
                break;
        }
        priceType.setOnItemSelectedListener(new SpinnerPriceTypeChangeListener(pType));
    }

    private class SpinnerPriceTypeChangeListener extends SpinnerChangeListener {

        SpinnerPriceTypeChangeListener(int position) {
            super(position);
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            super.onItemSelected(adapterView, view, i, l);
            onPcsCheck();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }


    private final LoaderManager.LoaderCallbacks<Cursor> variantsLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
            switch (loaderId) {
                case LOADER_TAG:
                    return CursorLoaderBuilder.forUri(URI_VARIANT_SUB_ITEMS_COUNT)
                            .where("", model.guid, TcrApplication.get().getShopId())
                            .build(EditReferenceItemActivity.this);
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loaderId));
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_TAG:
                    variantsAdapter.swapCursor(cursor);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case LOADER_TAG:
                    variantsAdapter.swapCursor(null);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(loader.getId()));
            }
        }
    };

    private static class VariantsAdapter extends ResourceCursorAdapter {
        private int varItemNameIdx, varSubItemCountIdx;

        public VariantsAdapter(Context context) {
            super(context, R.layout.variant_item, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            if (cursor != null) {
                varItemNameIdx = cursor.getColumnIndex(ShopStore.VariantSubItemsCountView.VARIANT_ITEM_NAME);
                varSubItemCountIdx = cursor.getColumnIndex(ShopStore.VariantsView.VARIANT_SUB_ITEMS_COUNT);
            }
            View view = super.newView(context, cursor, parent);
            view.setTag(new VariantItemHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            VariantItemHolder variantItemHolder = (VariantItemHolder) view.getTag();
            variantItemHolder.name.setText(cursor.getString(varItemNameIdx));
            variantItemHolder.count.setText(cursor.getString(varSubItemCountIdx));
        }

        private static class VariantItemHolder {
            TextView name;
            TextView count;

            VariantItemHolder(View v) {
                name = (TextView) v.findViewById(android.R.id.text1);
                count = (TextView) v.findViewById(android.R.id.text2);
            }
        }
    }

    public static void start(Context context, ItemExModel item) {
        EditReferenceItemActivity_.intent(context).model(item).start();
    }
}
