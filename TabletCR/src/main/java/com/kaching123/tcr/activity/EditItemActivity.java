package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantMatrixItemCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.wireless.UnitsEditFragment;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.composer.RecalculateHostCompositionMetadataCommand;
import com.kaching123.tcr.util.UnitUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;

/**
 * Created by vkompaniets on 02.12.13.
 */
@EActivity(R.layout.inventory_item_activity)
@OptionsMenu(R.menu.items_actions)
public class EditItemActivity extends BaseCommonItemActivity {
    private static final int ITEM_MATRIX_LOADER = 20;
    private static final int ITEM_PARENT_LOADER = 21;

    @InstanceState
    protected boolean hasLoadedComposerInfo;

    protected boolean ignoreMovementupdate;

    @AfterViews
    @Override
    protected void init() {
        hasLoadedComposerInfo = true;
        super.init();

        availableQty.setVisibility(View.GONE);
        availableQtyPencil.setVisibility(View.VISIBLE);
        availableQtyPencil.setOnClickListener(updateQtyListener);
        availableQtyPencil.setFocusable(false);

        fillFields();
        Logger.d(String.format("[%s]%d", model.description, model.orderNum));
        recollectComposerInfo();
    }

    @Override
    protected void updateStockTrackingBlock(boolean isChecked) {
        super.updateStockTrackingBlock(isChecked);
        availableQty.setEnabled(false);
        recollectUnitsInfo();
    }

    private void recollectUnitsInfo() {
        if (model.isSerializable()) {
            CollectUnitsCommand.start(this, null, model.guid, null, null, null, true, false, new CollectUnitsCommand.UnitCallback() {
                @Override
                protected void handleSuccess(List<Unit> unit) {
                    model.availableQty = new BigDecimal(unit.size());
                    final boolean stockTrackEnabled = stockTrackingFlag.isChecked();
                    if (stockTrackEnabled) {
                        setQuantities();
                    }
                }

                @Override
                protected void handleError() {
                    // ignore
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        recollectUnitsInfo();
        recollectComposerInfo();
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

    @Override
    protected void adjustTaxGroup() {
        super.adjustTaxGroup();
        if (!salableChBox.isChecked()) {
            model.taxGroupGuid = null;
            model.taxGroupGuid2 = null;
        }
    }

    @Override
    protected void onTaxLoaded(Cursor cursor) {
        super.onTaxLoaded(cursor);
        final int pos = taxGroupAdapter.getPosition4Id(model.taxGroupGuid);
        taxGroup.setSelection(pos);
    }

    @Override
    protected void callCommand(ItemModel model, final AddItemCallBack callaBck) {
        if (parentItemMatrix == null) {
            if (parentItem != null) {
                model.referenceItemGuid = parentItem.guid;
            }
        } else {
            EditVariantMatrixItemCommand.start(EditItemActivity.this, parentItemMatrix);
            model.referenceItemGuid = null;
        }
        EditItemCommand.start(this, model, new AddItemCommand.AddItemCommandCallback()
        {

            @Override
            protected void handleSuccess() {
                callaBck.success();
        }

            @Override
            protected void handleFailure() {

        }
        });
    }

    @Override
    protected void collectDataToModel(ItemModel model) {
        super.collectDataToModel(model);
        model.ignoreMovementupdate = ignoreMovementupdate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_composer);
        menuItem.setIcon(buildCounterDrawable(countWithNoRestrickted, android.R.drawable.ic_dialog_dialer));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_serial).setVisible(model.isSerializable() && PlanOptions.isSerializableAllowed());
        menu.findItem(R.id.action_composer).setVisible(!model.isSerializable() && !model.isAComposer);
        return true;
    }

    @Override
    protected void recollectComposerInfo() {
        hasLoadedComposerInfo = false;
        RecalculateHostCompositionMetadataCommand.start(this, model.guid, new RecalculateHostCompositionMetadataCommand.ComposerCallback() {

            @Override
            protected void handleSuccess(List<ComposerModel> unit, BigDecimal qty, BigDecimal cost) {
                countWithNoRestrickted = unit.size();
                count = 0;
                invalidateOptionsMenu();
                for (ComposerModel item : unit) {
                    if (item.restricted) {
                        count++;
                    }
                }
                ignoreMovementupdate = count != 0;
                if (countWithNoRestrickted > 0) {
                    self().cost.setEnabled(false);
                    self().cost.setText(UiHelper.valueOf(cost));
                } else {
                    self().cost.setEnabled(true);
                }

//                if (count > 0 || (countWithNoRestrickted > 0 && qty != null)) {
//                    model.availableQty = qty;
//                }
                setQuantities(qty);
                hasLoadedComposerInfo = true;
                //setQtyPencilVisibility();
            }

            @Override
            protected void handleError() {
                count = 0;
                hasLoadedComposerInfo = true;
                //setQtyPencilVisibility();
            }
        });
    }

    @OptionsItem
    protected void actionRemoveSelected() {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.item_activity_hide_item_dialog_title,
                getString(R.string.item_activity_hide_item_dialog_message),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteItemCommand.start(EditItemActivity.this, model.guid);
                        finish();
                        return true;
                    }
                }
        );

    }

    private void fillFields() {

        description.setText(model.description);
        ean.setText(model.eanCode);
        productCode.setText(model.productCode);
        active.setChecked(model.isActiveStatus);
        serializationType.setSelection(getIndexForType(model.codeType));

        //stockTrackingFlag.setChecked(model.isStockTracking);
        showPrice(salesPrice, model.price);
        discountable.setChecked(model.isDiscountable);

        salableChBox.setChecked(model.isSalable);
        enableForSaleParams(model.isSalable);
        taxable.setChecked(model.isTaxable);
        showPrice(cost, model.cost);

        commissionsEligible.setChecked(model.commissionEligible);
        showPrice(commissions, model.commission);

        stockTrackingFlag.setChecked(model.isStockTracking);
        updateStockTrackingBlock(model.isStockTracking);

        if (model.discountType != null) {
            int dType = 0;
            switch (model.discountType) {
                case PERCENT:
                    discountType.setSelection(INDEX_DISCOUNT_PERCENT);
                    dType = INDEX_DISCOUNT_PERCENT;
                    break;
                case VALUE:
                    discountType.setSelection(INDEX_DISCOUNT_VALUE);
                    dType = INDEX_DISCOUNT_VALUE;
                    break;
            }
            discountType.setOnItemSelectedListener(new SpinnerChangeListener(dType));
        }


        showPrice(discount, model.discount);

        buttonView.getBackground().setLevel(model.btnView);

        hasNotes.setChecked(model.hasNotes);

        setFieldsChangeListeners();

        setQuantities(model);

        if (model.referenceItemGuid == null) {
            getSupportLoaderManager().restartLoader(ITEM_MATRIX_LOADER, null, cursorLoaderCallbacks);
        } else {
            getSupportLoaderManager().restartLoader(ITEM_PARENT_LOADER, null, cursorLoaderCallbacks);
        }

        showInteger(this.loyaltyPoints, model.loyaltyPoints);
        useLoyaltyPoints.setChecked(model.excludeFromLoyaltyPlan);
    }

    protected void setQuantities(ItemExModel model) {
        if (UnitUtil.isNotUnitPriceType(model.priceType)) {
            showBrandQtyInteger(availableQty, model.availableQty);
            showBrandQtyInteger(availableQtyPencil, model.availableQty);
            showBrandQtyInteger(minimumQty, model.minimumQty);
            showBrandQtyInteger(recommendedQty, model.recommendedQty);
        } else {
            showBrandQty(availableQty, model.availableQty);
            showBrandQty(availableQtyPencil, model.availableQty);
            showBrandQty(minimumQty, model.minimumQty);
            showBrandQty(recommendedQty, model.recommendedQty);
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case ITEM_MATRIX_LOADER:
                    return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemMatrixByParentView.URI_CONTENT))
                            .where(ShopStore.ItemMatrixByParentView.CHILD_ITEM_GUID + " = ?", model.guid).build(EditItemActivity.this);
                case ITEM_PARENT_LOADER:
                    return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                            .where(ShopStore.ItemTable.GUID + " = ?", model.referenceItemGuid).build(EditItemActivity.this);
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(id));
            }

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case ITEM_MATRIX_LOADER:
                    if (data.moveToFirst()) {
                        referenceItem.setText(data.getString(data.getColumnIndex(ShopStore.ItemMatrixByParentView.ITEM_DESCRIPTION)));
                    }
                    break;
                case ITEM_PARENT_LOADER:
                    if (data.moveToFirst()) {
                        referenceItem.setText(data.getString(data.getColumnIndex(ShopStore.ItemTable.DESCRIPTION)));
                    }
                default:

            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    protected void setQuantities() {
        boolean isPcs = UnitUtil.isPcs(model.priceType);
        showQuantity(availableQty, model.availableQty, isPcs);
        showQuantity(availableQtyPencil, model.availableQty, isPcs);
        showQuantity(minimumQty, model.minimumQty, isPcs);
        showQuantity(recommendedQty, model.recommendedQty, isPcs);
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

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        Logger.d("EditItemActivity onReceive:" + barcode);
        onBarcodeReceived(barcode);
    }

    protected void setQtyPencilVisibility() {
        if (hasLoadedComposerInfo) {
            //monitoringQntEditRow.setVisibility(model.codeType == null && count == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private boolean redirectBarcodeResult = false;

    @Override
    public void onBarcodeReceived(String barcode) {
        if (redirectBarcodeResult) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(0);
            assert fragment != null;
            UnitsEditFragment editFragment = (UnitsEditFragment) fragment;
            editFragment.onBarcodeReceived(barcode);
        } else {
//            ItemCodeChooserAlertDialogFragment.show(EditItemActivity.this, barcode);
            super.onBarcodeReceived(barcode);
        }
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

    @Override
    protected EditItemActivity self() {
        return EditItemActivity.this;
    }

    public static void start(Context context, ItemExModel model) {
        EditItemActivity_.intent(context).model(model).start();
    }
}
