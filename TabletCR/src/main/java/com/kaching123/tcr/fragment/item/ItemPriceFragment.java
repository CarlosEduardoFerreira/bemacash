package com.kaching123.tcr.fragment.item;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseItemActivity2;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.PercentFormatInputFilter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.getDecimalValue;
import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_price_fragment)
public class ItemPriceFragment extends ItemBaseFragment {

    @ViewById
    protected CheckBox discountable;
    @ViewById
    protected Spinner discountType;
    @ViewById
    protected EditText discount;
    @ViewById
    protected Spinner priceType;
    @ViewById
    protected CheckBox taxable;
    @ViewById
    protected EditText cost;
    @ViewById
    protected CheckBox commissionEligible;
    @ViewById
    protected EditText commission;
    @ViewById
    protected CheckBox forSale;
    @ViewById
    protected TableRow forSaleRow;

    @Override
    protected void newItem(){}

    @Override
    public void duplicate() {
        if (priceType != null && priceType.getAdapter() != null &&
                discountType != null && discountType.getAdapter() != null) {
            setModel();
            priceType.setSelection(getPriceTypeSelected());
            discountType.setSelection(getDiscountTypeSelected());
            ((BaseItemActivity2) getActivity()).priceInfoReady();
        }
    }

    @Override
    protected void setViews() {
        ArrayAdapter<PriceType> priceTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_light, PriceType.values());
        priceTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        priceType.setAdapter(priceTypeAdapter);
        priceType.setSelection(getPriceTypeSelected());

        ArrayAdapter<DiscountType> discountTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_light, DiscountType.values());
        discountTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        discountType.setAdapter(discountTypeAdapter);
        discountType.setSelection(getDiscountTypeSelected());

        if (getModel().isReferenceItem()) {
            forSale.setChecked(false);
            forSaleRow.setVisibility(View.GONE);
        }

        setFilters();
    }

    private int getPriceTypeSelected() {
        switch (getModel().priceType) {
            case UNIT_PRICE:
                return 2;
            case FIXED:
                return 0;
            case OPEN:
                return 1;
            default:
                return 0;
        }
    }

    private int getDiscountTypeSelected() {
        switch (getModel().discountType) {
            case PERCENT:
                return 0;
            case VALUE:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    protected void setModel() {
        final ItemExModel model = getModel();
        discountable.setChecked(model.isDiscountable);
        showPrice(discount, model.discount);
        taxable.setChecked(model.isTaxable);
        showPrice(cost, model.cost);
        commissionEligible.setChecked(model.commissionEligible);
        showPrice(commission, model.commission);
        forSale.setChecked(model.isSalable);
    }

    @Override
    public void collectData() {
        final ItemModel model = getModel();
        model.isDiscountable = discountable.isChecked();
        model.discountType = (DiscountType) discountType.getSelectedItem();
        model.discount = getDecimalValue(discount);
        model.priceType = (PriceType) priceType.getSelectedItem();
        model.isTaxable = taxable.isChecked();
        model.cost = getDecimalValue(cost);
        model.commissionEligible = commissionEligible.isChecked();
        model.commission = getDecimalValue(commission);
        model.isSalable = forSale.isChecked();
    }

    @Override
    public boolean validateData() {
        if (!TextUtils.isEmpty(discount.getText())) {
            switch ((DiscountType) discountType.getSelectedItem()) {
                case PERCENT:
                    if (parseBigDecimal(discount.getText().toString(), BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) >= 0) {
                        Toast.makeText(getActivity(), R.string.item_activity_alert_discount_less_100_msg, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case VALUE:
                    if (parseBigDecimal(discount.getText().toString(), BigDecimal.ZERO).compareTo(getModel().price) >= 0) {
                        Toast.makeText(getActivity(), R.string.item_activity_alert_discount_less_price_msg, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private void setFilters() {
        InputFilter[] currencyFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        InputFilter[] percentFilter = new InputFilter[]{new PercentFormatInputFilter()};
        discount.setFilters(currencyFilter);
        cost.setFilters(currencyFilter);
        commission.setFilters(percentFilter);
        discount.addTextChangedListener(new CurrencyTextWatcher(discount));
        cost.addTextChangedListener(new CurrencyTextWatcher(cost));
    }

    @ItemSelect
    protected void priceTypeItemSelected(boolean selected, int position) {
        getModel().priceType = (PriceType) priceType.getItemAtPosition(position);
        getItemProvider().onPriceTypeChanged();
    }

    @CheckedChange
    protected void forSaleCheckedChanged(CompoundButton cb, boolean isChecked) {
        getModel().isSalable = isChecked;
    }
}
