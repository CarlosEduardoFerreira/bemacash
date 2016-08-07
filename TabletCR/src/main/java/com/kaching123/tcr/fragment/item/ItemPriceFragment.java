package com.kaching123.tcr.fragment.item;

import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.PercentFormatInputFilter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PriceType;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.getDecimalValue;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_price_fragment)
public class ItemPriceFragment extends ItemBaseFragment {

    @ViewById protected CheckBox discountable;
    @ViewById protected Spinner discountType;
    @ViewById protected EditText discount;
    @ViewById protected Spinner priceType;
    @ViewById protected CheckBox taxable;
    @ViewById protected EditText cost;
    @ViewById protected CheckBox commissionEligible;
    @ViewById protected EditText commission;
    @ViewById protected CheckBox forSale;


    @Override
    protected void setViews() {
        ArrayAdapter<PriceType> priceTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_light, PriceType.values());
        priceTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        priceType.setAdapter(priceTypeAdapter);

        ArrayAdapter<DiscountType> discountTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_light, DiscountType.values());
        discountTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        discountType.setAdapter(discountTypeAdapter);

        setFilters();
    }

    @Override
    protected void setModel() {
        final ItemExModel model = getModel();
        discountable.setChecked(model.isDiscountable);
        showPrice(discount, model.discount);
        taxable.setChecked(model.isTaxable);
        showPrice(cost, model.cost);
        commissionEligible.setChecked(model.commissionEligible);
        showPrice(cost, model.commission);
        forSale.setChecked(model.isSalable);
    }

    @Override
    protected void collectData() {
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

    private void setFilters(){
        InputFilter[] currencyFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        InputFilter[] percentFilter = new InputFilter[]{new PercentFormatInputFilter()};
        discount.setFilters(currencyFilter);
        cost.setFilters(currencyFilter);
        commission.setFilters(percentFilter);
        discount.addTextChangedListener(new CurrencyTextWatcher(discount));
        cost.addTextChangedListener(new CurrencyTextWatcher(cost));
    }

    @ItemSelect
    protected void priceTypeItemSelected(boolean selected, int position){
        getModel().priceType = (PriceType) priceType.getItemAtPosition(position);
        getItemProvider().updateQtyBlock();
    }
}
