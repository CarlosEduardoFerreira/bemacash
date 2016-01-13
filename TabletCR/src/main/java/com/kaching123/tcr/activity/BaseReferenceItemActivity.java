package com.kaching123.tcr.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.parseBrandDecimalInput;


/**
 * Created by aakimov on 27/05/15.
 */

@EActivity(R.layout.inventory_reference_item_activity)
public abstract class BaseReferenceItemActivity extends BaseItemActivity {

    @ViewById
    protected View eanLayout;
    @ViewById
    protected View productCodeLayout;
    @ViewById
    protected View salableLayout;

    @Override
    protected void init() {
        salableChBox.setChecked(false);
        salableLayout.setVisibility(View.GONE);
        eanLayout.setVisibility(View.GONE);
        productCodeLayout.setVisibility(View.GONE);
        super.init();
    }

    protected boolean validateForm() {
        BigDecimal priceValue = parseBrandDecimalInput(salesPrice.getText().toString());

        if (TextUtils.isEmpty(salesPrice.getText()) || priceValue.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(this, R.string.item_activity_alert_description_sales, Toast.LENGTH_SHORT).show();
            return false;
        }

        return super.validateForm();
    }
}
