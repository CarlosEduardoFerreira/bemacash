package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.component.TelephoneEditNumberCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItemDenomination;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.WirelessTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class WirelessCategoryFragmentDialog extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport, LoaderCallbacks<List<WirelessItem>> {

    private final String INVALID_COUNTRY = "Country";
    private final String INVALID_CARRIER = "Carrier";
    private final String INVALID_PRODUCT = "Product";
    private final String EMPTY_PHONE_NUMBER = "Phone Number";
    private final String NOT_EQUAL_PHONE_NUMBERS = "The phone numbers do not match";
    private final String EMPTY_AMOUNT = "Amount";
    private final String INVALID_AMOUNT = "Amount";
    private static final String DIALOG_NAME = "WirelessCategoryFragmentDialog";

    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(WirelessTable.URI_CONTENT);


    private static final int DEFAULT_ITEM_POSITION = 0;

    private static final int DEFAULT_LOADER = 0;
    private static final int SUPER_LOADER = 1;
    private ArrayAdapter<WirelessItemDenomination> billerAdapter;

    private List<WirelessItem> superCategories = new ArrayList<WirelessItem>();
    private List<WirelessItem> valuesCategory = new ArrayList<WirelessItem>();

    private List<Country> countries = new ArrayList<Country>();

    @Bean
    protected WirelessItemCursorAdapter categoryAdapter;

    @Bean
    protected SuperCategoryItemCursorAdapter superCategoryAdapter;

    private WirelessItem chosenCategory;

    private WirelessItemDenomination chosenBiller;
    private BigDecimal chosenAmount;
    private SuperCategory chosenSuperCategory;
    private String chosenLocale;

    private BigDecimal minAmount = BigDecimal.ZERO;
    private BigDecimal maxAmount = BigDecimal.ZERO;

    protected WirelessCategoryFragmentDialogCallback callback;


    @FragmentArg
    protected String transactionMode;
    @FragmentArg
    protected String cashierId;
    @FragmentArg
    protected PrepaidUser user;
    @FragmentArg
    protected Boolean longDistance;
    @FragmentArg
    protected Boolean international;
    @FragmentArg
    protected Boolean pinless;

    @ViewById
    protected KeyboardView keyboard;
    @ViewById
    protected Spinner country;
    @ViewById
    protected Spinner category;
    @ViewById
    protected Spinner supercategory;
    @ViewById
    protected Spinner biller;
    @ViewById
    protected CustomEditBox charge;
    @ViewById
    protected TextView limitation;

    @ViewById
    protected TextView dialCountryCodeTextview;
    @ViewById
    protected TextView dialCountryCodeTextviewVerify;
    @ViewById
    protected TextView error;
    @ViewById
    protected TextView errorContent;
    @ViewById
    protected CustomEditBox telephoneInput;
    @ViewById
    protected CustomEditBox telephoneInputValidate;
    @ViewById
    protected LinearLayout linearLayout;
    private String numText;
    private static final int WIRELESS_PROFILE_ID = 15;
    private static int dialogTitle;
    @FragmentArg
    protected Broker broker;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;


    @AfterTextChange
    protected void telephoneInputAfterTextChanged(Editable s) {

        setErrorGone();
        numText = dialCountryCodeTextview.getText().toString() + s.toString();
        if (s == null) {
            enableFinish(false);
            return;
        }
        if (!getTelephoneValidate()) {
            enableFinish(false);
            return;
        }
        try {
            if (chosenAmount != null && minAmount != null)
                if (chosenAmount.compareTo(minAmount) >= 0) {

                    if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                        charge.setText(UiHelper.valueOf(maxAmount));
                        return;
                    }
                    if (getTelephoneValidate()) {
                        enableFinish(true);
                    }
                } else {
                    enableFinish(false);
                }


        } catch (NumberFormatException e) {
            enableFinish(false);
        }
    }

    @AfterTextChange
    protected void telephoneInputValidateAfterTextChanged(Editable s) {
        setErrorGone();
        telephoneInputAfterTextChanged(s);
    }

    public void setCallback(WirelessCategoryFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @AfterViews
    protected void init() {
        setErrorGone();
        keyboard.attachEditView(telephoneInput);
        setTelephoneValidateView();
        setChargeView();
        enableFinish(true);
        setSpinners();
        setTelephoneView();

//        WaitDialogFragment.show(getActivity(), "Loading...");
//        getBillerCategories(getActivity());


        getLoaderManager().restartLoader(DEFAULT_LOADER, null, this);
    }

    private void setViewVisible(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void setErrorVisible() {
        if (!errorContent.getText().toString().equalsIgnoreCase(NOT_EQUAL_PHONE_NUMBERS))
            setViewVisible(error);
        setViewVisible(errorContent);
    }

    private void setErrorGone() {
        setViewGone(error);
        setViewGone(errorContent);
    }

    private void setViewGone(View view) {
        view.setVisibility(View.GONE);
    }

    private boolean getTelephoneValidate() {
        if (chosenCategory == null)
            return false;
        return ((!(chosenCategory.isPinBased()) && (numText != null && numText.length() > dialCountryCodeTextview.length() && numText.length() < 22) && telephoneInput.getText().toString().equalsIgnoreCase(telephoneInputValidate.getText().toString())) ||
                (chosenCategory.isPinBased()));
    }

    protected static int getDialogTitle(Broker broker) {
        switch (broker) {
            case LONG_DISTANCE:
                dialogTitle = R.string.prepaid_dialog_long_distance_title;
                break;
            case PINLESS:
                dialogTitle = R.string.prepaid_dialog_pinless_recharge_title;
                break;
            case WIRELESS_RECHARGE:
                dialogTitle = R.string.prepaid_dialog_wireless_recharge_title;
                break;
            case INTERNATIONAL_TOPUP:
                dialogTitle = R.string.prepaid_dialog_wireless_recharge_international_title;
                break;
            default:
                dialogTitle = 0;
        }
        return dialogTitle;
    }

    protected void setSpinners() {
        biller.setEnabled(false);
        category.setEnabled(false);
        keyboard.setEnabled(false);

        if (longDistance || pinless)
            biller.setVisibility(View.GONE);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ArrayList<WirelessItemDenomination> billerValues = new ArrayList<WirelessItemDenomination>(1);
        billerValues.add(DefaultAmount.DEFAULT);

        billerAdapter = new ArrayAdapter<WirelessItemDenomination>(getActivity(), R.layout.prepaid_spinner_item, billerValues);
        billerAdapter.setDropDownViewResource(R.layout.prepaid_spinner_dropdown_item);

        biller.setAdapter(billerAdapter);
        biller.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setErrorGone();
                if (position == DEFAULT_ITEM_POSITION) {
                    limitation.setText("");
                    resetInput(false);
                    charge.setText("");
                    chosenAmount = null;
                    return;
                }
                chosenBiller = (WirelessItemDenomination) parent.getAdapter().getItem(position);
                if (chosenBiller.denomination != null && chosenBiller.denomination.compareTo(BigDecimal.ZERO) > 0) {
                    chosenAmount = chosenBiller.denomination;
                    charge.setText(UiHelper.valueOf(chosenAmount));
                    return;
                }
                if (chosenBiller.min.compareTo(BigDecimal.ZERO) >= 0 && chosenBiller.max.compareTo(BigDecimal.ZERO) > 0) {
                    minAmount = chosenBiller.min;
                    maxAmount = chosenBiller.max;
                    limitation.setText(getString(R.string.prepaid_dialog_amount_limitations, UiHelper.valueOf(minAmount), UiHelper.valueOf(maxAmount)));
                } else {
                    limitation.setText(R.string.prepaid_dialog_amount_no_limitation);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        category.setAdapter(categoryAdapter);
        category.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setErrorGone();
                if (position == 0) {
                    if (longDistance)
                        biller.setVisibility(View.GONE);
                    chosenCategory = null;
                    clearBills();
                    enableEditViews(chosenCategory, false);
                    resetInput(false);
                } else {
                    if (longDistance)
                        biller.setVisibility(View.VISIBLE);
                    selectCategory(position);
                    chosenCategory = categoryAdapter.getItem(position);
                    Logger.d("trace chosenCategory:" + chosenCategory.carrierName + " " + chosenCategory.name);
                    enableEditViews(chosenCategory, true);
                }

                if (international) {
                    keyboard.setEnabled(true);
                    keyboard.setDotEnabled(false);
                }
//                keyboard.attachEditView(telephoneInput);

            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (longDistance || pinless || international)
            supercategory.setVisibility(View.GONE);
        supercategory.setAdapter(superCategoryAdapter);
        supercategory.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                biller.setVisibility(View.VISIBLE);
                chosenSuperCategory = (SuperCategory) parent.getAdapter().getItem(position);
                clearCategories();
                clearBills();
                WirelessCategoryFragmentDialog.this.getLoaderManager().restartLoader(DEFAULT_LOADER, null, WirelessCategoryFragmentDialog.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (!international)
            country.setVisibility(View.GONE);
        country.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setErrorGone();
                selectCountry(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> ignore) {
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void enableEditViews(WirelessItem chosenCategory, boolean isSuperOpen) {
        if (isSuperOpen) {
            setPinBasedView();
            if (chosenCategory.denominations() != null && chosenCategory.denominations().size() != 0 && chosenCategory.denominations().get(0).toString().contains("-")) {
                biller.setVisibility(View.GONE);
                billerAdapter.clear();
                billerAdapter.addAll(chosenCategory.denominations());
                minAmount = chosenCategory.minDenomination;
                maxAmount = chosenCategory.maxDenomination;
                charge.setVisibility(View.VISIBLE);
                charge.setHint(getString(R.string.prepaid_dialog_amount_limitations, UiHelper.valueOf(minAmount), UiHelper.valueOf(maxAmount)));
                resetInput(true);
            } else {
                biller.setVisibility(View.VISIBLE);
                minAmount = new BigDecimal(chosenCategory.denominations().get(0).toString());
                maxAmount = new BigDecimal(chosenCategory.denominations().get(chosenCategory.denominations().size() - 1).toString());
                charge.setVisibility(View.GONE);
                resetInput(!chosenCategory.isPinBased() || false);
            }


        } else {
            SetSuperRelatedView(false);
            resetInput(false);
        }
        setPinLessView();
        setLongDistanceView();
        enableFinish(false);
    }

    private void setLongDistanceView() {
        if (longDistance)
            linearLayout.setVisibility(View.VISIBLE);
    }

    private void SetSuperRelatedView(boolean visible) {
        if (visible) {
            charge.setVisibility(View.VISIBLE);
            if (!pinless) {
                dialCountryCodeTextview.setVisibility(View.VISIBLE);
                dialCountryCodeTextviewVerify.setVisibility(View.VISIBLE);
            }
            telephoneInput.setVisibility(View.VISIBLE);
            telephoneInputValidate.setVisibility(View.VISIBLE);
        } else {
            charge.setVisibility(View.GONE);
            dialCountryCodeTextview.setVisibility(View.GONE);
            dialCountryCodeTextviewVerify.setVisibility(View.GONE);
            telephoneInput.setVisibility(View.GONE);
            telephoneInputValidate.setVisibility(View.GONE);
        }
    }

    private void setPinBasedView() {
        if (!chosenCategory.isPinBased()) {
            dialCountryCodeTextview.setVisibility(View.VISIBLE);
            dialCountryCodeTextviewVerify.setVisibility(View.VISIBLE);
            dialCountryCodeTextview.setText(chosenCategory.dialCountryCode);
            dialCountryCodeTextviewVerify.setText(chosenCategory.dialCountryCode);
            resetInput(true);
            telephoneInput.setVisibility(View.VISIBLE);
            telephoneInputValidate.setVisibility(View.VISIBLE);
        } else {
            dialCountryCodeTextview.setVisibility(View.GONE);
            dialCountryCodeTextviewVerify.setVisibility(View.GONE);
            resetInput(false);
            telephoneInput.setVisibility(View.GONE);
            telephoneInputValidate.setVisibility(View.GONE);
        }

    }

    private void setPinLessView() {
        if (pinless) {
            SetSuperRelatedView(true);
        }
    }

    private void selectCategory(int position) {
        chosenCategory = categoryAdapter.getItem(position);

        clearBills();
    }

    private void selectCountry(int position) {
        Country country = (Country) this.country.getAdapter().getItem(position);

        chosenLocale = position == 0 ? null : country.code;

        clearSuperCategories();
        clearCategories();
        clearBills();

        WirelessCategoryFragmentDialog.this.getLoaderManager().restartLoader(SUPER_LOADER, null, WirelessCategoryFragmentDialog.this);
    }

    private void clearSuperCategories() {
        supercategory.setSelection(0);
        supercategory.setEnabled(true);
    }

    private void clearCategories() {
        category.setSelection(0);
        category.setEnabled(true);
    }

    private void clearBills() {
        keyboard.setEnabled(false);
        billerAdapter.clear();
        billerAdapter.add(DefaultAmount.DEFAULT);
        if (chosenCategory != null) {
            billerAdapter.addAll(chosenCategory.denominations());
        }

        biller.setSelection(0);
        biller.setEnabled(true);
    }

    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                if (isValid())
                    callback.onConditionSelected(chosenCategory, chosenAmount, numText, WIRELESS_PROFILE_ID, BigDecimal.ZERO);
                else {
                    setErrorVisible();
                }
                return true;
            }
        });
    }

    private void setTelephoneView() {
        telephoneInput.setKeyboardSupportConteiner(this);
        telephoneInput.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        telephoneInput.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                callback.onConditionSelected(chosenCategory, chosenAmount, numText, WIRELESS_PROFILE_ID, BigDecimal.ZERO);
                return true;
            }
        });
    }

    private void setTelephoneValidateView() {
        telephoneInputValidate.setKeyboardSupportConteiner(this);
        telephoneInputValidate.setFilters(new InputFilter[]{new TelephoneEditNumberCurrencyFormatInputFilter()});
        telephoneInputValidate.setEditListener(new CustomEditBox.IEditListener() {

            @Override
            public boolean onChanged(String text) {
                callback.onConditionSelected(chosenCategory, chosenAmount, numText, WIRELESS_PROFILE_ID, BigDecimal.ZERO);
                return true;
            }
        });
    }

    private void resetInput(boolean enabled) {
        keyboard.setEnabled(enabled);
        charge.setText("");

        linearLayout.setVisibility(View.GONE);
        if (dialCountryCodeTextview != null && chosenCategory != null) {
            dialCountryCodeTextview.setText(chosenCategory.dialCountryCode);
            dialCountryCodeTextviewVerify.setText(chosenCategory.dialCountryCode);
        }

        if (telephoneInput != null) {
            telephoneInput.setText("");
            telephoneInputValidate.setText("");
        }
    }

    @AfterTextChange
    protected void chargeAfterTextChanged(Editable s) {
        setErrorGone();
        if (s == null) {
            enableFinish(false);
            return;
        }
        if (!getTelephoneValidate()) {
            enableFinish(false);
        }
        try {
            BigDecimal amount;
            if (s.toString().length() > 0)
                amount = new BigDecimal(UiHelper.valueOf(new BigDecimal(s.toString())));
            else
                amount = new BigDecimal(0);
            if (BigDecimal.ZERO.compareTo(amount) >= 0) {
                enableFinish(false);
                return;
            }

            assert minAmount != null;
            assert maxAmount != null;
            chosenAmount = amount;
            if (chosenAmount.compareTo(minAmount) >= 0) {

                if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && chosenAmount.compareTo(maxAmount) > 0) {
                    charge.setText(UiHelper.valueOf(maxAmount));
                    return;
                }
                if (getTelephoneValidate()) {
                    enableFinish(true);
                }
            } else {
                enableFinish(false);
            }
        } catch (NumberFormatException e) {
            enableFinish(false);
        }
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        if (v == telephoneInput || v == telephoneInputValidate) {
            keyboard.setDotEnabled(false);
        } else if (v == charge) {
            keyboard.setDotEnabled(true);
        }
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getNegativeButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTypeface(Typeface.DEFAULT_BOLD);
        getPositiveButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_category_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getTitleIcon() {
        switch (dialogTitle) {
            case R.string.prepaid_dialog_long_distance_title:
                return R.drawable.icon_long_distance;
            case R.string.prepaid_dialog_pinless_recharge_title:
                return R.drawable.icon_pinless;
            case R.string.prepaid_dialog_wireless_recharge_title:
                return R.drawable.icon_wireless_recharge;
            case R.string.prepaid_dialog_wireless_recharge_international_title:
                return R.drawable.icon_international_topup;
        }
        return 0;
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    @Override
    protected boolean hasTitleTextTypeface() {
        return true;
    }

    @Override
    protected boolean hasNegativeButtonTextTypeface() {
        return true;
    }

    @Override
    protected int getTitleViewBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_title_background_color);
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @Override
    protected int getButtonsBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_buttons_background_color);
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_next;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.wireless_category_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return dialogTitle;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (isValid())
                    callback.onConditionSelected(chosenCategory, chosenAmount, numText, WIRELESS_PROFILE_ID, BigDecimal.ZERO);
                else {
                    setErrorVisible();
                }
                return false;
            }
        };
    }

    private boolean isValid() {
        if (international) {
//            if(chosenLocale == null || chosenLocale.length() == 0)
//            {
//                errorContent.setText(INVALID_COUNTRY);
//                return false;
//            }
            if (chosenCategory == null || chosenCategory.name.equals(DefaultCategory.DEFAULT.name)) {
                errorContent.setText(INVALID_PRODUCT);
                return false;
            }
            if (telephoneInput.length() == 0) {
                errorContent.setText(EMPTY_PHONE_NUMBER);
                return false;
            }
            if (!telephoneInput.getText().toString().equals(telephoneInputValidate.getText().toString())) {
                errorContent.setText(NOT_EQUAL_PHONE_NUMBERS);
                return false;
            }
        } else if (pinless) {
            if (chosenCategory == null || chosenCategory.name.equals(DefaultCategory.DEFAULT.name)) {
                errorContent.setText(INVALID_PRODUCT);
                return false;
            }
            if (telephoneInput.length() == 0) {
                errorContent.setText(EMPTY_PHONE_NUMBER);
                return false;
            }
            if (!telephoneInput.getText().toString().equals(telephoneInputValidate.getText().toString())) {
                errorContent.setText(NOT_EQUAL_PHONE_NUMBERS);
                return false;
            }
        } else if (longDistance) {
            if (chosenCategory == null || chosenCategory.name.equals(DefaultCategory.DEFAULT.name)) {
                errorContent.setText(INVALID_PRODUCT);
                return false;
            }
        } else {
//            if(chosenSuperCategory == null || chosenSuperCategory.carrierName.equals(DefaultSuperCategory.DEFAULT.carrierName))
//            {
//                errorContent.setText(INVALID_CARRIER);
//                return false;
//            }
            if (chosenCategory == null || chosenCategory.name.equals(DefaultCategory.DEFAULT.name)) {
                errorContent.setText(INVALID_PRODUCT);
                return false;
            }
            if (chosenCategory != null && chosenCategory.name != DefaultCategory.DEFAULT.name) {
                if (!chosenCategory.isPinBased()) {
                    if (telephoneInput.length() == 0) {
                        errorContent.setText(EMPTY_PHONE_NUMBER);
                        return false;
                    }
                    if (!telephoneInput.getText().toString().equals(telephoneInputValidate.getText().toString())) {
                        errorContent.setText(NOT_EQUAL_PHONE_NUMBERS);
                        return false;
                    }
                }
            }
        }

        if (chosenAmount == null || chosenAmount == BigDecimal.ZERO) {
            if (international || longDistance)
                errorContent.setText(EMPTY_AMOUNT + " invalid, range must be from " + chosenCategory.denominations().get(0) + " to " + chosenCategory.denominations().get(chosenCategory.denominations().size() - 1));
            else
                errorContent.setText(EMPTY_AMOUNT + " invalid, range must be from " + minAmount + " to " + maxAmount);
            return false;
        }
        if (chosenAmount.doubleValue() < minAmount.doubleValue()) {
            if (international || longDistance)
                errorContent.setText(EMPTY_AMOUNT + " invalid, range must be from " + chosenCategory.denominations().get(0) + " to " + chosenCategory.denominations().get(chosenCategory.denominations().size() - 1));
            else
                errorContent.setText(INVALID_AMOUNT + " invalid, range must be from " + minAmount + " to " + maxAmount);
            return false;
        }
        return true;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    public static void show(FragmentActivity context,
                            String transactionMode,
                            String cashierId,
                            PrepaidUser user,
                            Boolean international,
                            Boolean longDistance,
                            Boolean pinless,
                            Broker broker,
                            WirelessCategoryFragmentDialogCallback listener) {
        getDialogTitle(broker);
        WirelessCategoryFragmentDialog dialog = WirelessCategoryFragmentDialog_.builder()
                .transactionMode(transactionMode)
                .longDistance(longDistance)
                .pinless(pinless)
                .cashierId(cashierId)
                .user(user)
                .international(international)
                .broker(broker)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected void enableFinish(Boolean enabled) {
        getPositiveButton().setEnabled(true);
        getPositiveButton().setTextColor(colorOk);
//        getPositiveButton().setTextColor(enabled ? colorOk : colorDisabled);
        keyboard.setEnterEnabled(enabled);
    }

    @Override
    public Loader<List<WirelessItem>> onCreateLoader(final int i, Bundle bundle) {

        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_ORDER_ITEMS);
        if (chosenLocale != null && chosenLocale.length() > 0) {
            loader.where(WirelessTable.COUNTRYCODE + " = ?", chosenLocale);
        }

        if (chosenSuperCategory != null && !chosenSuperCategory.carrierName.equals(DefaultSuperCategory.DEFAULT.carrierName))
            loader.where(WirelessTable.CARRIERNAME + " = ?", chosenSuperCategory.carrierName);


        return loader.orderBy(WirelessTable.NAME)
                .transform(new Function<Cursor, List<WirelessItem>>() {
                    @Override
                    public List<WirelessItem> apply(Cursor c) {
                        List<WirelessItem> items = new ArrayList<WirelessItem>(c.getCount());
                        Set<String> localCache = new HashSet<String>();
                        Set<String> localCodeCache = new HashSet<String>();
                        List<SuperCategory> superItemsTmp = new ArrayList<SuperCategory>();
                        if (i == SUPER_LOADER) {
                            superCategories.clear();
//                            superCategories.add(DefaultSuperCategory.DEFAULT);
                        }
                        boolean shouldUpdate = countries.size() == 0;
                        while (c.moveToNext()) {
                            WirelessItem item = new WirelessItem(c);

                            if ((international && item.isWirelessInternational())
                                    || (pinless && item.isPinless())
                                    || (longDistance && item.isLongDistance())
                                    || (!pinless && !international && !longDistance && item.isWireless())) {
                                items.add(item);
                                String token = item.carrierName;
                                if (i == SUPER_LOADER && !localCache.contains(token)) {
                                    SuperCategory superCategory = new SuperCategory(c);
                                    localCache.add(token);
                                    superItemsTmp.add(superCategory);
                                }

                                if (!shouldUpdate)
                                    continue;
                                if (!localCodeCache.contains(item.countryCode)) {
                                    localCodeCache.add(item.countryCode);
                                    countries.add(new Country(item.countryCode, item.countryName));
                                }

                            }
                        }
                        if (shouldUpdate) {
                            Collections.sort(countries);
                            countries.add(0, Country.DEFAULT);
                        }
                        if (i == SUPER_LOADER) {
                            Collections.sort(superItemsTmp);
                            superCategories.add(DefaultSuperCategory.DEFAULT);
                            superCategories.addAll(superItemsTmp);
//                            superCategories.add(DefaultSuperCategory.DEFAULT);
                        }
                        return items;
                    }
                }).build(getActivity());

    }

    @Override
    public void onLoadFinished(Loader<List<WirelessItem>> listLoader, List<WirelessItem> wirelessItems) {
        boolean sameCategorySelection = category.getSelectedItemPosition() == 0;
        wirelessItems.add(0, DefaultCategory.DEFAULT);
        categoryAdapter.changeCursor(wirelessItems);
        category.setEnabled(true);
        superCategoryAdapter.changeCursor(superCategories);
        supercategory.setEnabled(true);

        if (sameCategorySelection && !categoryAdapter.isEmpty()) {
            selectCategory(category.getSelectedItemPosition());
        }

        if (countries.size() > 0 && country.getSelectedItemPosition() < 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    CountryAdapter adapter = new CountryAdapter(getActivity());
                    adapter.changeCursor(countries);
                    country.setAdapter(adapter);
                    country.setSelection(0);
                    if (!international)
                        selectCountry(0);
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<List<WirelessItem>> listLoader) {
        Logger.d("onLoaderReset");
        if (getActivity() == null)
            return;
        categoryAdapter.changeCursor(null);
    }

    public interface WirelessCategoryFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onConditionSelected(WirelessItem chosenCategory,
                                                 BigDecimal amount, String phoneNumber, int profileId, BigDecimal transactionFee);
    }

    private class CountryAdapter extends ObjectsCursorAdapter<Country> {

        public CountryAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.prepaid_spinner_item, parent, false);
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.prepaid_spinner_dropdown_item, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, Country item) {
            TextView countryLabel = (TextView) convertView;
            countryLabel.setText(item.name);
            return convertView;
        }
    }

    private static class Country implements Comparable<Country> {

        public static final Country DEFAULT = new Country("", "Select Country");

        public final String code;
        public final String name;

        private Country(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public int compareTo(Country another) {
            return name.compareTo(another.name);
        }
    }

    public static class DefaultCategory extends WirelessItem {

        protected static final DefaultCategory DEFAULT = new DefaultCategory();

        public DefaultCategory() {
            name = "Select Product";
        }
    }

    public static class DefaultSuperCategory extends SuperCategory {

        protected static final DefaultSuperCategory DEFAULT = new DefaultSuperCategory();

        public DefaultSuperCategory() {
            carrierName = "Select Carrier ";
        }
    }

    private static class SuperCategory extends WirelessItem implements Comparable<SuperCategory> {

        public SuperCategory(Cursor c) {
            super(c);
        }

        public SuperCategory() {
            super();
        }

        @Override
        public String toString() {
            return carrierName;
        }

        @Override
        public int compareTo(SuperCategory another) {
            return carrierName.compareTo(another.carrierName);
        }
    }

    public static class DefaultAmount extends WirelessItemDenomination {

        protected static final DefaultAmount DEFAULT = new DefaultAmount();

        @Override
        public String toString() {
            return "Select Amount";
        }
    }
}
