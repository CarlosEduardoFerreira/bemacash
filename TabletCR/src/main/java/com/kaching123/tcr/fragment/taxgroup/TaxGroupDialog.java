package com.kaching123.tcr.fragment.taxgroup;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.CreateTaxGroup;
import com.kaching123.tcr.commands.store.inventory.UpdateTaxGroup;
import com.kaching123.tcr.component.PercentFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.TaxGroupModel;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 25.12.13.
 */
@EFragment
public class TaxGroupDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = TaxGroupDialog.class.getSimpleName();

    @FragmentArg
    protected TaxGroupModel model;

    @ViewById
    protected EditText title;
    @ViewById
    protected EditText tax;

    @AfterViews
    protected void initViews() {
        InputFilter[] decimalFilter = new InputFilter[]{new PercentFormatInputFilter()};
        tax.setFilters(decimalFilter);
        tax.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_DONE == actionId){
                    if (doClick()) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.tax_group_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.tax_group_dialog_height);

        if (model != null){
            title.setText(model.title);
            UiHelper.showInteger(tax, model.tax);
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
        if(!isChanged()){
            return true;
        }
        if (fieldsValid()){
            final String title = this.title.getText().toString();
            final BigDecimal tax = new BigDecimal(this.tax.getText().toString());
            if (model != null){
                UpdateTaxGroup.start(getActivity(), null, model.guid, title, tax);
            }else{
                CreateTaxGroup.start(getActivity(), null, title, tax);
            }
            return true;
        }
        return false;
    }

    private boolean fieldsValid() {
        String title = this.title.getText().toString().trim();
        BigDecimal tax = TextUtils.isEmpty(this.tax.getText().toString()) ? null : new BigDecimal(this.tax.getText().toString());
        return !TextUtils.isEmpty(title) && tax != null && tax.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isChanged(){
        String title = this.title.getText().toString().trim();
        BigDecimal tax = TextUtils.isEmpty(this.tax.getText().toString()) ? null : new BigDecimal(this.tax.getText().toString());
        return model == null || (!model.title.equals(title) || model.tax.compareTo(tax) != 0);
    }

    public static void show (FragmentActivity activity, TaxGroupModel model){
        DialogUtil.show(activity, DIALOG_NAME, TaxGroupDialog_.builder().model(model).build());
    }


}
