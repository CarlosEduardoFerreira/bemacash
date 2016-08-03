package com.kaching123.tcr.fragment.item;

import android.widget.CheckBox;
import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ItemModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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

    @Override
    protected void setViews() {

    }

    @Override
    protected void setModel() {
        showQuantities();
    }

    @Override
    protected void collectData() {

    }

    public void showQuantities(){
        final ItemModel model = getModel();
        if (monitoring.isChecked()){
            showQuantity(availableQty, model.availableQty, model.isPcsUnit);
            showQuantity(minimumQty, model.minimumQty, model.isPcsUnit);
            showQuantity(recommendedQty, model.recommendedQty, model.isPcsUnit);
        }else{
            availableQty.setText(null);
            minimumQty.setText(null);
            recommendedQty.setText(null);
        }
    }
}
