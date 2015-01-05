package com.kaching123.tcr.fragment.editmodifiers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddModifierCommand;
import com.kaching123.tcr.commands.store.inventory.EditModifiersCommand;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 09.12.13.
 */
@EFragment
public class EditDialog extends StyledDialogFragment {

    public static final int INDEX_MODIFIER = 0;
    public static final int INDEX_ADDON = 1;
    public static final int INDEX_OPTIONAL = 2;

    public static enum ActionType {
        EDIT, CREATE
    }

    public static String DIALOG_NAME = "modifier_edit_dialog";

    @FragmentArg
    protected ModifierModel model;

    @FragmentArg
    protected ActionType action;

    @ViewById
    protected EditText title;

    @ViewById
    protected EditText price;

    @ViewById
    protected Spinner type;

    @ViewById
    protected View focusGrabber;

    @ViewById
    protected CheckBox checkboxDefault;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int disableTextColor;

    private ModifierType preferableType;

    @FragmentArg
    protected String itemDefaultModifierGuid;

    private OnEditListener editListener;

    @AfterViews
    protected void init() {
        price.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        price.setImeOptions(EditorInfo.IME_ACTION_DONE);
        price.setOnEditorActionListener(new OnEditorActionListener() {
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
        title.setOnEditorActionListener(new OnEditorActionListener() {
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
    protected int getDialogContentLayout() {
        return R.layout.editmodifiers_edit_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return action == ActionType.EDIT ? R.string.modify_dialog_title_edit : R.string.modify_dialog_title_add;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter adapter = new ArrayAdapter<Type>(getActivity(), R.layout.modifier_type_item, new Type[]{
                new Type(getString(R.string.modifiers_copy_header_modifiers), ModifierType.MODIFIER),
                new Type(getString(R.string.modifiers_copy_header_addons), ModifierType.ADDON),
                new Type(getString(R.string.modifiers_copy_header_optionals), ModifierType.OPTIONAL)
        });
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        type.setAdapter(adapter);
        type.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Type type = (Type) adapterView.getSelectedItem();
                switch (type.type){
                    case OPTIONAL:
                        enableCheckboxDefault(false);
                        disablePriceField();
                        break;
                    case ADDON:
                        enablePriceField();
                        enableCheckboxDefault(false);
                        break;
                    case MODIFIER:
                        enablePriceField();
                        enableCheckboxDefault(true);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (preferableType != null) {
            type.setSelection(preferableType.ordinal());
        }
        if (model != null) {
            title.setText(model.title);
            showPrice(price, model.cost);
            setModifierType(model.type);
            checkboxDefault.setChecked(model.modifierGuid != null && model.modifierGuid.equals(itemDefaultModifierGuid));
        }

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.edit_modify_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.edit_modify_dialog_height);
    }

    public void setType(ModifierType type){
        this.preferableType = type;
    }

    private void enablePriceField() {
        showPrice(price, model.cost);
        price.setEnabled(true);
        price.setTextColor(normalTextColor);
        changeTitleImeOptions(false);
    }

    private void disablePriceField() {
        showPrice(price, BigDecimal.ZERO);
        price.setEnabled(false);
        price.setTextColor(disableTextColor);
        changeTitleImeOptions(true);
    }

    private void enableCheckboxDefault(boolean enable) {
        checkboxDefault.setEnabled(enable);
        if(enable){
            checkboxDefault.setChecked(model.modifierGuid != null && model.modifierGuid.equals(itemDefaultModifierGuid));
        }else{
            checkboxDefault.setChecked(false);
        }
    }

    private void changeTitleImeOptions(boolean actionDone) {
        if (actionDone != (title.getImeOptions() == EditorInfo.IME_ACTION_DONE)) {
            title.setImeOptions(actionDone ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            if (title.hasFocus()) {
                focusGrabber.requestFocus();
                title.post(new Runnable() {
                    @Override
                    public void run() {
                        title.requestFocus();
                    }
                });
            }
        }
    }

    private boolean doClick() {
        boolean useAsDefault = checkboxDefault.isEnabled() && checkboxDefault.isChecked() && !model.modifierGuid.equals(itemDefaultModifierGuid);
        boolean resetDefaultModifier = checkboxDefault.isEnabled() && !checkboxDefault.isChecked() && model.modifierGuid.equals(itemDefaultModifierGuid);

        if (fieldsValid()) {
            collectDataToModel();
            switch (action) {
                case CREATE:
                    AddModifierCommand.start(getActivity(), model, useAsDefault, resetDefaultModifier);
                    break;
                case EDIT:
                    EditModifiersCommand.start(getActivity(), model, useAsDefault, resetDefaultModifier);
                    break;
            }
            if(editListener != null){
                editListener.onDefaultModifierChanged(model.modifierGuid, useAsDefault, resetDefaultModifier);
            }
            return true;
        }
        return false;
    }

    private boolean fieldsValid() {
        if (TextUtils.isEmpty(title.getText().toString().trim()))
            return false;
        return true;
    }

    private void collectDataToModel() {
        model.title = title.getText().toString().trim();
        model.cost = UiHelper.parseBigDecimal(price, BigDecimal.ZERO);
        model.type = getModifierType();
    }

    private ModifierType getModifierType() {
        Type selectedItem = (Type) type.getSelectedItem();
        return selectedItem.type;
    }

    private void setModifierType(ModifierType type) {
        if (type == null)
            return;

        switch (type) {
            case MODIFIER:
                this.type.setSelection(INDEX_MODIFIER);
                break;
            case ADDON:
                this.type.setSelection(INDEX_ADDON);
                break;
            case OPTIONAL:
                this.type.setSelection(INDEX_OPTIONAL);
                break;
        }
    }

    public EditDialog setEditListener(OnEditListener editListener) {
        this.editListener = editListener;
        return this;
    }

    public static void show(FragmentActivity activity, String itemDefaultModifierGuid, ModifierModel model, ActionType action, OnEditListener editListener) {
        DialogUtil.show(activity, DIALOG_NAME, EditDialog_.builder().itemDefaultModifierGuid(itemDefaultModifierGuid).model(model).action(action).build()).setEditListener(editListener);
    }

    public static void showWithType(FragmentActivity activity, ModifierModel model, ModifierType type, OnEditListener editListener) {
        DialogUtil.show(activity, DIALOG_NAME, EditDialog_.builder().model(model).action(ActionType.CREATE).build()).setEditListener(editListener).setType(type);
    }

    private static class Type {
        final String label;
        final ModifierType type;

        Type(String label, ModifierType type) {
            this.label = label;
            this.type = type;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static interface OnEditListener{
        void onDefaultModifierChanged(String modifierId, boolean useAsDefault, boolean resetDefaultModifier);
    }
}
