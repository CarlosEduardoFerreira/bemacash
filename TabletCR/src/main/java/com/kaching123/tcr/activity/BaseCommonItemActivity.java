package com.kaching123.tcr.activity;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.RegisterQtyFormatInputFilter;
import com.kaching123.tcr.component.UnsignedQuantityFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.inventory.ChooseParentItemDialogFragment;
import com.kaching123.tcr.fragment.inventory.InventoryQtyEditDialog;
import com.kaching123.tcr.fragment.inventory.ItemCodeChooserAlertDialogFragment;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.model.ItemCodeType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.Validator;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;


/**
 * Created by vkompaniets on 02.12.13.
 */
@EActivity(R.layout.inventory_item_activity)
public abstract class BaseCommonItemActivity extends BaseItemActivity implements ItemCodeChooserAlertDialogFragment.ItemCodeTypeChooseListener {

    @ViewById
    protected EditText ean;
    @ViewById
    protected EditText productCode;
    @ViewById
    protected EditText minimumQty;
    @ViewById
    protected EditText recommendedQty;
    @ViewById
    protected EditText availableQty;
    @ViewById
    protected TextView availableQtyPencil;
    @ViewById
    protected CheckBox stockTrackingFlag;
    @ViewById
    protected View referenceItemRow;
    @ViewById
    protected EditText referenceItem;
    @Extra
    protected ItemExModel parentItem;
    @Extra
    protected ItemMatrixModel parentItemMatrix;

    protected int count;
    protected int countWithNoRestrickted;

    @OptionsItem
    protected void actionSerialSelected() {
        if (model.isSerializable()) {
            UnitActivity.start(this, model, TAG_RESULT);
            return;
        } else {
            Toast.makeText(this, R.string.cashier_action_serial_disabled, Toast.LENGTH_LONG).show();
        }
    }

    @OptionsItem
    protected void actionComposerSelected() {
        ComposerActivity.start(this, model, TAG_RESULT_COMPOSER);
    }

    @OptionsItem
    protected void actionModifierSelected() {
        ModifierActivity.start(this, model, TAG_RESULT_MODIFIER);
    }

    @Override
    protected void init() {
        super.init();
        referenceItemRow.setVisibility(View.VISIBLE);
        referenceItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseParentItemDialogFragment.show(BaseCommonItemActivity.this, model.guid,
                        new ChooseParentItemDialogFragment.OnItemChosenListener() {
                            @Override
                            public void onItemChosen(ItemExModel parentItem, ItemMatrixModel parentItemMatrix) {
                                referenceItem.setText(parentItem.description);
                                BaseCommonItemActivity.this.parentItem = parentItem;
                                BaseCommonItemActivity.this.parentItemMatrix = parentItemMatrix;
                            }
                        });
            }
        });

        stockTrackingFlag.setEnabled(PlanOptions.isStockTrackingAllowed());
        availableQtyPencil.setEnabled(PlanOptions.isStockTrackingAllowed());
    }

    protected void recollectComposerInfo(){
    }

    @Override
    protected void recalculateSerialization() {
        final Unit.CodeType oldCodeType = model.codeType;
        final Unit.CodeType newCodeType = getTypeForIndex(serializationType);
        final boolean stockTrackEnabled = stockTrackingFlag.isChecked();

        CollectUnitsCommand.start(this, null, model.guid, null, null, null, false, false, new CollectUnitsCommand.UnitCallback() {
            @Override
            protected void handleSuccess(final List<Unit> unit) {

                if (oldCodeType != null && newCodeType == null && !unit.isEmpty()) {
                    AlertDialogWithCancelFragment.showWithTwo(BaseCommonItemActivity.this,
                            R.string.wireless_remove_items_title,
                            getString(R.string.wireless_remove_items_body),
                            R.string.btn_ok,
                            new AlertDialogWithCancelFragment.OnDialogListener() {
                                @Override
                                public boolean onClick() {
                                    model.codeType = newCodeType;
                                    DropUnitsCommand.start(BaseCommonItemActivity.this,
                                            (ArrayList<Unit>) unit, model, new DropUnitsCommand.UnitCallback() {
                                                @Override
                                                protected void handleSuccess(ItemExModel model) {
                                                    Toast.makeText(BaseCommonItemActivity.this, getString(R.string.unit_drop_ok), Toast.LENGTH_LONG).show();
                                                    model.serializable = false;
                                                    onSerializableSet(false);
                                                    if (!stockTrackEnabled) {
                                                        model.availableQty = null;
                                                    }
                                                    BaseCommonItemActivity.this.model = model;
                                                    setQuantities(null);
                                                    invalidateOptionsMenu();
                                                }

                                                @Override
                                                protected void handleError() {
                                                    Toast.makeText(BaseCommonItemActivity.this, getString(R.string.unit_drop_partially_faield), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                    return true;
                                }

                                @Override
                                public boolean onCancel() {
                                    serializationType.setSelection(getIndexForType(oldCodeType));
                                    return true;
                                }
                            }
                    );

                } else if (oldCodeType == null && newCodeType != null) {
                    model.codeType = newCodeType;
                    model.serializable = true;
                    onSerializableSet(true);
                    model.availableQty = stockTrackEnabled ? BigDecimal.ZERO : null;
                    setQuantities(null);
                    invalidateOptionsMenu();
                } else {
                    onSerializableSet((model.codeType = newCodeType) != null);
                    invalidateOptionsMenu();
                }
            }

            @Override
            protected void handleError() {
                Toast.makeText(BaseCommonItemActivity.this, getString(R.string.unit_drop_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setQuantities(BigDecimal qty) {
        if (model == null) {
            return;
        }
        if (isPcs()) {
            if (qty != null) {
                showBrandQtyInteger(availableQty, qty.setScale(0, BigDecimal.ROUND_FLOOR));
            } else if (model.availableQty != null) {
                showBrandQtyInteger(availableQty, model.availableQty.setScale(0, BigDecimal.ROUND_FLOOR));
            }
            if (model.minimumQty != null) {
                showBrandQtyInteger(minimumQty, model.minimumQty.setScale(0, BigDecimal.ROUND_FLOOR));
            }
            if (model.recommendedQty != null) {
                showBrandQtyInteger(recommendedQty, model.recommendedQty.setScale(0, BigDecimal.ROUND_FLOOR));
            }
        } else {
            if (qty != null) {
                showBrandQty(availableQty, qty.setScale(3, BigDecimal.ROUND_FLOOR));
            } else if (model.availableQty != null) {
                showBrandQty(availableQty, model.availableQty.setScale(3, BigDecimal.ROUND_FLOOR));
            }
            showBrandQty(minimumQty, model.minimumQty);
            showBrandQty(recommendedQty, model.recommendedQty);
        }
    }

    @Deprecated
    protected void stockTrackingSetup(Unit.CodeType codeType) {
        final boolean isSerializable = codeType != null;
        if (isSerializable) {
            stockTrackingFlag.setChecked(true);
        }
        stockTrackingFlag.setEnabled(PlanOptions.isStockTrackingAllowed());
    }

    @Override
    protected void setFieldsFilters() {
        InputFilter[] productCodeFilter = new InputFilter[]{new InputFilter.LengthFilter(TcrApplication.PRODUCT_CODE_MAX_LEN), alphanumericFilter};
        productCode.setFilters(productCodeFilter);

        InputFilter inputFilter = isPcs() ? new UnsignedQuantityFormatInputFilter() : new RegisterQtyFormatInputFilter();
        InputFilter.LengthFilter lengthFilter = isPcs() ?
                new InputFilter.LengthFilter(this instanceof AddItemActivity_ ? 8 : 10) : new InputFilter.LengthFilter(10);
        InputFilter[] avaliableQtyFilter = new InputFilter[]{inputFilter, lengthFilter};
        availableQty.setFilters(avaliableQtyFilter);
        minimumQty.setFilters(avaliableQtyFilter);
        recommendedQty.setFilters(avaliableQtyFilter);

        super.setFieldsFilters();
    }

    @Override
    protected void setFieldsChangeListeners() {
        super.setFieldsChangeListeners();
        QtyTextChangeListener availableQtyTextChangeListener = new QtyTextChangeListener(availableQty);
        QtyTextChangeListener minimumQtyTextChangeListener = new QtyTextChangeListener(minimumQty);
        QtyTextChangeListener recommendedQtyTextChangeListener = new QtyTextChangeListener(recommendedQty);

        CheckedChangeListener checkedChangeListener = new CheckedChangeListener();
        taxable.setOnCheckedChangeListener(checkedChangeListener);

        if (this instanceof AddItemActivity_) {
            availableQty.addTextChangedListener(new BrandTextWatcher(availableQty, true));
        }

        minimumQty.addTextChangedListener(minimumQtyTextChangeListener);
        recommendedQty.addTextChangedListener(recommendedQtyTextChangeListener);

        stockTrackingFlag.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changed = true;
                updateStockTrackingBlock(isChecked); // onAdd // onEdit
            }
        });
        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                changed = true;
                if (editable.length() >= TcrApplication.BARCODE_MIN_LEN) {
                    getSupportLoaderManager().restartLoader(UPC_LOADER_ID, null, new UpcLoader());
                }
            }
        });

        productCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                changed = true;
                if (editable.length() >= TcrApplication.BARCODE_MIN_LEN) {
                    getSupportLoaderManager().restartLoader(PRODUCT_CODE_LOADER_ID, null, new ProductCodeLoader());
                }
            }
        });
        setPriceType();

        onPcsCheck(true);
    }

    protected void onPcsCheck(boolean init) {
        if (isPcs()) {
//            availableQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
            availableQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            availableQtyPencil.setInputType(InputType.TYPE_CLASS_NUMBER);
            minimumQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            recommendedQty.setInputType(InputType.TYPE_CLASS_NUMBER);

            availableQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            availableQtyPencil.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            minimumQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
            recommendedQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

            if (init)
                return;

            try {
                if (!TextUtils.isEmpty(availableQty.getText()))
                    showBrandQtyInteger(availableQty, UiHelper.getDecimalValue(availableQty).setScale(0, BigDecimal.ROUND_FLOOR));
                if (!TextUtils.isEmpty(availableQtyPencil.getText()))
                    showBrandQtyInteger(availableQtyPencil, UiHelper.getDecimalValue(availableQtyPencil).setScale(0, BigDecimal.ROUND_FLOOR));
                if (!TextUtils.isEmpty(minimumQty.getText()))
                    showBrandQtyInteger(minimumQty, UiHelper.getDecimalValue(minimumQty).setScale(0, BigDecimal.ROUND_FLOOR));
                if (!TextUtils.isEmpty(recommendedQty.getText()))
                    showBrandQtyInteger(recommendedQty, UiHelper.getDecimalValue(recommendedQty).setScale(0, BigDecimal.ROUND_FLOOR));
            } catch (NumberFormatException ignore) {
            }
        } else {
            availableQty.setInputType(InputType.TYPE_CLASS_PHONE);
            availableQtyPencil.setInputType(InputType.TYPE_CLASS_PHONE);
            minimumQty.setInputType(InputType.TYPE_CLASS_PHONE);
            recommendedQty.setInputType(InputType.TYPE_CLASS_PHONE);
//            recommendedQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER  | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            availableQty.setFilters(new InputFilter[]{new RegisterQtyFormatInputFilter()});
            availableQtyPencil.setFilters(new InputFilter[]{new RegisterQtyFormatInputFilter()});
            minimumQty.setFilters(new InputFilter[]{new RegisterQtyFormatInputFilter()});
            recommendedQty.setFilters(new InputFilter[]{new RegisterQtyFormatInputFilter()});

            if (init)
                return;

            try {
                if (!TextUtils.isEmpty(availableQty.getText()))
                    if (count > 0) {
                        recollectComposerInfo();
                    } else {
                        showBrandQty(availableQty, UiHelper.getDecimalValue(availableQty));
                    }
                if (!TextUtils.isEmpty(availableQtyPencil.getText()))
                    if (count > 0) {
                        recollectComposerInfo();
                    } else {
                        showBrandQty(availableQtyPencil, UiHelper.getDecimalValue(availableQtyPencil));
                    }

                if (!TextUtils.isEmpty(minimumQty.getText()))
                    showBrandQty(minimumQty, UiHelper.getDecimalValue(minimumQty));
                if (!TextUtils.isEmpty(recommendedQty.getText()))
                    showBrandQty(recommendedQty, UiHelper.getDecimalValue(recommendedQty));
            } catch (NumberFormatException ignore) {
            }
        }
    }

    protected void updateStockTrackingBlock(boolean isChecked) {

        View[] views = {availableQty, recommendedQty, minimumQty, availableQty, availableQtyPencil};
        for (View view : views) {
            view.setEnabled(isChecked && PlanOptions.isStockTrackingAllowed());
        }

        if (!isChecked) {
            model.recommendedQty = null;
            model.availableQty = null;
            model.minimumQty = null;

            recommendedQty.setText(null);
            availableQty.setText(null);
            minimumQty.setText(null);
        } else {
            if (count > 0) {
                recollectComposerInfo();
            }
        }
    }

    @Override
    protected boolean validateForm() {
        final String ean = this.ean.getText().toString();
        if (!TextUtils.isEmpty(ean) && !Validator.isEanValid(ean)) {
            Toast.makeText(this, getString(R.string.item_activity_alert_ean_invalid_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        final String productCode = this.productCode.getText().toString();
//        if (TextUtils.isEmpty(productCode)) {
//            Toast.makeText(this, getString(R.string.item_activity_alert_product_code_empty_error), Toast.LENGTH_SHORT).show();
//            return false;
//        }

        if (!TextUtils.isEmpty(productCode) && (productCode.length() < TcrApplication.BARCODE_MIN_LEN || productCode.length() > TcrApplication.PRODUCT_CODE_MAX_LEN)) {
            Toast.makeText(this, getString(R.string.item_activity_alert_product_code_msg,
                    TcrApplication.BARCODE_MIN_LEN, TcrApplication.PRODUCT_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.validateForm();
    }

    @Override
    protected void collectDataToModel(ItemModel model) {
        super.collectDataToModel(model);
        model.eanCode = this.ean.getText().toString();

        model.isTaxable = this.taxable.isChecked();

        model.productCode = this.productCode.getText().toString();

        model.availableQty = UiHelper.getDecimalValue(this.availableQty);
        model.minimumQty = UiHelper.getDecimalValue(this.minimumQty);
        model.recommendedQty = UiHelper.getDecimalValue(this.recommendedQty);

        /*if (isPcs()) {
            model.availableQty = model.availableQty.setScale(0, BigDecimal.ROUND_FLOOR);
            model.minimumQty = model.minimumQty.setScale(0, BigDecimal.ROUND_FLOOR);
            model.recommendedQty = model.recommendedQty.setScale(0, BigDecimal.ROUND_FLOOR);
        }*/

        model.isStockTracking = this.stockTrackingFlag.isChecked();
    }

    @Override
    public void onItemCodeTypeChosen(ItemCodeType codeType, String code) {
        onItemCodeType(codeType, code);
    }

    protected void onItemCodeType(ItemCodeType codeType, String code) {
        if (ItemCodeType.EAN_UPC == codeType) {
            try {
                if (Validator.isEanValid(code)) {
                    ean.setText(code);
                } else {
                    Toast.makeText(this, getString(R.string.item_activity_alert_ean_error), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException badEan) {
                Toast.makeText(this, getString(R.string.item_activity_alert_ean_bad_error, TcrApplication.EAN_UPC_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            }
        } else if (ItemCodeType.PRODUCT_CODE == codeType) {
            if (Validator.isProductCodeValid(code)) {
                productCode.setText(code);
            } else {
                Toast.makeText(this, getString(R.string.item_activity_alert_product_code_error, TcrApplication.PRODUCT_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected OnClickListener updateQtyListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (model.isSerializable()) {
                UnitActivity.start(BaseCommonItemActivity.this, model, TAG_RESULT);
                return;
            }

            InventoryQtyEditDialog.show(BaseCommonItemActivity.this, model.availableQty == null ? BigDecimal.ZERO : model.availableQty, isPcs(),
                    new InventoryQtyEditDialog.OnEditQtyListener() {
                        @Override
                        public void onReplace(BigDecimal value) {
                            model.availableQty = value;
                            if (isPcs()) {
                                showInteger(availableQty, model.availableQty);
                                showInteger(availableQtyPencil, model.availableQty);
                            } else {
                                showQuantity(availableQty, model.availableQty, false);
                                showQuantity(availableQtyPencil, model.availableQty, false);
                            }
                        }

                        @Override
                        public void onAdjust(BigDecimal value) {
                            BigDecimal old = model.availableQty == null ? BigDecimal.ZERO : model.availableQty;
                            model.availableQty = old.add(value);
                            if (isPcs()) {
                                showInteger(availableQty, model.availableQty);
                                showInteger(availableQtyPencil, model.availableQty);
                            } else {
                                showQuantity(availableQty, model.availableQty, false);
                                showQuantity(availableQtyPencil, model.availableQty, false);
                            }
                        }
                    }
            );
        }
    };


    private class UpcLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .projection(ShopStore.ItemTable.GUID)
                    .where(ShopStore.ItemTable.EAN_CODE + " = ?", ean.getText().toString())
                    .where(ShopStore.ItemTable.GUID + " <> ?", model.guid)
                    .build(BaseCommonItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateUpc = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }

    class GetNextProductCodeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return NextProductCodeQuery.getCode(BaseCommonItemActivity.this);
        }

        @Override
        protected void onPostExecute(String code) {
            productCode.setText(code);
        }
    }

    private class ProductCodeLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .projection(ShopStore.ItemTable.GUID)
                    .where(ShopStore.ItemTable.PRODUCT_CODE + " = ?", productCode.getText().toString())
                    .where(ShopStore.ItemTable.GUID + " <> ?", model.guid)
                    .build(BaseCommonItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateProductCode = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}