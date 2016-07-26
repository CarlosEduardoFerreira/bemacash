package com.kaching123.tcr.fragment.item;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ItemModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_common_information_fragment)
public class ItemCommonInformationFragment extends ItemBaseFragment {

    @ViewById EditText description;
    @ViewById EditText salesPrice;
    @ViewById Spinner department;
    @ViewById Spinner category;
    @ViewById CheckBox activeStatus;

    @Override
    protected void setViews() {

    }

    @Override
    protected void setCustomer() {
        final ItemModel model = getModel();
        description.setText(model.description);
        showPrice(salesPrice, model.price);
        activeStatus.setChecked(model.isActiveStatus);

    }
}
