package com.kaching123.tcr.fragment.item;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.UnitLabelActivity;
import com.kaching123.tcr.adapter.UnitsLabelAdapter;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment;
import com.kaching123.tcr.fragment.inventory.ButtonViewSelectDialogFragment.IButtonViewDialogListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

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

        getLoaderManager().initLoader(0, null, new UnitsLabelLoader());
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
    }

    @Override
    protected void collectData() {
        ItemModel model = getModel();
        model.eanCode = eanUpc.getText().toString();
        model.productCode = productCode.getText().toString();
        model.unitsLabelId = ((UnitLabelModel)unitsType.getSelectedItem()).getGuid();
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
            String unitLabelId;
            if ((unitLabelId = getModel().unitsLabelId) != null){
                int position = unitsLabelAdapter.getPositionById(unitLabelId);
                if (position != AdapterView.INVALID_POSITION)
                    unitsLabel.setSelection(position);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<UnitLabelModel>> loader) {
            unitsLabelAdapter.changeCursor(null);
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
}
