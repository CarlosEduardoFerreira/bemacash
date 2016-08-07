package com.kaching123.tcr.fragment.item;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseItemActivity2;
import com.kaching123.tcr.activity.UnitLabelActivity;
import com.kaching123.tcr.adapter.UnitsLabelAdapter;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment.IButtonViewDialogListener;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.getDecimalValue;
import static com.kaching123.tcr.fragment.UiHelper.showIntegralInteger;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_additional_information_fragment)
public class ItemAdditionalInformationFragment extends ItemBaseFragment {

    @ViewById protected EditText eanUpc;
    @ViewById protected EditText productCode;
    @ViewById protected Spinner unitsLabel;
    @ViewById protected Spinner unitsType;
    @ViewById protected TextView referenceItem;
    @ViewById protected View buttonView;
    @ViewById protected CheckBox ebtEligible;
    @ViewById protected EditText bonusPoints;
    @ViewById protected CheckBox excludeFromLoyaltyPlan;

    private UnitsLabelAdapter unitsLabelAdapter;

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
        unitsType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onUnitTypeSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getLoaderManager().initLoader(0, null, new UnitsLabelLoader());
        if (getItemProvider().isCreate()){
            new GetNextProductCodeTask().execute();
        }
    }

    @Override
    protected void setModel() {
        ItemExModel model = getModel();
        eanUpc.setText(model.eanCode);
        productCode.setText(model.productCode);
        if (model.codeType != null)
            unitsType.setSelection(model.codeType.ordinal() + 1);
        buttonView.getBackground().setLevel(model.btnView);
        showIntegralInteger(bonusPoints, model.loyaltyPoints);
        excludeFromLoyaltyPlan.setChecked(model.excludeFromLoyaltyPlan);
    }

    @Override
    protected void collectData() {
        ItemModel model = getModel();
        model.eanCode = eanUpc.getText().toString();
        model.productCode = productCode.getText().toString();
        model.unitsLabelId = ((UnitLabelModel)unitsType.getSelectedItem()).getGuid();
        model.btnView = buttonView.getBackground().getLevel();
        model.loyaltyPoints = getDecimalValue(bonusPoints);
        model.excludeFromLoyaltyPlan = excludeFromLoyaltyPlan.isChecked();
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

    private class UnitsLabelLoader implements LoaderCallbacks<List<UnitLabelModel>> {

        @Override
        public Loader<List<UnitLabelModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder
                    .forUri(ShopProvider.contentUri(UnitLabelTable.URI_CONTENT))
                    .orderBy(ShopStore.UnitLabelTable.SHORTCUT)
                    .transform(new UnitLabelFunction())
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

    void onUnitTypeSelected(){
        final CodeType codeType = ((SerializationTypeHolder) unitsType.getSelectedItem()).type;
        final CodeType oldCodeType = getModel().codeType;
        getModel().codeType = codeType;
        getModel().serializable = codeType != null;
        getActivity().invalidateOptionsMenu();
        ((BaseItemActivity2) getActivity()).reloadItem();
    }

    void onUnitTypeSelected2(){
        final CodeType oldCodeType = getModel().codeType;
        final CodeType newCodeType = ((SerializationTypeHolder) unitsType.getSelectedItem()).type;

        CollectUnitsCommand.start(getActivity(), null, getModel().guid, null, null, null, false, false, new CollectUnitsCommand.UnitCallback() {
            @Override
            protected void handleSuccess(final List<Unit> unit) {

                if (oldCodeType != null && newCodeType == null && !unit.isEmpty()) {
                    AlertDialogWithCancelFragment.showWithTwo(getActivity(),
                            R.string.wireless_remove_items_title,
                            getString(R.string.wireless_remove_items_body),
                            R.string.btn_ok,
                            new AlertDialogWithCancelFragment.OnDialogListener() {
                                @Override
                                public boolean onClick() {
                                    getModel().codeType = newCodeType;
                                    DropUnitsCommand.start(getActivity(),
                                            (ArrayList<Unit>) unit, getModel(), new DropUnitsCommand.UnitCallback() {
                                                @Override
                                                protected void handleSuccess(ItemExModel model) {
                                                    Toast.makeText(getActivity(), getString(R.string.unit_drop_ok), Toast.LENGTH_LONG).show();
                                                    getModel().serializable = false;
                                                    getModel().availableQty = BigDecimal.ZERO;
                                                    getActivity().invalidateOptionsMenu();
                                                    onSerializableSet(false);
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
                                    int oldPosition = oldCodeType == null ? null : oldCodeType.ordinal() + 1;
                                    unitsType.setSelection(oldPosition);
                                    return true;
                                }
                            }
                    );

                } else if (oldCodeType == null && newCodeType != null) {
                    getModel().codeType = newCodeType;
                    getModel().serializable = true;
                    getModel().availableQty = new BigDecimal(unit.size());
                    getActivity().invalidateOptionsMenu();
                    onSerializableSet(true);
                } else {
                    onSerializableSet((getModel().codeType = newCodeType) != null);
                    getActivity();
                }
            }

            @Override
            protected void handleError() {
                Toast.makeText(getActivity(), getString(R.string.unit_drop_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onSerializableSet(boolean isSerializable){

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

    class GetNextProductCodeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return NextProductCodeQuery.getCode(getActivity());
        }

        @Override
        protected void onPostExecute(String code) {
            productCode.setText(code);
        }
    }
}
