package com.kaching123.tcr.fragment.wireless;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.SearchUnitCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.Unit;

import java.util.ArrayList;

/**
 * Created by mayer
 */
@EFragment
public class UnitsSearchFragment extends UnitEditFragmentBase {

    private static final String DIALOG_NAME = "UnitsSearchFragment";

    protected UnitCallback callback;

    @FragmentArg
    protected String predefSerial;

    public void setCallback(UnitCallback callback) {
        this.callback = callback;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_unit_dialog_fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        configureUI();
    }
    @Override
    protected void attachViews() {
    }

    protected void configureUI() {
        warrEditbox.setVisibility(View.GONE);
        warrTextview.setVisibility(View.GONE);
        purposeSwitch.setVisibility(View.GONE);
        formatter = getFormatter();
        etSerial.setInputType(getInputType());
        etSerial.setText("");
        etSerial.setFilters(getFilters());
        etSerial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvSerial.setText(formatter.format(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etSerial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etSerial.setSelection(etSerial.getText().length());
            }
        });
        etSerial.setText(predefSerial == null? "" : predefSerial);
    }

    @Override
    protected boolean ignoreWarrantyCondition() {
        return true;
    }

    @Override
    protected boolean onSubmitForm() {
        final String serialCode = etSerial.getText().toString();
        SearchUnitCommand.start(getActivity(), serialCode, null, null, true, new SearchUnitCommand.UnitCallback() {

            @Override
            protected void handleSuccess(ArrayList<Unit> unit, ArrayList<SaleOrderViewModel> order) {
                callback.handleSuccess(serialCode, unit, order);
            }

            @Override
            protected void handleError(String message) {
                callback.handleError(serialCode, message);
            }
        });
        return true;
    }

    @Override
    protected int getDialogTitle() {
       return R.string.dlg_unit_search;
    }

    @Override
    protected boolean hasToPlayTune() {
        return false;
    }

    public static void show(FragmentActivity activity, String predefSerial, UnitCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, UnitsSearchFragment_
                .builder()
                .predefSerial(predefSerial)
                .build())
                .setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.handleCancel();
                return true;
            }
        };
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        callback.handleCancel();
        super.onCancel(dialog);
    }

    public interface UnitCallback {

        void handleSuccess(String serialCode, ArrayList<Unit> unit, ArrayList<SaleOrderViewModel> order);

        void handleError(String serialCode, String message);

        void handleCancel();
    }
}
