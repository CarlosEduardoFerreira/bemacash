package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseItemActivity2;
import com.kaching123.tcr.adapter.SpinnerAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.countries.ecuador.TaxHelper;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.taxgroup.ChooseTaxGroupsDialog;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.util.BemaKeyboard;
import com.kaching123.tcr.util.BemaKeyboardDecimalsWithNegative;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.kaching123.tcr.fragment.UiHelper.getDecimalValue;
import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_common_information_fragment)
public class ItemCommonInformationFragment extends ItemBaseFragment implements LoaderCallbacks<Cursor>{

    @ViewById protected EditText description;
    @ViewById protected EditText salesPrice;
    @ViewById protected Spinner department;
    @ViewById protected Spinner category;
    @ViewById(R.id.tax_group) protected Spinner taxGroup;
    @ViewById protected TextView ecuadorTaxGroup;
    @ViewById protected View taxGroupRow;
    @ViewById protected View ecuadorTaxGroupRow;
    @ViewById protected CheckBox activeStatus;

    private static final int DEPARTMENT_LOADER_ID = 0;
    private static final int CATEGORY_LOADER_ID = 1;
    private static final int TAX_GROUP_LOADER_ID = 2;

    private boolean departmantLoaded;
    private boolean categoryLoaded;
    private boolean taxLoaded;
    private boolean duplicated;

    protected DepartmentSpinnerAdapter departmentAdapter;
    protected CategorySpinnerAdapter categoryAdapter;
    protected TaxGroupSpinnerAdapter taxGroupAdapter;

    BemaKeyboard bemaKeyboard = null;
    View viewAux;

    @Override
    protected void setViews() {
        departmentAdapter = new DepartmentSpinnerAdapter(getActivity());
        department.setAdapter(departmentAdapter);

        categoryAdapter = new CategorySpinnerAdapter(getActivity());
        category.setAdapter(categoryAdapter);

        if (!TcrApplication.getCountryFunctionality().isMultiTaxGroup()) { //isCurrentCountryUsesMultiTax()){
            taxGroupAdapter = new TaxGroupSpinnerAdapter(getActivity());
            taxGroup.setAdapter(taxGroupAdapter);
        }

//        taxGroupRow.setVisibility(TcrApplication.isCurrentCountryUsesMultiTax() ? View.GONE : View.VISIBLE);
//        ecuadorTaxGroupRow.setVisibility(TcrApplication.isCurrentCountryUsesMultiTax() ? View.VISIBLE : View.GONE);
        taxGroupRow.setVisibility(TcrApplication.getCountryFunctionality().isMultiTaxGroup() ? View.GONE : View.VISIBLE);
        ecuadorTaxGroupRow.setVisibility(TcrApplication.getCountryFunctionality().isMultiTaxGroup() ? View.VISIBLE : View.GONE);

        setFilters();
        initLoaders();
    }

    @Override
    public void duplicate(){
        if(departmentAdapter != null && categoryAdapter != null) {
            setModel();
            duplicateNextStep();
        }
    }

    public void duplicateNextStep(){
        if (duplicated) {
            return;
        }
        duplicated = true;

        if (getModel().departmentGuid != null) {
            department.setSelection(departmentAdapter.getPosition4Id(getModel().departmentGuid));
        } else {
            department.setSelection(0);
        }
        if (getModel().categoryId != null) {
            category.setSelection(categoryAdapter.getPosition4Id(getModel().categoryId));
        } else {
            category.setSelection(0);
        }
        if (getModel().taxGroupGuid != null) {
            taxGroup.setSelection(taxGroupAdapter.getPosition4Id(getModel().taxGroupGuid));
        } else {
            taxGroup.setSelection(0);
        }
        ((BaseItemActivity2)getActivity()).commonInfoReady();
    }

    protected void callBemaKeyboard(){
        salesPrice.setFocusableInTouchMode(false);
        salesPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bemaKeyboard == null) {
                    salesPrice.setFocusableInTouchMode(true);
                    bemaKeyboard = new BemaKeyboard(getView(), salesPrice);
                } else {
                    bemaKeyboard.closeSoftKeyboard();
                }
            }
        });
    }

    @Override
    protected void newItem(){
        callBemaKeyboard();
    }

    @Override
    protected void setModel() {
        final ItemModel model = getModel();
        description.setText(model.description);
        showPrice(salesPrice, model.price);
        activeStatus.setChecked(model.isActiveStatus);
        callBemaKeyboard();
    }


    @Override
    public void collectData() {
        final ItemModel model = getModel();
        model.description = description.getText().toString();
        model.price = getDecimalValue(salesPrice);
        model.categoryId = categoryAdapter.getGuid(category.getSelectedItemPosition());
        model.isActiveStatus = activeStatus.isChecked();
        if (!getModel().isSalable){
            model.taxGroupGuid = null;
            model.taxGroupGuid2 = null;
        }
    }

    @Override
    public boolean validateData() {
        if (TextUtils.isEmpty(description.getText())) {
            Toast.makeText(getActivity(), R.string.item_activity_alert_description_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(getModel().categoryId)) {
            Toast.makeText(getActivity(), R.string.item_activity_alert_category_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (getModel().priceType != PriceType.OPEN && getModel().isSalable) {
            BigDecimal priceValue = parseBigDecimal(salesPrice.getText().toString(), null);
            if (priceValue == null) {
                Toast.makeText(getActivity(), R.string.item_activity_alert_price_empty_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void initLoaders() {
        getLoaderManager().initLoader(DEPARTMENT_LOADER_ID, null, this);
        getLoaderManager().initLoader(TAX_GROUP_LOADER_ID, null, this);
    }

    private void setFilters(){
        InputFilter[] currencyFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        salesPrice.setFilters(currencyFilter);
        //salesPrice.setFilters(new InputFilter[] { new BemaKeyboardDecimalsWithNegative(7, 2) });
        //salesPrice.addTextChangedListener(new CurrencyTextWatcher(salesPrice));
    }

    @Click
    protected void ecuadorTaxGroupClicked(){
        boolean hasStoreTaxOnly = getModel().taxGroupGuid == null && getModel().taxGroupGuid2 == null;
        ChooseTaxGroupsDialog.show(getActivity(), getModel().taxGroupGuid,
                getModel().taxGroupGuid2, hasStoreTaxOnly, new ChooseTaxGroupsDialog.ChooseTaxCallback() {
                    @Override
                    public void onTaxGroupsChosen(TaxGroupModel model1, TaxGroupModel model2) {
                        List<TaxGroupModel> itemTaxes = new ArrayList<>();
                        if (model1 != null) {
                            getModel().taxGroupGuid = model1.guid;
                            itemTaxes.add(model1);
                        }else{
                            getModel().taxGroupGuid = null;
                        }
                        if (model2 != null) {
                            getModel().taxGroupGuid2 = model2.guid;
                            itemTaxes.add(model2);
                        }else{
                            getModel().taxGroupGuid2 = null;
                        }
                        ecuadorTaxGroup.setText(TaxHelper.getTaxDisplayText(itemTaxes));
                    }
                });
    }

    @ItemSelect
    protected void departmentItemSelected(boolean selected, int position){
        getModel().departmentGuid = departmentAdapter.getGuid(position);
        getLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, this);
    }

    @ItemSelect(R.id.tax_group)
    protected void taxGroupItemSelected(boolean selected, int position){
        getModel().taxGroupGuid = taxGroupAdapter.getGuid(position);
    }

    @AfterTextChange
    protected void salesPriceAfterTextChanged(Editable s){
        getModel().price = parseBigDecimal(s.toString(), BigDecimal.ZERO);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DEPARTMENT_LOADER_ID:
                return CursorLoaderBuilder.forUri(ShopProvider.contentUri(DepartmentTable.URI_CONTENT))
                        .projection(new String[]{DepartmentTable.ID, DepartmentTable.TITLE, DepartmentTable.GUID})
                        .orderBy(DepartmentTable.TITLE + " asc")
                        .build(getActivity());
            case CATEGORY_LOADER_ID:
                return CursorLoaderBuilder.forUri(ShopProvider.contentUri(CategoryTable.URI_CONTENT))
                        .projection(new String[]{CategoryTable.ID, CategoryTable.TITLE, CategoryTable.GUID})
                        .where(CategoryTable.DEPARTMENT_GUID + " = ?", getModel().departmentGuid)
                        .orderBy(CategoryTable.TITLE + " asc")
                        .build(getActivity());
            case TAX_GROUP_LOADER_ID:
                return CursorLoaderBuilder.forUri(ShopProvider.contentUri(TaxGroupTable.URI_CONTENT))
                        .projection(new String[]{TaxGroupTable.ID, TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX, TaxGroupTable.IS_DEFAULT})
                        .build(getActivity());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case DEPARTMENT_LOADER_ID:
                departmentAdapter.changeCursor(data);
                if (getModel().departmentGuid != null)
                    department.setSelection(departmentAdapter.getPosition4Id(getModel().departmentGuid));
                if(((BaseItemActivity2)getActivity()).isDuplicate()) {
                    departmantLoaded = true;
                    if (categoryLoaded && taxLoaded) {
                        duplicateNextStep();
                    }
                }
                break;
            case CATEGORY_LOADER_ID:
                categoryAdapter.changeCursor(data);
                if (getModel().categoryId != null)
                    category.setSelection(categoryAdapter.getPosition4Id(getModel().categoryId));

                if(((BaseItemActivity2)getActivity()).isDuplicate()) {
                    categoryLoaded = true;
                    if (departmantLoaded && taxLoaded) {
                        duplicateNextStep();
                    }
                }
                break;
            case TAX_GROUP_LOADER_ID:
                onTaxGroupLoaded(data);
                if (((BaseItemActivity2)getActivity()).isDuplicate()) {
                    taxLoaded = true;
                    if (departmantLoaded && categoryLoaded) {
                        duplicateNextStep();
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DEPARTMENT_LOADER_ID:
                departmentAdapter.changeCursor(null);
                break;
            case CATEGORY_LOADER_ID:
                categoryAdapter.changeCursor(null);
                break;
            case TAX_GROUP_LOADER_ID:
                break;
        }
    }

    private void onTaxGroupLoaded(Cursor cursor){
        /*if (TcrApplication.isEcuadorVersion()){
            handleEcuadorTaxes(cursor);
        } else if(TcrApplication.isPeruVersion()) {
            handlePeruTaxes(cursor);
        }*/if(TcrApplication.getCountryFunctionality().isMultiTaxGroup()) { //isCurrentCountryUsesMultiTax()) {
            handleEcuadorTaxes(cursor);
        }else{
            taxGroupAdapter.changeCursor(cursor);
            if (getModel().taxGroupGuid != null)
                taxGroup.setSelection(taxGroupAdapter.getPosition4Id(getModel().taxGroupGuid));
        }
    }

    private void handleEcuadorTaxes(Cursor cursor) {
        ArrayList<TaxGroupModel> storeTaxes = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()){
            storeTaxes.add(new TaxGroupModel(cursor));
        }

        ArrayList<TaxGroupModel> itemTaxes = new ArrayList<>();
        final TaxGroupModel virtualStoreTax = new TaxGroupModel(null, getString(R.string.item_tax_group_default), getApp().getShopInfo().taxVat);
        if (getItemProvider().isCreate()){
            List<TaxGroupModel> defaultTaxes = TaxHelper.getDefaultTaxes(storeTaxes);
            if (defaultTaxes.isEmpty()){
                defaultTaxes.add(virtualStoreTax);
            }
            itemTaxes.addAll(defaultTaxes);
        }else{
            TaxGroupModel tax1 = TaxHelper.getTaxById(storeTaxes, getModel().taxGroupGuid);
            TaxGroupModel tax2 = TaxHelper.getTaxById(storeTaxes, getModel().taxGroupGuid2);
            if (tax1 != null){
                itemTaxes.add(tax1);
            }else{
                itemTaxes.add(virtualStoreTax);
            }
            if (tax2 != null){
                itemTaxes.add(tax2);
            }
        }

        for (int i = 0; i < itemTaxes.size(); i++){
            if (i == 0)
                getModel().taxGroupGuid = itemTaxes.get(i).guid;
            if (i == 1)
                getModel().taxGroupGuid2 = itemTaxes.get(i).guid;
        }

        ecuadorTaxGroup.setText(TaxHelper.getTaxDisplayText(itemTaxes));
        taxGroupRow.invalidate();
    }

    private void handlePeruTaxes(Cursor cursor) {

//// TODO: 26.09.2016 add tax calculations
        //leave the same calculation as for ecuador for a while

        handleEcuadorTaxes(cursor);
    }



    private static class CategorySpinnerAdapter extends SpinnerAdapter {

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

    private static class DepartmentSpinnerAdapter extends SpinnerAdapter {

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

    private class TaxGroupSpinnerAdapter extends SpinnerAdapter {

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
                            UiHelper.percentFormat(_decimal(c, c.getColumnIndex(TaxGroupTable.TAX), BigDecimal.ZERO))
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
}
