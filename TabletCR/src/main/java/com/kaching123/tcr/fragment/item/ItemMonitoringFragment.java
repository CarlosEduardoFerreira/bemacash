package com.kaching123.tcr.fragment.item;

import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseItemActivity2;
import com.kaching123.tcr.activity.ComposerActivity;
import com.kaching123.tcr.activity.UnitActivity;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.RegisterQtyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.inventory.InventoryQtyEditDialog;
import com.kaching123.tcr.fragment.inventory.InventoryQtyEditDialog.OnEditQtyListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_monitoring_fragment)
public class ItemMonitoringFragment extends ItemBaseFragment{

    @ViewById protected CheckBox monitoring;
    @ViewById protected EditText availableQty;
    @ViewById protected EditText minimumQty;
    @ViewById protected EditText recommendedQty;

    private EditText[] qtyViews;

    @Override
    protected void setViews() {

        qtyViews = new EditText[]{availableQty, minimumQty, recommendedQty};

        monitoring.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getModel().isStockTracking= isChecked;
                showQuantities();
            }
        });

        minimumQty.addTextChangedListener(new BrandTextWatcher(minimumQty, true) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                if (getModel().isStockTracking) {
                    getModel().minimumQty = UiHelper.getDecimalValue(s);
                }
            }
        });

        recommendedQty.addTextChangedListener(new BrandTextWatcher(recommendedQty, true) {
            @Override
            public synchronized void onTextChanged(CharSequence s, int start, int count, int after) {
                super.beforeTextChanged(s, start, count, after);
                if (getModel().isStockTracking ) {
                    getModel().recommendedQty = UiHelper.getDecimalValue(s);
                }
            }
        });
    }

    @Override
    protected void setModel() {
        final ItemExModel model = getModel();
        monitoring.setChecked(model.isStockTracking);
        updateQty();
    }

    @Override
    public void collectData() {
        final ItemModel model = getModel();
        model.minimumQty = parseBigDecimal(minimumQty, BigDecimal.ZERO);
        model.recommendedQty = parseBigDecimal(recommendedQty, BigDecimal.ZERO);
        model.availableQty = getItemProvider().getQtyInfo().availableQty;
    }

    public void updateQty(){
        final int inputType = getModel().isPcsUnit() ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_PHONE;
        final InputFilter[] filter = new InputFilter[]{getModel().isPcsUnit() ? new InputFilter.LengthFilter(10) : new RegisterQtyFormatInputFilter()};
        for (EditText v : qtyViews){
            v.setInputType(inputType);
            v.setFilters(filter);
        }
        showQuantities();
    }

    public void showQuantities(){
        final ItemModel model = getModel();

        availableQty.setEnabled(model.isStockTracking);
        availableQty.setEnabled(model.isStockTracking);
        availableQty.setEnabled(model.isStockTracking);

        if (model.isStockTracking){
            showQuantity(availableQty, getItemProvider().getQtyInfo().availableQty, model.isPcsUnit());
            showQuantity(minimumQty, model.minimumQty, model.isPcsUnit());
            showQuantity(recommendedQty, model.recommendedQty, model.isPcsUnit());
        }else{
            availableQty.setText(null);
            minimumQty.setText(null);
            recommendedQty.setText(null);
        }

        //set onClickListener
        if (getItemProvider().isCreate()){
            if (getModel().codeType == null){
                availableQty.setFocusable(true);
                availableQty.setOnClickListener(null);
            }else{
                availableQty.setFocusable(false);
                availableQty.setOnClickListener(clickListener);
            }
        }else{
            availableQty.setFocusable(false);
            availableQty.setOnClickListener(clickListener);
        }
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getModel().codeType != null){
                onSerialQtyClicked();
                return;
            }else if (getItemProvider().getQtyInfo().restrictComposersCount > 0){
                onComposedQtyClicked();
                return;
            }else {
                onQtyClicked();
                return;
            }
        }
    };

    private void onSerialQtyClicked() {
        if (getItemProvider().isCreate()){
            Toast.makeText(getActivity(), "Item should be saved first", Toast.LENGTH_SHORT).show();
        }else{
            UnitActivity.start(getActivity(), getModel(), BaseItemActivity2.TAG_RESULT_SERIAL);
        }
    }

    private void onComposedQtyClicked() {
        ComposerActivity.start(getActivity(), getModel(), BaseItemActivity2.TAG_RESULT_COMPOSER);
    }

    private void onQtyClicked() {
        InventoryQtyEditDialog.show(getActivity(),
                getModel().availableQty == null ? BigDecimal.ZERO : getModel().availableQty,
                getModel().isPcsUnit(),
                new OnEditQtyListener() {
                    @Override
                    public void onReplace(BigDecimal value) {
                        getModel().availableQty = value;
                        getItemProvider().onStockTypeChanged();
                    }

                    @Override
                    public void onAdjust(BigDecimal value) {
                        getModel().availableQty = getModel().availableQty.add(value);
                        getItemProvider().onStockTypeChanged();
                    }
                });
    }
}
