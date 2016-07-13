package com.kaching123.tcr.fragment.employee;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.QuantityFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by vkompaniets on 29.05.2014.
 */
@EFragment
public class EmployeeTipsFragmentDialog extends KeyboardDialogFragment {

    private static final BigDecimal MAX_VALUE = new BigDecimal("9999.999");

    private static final String DIALOG_NAME = EmployeeTipsFragmentDialog.class.getSimpleName();

    @ViewById
    protected CustomEditBox tipsEditbox;

    @ViewById
    protected Spinner employeeSpinner;

    @ViewById
    protected EditText notesEditbox;

    @FragmentArg
    protected boolean splitEnabled;

    @FragmentArg
    protected BigDecimal orderTotal;

    private boolean skipWarning;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    private EmployeeAdapter employeeAdapter;

    private BigDecimal tipsSplitTreshold;

    private IAddTipsListener listener;

    private List<String> tipsableEmployeeGuids;

    private OnDialogClickListener onSkipListener;

    public void setOnSkipListener(OnDialogClickListener onSkipListener) {
        this.onSkipListener = onSkipListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.employee_tips_dialog_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews(){
        tipsSplitTreshold = getApp().getShopInfo().tipsSplitTreshold;

        tipsEditbox.setKeyboardSupportConteiner(this);
        tipsEditbox.setFilters(new InputFilter[]{new CurrencyFormatInputFilter(), new QuantityFormatInputFilter()});
        keyboard.attachEditView(tipsEditbox);
        keyboard.setDotEnabled(true);
        tipsEditbox.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                callInternalListener(submitListener);
                return false;
            }
        });

        employeeAdapter = new EmployeeAdapter(getActivity());
        employeeSpinner.setAdapter(employeeAdapter);
        employeeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkPositiveButtonCondition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getLoaderManager().initLoader(0, null, employeeLoader);
    }

    @AfterTextChange
    protected void tipsEditboxAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    protected void checkPositiveButtonCondition() {
        BigDecimal value = validateForm();
        enablePositiveButtons(value != null);
        tipsEditbox.setTextColor(value != null ? normalTextColor : errorTextColor);
    }

    public EmployeeTipsFragmentDialog setListener(IAddTipsListener listener) {
        this.listener = listener;
        return this;
    }

    protected boolean onSubmitForm() {
        BigDecimal value = validateForm();
        if (value == null)
            return false;

        BigDecimal maxValue = getMaxValue();
        if (!skipWarning && maxValue != null && value.compareTo(maxValue) == 1){
            String msg = getString(R.string.apply_tips_dialog_warn_message, UiHelper.percentFormat(getApp().getShopInfo().tipsWarnThreshold));
            AlertDialogFragment.showAlert(getActivity(), R.string.warning_dialog_title, msg, R.string.btn_continue, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    skipWarning = true;
                    EmployeeTipsFragmentDialog.this.dismiss();
                    onSubmitForm();
                    return true;
                }
            });
            return false;
        }

        if (listener != null) {
            ArrayList<String> tipsableGuids = new ArrayList<String>();
            if (splitEnabled && employeeSpinner.getSelectedItemPosition() == 1){
                tipsableGuids.addAll(tipsableEmployeeGuids);
            }else {
                tipsableGuids.add(employeeAdapter.getItem(employeeSpinner.getSelectedItemPosition()).guid);
            }
            listener.onTipsConfirmed(value, tipsableGuids, notesEditbox.getText().toString());
        }

        return true;
    }

    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        boolean hasEmployees = employeeSpinner.getCount() > 0;
        EmployeeModel selectedEmployee = (EmployeeModel) employeeSpinner.getSelectedItem();
        String selectedEmployeeGuid = selectedEmployee == null ? null : selectedEmployee.getGuid();
        boolean allEmployeesSelected = selectedEmployeeGuid == null && employeeSpinner.getSelectedItemPosition() != 0;
        return value != null
                && value.compareTo(BigDecimal.ZERO) == 1
                && hasEmployees
                && (!allEmployeesSelected || !splitEnabled || tipsSplitTreshold == null || tipsSplitTreshold.compareTo(BigDecimal.ZERO) == 0 || tipsSplitTreshold.compareTo(value) != 1)
                ? value : null;
    }

    protected BigDecimal getMaxValue() {
        BigDecimal tipsWarnThreshold = getApp().getShopInfo().tipsWarnThreshold;
        if (orderTotal == null || tipsWarnThreshold == null || tipsWarnThreshold.compareTo(BigDecimal.ZERO) <= 0)
            return null;

        return CalculationUtil.getTipValue(orderTotal, tipsWarnThreshold, DiscountType.PERCENT);
    }

    protected BigDecimal getDecimalValue() {
        String text = tipsEditbox.getText().toString();
        try {
            if (text.endsWith("-")){
                return negative(new BigDecimal(text.substring(0, text.length() - 1)));
            }else {
                return new BigDecimal(text);
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected View createDialogContentView() {
        View v = layoutInflater.inflate(R.layout.employee_tips_dialog_outer_fragment, null, false);
        layoutInflater.inflate(getDialogContentLayout(), (ViewGroup) v.findViewById(R.id.dialog_content), true);
        return v;
    }

    private OnDialogClickListener submitListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            return onSubmitForm();
        }
    };

    public static void show (FragmentActivity activity, IAddTipsListener listener){
        show(activity, listener, true, null, null);
    }

    public static void show(FragmentActivity activity, IAddTipsListener listener, boolean splitEnabled, BigDecimal orderTotal, OnDialogClickListener onSkipListener){
        DialogUtil.show(activity, DIALOG_NAME, EmployeeTipsFragmentDialog_.builder().splitEnabled(splitEnabled).orderTotal(orderTotal).build()).setListener(listener).setOnSkipListener(onSkipListener);
    }

    private LoaderCallbacks<List<EmployeeModel>> employeeLoader = new LoaderCallbacks<List<EmployeeModel>>() {

        private final Uri URI_EMPLOYEE = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

        @Override
        public Loader<List<EmployeeModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(URI_EMPLOYEE)
                    .projection(EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME)
                    .where(EmployeeTable.TIPS_ELIGIBLE + " = ?", 1)
                    .where(EmployeeTable.STATUS + " = ?", EmployeeStatus.ACTIVE.ordinal())
                    .where(EmployeeTable.IS_MERCHANT + " = ?", 0)
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
                    })
                    .wrap(new Function<List<EmployeeModel>, List<EmployeeModel>>() {
                        @Override
                        public List<EmployeeModel> apply(List<EmployeeModel> result) {
                            tipsableEmployeeGuids = getTipsableEmployeeGuids(result);
                            ArrayList<EmployeeModel> arrayList = new ArrayList<EmployeeModel>(result.size() + (splitEnabled && !result.isEmpty() ? 2 : 1));
                            arrayList.add(new EmployeeModel(null, getString(R.string.houde_tips_label), "", null));
                            if (splitEnabled && !result.isEmpty()){
                                arrayList.add(new EmployeeModel(null, getString(R.string.register_label_all), "", null));
                            }
                            arrayList.addAll(result);
                            return arrayList;
                        }

                        private List<String> getTipsableEmployeeGuids(List<EmployeeModel> employees) {
                            ArrayList<String> guids = new ArrayList<String>(employees.size());
                            for (EmployeeModel model : employees) {
                                guids.add(model.guid);
                            }
                            return guids;
                        }

                        ;
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<EmployeeModel>> listLoader, List<EmployeeModel> employeeModels) {
            employeeAdapter.changeCursor(employeeModels);
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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
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

    @Override
    protected int getDialogContentLayout() {
        return R.layout.employee_tips_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.employee_tips_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected boolean hasSkipButton() {
        return onSkipListener != null;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return onSkipListener;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return onSubmitForm();
            }
        };
    }

    public static interface IAddTipsListener{
        void onTipsConfirmed (BigDecimal amount, List<String> employeeGuids, String notes);
    }


}
