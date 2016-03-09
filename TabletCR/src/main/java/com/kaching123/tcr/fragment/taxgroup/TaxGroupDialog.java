package com.kaching123.tcr.fragment.taxgroup;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.inventory.CreateTaxGroup;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment_;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.TaxGroupModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 25.12.13.
 */
@EFragment
public class TaxGroupDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = TaxGroupDialog.class.getSimpleName();
    protected CurrencyTextWatcher currencyTextWatcher;

    @FragmentArg
    protected TaxGroupModel model;

    @FragmentArg
    protected boolean canBeDefault;

    @ViewById
    protected EditText title;
    @ViewById
    protected EditText tax;
    @ViewById
    protected CheckBox isDefault;

    @AfterViews
    protected void initViews() {
        currencyTextWatcher = new CurrencyTextWatcher(tax);
        InputFilter[] decimalFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        tax.setFilters(decimalFilter);
        tax.addTextChangedListener(currencyTextWatcher);
        tax.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    if (doClick()) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        isDefault.setVisibility(TcrApplication.isEcuadorVersion() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.tax_group_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.tax_group_dialog_height);

        if (model != null) {
            title.setText(model.title);
            UiHelper.showDecimal(tax, model.tax);
            if (TcrApplication.isEcuadorVersion()) {
                isDefault.setChecked(model.isDefault);
            }
        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.tax_group_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return model == null ? R.string.tax_group_dialog_title_create : R.string.tax_group_dialog_title_edit;
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
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return doClick();
            }
        };
    }

    private boolean doClick() {
        if (fieldsValid()) {
            if (!isChanged()) {
                return true;
            }
            if (TcrApplication.isEcuadorVersion() && !canBeDefault && this.isDefault.isChecked()) {
                AlertDialogFragment_.showAlert(getActivity(), R.string.warning_dialog_title, "There have already been defined 2 default tax groups");
                return false;
            }
            final String title = this.title.getText().toString();
            String value = this.tax.getText().toString().replaceAll(",", "");
            final BigDecimal tax = new BigDecimal(value);
            boolean isDefault = this.isDefault.isChecked();
            if (model != null) {
                if (callback != null) {
                    callback.onTaxGroupChanged(model.guid, title, tax, isDefault);
                }
            } else {
                CreateTaxGroup.start(getActivity(), null, title, tax, isDefault);
            }
            return true;
        }
        return false;
    }

    private boolean fieldsValid() {
        String title = this.title.getText().toString().trim();
        String value = this.tax.getText().toString().replaceAll(",", "");
        BigDecimal tax = TextUtils.isEmpty(value) ? null : new BigDecimal(value);
        return !TextUtils.isEmpty(title) && tax != null && tax.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isChanged() {
        String title = this.title.getText().toString().trim();
        String value = this.tax.getText().toString().replaceAll(",", "");
        BigDecimal tax = TextUtils.isEmpty(value) ? null : new BigDecimal(value);
        boolean isDefault = this.isDefault.isChecked();
        return model == null || (!model.title.equals(title) || model.tax.compareTo(tax) != 0 || isDefault != model.isDefault);
    }

    public static void show(FragmentActivity activity, TaxGroupModel model, TaxGroupListener callback) {
        DialogUtil.show(activity, DIALOG_NAME, TaxGroupDialog_.builder()
                .model(model)
                .build()
                .setCallback(callback));
    }

    public static void show(FragmentActivity activity, TaxGroupModel model, boolean canBeDefault, TaxGroupListener callback) {
        DialogUtil.show(activity, DIALOG_NAME, TaxGroupDialog_.builder()
                .model(model)
                .canBeDefault(canBeDefault)
                .build()
                .setCallback(callback));
    }

    public interface TaxGroupListener {
        void onTaxGroupChanged(String guid, String title, BigDecimal tax, boolean isDefault);
    }

    private TaxGroupListener callback;

    protected TaxGroupDialog setCallback(TaxGroupListener callback) {
        this.callback = callback;
        return this;
    }

}
