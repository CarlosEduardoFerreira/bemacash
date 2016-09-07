package com.kaching123.tcr.fragment.tendering.payment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.CustomEditBox.IEditListener;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.fragment.employee.EmployeeTipsFragmentDialog;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 19.06.2014.
 */
@EFragment
public class ApplyTipsFragmentDialog extends KeyboardDialogFragment {

    private static final String DIALOG_NAME = EmployeeTipsFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected String orderGuid;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    @ViewById
    protected TextView subtotal;
    @ViewById
    protected TextView tax;
    @ViewById
    protected TextView total;
    @ViewById
    protected CustomEditBox tipsEditbox;
    @ViewById
    protected Spinner employeeSpinner;
    @ViewById
    protected RadioGroup tipsGroup;
    @ViewById
    protected RadioButton tip0;
    @ViewById
    protected RadioButton tip1;
    @ViewById
    protected RadioButton tip2;
    @ViewById
    protected RadioButton tipCustom;
    @ViewById
    protected Switch percentValueSwitch;

    private IApplyTipsListener listener;

    private EmployeeAdapter employeeAdapter;

    private RadioButton[] radioArray = new RadioButton[3];
    private int[] tipsArray;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal tipsAmount;
    private BigDecimal totalAmount;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.employee_tips_dialog_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews(){
        tipsArray = getActivity().getResources().getIntArray(R.array.tips_predefine);
        radioArray[0] = tip0;
        radioArray[1] = tip1;
        radioArray[2] = tip2;

        tip0.setOnCheckedChangeListener(predefineTipCheckedListener);
        tip1.setOnCheckedChangeListener(predefineTipCheckedListener);
        tip2.setOnCheckedChangeListener(predefineTipCheckedListener);
        tipCustom.setOnCheckedChangeListener(customTipCheckedListener);

        percentValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recalcTipsAndTotal();
            }
        });

        tipsEditbox.setKeyboardSupportConteiner(this);
        tipsEditbox.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        tipsEditbox.setEditListener(new IEditListener() {
            @Override
            public boolean onChanged(String text) {
                onSubmitForm();
                return false;
            }
        });

        keyboard.attachEditView(tipsEditbox);
        keyboard.setDotEnabled(true);

        employeeAdapter = new EmployeeAdapter(getActivity());
        employeeSpinner.setAdapter(employeeAdapter);
        getLoaderManager().restartLoader(0, null, employeeLoader);

        OrderTotalPriceLoaderCallback totalPriceLoaderCallback = new OrderTotalPriceLoaderCallback(getActivity(), orderGuid) {
            @Override
            public void onZeroValue() {
                setOrderPrice(BigDecimal.ZERO, BigDecimal.ZERO);
            }

            @Override
            public void onCalcTotal(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType, BigDecimal orderDiscountVal, BigDecimal totalItemTotal,
                                    BigDecimal totalTaxVatValue, BigDecimal totalEbtTaxVatValue, BigDecimal totalItemDiscount, BigDecimal totalOrderPrice, BigDecimal totalOrderEbtPrice, BigDecimal availableDiscount, BigDecimal transactionFee) {
                setOrderPrice(totalOrderPrice, totalTaxVatValue);
            }
        };

        getLoaderManager().restartLoader(1, null, totalPriceLoaderCallback);

    }

    @AfterTextChange
    protected void tipsEditboxAfterTextChanged(Editable s) {
        recalcTipsAndTotal();
    }

    protected void checkPositiveButtonsCondition() {
        boolean tipValid = tipsAmount != null && tipsAmount.compareTo(BigDecimal.ZERO) == 1 && !employeeAdapter.isEmpty();
        enablePositiveButton(tipValid, greenBtnColor);
        keyboard.setEnterEnabled(tipValid && tipCustom.isChecked());
        tipsEditbox.setTextColor(validateForm() != null ? normalTextColor : errorTextColor);
    }

    private BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        return value != null && value.compareTo(BigDecimal.ZERO) == 1 ? value : null;
    }

    private void setOrderPrice(BigDecimal totalOrderPrice, BigDecimal totalTaxVat) {
        getLoaderManager().destroyLoader(1);

        subtotalAmount = totalOrderPrice.subtract(totalTaxVat);
        taxAmount = totalTaxVat;
        UiHelper.showPrice(subtotal, subtotalAmount);
        UiHelper.showPrice(tax, taxAmount);
        setRadioLabels();
        recalcTipsAndTotal();
    }

    private void setRadioLabels() {
        for (int i = 0; i< radioArray.length; i++){
            radioArray[i].setText(getString(R.string.apply_tips_dialog_radi_label, tipsArray[i] + "%", UiHelper.priceFormat(CalculationUtil.getTipValue(subtotalAmount, new BigDecimal(tipsArray[i]), DiscountType.PERCENT))));
        }
    }

    private void recalcTipsAndTotal(){
        tipsAmount = getTipsAmount();
        totalAmount = subtotalAmount.add(tipsAmount).add(taxAmount);
        UiHelper.showPrice(total, totalAmount);
        checkPositiveButtonsCondition();
    }

    private BigDecimal getTipsAmount() {
        BigDecimal tip;
        DiscountType tipType = DiscountType.PERCENT;
        switch (tipsGroup.getCheckedRadioButtonId()){
            case R.id.tip0:
                tip = new BigDecimal(tipsArray[0]);
                break;
            case R.id.tip1:
                tip = new BigDecimal(tipsArray[1]);
                break;
            case R.id.tip2:
                tip = new BigDecimal(tipsArray[2]);
                break;
            default:
                tip = UiHelper.parseBigDecimal(tipsEditbox, BigDecimal.ZERO);
                if (!percentValueSwitch.isChecked())
                    tipType = DiscountType.VALUE;
                break;
        }

        return CalculationUtil.getTipValue(subtotalAmount, tip, tipType);
    }

    protected BigDecimal getDecimalValue() {
        return UiHelper.parseBigDecimal(tipsEditbox, null);
    }

    private void onSubmitForm() {
        if (tipsAmount == null || tipsAmount.compareTo(BigDecimal.ZERO) == 0 || employeeAdapter.isEmpty())
            return;

        boolean tipsPermitted = getApp().hasPermission(Permission.TIPS);
        if (!tipsPermitted) {
            PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    onSubmitForm();
                }
            }, Permission.TIPS);
            return;
        }

        BigDecimal tipsWarnThreshold = getApp().getShopInfo().tipsWarnThreshold;
        if (tipsWarnThreshold != null && tipsWarnThreshold.compareTo(BigDecimal.ZERO) == 1 && tipsAmount.compareTo(CalculationUtil.getTipValue(subtotalAmount, tipsWarnThreshold, DiscountType.PERCENT)) == 1){
            String msg = getString(R.string.apply_tips_dialog_warn_message, UiHelper.percentFormat(tipsWarnThreshold));
            AlertDialogFragment.showAlert(getActivity(), R.string.warning_dialog_title, msg, R.string.btn_continue, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    onComplete();
                    return true;
                }
            });
            return;
        }

        onComplete();
    }

    private void onComplete() {
        String tippedEmployeeGuid = ((EmployeeModel)employeeSpinner.getSelectedItem()).getGuid();
        if (listener != null)
            listener.onTipsConfirmed(tipsAmount, tippedEmployeeGuid, null);
    }

    @Override
    protected View createDialogContentView() {
        View v = layoutInflater.inflate(R.layout.apply_tips_dialog_outer_fragment, null, false);
        layoutInflater.inflate(getDialogContentLayout(), (ViewGroup) v.findViewById(R.id.dialog_content), true);
        return v;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.apply_tips_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.apply_tips_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_skip;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onSubmitForm();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onSkip();
                return false;
            }
        };
    }

    private LoaderManager.LoaderCallbacks<List<EmployeeModel>> employeeLoader = new LoaderCallbacks<List<EmployeeModel>>() {

        private final Uri URI_EMPLOYEE = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

        @Override
        public Loader<List<EmployeeModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(URI_EMPLOYEE)
                    .projection(EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME)
                    .where(EmployeeTable.TIPS_ELIGIBLE + " = ?", 1)
                    .where(EmployeeTable.STATUS + " = ?", EmployeeStatus.ACTIVE.ordinal())
                    .orderBy(EmployeeTable.FIRST_NAME)
                    .transform(new Function<Cursor, EmployeeModel>() {
                        @Override
                        public EmployeeModel apply(Cursor c) {
                            return new EmployeeModel(
                                    c.getString(0),
                                    c.getString(1),
                                    c.getString(2),
                                    null);
                        }
                    }).wrap(new Function<List<EmployeeModel>, List<EmployeeModel>>() {
                        @Override
                        public List<EmployeeModel> apply(List<EmployeeModel> result) {
                            ArrayList<EmployeeModel> arrayList = new ArrayList<EmployeeModel>(result.size() + 1);
                            arrayList.add(new EmployeeModel(null, getString(R.string.houde_tips_label), "", null));
                            arrayList.addAll(result);
                            return arrayList;
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<EmployeeModel>> listLoader, List<EmployeeModel> employeeModels) {
            employeeAdapter.changeCursor(employeeModels);
            checkPositiveButtonsCondition();
        }

        @Override
        public void onLoaderReset(Loader<List<EmployeeModel>> listLoader) {
            employeeAdapter.changeCursor(null);
        }
    };


    private static class EmployeeAdapter extends ObjectsCursorAdapter<EmployeeModel> {

        public EmployeeAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_dark, parent, false);
        }

        @Override
        protected View bindView(View view, int position, EmployeeModel item) {
            ((TextView) view).setText(item.fullName());
            return view;
        }
    }

    private OnCheckedChangeListener predefineTipCheckedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                tipCustom.setChecked(false);
                delayedRecalc();
            }
        }
    };

    private OnCheckedChangeListener customTipCheckedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                tipsGroup.clearCheck();
                delayedRecalc();
            }
        }
    };

    //this artifact added to let radiobuttons change their states before recalculation proceeds
    private void delayedRecalc(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                recalcTipsAndTotal();
            }
        }, 100);
    }

    public static interface IApplyTipsListener {

        void onSkip();

        void onTipsConfirmed(BigDecimal amount, String employeeGuid, String notes);

    }

    public void setListener(IApplyTipsListener listener) {
        this.listener = listener;
    }

    public static void show (FragmentActivity activity, String orderGuid, IApplyTipsListener listener){
        DialogUtil.show(activity, DIALOG_NAME, ApplyTipsFragmentDialog_.builder().orderGuid(orderGuid).build()).setListener(listener);
    }

    public static void hide (FragmentActivity activity){
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
