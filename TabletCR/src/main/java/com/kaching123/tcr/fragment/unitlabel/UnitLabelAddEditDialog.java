package com.kaching123.tcr.fragment.unitlabel;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.unitlabel.AddUnitLabelCommand;
import com.kaching123.tcr.commands.unitlabel.EditUnitLabelCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.UnitLabelModelFactory;
import com.kaching123.tcr.util.UnitUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;


/**
 * Created by alboyko 07.12.2015
 */
@EFragment
public class UnitLabelAddEditDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = "unit_label_edit_dialog";

    private char[] blockCharacterSet = new char[]{ 32, 43, 215, 247, 61, 37, 95, 8364, 163, 165, 8361, 64, 35, 36, 47,
            94, 38, 42, 40, 41, 45, 39, 34, 58, 59, 33, 63, 44, 46, 96, 126,
            92, 124, 60, 62, 123, 125, 91, 93, 9642, 9675, 9679, 9633, 9632,
            9828, 9825, 9826, 9831, 9734, 8857, 176, 8226, 164, 12298, 12299, 161, 191,};

    protected UnitLabelCallback callback;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ViewById
    protected EditText etShortcut;

    @ViewById
    protected EditText etDescription;

    @FragmentArg
    protected UnitLabelModel unitLabelModel;

    @FragmentArg
    protected boolean isEdit;

    public void setCallback(UnitLabelCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.add_edit_unit_label_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.add_edit_unit_label_dlg_heigth)
        );
    }

    @AfterViews
    protected void initDialog() {
        etShortcut.setFilters(new InputFilter[]{filter});
        etShortcut.addTextChangedListener(textWatcherShortcut);
        etDescription.addTextChangedListener(textWatcherDescription);

        if (unitLabelModel != null) {
            etShortcut.setText(unitLabelModel.shortcut);
            etDescription.setText(unitLabelModel.description);
        } else {
            enablePositiveButtons(false);
        }
    }

    protected InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = 0; i < blockCharacterSet.length; i++) {
                if (source != null) {
                    for (int j = 0; j < source.length(); j++) {
                        if(blockCharacterSet[i] == source.charAt(j)){
                            return "";
                        }
                    }
                }
            }
            return null;
        }
    };

    @Override
    protected int getDialogContentLayout() {
        return R.layout.unit_labels_add_edit_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return isEdit ? R.string.unit_label_edit_dialog_title : R.string.unit_label_create_dialog_title;
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
        if (isFormFilled()) {
            String description = this.etDescription.getText().toString().trim();
            String shortCut = this.etShortcut.getText().toString().trim();

            if (unitLabelModel != null) {

                unitLabelModel.description = description;
                unitLabelModel.shortcut = shortCut;

                EditUnitLabelCommand.start(
                        getActivity(),
                        unitLabelModel,
                        new EditUnitLabelCommand.UnitLabelCallback() {
                            @Override
                            protected void handleSuccess() {
                                callback.handleSuccess();
                            }

                            @Override
                            protected void handleError(String message) {
                                callback.handleError(message);
                            }
                        }
                );
            } else {
                AddUnitLabelCommand.start(
                        getActivity(),
                        UnitLabelModelFactory.getNewModel(description, shortCut),
                        new AddUnitLabelCommand.UnitLabelCallback() {
                            @Override
                            protected void handleSuccess() {
                                callback.handleSuccess();
                            }

                            @Override
                            protected void handleError(String message) {
                                callback.handleError(message);
                            }
                        }
                );
            }

            return true;
        }
        return false;
    }

    private boolean isFormFilled() {
        return !(TextUtils.isEmpty(etDescription.getText().toString().trim()) ||
                TextUtils.isEmpty(etShortcut.getText().toString().trim()));
    }

    public static void show(FragmentActivity context, UnitLabelModel unitLabelModel, UnitLabelCallback callback) {
        DialogUtil.show(
                context,
                DIALOG_NAME,
                UnitLabelAddEditDialog_
                        .builder()
                        .unitLabelModel(unitLabelModel)
                        .isEdit(unitLabelModel != null)
                        .build())
                .setCallback(callback);
    }

    public interface UnitLabelCallback {

        void handleSuccess();

        void handleError(String message);
    }

    private boolean find = false;
    private TextWatcher textWatcherShortcut = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() > UnitUtil.MAX_LENGTH){
                etShortcut.setText("");
                etShortcut.append(s.subSequence(0,6));
            }

            find = UnitUtil.isContainInvalidChar(s);

            enablePositiveButtons(!find && isFormFilled());
            etShortcut.setTextColor(!find ? normalTextColor : errorTextColor);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher textWatcherDescription = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            enablePositiveButtons(!find && isFormFilled());
            if(s.toString().equals(etShortcut.getText().toString()))
                enablePositiveButtons(false);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

}
