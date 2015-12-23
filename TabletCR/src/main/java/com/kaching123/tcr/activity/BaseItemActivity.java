package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.PrinterAliasActivity.PrinterAliasConverter;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.adapter.SpinnerAdapter;
import com.kaching123.tcr.adapter.UnitsLabelAdapter;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand.UnitCallback;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.QuantityFormatInputFilter;
import com.kaching123.tcr.component.SignedQuantityFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog.OnEditListener;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment.IButtonViewDialogListener;
import com.kaching123.tcr.fragment.inventory.InventoryQtyEditDialog;
import com.kaching123.tcr.fragment.inventory.ItemCodeChooserAlertDialogFragment;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo.ViewType;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemCodeType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.UnitLabelModelFactory;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.UnitUtil;
import com.kaching123.tcr.util.Validator;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showIntegralInteger;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 02.12.13.
 */
@EActivity(R.layout.inventory_item_activity)
public abstract class BaseItemActivity extends ScannerBaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, ItemCodeChooserAlertDialogFragment.ItemCodeTypeChooseListener {

    private static final int REQ_MODIFIER = 1;
    private static final String[] UNITS_LABEL = {"PCS", "LB", "OZ"};

    private static final Uri MODIFIER_URI = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    @OptionsMenuItem(R.id.action_composer)
    protected MenuItem composer;

    @OptionsMenuItem(R.id.action_modifier)
    protected MenuItem modifier;

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    protected boolean changed = false;

    protected static int INDEX_FIXED_PRICE = 0;
    protected static int INDEX_OPEN_PRICE = 1;
    protected static int INDEX_UNIT_PRICE = 2;

    protected static int INDEX_DISCOUNT_PERCENT = 0;
    protected static int INDEX_DISCOUNT_VALUE = 1;

    protected static final long BACK_TIMEOUT = 2000L;

    protected static final Uri DEPARTMENT_URI = ShopProvider.getContentUri(DepartmentTable.URI_CONTENT);
    protected static final Uri CATEGORY_URI = ShopProvider.getContentUri(CategoryTable.URI_CONTENT);
    protected static final Uri TAX_GROUP_URI = ShopProvider.getContentUri(TaxGroupTable.URI_CONTENT);
    protected static final Uri PRINTER_ALIAS_URI = ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT);
    protected static final Uri UNIT_LABEL_URI = ShopProvider.contentUri(ShopStore.UnitLabelTable.URI_CONTENT);

    protected static final int DEPARTMENT_LOADER_ID = 0;
    protected static final int CATEGORY_LOADER_ID = 1;
    private static final int MODIFIERS_LOADER = 2;
    protected static final int TAX_GROUP_LOADER_ID = 3;
    protected static final int UPC_LOADER_ID = 4;
    protected static final int PRINTER_ALIAS_LOADER_ID = 5;
    protected static final int PRODUCT_CODE_LOADER_ID = 6;
    protected static final int UNITS_LABEL_LOADER = 7;

    @ViewById
    protected TableRow serializationHolder;
    @ViewById
    protected EditText description;
    @ViewById
    protected Spinner department;
    @ViewById
    protected Spinner category;
    @ViewById
    protected Spinner taxGroup;
    @ViewById
    protected EditText ean;
    @ViewById
    protected EditText productCode;
    @ViewById
    protected Spinner unitsLabel;
    @ViewById
    protected Button unitsButton;
    @ViewById
    protected Spinner priceType;
    @ViewById
    protected Spinner serializationType;
    @ViewById
    protected CheckBox active;
    @ViewById
    protected Spinner printerAlias;
    @ViewById
    protected TextView modifiers;
    @ViewById
    protected TextView modifiersLabel;
    @ViewById
    protected TextView addons;
    @ViewById
    protected TextView addonsLabel;
    @ViewById
    protected TextView optionals;
    @ViewById
    protected TextView optionalsLabel;
    @ViewById
    protected EditText minimumQty;
    @ViewById
    protected EditText recommendedQty;
    @ViewById
    protected EditText availableQty;
    @ViewById
    protected CheckBox stockTrackingFlag;
    @ViewById
    protected EditText salesPrice;
    @ViewById
    protected CheckBox salableChBox;
    @ViewById
    protected CheckBox discountable;
    @ViewById
    protected Spinner discountType;
    @ViewById
    protected EditText discount;
    @ViewById
    protected CheckBox taxable;
    @ViewById
    protected EditText cost;
    @ViewById
    protected ViewGroup buttonViewBlock;
    @ViewById
    protected View buttonView;
    @ViewById
    protected CheckBox hasNotes;
    @ViewById
    protected TableLayout modifiersTable;
    @ViewById
    protected TextView availableQtyPencil;
    @ViewById
    protected View availableQtyBlock;
    @ViewById
    protected View commissionsEligibleContainer;
    @ViewById
    protected View commissionsContainer;
    @ViewById
    protected CheckBox commissionsEligible;
    @ViewById
    protected EditText commissions;

    protected DepartmentSpinnerAdapter departmentAdapter;
    protected CategorySpinnerAdapter categoryAdapter;
    protected TaxGroupSpinnerAdapter taxGroupAdapter;
    protected PrinterAliasAdapter printerAliasAdapter;

    protected long lastBackPressedTime;

    protected int countWithNoRestrickted;

    @Extra
    protected ItemExModel model;

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    private boolean duplicateUpc;

    protected boolean duplicateProductCode;

    protected static final int TAG_RESULT = 12;
    protected static final int TAG_RESULT_COMPOSER = 13;
    protected static final int TAG_RESULT_MODIFIER = 14;

    protected UnitsLabelAdapter unitsLabelAdapter;

    protected int count;

    @OptionsItem
    protected void actionSerialSelected() {
        if (model.isSerializable()) {
            UnitActivity.start(this, model, TAG_RESULT);
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
        if (!PlanOptions.isModifiersAllowed()) {
            AlertDialogFragment.showAlert(BaseItemActivity.this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            ModifierActivity.start(BaseItemActivity.this, model, TAG_RESULT_MODIFIER);
        }

    }

    @OnActivityResult(TAG_RESULT)
    protected void onResult(Intent data) {
        if (data == null) {
            return;
        }
        model = (ItemExModel) data.getSerializableExtra(UnitActivity.RESULT_OK);
    }

    protected void init() {

        setFieldsFilters();

        departmentAdapter = new DepartmentSpinnerAdapter(this);
        department.setAdapter(departmentAdapter);
        department.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                model.departmentGuid = departmentAdapter.getGuid(position);
                getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, BaseItemActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        categoryAdapter = new CategorySpinnerAdapter(this);
        category.setAdapter(categoryAdapter);

        taxGroupAdapter = new TaxGroupSpinnerAdapter(this);
        taxGroup.setAdapter(taxGroupAdapter);

        printerAliasAdapter = new PrinterAliasAdapter(this);
        printerAlias.setAdapter(printerAliasAdapter);

        ArrayAdapter<PriceTypeHolder> priceTypeAdapter = new ArrayAdapter<PriceTypeHolder>(this, R.layout.spinner_item_light, new PriceTypeHolder[]{
                new PriceTypeHolder("Fixed", PriceType.FIXED),
                new PriceTypeHolder("Open", PriceType.OPEN),
                new PriceTypeHolder("Unit Price", PriceType.UNIT_PRICE),
        });
        priceTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        priceType.setAdapter(priceTypeAdapter);

        //*************************************************************************
        ArrayAdapter<SerializationTypeHolder> unitTypeAdapter =
                new ArrayAdapter<SerializationTypeHolder>(this,
                        R.layout.spinner_item_light,
                        new SerializationTypeHolder[]
                                {
                                        new SerializationTypeHolder(null),
                                        new SerializationTypeHolder(CodeType.SN),
                                        new SerializationTypeHolder(CodeType.IMEI),
                                        new SerializationTypeHolder(CodeType.ICCID),
                                }
                );
        unitTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        serializationType.setAdapter(unitTypeAdapter);
//        serializationType.setVisibility( ViewType.WIRELESS.toString().equals( getApp().getShopPref().shopViewType().get()) ? View.VISIBLE : View.GONE );
        serializationHolder.setVisibility(PlanOptions.isSerializableAllowed() ? View.VISIBLE : View.GONE);
        serializationType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recalculateSerialization();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //*************************************************************************


        ArrayAdapter<DiscountTypeHolder> discountTypeAdapter = new ArrayAdapter<DiscountTypeHolder>(this, R.layout.spinner_item_light, new DiscountTypeHolder[]{
                new DiscountTypeHolder("Percent", DiscountType.PERCENT),
                new DiscountTypeHolder("Value", DiscountType.VALUE),
        });
        discountTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        discountType.setAdapter(discountTypeAdapter);

        modifiersTable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PlanOptions.isModifiersAllowed()) {
                    AlertDialogFragment.showAlert(BaseItemActivity.this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                } else {
                    ModifierActivity.start(BaseItemActivity.this, model, TAG_RESULT_MODIFIER);
                }
            }
        });

        getSupportLoaderManager().restartLoader(MODIFIERS_LOADER, null, new ModifierModelLoader());

        unitsLabelAdapter = new UnitsLabelAdapter(this);
        unitsLabel.setAdapter(unitsLabelAdapter);


        buttonViewBlock.setVisibility(getApp().getShopInfo().viewType == ViewType.QUICK_SERVICE ? View.VISIBLE : View.GONE);
        buttonView.getBackground().setLevel(0);

        commissionsEligibleContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
        commissionsContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);

        getSupportLoaderManager().initLoader(DEPARTMENT_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(TAX_GROUP_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(PRINTER_ALIAS_LOADER_ID, null, new PrinterAliasLoader());
        getSupportLoaderManager().initLoader(UNITS_LABEL_LOADER, null, new UnitsLabelLoader());

        fillModifierLabels(0, 0, 0);

        stockTrackingFlag.setEnabled(PlanOptions.isStockTrackingAllowed());

        availableQtyPencil.setEnabled(PlanOptions.isStockTrackingAllowed());
    }

    protected void setQuantities() {

    }

    @Override
    public void onItemCodeTypeChosen(ItemCodeType codeType, String code) {
        onItemCodeType(codeType, code);
    }

    protected void onItemCodeType(ItemCodeType codeType, String code) {
        if (ItemCodeType.EAN_UPC == codeType) {
            if (Validator.isEanValid(code)) {
                ean.setText(code);
            } else {
                Toast.makeText(this, getString(R.string.item_activity_alert_ean_error, TcrApplication.EAN_UPC_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            }
        } else if (ItemCodeType.PRODUCT_CODE == codeType) {
            if (Validator.isProductCodeValid(code)) {
                productCode.setText(code);
            } else {
                Toast.makeText(this, getString(R.string.item_activity_alert_product_code_error, TcrApplication.PRODUCT_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void recalculateSerialization() {
        final CodeType oldCodeType = model.codeType;
        final CodeType newCodeType = getTypeForIndex(serializationType);
        final boolean stockTrackEnabled = stockTrackingFlag.isChecked();

        stockTrackingSetup(newCodeType);

        CollectUnitsCommand.start(this, null, model.guid, null, null, null, false, false, new CollectUnitsCommand.UnitCallback() {
            @Override
            protected void handleSuccess(final List<Unit> unit) {

                if (oldCodeType != null && newCodeType == null && !unit.isEmpty()) {
                    AlertDialogWithCancelFragment.showWithTwo(BaseItemActivity.this,
                            R.string.wireless_remove_items_title,
                            getString(R.string.wireless_remove_items_body),
                            R.string.btn_ok,
                            new AlertDialogWithCancelFragment.OnDialogListener() {
                                @Override
                                public boolean onClick() {
                                    model.codeType = newCodeType;
                                    DropUnitsCommand.start(BaseItemActivity.this, (ArrayList<Unit>) unit, model, new UnitCallback() {
                                        @Override
                                        protected void handleSuccess(ItemExModel model) {
                                            Toast.makeText(BaseItemActivity.this, getString(R.string.unit_drop_ok), Toast.LENGTH_LONG).show();
                                            model.serializable = false;
                                            onSerializableSet(false);
                                            if (!stockTrackEnabled) {
                                                model.availableQty = null;
                                            }
                                            BaseItemActivity.this.model = model;
                                            setQuantities();
                                            invalidateOptionsMenu();
                                        }

                                        @Override
                                        protected void handleError() {
                                            Toast.makeText(BaseItemActivity.this, getString(R.string.unit_drop_partially_faield), Toast.LENGTH_LONG).show();
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
                    setQuantities();
                    invalidateOptionsMenu();
                }
            }

            @Override
            protected void handleError() {
                Toast.makeText(BaseItemActivity.this, getString(R.string.unit_drop_failed), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void stockTrackingSetup(CodeType codeType) {
        final boolean isSerializable = codeType != null;
        if (isSerializable) {
            stockTrackingFlag.setChecked(true);
        }
        stockTrackingFlag.setEnabled(!isSerializable);
    }

    protected void onSerializableSet(boolean isSerializable) {

    }

    private CodeType getTypeForIndex(Spinner position) {
        return ((SerializationTypeHolder) position.getSelectedItem()).type;
    }

    protected int getIndexForType(CodeType type) {
        if (type == null) return 0;
        switch (type) {
            case SN:
                return 1;
            case IMEI:
                return 2;
            case ICCID:
                return 3;

            default:
                return 0;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case DEPARTMENT_LOADER_ID:
                return CursorLoaderBuilder.forUri(DEPARTMENT_URI)
                        .projection(new String[]{DepartmentTable.ID, DepartmentTable.TITLE, DepartmentTable.GUID})
                        .build(this);
            case CATEGORY_LOADER_ID:
                return CursorLoaderBuilder.forUri(CATEGORY_URI)
                        .projection(new String[]{CategoryTable.ID, CategoryTable.TITLE, CategoryTable.GUID})
                        .where(CategoryTable.DEPARTMENT_GUID + " = ?", model.departmentGuid)
                        .build(this);
            case TAX_GROUP_LOADER_ID:
                return CursorLoaderBuilder.forUri(TAX_GROUP_URI)
                        .projection(new String[]{TaxGroupTable.ID, TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX})
                        .build(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case DEPARTMENT_LOADER_ID:
                departmentAdapter.changeCursor(cursor);
                break;
            case CATEGORY_LOADER_ID:
                categoryAdapter.changeCursor(cursor);
                break;
            case TAX_GROUP_LOADER_ID:
                taxGroupAdapter.changeCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case DEPARTMENT_LOADER_ID:
                departmentAdapter.changeCursor(null);
                break;
            case CATEGORY_LOADER_ID:
                categoryAdapter.changeCursor(null);
                break;
            case TAX_GROUP_LOADER_ID:
                taxGroupAdapter.changeCursor(null);
                break;
        }
    }

    @Click
    protected void buttonClicked() {
        if (validateForm()) {
            collectDataToModel(model);
            callCommand(model);
            this.finish();
        }
    }

    protected boolean saveItem() {
        if (validateForm()) {
            collectDataToModel(model);
            callCommand(model);
            return true;
        } else {
            return false;
        }
    }

    @Click
    protected void modifiersAddClicked() {
        if (!PlanOptions.isModifiersAllowed()) {
            AlertDialogFragment.showAlert(this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            if (saveItem())
                addModifierClicked(ModifierType.MODIFIER);
        }
    }

    @Click
    protected void addonsAddClicked() {
        if (!PlanOptions.isModifiersAllowed()) {
            AlertDialogFragment.showAlert(this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            if (saveItem())
                addModifierClicked(ModifierType.ADDON);
        }
    }

    @Click
    protected void optionsAddClicked() {
        if (!PlanOptions.isModifiersAllowed()) {
            AlertDialogFragment.showAlert(this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            if (saveItem())
                addModifierClicked(ModifierType.OPTIONAL);
        }
    }

    @Click(R.id.units_button)
    protected void unitsButtonClicked() {
        UnitLabelActivity.start(this);
    }

    @Click
    protected void buttonViewClicked() {
        ButtonViewSelectDialogFragment.show(this, new IButtonViewDialogListener() {
            @Override
            public void onSelect(int level) {
                buttonView.getBackground().setLevel(level);
            }
        });
    }

    private void addModifierClicked(ModifierType modifierType) {
        EditDialog.showWithType(this, new ModifierModel(UUID.randomUUID().toString(), model.guid, null, null, null, null, null, null), modifierType, new OnEditListener() {
            @Override
            public void onDefaultModifierChanged(String modifierId, boolean useAsDefault, boolean resetDefaultModifier) {
                if (useAsDefault) {
                    model.defaultModifierGuid = modifierId;
                } else if (resetDefaultModifier) {
                    model.defaultModifierGuid = null;
                }
            }
        });
    }

    protected abstract void callCommand(ItemModel model);

    protected void fillModifierFields(List<ModifierModel> modifierModels) {
        clearlModifierFields();

        int modCount = 0;
        int addonCount = 0;
        int optionCount = 0;

        for (ModifierModel mod : modifierModels) {
            if (mod.type == ModifierType.MODIFIER) {
                appendModifier(this.modifiers, mod);
                modCount++;
            } else if (mod.type == ModifierType.ADDON) {
                appendModifier(this.addons, mod);
                addonCount++;
            } else if (mod.type == ModifierType.OPTIONAL) {
                appendModifier(this.optionals, mod);
                optionCount++;
            }
        }

        fillModifierLabels(modCount, addonCount, optionCount);
    }

    protected void clearlModifierFields() {
        modifiers.setText(null);
        addons.setText(null);
        optionals.setText(null);
        fillModifierLabels(0, 0, 0);
    }

    protected void fillModifierLabels(int modCount, int addonCount, int optionCount) {
        modifiersLabel.setText(String.format(getString(R.string.item_activity_modifiers), modCount));
        addonsLabel.setText(String.format(getString(R.string.item_activity_addons), addonCount));
        optionalsLabel.setText(String.format(getString(R.string.item_activity_optionals), optionCount));
    }

    protected void setFieldsFilters() {
        InputFilter[] currencyFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        InputFilter[] quantityFilter = new InputFilter[]{new QuantityFormatInputFilter()};
        InputFilter[] signedQuantityFilter = new InputFilter[]{new SignedQuantityFormatInputFilter()};
        InputFilter[] productCodeFilter = new InputFilter[]{new InputFilter.LengthFilter(TcrApplication.PRODUCT_CODE_MAX_LEN), alphanumericFilter};
        availableQty.setFilters(signedQuantityFilter);
        minimumQty.setFilters(quantityFilter);
        recommendedQty.setFilters(quantityFilter);
        salesPrice.setFilters(currencyFilter);
        cost.setFilters(currencyFilter);
        commissions.setFilters(currencyFilter);
        discount.setFilters(currencyFilter);
        productCode.setFilters(productCodeFilter);
    }

    final InputFilter alphanumericFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    protected void setFieldsChangeListeners() {
        TextChangeListener textChangeListener = new TextChangeListener();
        CheckedChangeListener checkedChangeListener = new CheckedChangeListener();
        SalableCheckedChangeListener salableCheckedChangeListener = new SalableCheckedChangeListener();

        description.addTextChangedListener(textChangeListener);
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

        active.setOnCheckedChangeListener(checkedChangeListener);
        availableQty.addTextChangedListener(textChangeListener);
        minimumQty.addTextChangedListener(textChangeListener);
        recommendedQty.addTextChangedListener(textChangeListener);

        stockTrackingFlag.setOnCheckedChangeListener(checkedChangeListener);
        salesPrice.addTextChangedListener(textChangeListener);
        discountable.setOnCheckedChangeListener(checkedChangeListener);
        taxable.setOnCheckedChangeListener(checkedChangeListener);
        salableChBox.setOnCheckedChangeListener(salableCheckedChangeListener);
        commissionsEligible.setOnCheckedChangeListener(checkedChangeListener);
        cost.addTextChangedListener(textChangeListener);
        commissions.addTextChangedListener(textChangeListener);
        discount.addTextChangedListener(textChangeListener);
        stockTrackingFlag.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changed = true;
                updateStockTrackingBlock(isChecked);
            }
        });
        hasNotes.setOnCheckedChangeListener(checkedChangeListener);

        setPriceType();

        onPcsCheck(true);
    }

    protected void setPriceType() {
        priceType.setOnItemSelectedListener(new PriceTypeChangeListener());
    }

    protected void onPcsCheck() {
        onPcsCheck(false);
    }

    protected void onPcsCheck(boolean init) {
        if (isPcs()) {
            availableQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
            minimumQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            recommendedQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

            if (init)
                return;

            try {
                if (!TextUtils.isEmpty(availableQty.getText()))
                    showIntegralInteger(availableQty, new BigDecimal(availableQty.getText().toString()));
                if (!TextUtils.isEmpty(availableQtyPencil.getText()))
                    showIntegralInteger(availableQtyPencil, new BigDecimal(availableQtyPencil.getText().toString()));
                if (!TextUtils.isEmpty(minimumQty.getText()))
                    showIntegralInteger(minimumQty, new BigDecimal(minimumQty.getText().toString()));
                if (!TextUtils.isEmpty(recommendedQty.getText()))
                    showIntegralInteger(recommendedQty, new BigDecimal(recommendedQty.getText().toString()));
            } catch (NumberFormatException ignore) {
            }
        } else {
            availableQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            minimumQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            recommendedQty.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);

            if (init)
                return;

            try {
                if (!TextUtils.isEmpty(availableQty.getText()))
                    UiHelper.showQuantity(availableQty, new BigDecimal(availableQty.getText().toString()));
                if (!TextUtils.isEmpty(availableQtyPencil.getText()))
                    UiHelper.showQuantity(availableQtyPencil, new BigDecimal(availableQtyPencil.getText().toString()));
                if (!TextUtils.isEmpty(minimumQty.getText()))
                    UiHelper.showQuantity(minimumQty, new BigDecimal(minimumQty.getText().toString()));
                if (!TextUtils.isEmpty(recommendedQty.getText()))
                    UiHelper.showQuantity(recommendedQty, new BigDecimal(recommendedQty.getText().toString()));
            } catch (NumberFormatException ignore) {
            }
        }
    }

    protected boolean isPcs() {
        PriceType pt = ((PriceTypeHolder) priceType.getSelectedItem()).type;
        return UnitUtil.isPcs(pt);
    }

    protected void updateStockTrackingBlock(boolean isChecked) {
        View [] views = {availableQty, recommendedQty, minimumQty, availableQtyPencil};
        for (View view: views) {
            view.setEnabled(isChecked);
        }

        if (!isChecked) {
            model.recommendedQty = null;
            model.availableQty = null;
            model.minimumQty = null;

            recommendedQty.setText(null);
            availableQty.setText(null);
            minimumQty.setText(null);
            availableQtyPencil.setText(null);
        } else {
            if (count > 0) {
                recollectComposerInfo();
            }
        }
    }

    protected void recollectComposerInfo() {
    }

    @Override
    public void onBackPressed() {
        if (changed) {
            if (System.currentTimeMillis() - lastBackPressedTime <= BACK_TIMEOUT) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, getString(R.string.back_twice_hint_data_not_saved), Toast.LENGTH_SHORT).show();
            }
            lastBackPressedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }

    }

    protected static void appendModifier(TextView textView, ModifierModel mod) {
        if (TextUtils.isEmpty(textView.getText())) {
            textView.append(mod.title);
        } else {
            textView.append(", " + mod.title);
        }
    }

    protected void collectDataToModel(ItemModel model) {
        Cursor c;

        model.description = this.description.getText().toString();

        c = (Cursor) this.category.getSelectedItem();
        model.categoryId = c == null ? null : c.getString(c.getColumnIndex(CategoryTable.GUID));

        model.eanCode = this.ean.getText().toString();

        model.productCode = this.productCode.getText().toString();


        model.unitsLabelId = ((UnitLabelModel) this.unitsLabel.getSelectedItem()).guid;

        if (!TextUtils.isEmpty(model.unitsLabelId) && !TextUtils.isEmpty(model.unitsLabel)) {
            model.unitsLabel = null;
        }

        model.priceType = ((PriceTypeHolder) this.priceType.getSelectedItem()).type;

        model.isActiveStatus = this.active.isChecked();

        model.printerAliasGuid = ((PrinterAliasModel) this.printerAlias.getSelectedItem()).guid;

        model.availableQty = UiHelper.getDecimalValue(this.availableQty);
        model.minimumQty = UiHelper.getDecimalValue(this.minimumQty);
        model.recommendedQty = UiHelper.getDecimalValue(this.recommendedQty);

        model.isStockTracking = this.stockTrackingFlag.isChecked();

        String price = !salableChBox.isChecked() && TextUtils.isEmpty(this.salesPrice.getText()) ?
                BigDecimal.ZERO.toString() : this.salesPrice.getText().toString();

        model.price = parseBigDecimal(price, BigDecimal.ZERO);

        model.isDiscountable = this.discountable.isChecked();

        model.isSalable = this.salableChBox.isChecked();


        model.discountType = ((DiscountTypeHolder) discountType.getSelectedItem()).type;

        String discount = this.discount.getText().toString();
        model.discount = parseBigDecimal(discount, BigDecimal.ZERO);

        model.isTaxable = this.taxable.isChecked();
        // c = (Cursor) this.taxGroup.getSelectedItem();
        //   model.taxGroupGuid = c.getString(c.getColumnIndex(TaxGroupTable.GUID));

        c = (Cursor) this.taxGroup.getSelectedItem();

        model.taxGroupGuid =
                (c == null) ? null : (salableChBox.isChecked() ?
                        c.getString(c.getColumnIndex(TaxGroupTable.GUID)) : null);


        String cost = this.cost.getText().toString();
        model.cost = parseBigDecimal(cost, BigDecimal.ZERO);

        model.commission = parseBigDecimal(commissions.getText().toString(), BigDecimal.ZERO);

        model.btnView = this.buttonView.getBackground().getLevel();

        model.hasNotes = this.hasNotes.isChecked();

        model.codeType = getTypeForIndex(serializationType);
        model.serializable = model.codeType != null;

        model.commissionEligible = this.commissionsEligible.isChecked();
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

    protected boolean validateForm() {

        if (TextUtils.isEmpty(description.getText())) {
            Toast.makeText(this, R.string.item_activity_alert_description_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        Cursor c = (Cursor) this.category.getSelectedItem();
        if (c == null) {
            Toast.makeText(this, R.string.item_activity_alert_category_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        String ean = this.ean.getText().toString();
        if (!TextUtils.isEmpty(ean) && (ean.length() < TcrApplication.BARCODE_MIN_LEN || ean.length() > TcrApplication.BARCODE_MAX_LEN)) {
            Toast.makeText(this, getString(R.string.item_activity_alert_ean_msg, TcrApplication.BARCODE_MIN_LEN, TcrApplication.BARCODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            return false;
        }

        String productCode = this.productCode.getText().toString();
        if (!TextUtils.isEmpty(productCode) && (productCode.length() < TcrApplication.BARCODE_MIN_LEN || productCode.length() > TcrApplication.PRODUCT_CODE_MAX_LEN)) {
            Toast.makeText(this, getString(R.string.item_activity_alert_product_code_msg, TcrApplication.BARCODE_MIN_LEN, TcrApplication.PRODUCT_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (duplicateUpc) {
            Toast.makeText(this, R.string.item_activity_alert_ean_duplicate_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (duplicateProductCode) {
            Toast.makeText(this, R.string.item_activity_alert_product_code_duplicate_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        PriceType pt = ((PriceTypeHolder) priceType.getSelectedItem()).type;
        if (pt != PriceType.OPEN && salableChBox.isChecked()) {
            BigDecimal priceValue = parseBigDecimal(salesPrice.getText().toString(), null);
            if (priceValue == null) {
                Toast.makeText(this, R.string.item_activity_alert_price_empty_msg, Toast.LENGTH_SHORT).show();
                return false;
            } /*else if (priceValue.compareTo(BigDecimal.ZERO) != 1) {
                Toast.makeText(this, R.string.item_activity_alert_price_positive_msg, Toast.LENGTH_SHORT).show();
                return false;
            }*/

        }

        if (!TextUtils.isEmpty(discount.getText())) {
            switch (((DiscountTypeHolder) discountType.getSelectedItem()).type) {
                case PERCENT:
                    if (parseBigDecimal(discount.getText().toString(), BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) >= 0) {
                        Toast.makeText(this, R.string.item_activity_alert_discount_less_100_msg, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case VALUE:
                    if (parseBigDecimal(discount.getText().toString(), BigDecimal.ZERO).compareTo(parseBigDecimal(salesPrice.getText().toString(), BigDecimal.ZERO)) >= 0) {
                        Toast.makeText(this, R.string.item_activity_alert_discount_less_price_msg, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
            }
        }

        if (!TextUtils.isEmpty(commissions.getText())) {
            if (parseBigDecimal(commissions.getText().toString(), BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) >= 0) {
                Toast.makeText(this, R.string.commission_validation_alert_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (unitsLabel.getAdapter().isEmpty()) {
            Toast.makeText(this, R.string.item_activity_alert_unit_label, Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    /**
     * Enable\disable views depends on "For Sale" checkBox.
     *
     * @param enable enable or disable views
     */
    protected void enableForSaleParams(boolean enable) {
        View views[] = {salesPrice, discountable, discountType, discount,
                commissionsEligible, commissions, taxable, taxGroup};
        for (View view : views) {
            view.setEnabled(enable);
        }
    }

    @Override
    protected void onBarcodeReceived(String barcode) {
        ItemCodeChooserAlertDialogFragment.show(BaseItemActivity.this, barcode);
    }

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        Logger.d("BaseItemActivity onReceive:" + barcode);

        ItemCodeChooserAlertDialogFragment.show(BaseItemActivity.this, barcode);
    }

    private static class PriceTypeHolder {
        final String label;
        final PriceType type;

        PriceTypeHolder(String label, PriceType type) {
            this.label = label;
            this.type = type;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class SerializationTypeHolder {
        final String label;
        final CodeType type;

        SerializationTypeHolder(CodeType type) {
            this.label = type == null ? "Non-serializable" : type.toString();
            this.type = type;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    protected static class DiscountTypeHolder {
        final String label;
        final DiscountType type;

        DiscountTypeHolder(String label, DiscountType type) {
            this.label = label;
            this.type = type;
        }

        @Override
        public String toString() {
            return label;
        }

        public int getPosition() {
            return type.ordinal();
        }
    }

    public static class CategorySpinnerAdapter extends SpinnerAdapter {

        public CategorySpinnerAdapter(Context context) {
            super(context,
                    R.layout.spinner_item_light,
                    new String[]{CategoryTable.TITLE},
                    new int[]{android.R.id.text1},
                    R.layout.spinner_dropdown_item);
        }

        @Override
        protected String getIdColumnName() {
            return CategoryTable.GUID;
        }
    }

    public static class DepartmentSpinnerAdapter extends SpinnerAdapter {

        public DepartmentSpinnerAdapter(Context context) {
            super(context,
                    R.layout.spinner_item_light,
                    new String[]{DepartmentTable.TITLE},
                    new int[]{android.R.id.text1},
                    R.layout.spinner_dropdown_item);
        }

        @Override
        protected String getIdColumnName() {
            return DepartmentTable.GUID;
        }
    }

    public class TaxGroupSpinnerAdapter extends SpinnerAdapter {

        public TaxGroupSpinnerAdapter(Context context) {
            super(context,
                    R.layout.spinner_item_light,
                    new String[]{TaxGroupTable.TITLE},
                    new int[]{android.R.id.text1},
                    R.layout.spinner_dropdown_item);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ((TextView) view.findViewById(android.R.id.text1)).setText(
                    String.format("%s (%s)",
                            c.getString(c.getColumnIndex(TaxGroupTable.TITLE)),
                            UiHelper.formatQuantity(_decimal(c, c.getColumnIndex(TaxGroupTable.TAX)))
                    )
            );
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor == null) {
                super.changeCursor(cursor);
                return;
            }

            MatrixCursor extras = new MatrixCursor(new String[]{TaxGroupTable.ID, TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX});
            extras.addRow(new String[]{"0", null, getString(R.string.item_tax_group_default), _decimal(getApp().getShopInfo().taxVat)});
            Cursor[] cursors = {extras, cursor};
            Cursor extendedCursor = new MergeCursor(cursors);

            super.changeCursor(extendedCursor);
        }

        @Override
        protected String getIdColumnName() {
            return TaxGroupTable.GUID;
        }
    }

    private class PrinterAliasAdapter extends ObjectsCursorAdapter<PrinterAliasModel> {

        public PrinterAliasAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, PrinterAliasModel item) {
            ((TextView) convertView).setText(item.alias);
            return convertView;
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        public int getPosition(String guid) {
            if (guid == null)
                return 0;

            for (int i = 0; i < getCount(); i++) {
                if (guid.equals(getItem(i).guid))
                    return i;
            }
            return 0;
        }

    }

    private class ModifierModelLoader implements LoaderCallbacks<List<ModifierModel>> {

        @Override
        public Loader<List<ModifierModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(MODIFIER_URI)
                    .where(ModifierTable.ITEM_GUID + " = ?", model.guid)
                    .transform(new ModifierFunction()).build(BaseItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierModel>> listLoader, List<ModifierModel> modifierModels) {
            fillModifierFields(modifierModels);
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierModel>> listLoader) {
            clearlModifierFields();
        }
    }

    private class PrinterAliasLoader implements LoaderCallbacks<List<PrinterAliasModel>> {

        @Override
        public Loader<List<PrinterAliasModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(PRINTER_ALIAS_URI)
                    .transform(new PrinterAliasConverter())
                    .build(BaseItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<PrinterAliasModel>> listLoader, List<PrinterAliasModel> printerAliasModels) {
            ArrayList<PrinterAliasModel> models = new ArrayList<PrinterAliasModel>(printerAliasModels.size() + 1);
            models.add(new PrinterAliasModel(null, "None"));
            models.addAll(printerAliasModels);

            printerAliasAdapter.changeCursor(models);

            final String aliasGuid = model.printerAliasGuid;
            if (model.printerAliasGuid != null) {
                printerAlias.setSelection(printerAliasAdapter.getPosition(model.printerAliasGuid));
            }

            printerAlias.setOnItemSelectedListener(new SpinnerChangeListener(aliasGuid != null ? printerAliasAdapter.getPosition(model.printerAliasGuid) : 0));


        }

        @Override
        public void onLoaderReset(Loader<List<PrinterAliasModel>> listLoader) {
            printerAliasAdapter.changeCursor(null);
        }

    }

    private class TextChangeListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            changed = true;
        }
    }


    protected class PriceTypeChangeListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            onPcsCheck();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    protected class SpinnerChangeListener implements OnItemSelectedListener {

        private int position;

        SpinnerChangeListener(int position) {
            this.position = position;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != position) {
                changed = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private class CheckedChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            changed = true;
        }
    }

    private class SalableCheckedChangeListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            changed = true;
            enableForSaleParams(isChecked);
        }
    }

    private class UnitsLabelLoader implements LoaderCallbacks<List<UnitLabelModel>> {

        @Override
        public Loader<List<UnitLabelModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder
                    .forUri(UNIT_LABEL_URI)
                    .orderBy(ShopStore.UnitLabelTable.SHORTCUT)
                    .transform(new UnitLabelFunction())
                    .build(BaseItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<UnitLabelModel>> loader, List<UnitLabelModel> data) {
            ArrayList<UnitLabelModel> models = new ArrayList<UnitLabelModel>(data.size() + 1);

            String oldUnitLabel = model.unitsLabel;
            if (!TextUtils.isEmpty(oldUnitLabel)) {
                models.add(UnitLabelModelFactory.getSimpleModel(oldUnitLabel));
            }

            models.addAll(data);

            if(models.isEmpty()) {
                models.add(UnitLabelModelFactory.getSimpleModel(TcrApplication.get().getShopInfo().defUnitLabelShortcut));
            }

            unitsLabelAdapter.changeCursor(models);

            if (!TextUtils.isEmpty(oldUnitLabel)) {
                unitsLabel.setSelection(unitsLabelAdapter.getPositionByShortcut(oldUnitLabel));
            } else if (!TextUtils.isEmpty(model.unitsLabelId)) {
                unitsLabel.setSelection(unitsLabelAdapter.getPositionById(model.unitsLabelId));
            } else {
                // for new Items
                unitsLabel.setSelection(unitsLabelAdapter.getPositionByShortcut(
                        TcrApplication.get().getShopInfo().defUnitLabelShortcut));
            }
        }

        @Override
        public void onLoaderReset(Loader<List<UnitLabelModel>> loader) {
            unitsLabelAdapter.changeCursor(null);
        }
    }


    private class UpcLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ItemTable.URI_CONTENT))
                    .projection(ItemTable.GUID)
                    .where(ItemTable.EAN_CODE + " = ?", ean.getText().toString())
                    .where(ItemTable.GUID + " <> ?", model.guid)
                    .build(BaseItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateUpc = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }

    private class ProductCodeLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ItemTable.URI_CONTENT))
                    .projection(ItemTable.GUID)
                    .where(ItemTable.PRODUCT_CODE + " = ?", productCode.getText().toString())
                    .where(ItemTable.GUID + " <> ?", model.guid)
                    .build(BaseItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateProductCode = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }

    protected OnClickListener updateQtyListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (model.isSerializable()) {
                UnitActivity.start(BaseItemActivity.this, model, TAG_RESULT);
                return;
            }

            InventoryQtyEditDialog.show(BaseItemActivity.this, model.availableQty == null ? BigDecimal.ZERO : model.availableQty, isPcs(),
                    new InventoryQtyEditDialog.OnEditQtyListener() {
                        @Override
                        public void onReplace(BigDecimal value) {
                            model.availableQty = value;
                            if (isPcs()) {
                                showInteger(availableQty, model.availableQty);
                                showInteger(availableQtyPencil, model.availableQty);
                            } else {
                                showQuantity(availableQty, model.availableQty);
                                showQuantity(availableQtyPencil, model.availableQty);
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
                                showQuantity(availableQty, model.availableQty);
                                showQuantity(availableQtyPencil, model.availableQty);
                            }
                        }
                    }
            );
        }
    };

    protected Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.badge, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }

}
