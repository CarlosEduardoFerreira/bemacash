package com.kaching123.tcr.fragment.item;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.UnitLabelActivity;
import com.kaching123.tcr.adapter.UnitsLabelAdapter;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment.IButtonViewDialogListener;
import com.kaching123.tcr.fragment.inventory.ChooseParentItemDialogFragment;
import com.kaching123.tcr.fragment.inventory.ItemCodeChooserAlertDialogFragment;
import com.kaching123.tcr.fragment.inventory.ItemCodeChooserAlertDialogFragment.ItemCodeTypeChooseListener;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.model.ItemCodeType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMatrixByParentView;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;
import com.kaching123.tcr.util.Validator;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.getDecimalValue;
import static com.kaching123.tcr.fragment.UiHelper.showIntegralInteger;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_additional_information_fragment)
public class ItemAdditionalInformationFragment extends ItemBaseFragment implements ItemCodeTypeChooseListener, BarcodeReceiver {

    @ViewById protected EditText eanUpc;
    @ViewById protected EditText productCode;
    @ViewById protected View eanUpcRow;
    @ViewById protected View productCodeRow;
    @ViewById protected Spinner unitsLabel;
    @ViewById protected Spinner unitsType;
    @ViewById protected TextView referenceItem;
    @ViewById protected View referenceItemRow;
    @ViewById protected View buttonView;
    @ViewById protected CheckBox ebtEligible;
    @ViewById protected EditText bonusPoints;
    @ViewById protected CheckBox excludeFromLoyaltyPlan;
    @ViewById protected CheckBox hasNotes;

    private UnitsLabelAdapter unitsLabelAdapter;

    private static final int UNIT_LABEL_LOADER_ID = 0;
    private static final int EAN_LOADER_ID = 1;
    private static final int PRODUCT_CODE_LOADER_ID = 2;
    private static final int ITEM_MATRIX_LOADER_ID = 3;
    private static final int ITEM_PARENT_LOADER_ID = 4;

    private boolean duplicateEanUpc;
    private boolean duplicateProductCode;

    @Override
    protected void newItem(){}


    @Override
    protected void setViews() {
        InputFilter[] productCodeFilter = new InputFilter[]{new InputFilter.LengthFilter(TcrApplication.PRODUCT_CODE_MAX_LEN), alphanumericFilter};
        productCode.setFilters(productCodeFilter);

        unitsLabelAdapter = new UnitsLabelAdapter(getActivity());
        unitsLabel.setAdapter(unitsLabelAdapter);

        ArrayAdapter<SerializationTypeHolder> unitTypeAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.spinner_item_light, new SerializationTypeHolder[]{
                        new SerializationTypeHolder(null),
                        new SerializationTypeHolder(CodeType.SN),
                        new SerializationTypeHolder(CodeType.IMEI),
                        new SerializationTypeHolder(CodeType.ICCID),
                });
        unitTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        unitsType.setAdapter(unitTypeAdapter);

        if (!getModel().isReferenceItem()){
            if (getModel().referenceItemGuid == null) {
                getLoaderManager().restartLoader(ITEM_MATRIX_LOADER_ID, null, cursorLoaderCallbacks);
            } else {
                getLoaderManager().restartLoader(ITEM_PARENT_LOADER_ID, null, cursorLoaderCallbacks);
            }
            if (getItemProvider().isCreate()){
                new GetNextProductCodeTask().execute();
            }
        }else{
           eanUpcRow.setVisibility(View.GONE);
           productCodeRow.setVisibility(View.GONE);
           referenceItemRow.setVisibility(View.GONE);
        }

        if (getModel().tmpBarcode != null){
            ItemCodeChooserAlertDialogFragment.show(getActivity(), filterBarcode(getModel().tmpBarcode), this);
        }

        getLoaderManager().initLoader(UNIT_LABEL_LOADER_ID, null, new UnitsLabelLoader());
    }

    @Override
    protected void setModel() {
        ItemExModel model = getModel();
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.setModel.model.eanCode: " + model.eanCode);
        eanUpc.setText(model.eanCode);
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.setModel.eanUpc.getText(): " + eanUpc.getText());
        productCode.setText(model.productCode);
        if (model.codeType != null)
            unitsType.setSelection(model.codeType.ordinal() + 1);
        buttonView.getBackground().setLevel(model.btnView);
        showIntegralInteger(bonusPoints, model.loyaltyPoints);
        excludeFromLoyaltyPlan.setChecked(model.excludeFromLoyaltyPlan);
        ebtEligible.setChecked(model.isEbtEligible);
        hasNotes.setChecked(model.hasNotes);
    }

    @Override
    public void collectData() {
        ItemModel model = getModel();
        if (!getModel().isReferenceItem()){
            model.eanCode = eanUpc.getText().toString();
            Log.d("BemaCarl4","ItemAdditionalInformationFragment.collectData.model.eanCode: " + model.eanCode);
            model.productCode = productCode.getText().toString();
        }
        model.unitsLabelId = ((UnitLabelModel)unitsLabel.getSelectedItem()).getGuid();
        model.btnView = buttonView.getBackground().getLevel();
        model.loyaltyPoints = getDecimalValue(bonusPoints);
        model.excludeFromLoyaltyPlan = excludeFromLoyaltyPlan.isChecked();
        model.isEbtEligible = ebtEligible.isChecked();
        model.hasNotes = hasNotes.isChecked();
    }

    @Override
    public boolean validateData() {
        if (duplicateEanUpc) {
            Toast.makeText(getActivity(), R.string.item_activity_alert_ean_duplicate_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (duplicateProductCode) {
            Toast.makeText(getActivity(), R.string.item_activity_alert_product_code_duplicate_msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Click
    protected void unitsButtonClicked() {
        UnitLabelActivity.start(getActivity());
    }

    @Click
    protected void buttonViewClicked() {
        ButtonViewSelectDialogFragment.show(getActivity(), new IButtonViewDialogListener() {
            @Override
            public void onSelect(int level) {
                buttonView.getBackground().setLevel(level);
            }
        });
    }

    @Click
    protected void referenceItemClicked(){
        ChooseParentItemDialogFragment.show(getActivity(), getModel().guid,
                new ChooseParentItemDialogFragment.OnItemChosenListener() {
                    @Override
                    public void onItemChosen(ItemExModel parentItem, ItemMatrixModel parentItemMatrix) {
                        referenceItem.setText(parentItem.description);
                        getItemProvider().setParentItem(parentItem);
                        getItemProvider().setParentMatrixItem(parentItemMatrix);
                    }
                }
        );
    }

    @ItemSelect
    protected void unitsTypeItemSelected(boolean selected, int position){
        final CodeType oldType = getModel().codeType;
        final CodeType newType = ((SerializationTypeHolder) unitsType.getItemAtPosition(position)).type;

        if (oldType != null && newType == null && getItemProvider().getQtyInfo().unitsCount > 0) {
            AlertDialogWithCancelFragment.showWithTwo(getActivity(),
                    R.string.wireless_remove_items_title,
                    getString(R.string.wireless_remove_items_body),
                    R.string.btn_ok,
                    new AlertDialogWithCancelFragment.OnDialogListener() {
                        @Override
                        public boolean onClick() {
                            getModel().codeType = newType;
                            DropUnitsCommand.start(getActivity(),
                                    null, getModel(), new DropUnitsCommand.UnitCallback() {
                                        @Override
                                        protected void handleSuccess(ItemExModel model) {
                                            getModel().codeType = newType;
                                            getModel().serializable = getModel().codeType != null;
                                            getModel().availableQty = BigDecimal.ZERO;
                                            getItemProvider().getQtyInfo().setAvailableQty(BigDecimal.ZERO);
                                            getItemProvider().onStockTypeChanged();
                                        }

                                        @Override
                                        protected void handleError() {
                                            Toast.makeText(getActivity(), getString(R.string.unit_drop_partially_faield), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            return true;
                        }

                        @Override
                        public boolean onCancel() {
                            unitsType.setSelection(oldType.ordinal() + 1);
                            return true;
                        }
                    }
            );
        }else{
            getModel().codeType = newType;
            getModel().serializable = getModel().codeType != null;
            getItemProvider().onStockTypeChanged();
        }
    }

    @AfterTextChange
    protected void eanUpcAfterTextChanged(Editable s){
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.eanUpcAfterTextChanged.s.toString(): " + s.toString());
        if (s.length() > 0){
            getLoaderManager().restartLoader(EAN_LOADER_ID, null, new EanLoader());
        }else{
            duplicateEanUpc = false;
        }
    }

    @AfterTextChange
    protected void productCodeAfterTextChanged(Editable s){
        if (s.length() > 0){
            getLoaderManager().restartLoader(PRODUCT_CODE_LOADER_ID, null, new ProductCodeLoader());
        }else{
            duplicateProductCode = false;
        }
    }

    @Override
    public void onItemCodeTypeChosen(ItemCodeType codeType, String code) {
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.onItemCodeTypeChosen.codeType: " + codeType);
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.onItemCodeTypeChosen.code: " + code);
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.onItemCodeTypeChosen.ItemCodeType.EAN_UPC: " + ItemCodeType.EAN_UPC);
        if (ItemCodeType.EAN_UPC == codeType) {
            try {
                if (Validator.isEanValid(code)) {
                    eanUpc.setText(code);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.item_activity_alert_ean_error), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException badEan) {
                Toast.makeText(getActivity(), getString(R.string.item_activity_alert_ean_bad_error), Toast.LENGTH_SHORT).show();
            }
        } else if (ItemCodeType.PRODUCT_CODE == codeType) {
            if (Validator.isProductCodeValid(code)) {
                productCode.setText(code);
            } else {
                Toast.makeText(getActivity(), getString(R.string.item_activity_alert_product_code_error, TcrApplication.PRODUCT_CODE_MAX_LEN), Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("BemaCarl4","ItemAdditionalInformationFragment.onItemCodeTypeChosen.eanUpc.getText(): " + eanUpc.getText());
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        ItemCodeChooserAlertDialogFragment.show(getActivity(), filterBarcode(barcode), this);
    }

    private class UnitsLabelLoader implements LoaderCallbacks<List<UnitLabelModel>> {

        @Override
        public Loader<List<UnitLabelModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder
                    .forUri(ShopProvider.contentUri(UnitLabelTable.URI_CONTENT))
                    .orderBy(ShopStore.UnitLabelTable.SHORTCUT)
                    .transformRow(new UnitLabelFunction())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<UnitLabelModel>> loader, List<UnitLabelModel> data) {
            unitsLabelAdapter.changeCursor(data);
            if (getModel().unitsLabelId != null){
                int position = unitsLabelAdapter.getPositionById(getModel().unitsLabelId);
                if (position != AdapterView.INVALID_POSITION)
                    unitsLabel.setSelection(position);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<UnitLabelModel>> loader) {
            unitsLabelAdapter.changeCursor(null);
        }
    }

    private static String filterBarcode(String barcode) {
        return barcode.replace("-","").replace("#","");
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

    private class GetNextProductCodeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return getApp().getAutogenerateProductCode() ? NextProductCodeQuery.getCode(getActivity()) : "";
        }

        @Override
        protected void onPostExecute(String code) {
            productCode.setText(code);
        }
    }

    private class EanLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .projection(ShopStore.ItemTable.GUID)
                    .where(ShopStore.ItemTable.EAN_CODE + " = ?", eanUpc.getText())
                    .where(ShopStore.ItemTable.GUID + " <> ?", getModel().guid)
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateEanUpc = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {}
    }

    private class ProductCodeLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .projection(ShopStore.ItemTable.GUID)
                    .where(ShopStore.ItemTable.PRODUCT_CODE + " = ?", productCode.getText())
                    .where(ShopStore.ItemTable.GUID + " <> ?", getModel().guid)
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateProductCode = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {}
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case ITEM_MATRIX_LOADER_ID:
                    return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ItemMatrixByParentView.URI_CONTENT))
                            .where(ItemMatrixByParentView.CHILD_ITEM_GUID + " = ?", getModel().guid).build(getActivity());
                case ITEM_PARENT_LOADER_ID:
                    return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ItemTable.URI_CONTENT))
                            .where(ItemTable.GUID + " = ?", getModel().referenceItemGuid).build(getActivity());
                default:
                    throw new IllegalArgumentException("Unsupported loader id: "
                            + Integer.toHexString(id));
            }

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case ITEM_MATRIX_LOADER_ID:
                    if (data.moveToFirst()) {
                        referenceItem.setText(data.getString(data.getColumnIndex(ItemMatrixByParentView.ITEM_DESCRIPTION)));
                    }
                    break;
                case ITEM_PARENT_LOADER_ID:
                    if (data.moveToFirst()) {
                        referenceItem.setText(data.getString(data.getColumnIndex(ItemTable.DESCRIPTION)));
                    }
                default:
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

}
