package com.kaching123.tcr.fragment.item;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.ItemModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;
import static com.kaching123.tcr.util.UnitUtil.isPcs;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_monitoring_fragment)
public class ItemMonitoringFragment extends ItemBaseFragment{

    @ViewById protected CheckBox monitoring;
    @ViewById protected EditText availableQty;
    @ViewById protected EditText minimumQty;
    @ViewById protected EditText recommendedQty;

    @Override
    protected void setViews() {
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
        showQuantities();
    }

    @Override
    protected void collectData() {
        final ItemModel model = getModel();
        model.minimumQty = parseBigDecimal(minimumQty, BigDecimal.ZERO);
        model.recommendedQty = parseBigDecimal(recommendedQty, BigDecimal.ZERO);
    }

    public void showQuantities(){
        final ItemModel model = getModel();
        if (model.isStockTracking){
            boolean isPcs = isPcs(model.priceType);
            showQuantity(availableQty, model.availableQty, isPcs);
            showQuantity(minimumQty, model.minimumQty, isPcs);
            showQuantity(recommendedQty, model.recommendedQty, isPcs);
        }else{
            availableQty.setText(null);
            minimumQty.setText(null);
            recommendedQty.setText(null);
        }
    }
}
