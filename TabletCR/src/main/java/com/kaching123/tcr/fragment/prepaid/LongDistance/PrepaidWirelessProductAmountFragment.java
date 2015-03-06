package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.PrepaidKeyboardView;
import com.kaching123.tcr.component.TelephoneEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.prepaid.utilities.ProductWirelessAmountItemsPageAdapter;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.viewpagerindicator.LinePageIndicator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidWirelessProductAmountFragment extends PrepaidLongDistanceBaseBodyFragment implements CustomEditBox.IKeyboardSupport {

    @FragmentArg
    protected WirelessItem chosenCategory;

    @ViewById
    protected ImageView icon;
    @ViewById
    protected TextView productName, amountZone, phoneNumber;

    @ViewById
    protected TextView submit;
    @ViewById
    protected PrepaidKeyboardView keyboard;
    @ViewById
    protected CustomEditBox phoneEditView;
    @ViewById
    protected CustomEditBox amountEditView;

    @ViewById
    protected ViewPager viewPager;
    @ViewById
    protected LinePageIndicator viewPagerIndicator;
    private BigDecimal amount;
    private String phoneNumberStr;
    private ProductWirelessAmountItemsPageAdapter wirelessAmountItemsPageAdapter;
    private AmountSelectedCallback listener = new AmountSelectedCallback();
    private List list;
    private final String MIN = "Min";
    private final String MAX = "Max";
    private BigDecimal feeAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.prepaid_wireless_amount_fragement, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    public void init() {
        keyboard.attachEditView(phoneEditView);
        feeAmount = new BigDecimal(chosenCategory.feeAmount);
        updatePhoneViews();
        setPhoneEditView();
        setAmountEditView();
        initFragment();
        updateUI();
    }

    private void updatePhoneViews() {
        if (chosenCategory.isPinBased()) {
            phoneNumber.setVisibility(View.GONE);
            phoneEditView.setVisibility(View.GONE);
        }
    }

    private void initFragment() {
        productName.setText(chosenCategory.name);
        UrlImageViewHelper.setUrlDrawable(icon, chosenCategory.iconUrl, R.drawable.operator_default_icon, 60000);
        wirelessAmountItemsPageAdapter = new ProductWirelessAmountItemsPageAdapter(getActivity(), listener);
        wirelessAmountItemsPageAdapter.setList(getList());
        viewPager.setAdapter(wirelessAmountItemsPageAdapter);
        viewPagerIndicator.setViewPager(viewPager);
    }

    private void updateUI() {
        if (chosenCategory.denominations.length == 1 && (chosenCategory.denominations[0] == BigDecimal.ZERO)) {
            amountEditView.setVisibility(View.VISIBLE);
            amountZone.setVisibility(View.VISIBLE);
            setAmountZone();
            viewPager.setVisibility(View.GONE);
            viewPagerIndicator.setVisibility(View.GONE);
        } else {
            amountEditView.setVisibility(View.GONE);
            if (phoneEditView.getVisibility() == View.GONE)
                keyboard.setVisibility(View.GONE);
            amountZone.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPagerIndicator.setVisibility(View.VISIBLE);
        }

    }

    private void setAmountZone() {
        amountZone.setText(MIN + " " + chosenCategory.minDenomination + "/" + MAX + chosenCategory.maxDenomination);
    }

    private boolean enableFinish() {
        return ((phoneNumber != null && phoneNumberStr != null && !phoneNumberStr.equalsIgnoreCase("")) || chosenCategory.isPinBased()) && (amount != null && amount != BigDecimal.ZERO);
    }

    @AfterTextChange
    protected void phoneEditViewAfterTextChanged(Editable s) {
        longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
        if (s.toString().length() > 0)
            phoneNumberStr = s.toString();
        else
            phoneNumberStr = "";

    }

    @AfterTextChange
    protected void amountEditViewAfterTextChanged(Editable s) {
        longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
        if (s.toString().length() > 0)
            amount = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));
        else
            amount = BigDecimal.ZERO;
        if (amount.compareTo(chosenCategory.maxDenomination) > 0) {
            amountEditView.setText(UiHelper.valueOf(chosenCategory.maxDenomination));
        }
    }

    private void setPhoneEditView() {
        phoneEditView.setKeyboardSupportConteiner(this);
        phoneEditView.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        phoneEditView.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                submit();
                return false;
            }
        });
    }

    private void setAmountEditView() {
        amountEditView.setKeyboardSupportConteiner(this);
        amountEditView.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        amountEditView.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                submit();
                return false;
            }
        });
    }

    private PrepaidLongDistanceProductAmountFragment.LongDistanceProductAmount longDistanceProductAmount;

    public void setCallback(PrepaidLongDistanceProductAmountFragment.LongDistanceProductAmount callback) {
        this.longDistanceProductAmount = callback;
    }

    private List getList() {
        list = new ArrayList();
        for (BigDecimal amount : chosenCategory.denominations) {
            list.add(amount);
        }
        return list;
    }

    @Click
    void submit() {
        if (enableFinish())
            complete();
        else
            error();

    }

    private void error() {
        if (phoneNumber == null && ((amount == BigDecimal.ZERO) || amount == null))
            longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.PHONE_NUMBER_NULL_AND_AMOUNT_ZERO_ERROR);
        else if (phoneNumber == null || phoneNumberStr == null || phoneNumberStr.equalsIgnoreCase(""))
            longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.PHONE_NUMBER_NULL_ERROR);
        else if (amount == null || amount == BigDecimal.ZERO)
            longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_ZERO_ERROR);
        else if (amount != null && amount.compareTo(chosenCategory.minDenomination) < 0) {
            longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.AMOUNT_MINIMUN_ERROR);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
        if (v == phoneEditView) {
            keyboard.setDotEnabled(false);
        } else if (v == amountEditView) {
            keyboard.setDotEnabled(true);
        }
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    public interface AmountSelectedListener {
        void amountSelected(BigDecimal a);
    }

    class AmountSelectedCallback implements AmountSelectedListener {

        @Override
        public void amountSelected(BigDecimal a) {
            amount = a;
            longDistanceProductAmount.headMessage(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
        }
    }

    private void complete() {
        longDistanceProductAmount.conditionSelected(amount, phoneNumberStr, feeAmount);
        longDistanceProductAmount.headMessage(PrepaidLongDistanceHeadFragment.PURCHASE_SUMMARY);
    }

}
