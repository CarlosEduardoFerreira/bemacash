package com.kaching123.tcr.fragment.inventory;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemCodeType;

/**
 * Created by idyuzheva on 30.06.2014.
 */

@EFragment
public class ItemCodeChooserAlertDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "ItemCodeChooserAlertDialogFragment";

    public interface ItemCodeTypeChooseListener {
        void onItemCodeTypeChosen(ItemCodeType codeType, String code);
    }

    @FragmentArg
    String code;

    @ViewById(R.id.chooser_dialog_msg)
    TextView msg;

    @ViewById(R.id.chooser_radio_group)
    RadioGroup chooserGroup;

    @ViewById(R.id.chooser_first_radio_button)
    RadioButton eanRadioButton;

    @ViewById(R.id.chooser_second_radio_button)
    RadioButton productCodeRadioButton;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.chooser_fragment;
    }


    @Override
    protected int getDialogTitle() {
        return R.string.item_activity_alert_code_type_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    private ItemCodeTypeChooseListener codeTypeChooseListener;

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (codeTypeChooseListener != null) {
                    if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_first_radio_button) {
                        codeTypeChooseListener.onItemCodeTypeChosen(ItemCodeType.EAN_UPC, code);
                    } else if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_second_radio_button) {
                        codeTypeChooseListener.onItemCodeTypeChosen(ItemCodeType.PRODUCT_CODE, code);
                    }
                }
                return true;
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            codeTypeChooseListener = ((ItemCodeTypeChooseListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " or parent fragments must implement UnitCodeTypeChooseListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eanRadioButton.setText(R.string.item_activity_alert_code_type_ean);
        productCodeRadioButton.setText(R.string.item_activity_alert_code_type_product_code);
        msg.setVisibility(View.VISIBLE);
        msg.setText(code);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    public static void show(FragmentActivity activity, String code) {
        DialogUtil.show(activity, DIALOG_NAME, ItemCodeChooserAlertDialogFragment_.builder().code(code).build());
    }

}

