package com.kaching123.tcr.fragment.modify;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddModifierGroupCommand;
import com.kaching123.tcr.commands.store.inventory.EditModifierGroupCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ModifierGroupModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.UUID;


/**
 * Created by irikhmayer on 06.05.2015.
 */
@EFragment(R.layout.modifier_edit_group_dialog_fragment)
public class ModifierGroupEditFragment extends StyledDialogFragment implements BarcodeReceiver {

    private static final String DIALOG_NAME = "ModifierGroupEditFragment";

    @ViewById
    protected EditText description;

    @ColorRes(R.color.light_gray) protected int normalTextColor;
    @ColorRes(R.color.gray_dark) protected int badTextColor;

    @InstanceState
    protected MODE mode;

    @FragmentArg
    protected ModifierGroupModel model;

    @FragmentArg
    protected String itemGuid;

    protected ModifierGroupCallback callback;

    protected enum MODE {
        EDIT,
        ADD
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews() {
        if (model != null) {
            mode = MODE.EDIT;
            description.setText(model.title);
        } else {
            mode = MODE.ADD;
            model = new ModifierGroupModel();
            model.guid = UUID.randomUUID().toString();
            model.itemGuid = itemGuid;
            refreshEnabled();
        }
        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    protected void refreshEnabled() {
        boolean enabled = !TextUtils.isEmpty(description.getText().toString());
        enablePositiveButton(enabled, greenBtnColor);
    }

    protected ModifierGroupModel collectData() {
        model.title = description.getText().toString();
        return model;
    }

    public void setCallback(ModifierGroupCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.modifier_edit_group_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        if (model == null) {
             return R.string.modifier_dialog_group_add_title;
        } else {
             return R.string.modifier_dialog_group_edit_title;
        }
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return model == null ? R.string.btn_add : R.string.btn_adjust;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                final ModifierGroupModel dataModel = collectData();
                if (mode == MODE.EDIT) {
                    EditModifierGroupCommand.start(getActivity(), model);
                    callback.handleSuccess(dataModel);
                } else if (mode == MODE.ADD) {
                    callback.handleSuccess(dataModel);
                    AddModifierGroupCommand.start(getActivity(), model);
                }
                return false;
            }
        };
    }

    public static void show(FragmentActivity activity,
                            String itemGuid,
                            ModifierGroupModel model,
                            ModifierGroupCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, ModifierGroupEditFragment_
                .builder()
                .itemGuid(itemGuid)
                .model(model)
                .build())
                .setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface ModifierGroupCallback {

        void handleSuccess(ModifierGroupModel parent);

        void handleError(String message);

        void handleCancel();
    }
}
