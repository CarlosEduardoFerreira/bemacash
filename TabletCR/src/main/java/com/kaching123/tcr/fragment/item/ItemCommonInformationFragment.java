package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.SpinnerAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

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
    @ViewById protected CheckBox activeStatus;

    private static final int DEPARTMENT_LOADER_ID = 0;
    private static final int CATEGORY_LOADER_ID = 1;
    private static final int TAX_GROUP_LOADER_ID = 2;

    protected DepartmentSpinnerAdapter departmentAdapter;
    protected CategorySpinnerAdapter categoryAdapter;
    protected TaxGroupSpinnerAdapter taxGroupAdapter;

    @Override
    protected void setViews() {
        departmentAdapter = new DepartmentSpinnerAdapter(getActivity());
        department.setAdapter(departmentAdapter);

        categoryAdapter = new CategorySpinnerAdapter(getActivity());
        category.setAdapter(categoryAdapter);

        taxGroupAdapter = new TaxGroupSpinnerAdapter(getActivity());
        taxGroup.setAdapter(taxGroupAdapter);

        setFilters();
        initLoaders();
    }

    @Override
    protected void setModel() {
        final ItemModel model = getModel();
        description.setText(model.description);
        showPrice(salesPrice, model.price);
        activeStatus.setChecked(model.isActiveStatus);

    }

    @Override
    public void collectData() {
        final ItemModel model = getModel();
        model.description = description.getText().toString();
        model.price = UiHelper.getDecimalValue(salesPrice);
        model.categoryId = categoryAdapter.getGuid(category.getSelectedItemPosition());
        model.isActiveStatus = activeStatus.isChecked();
        model.taxGroupGuid = taxGroupAdapter.getGuid(taxGroup.getSelectedItemPosition());
    }

    @Override
    public boolean validateData() {
        if (TextUtils.isEmpty(description.getText())) {
            Toast.makeText(getActivity(), R.string.item_activity_alert_description_msg, Toast.LENGTH_SHORT).show();
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
        salesPrice.addTextChangedListener(new CurrencyTextWatcher(salesPrice));
    }

    @ItemSelect
    protected void departmentItemSelected(boolean selected, int position){
        getModel().departmentGuid = departmentAdapter.getGuid(position);
        getLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, this);
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
                        .build(getActivity());
            case CATEGORY_LOADER_ID:
                return CursorLoaderBuilder.forUri(ShopProvider.contentUri(CategoryTable.URI_CONTENT))
                        .projection(new String[]{CategoryTable.ID, CategoryTable.TITLE, CategoryTable.GUID})
                        .where(CategoryTable.DEPARTMENT_GUID + " = ?", getModel().departmentGuid)
                        .build(getActivity());
            case TAX_GROUP_LOADER_ID:
                return CursorLoaderBuilder.forUri(ShopProvider.contentUri(TaxGroupTable.URI_CONTENT))
                    .projection(new String[]{TaxGroupTable.ID, TaxGroupTable.GUID, TaxGroupTable.TITLE, TaxGroupTable.TAX})
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
                break;
            case CATEGORY_LOADER_ID:
                categoryAdapter.changeCursor(data);
                if (getModel().categoryId != null)
                    category.setSelection(categoryAdapter.getPosition4Id(getModel().categoryId));
                break;
            case TAX_GROUP_LOADER_ID:
                taxGroupAdapter.changeCursor(data);
                if (getModel().taxGroupGuid != null)
                    taxGroup.setSelection(taxGroupAdapter.getPosition4Id(getModel().taxGroupGuid));
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
                taxGroupAdapter.changeCursor(null);
                break;
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
                            UiHelper.percentFormat(_decimal(c, c.getColumnIndex(TaxGroupTable.TAX)))
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
