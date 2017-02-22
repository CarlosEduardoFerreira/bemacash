package com.kaching123.tcr.fragment.inventory;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddVariantSubItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantSubItemCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.VariantSubItemModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.UUID;

/**
 * Created by aakimov on 30/04/15.
 */

@EFragment
public class VariantSubItemDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = VariantSubItemDialogFragment.class.getName();

    @ColorRes(android.R.color.white)
    protected int whiteBtnColor;

    @ViewById
    protected EditText nameEdit;
    @ViewById
    protected TextView countText;

    @FragmentArg
    protected int requiredCount;
    @FragmentArg
    protected int currentCount = 1;
    @FragmentArg
    protected String variantItemGuid;
    @FragmentArg
    protected String itemGuid;
    @FragmentArg
    protected VariantSubItemModel variantSubItemModel;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.variant_sub_item_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.value;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (validateFields()) {
                    if (variantSubItemModel != null) {
                        //updateVariantSubItem();
                        variantSubItemModel.name = nameEdit.getText().toString();
                        EditVariantSubItemCommand.start(getActivity(), variantSubItemModel);
                        hide(getActivity());
                    } else {
                        AddVariantSubItemCommand.start(getActivity(),
                                new VariantSubItemModel(UUID.randomUUID().toString(),
                                        nameEdit.getText().toString(), variantItemGuid, itemGuid, null));
                        if (requiredCount == 1) {
                            hide(getActivity());
                        } else {
                            currentCount++;
                            updateCount();
                        }
                    }
                }
                return false;

            }
        };
    }

    @AfterViews
    protected void init() {
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    enablePositiveButton(false, whiteBtnColor);
                } else {
                    enablePositiveButton(true, whiteBtnColor);
                }
            }
        });
        if (variantSubItemModel != null) {
            nameEdit.setText(variantSubItemModel.name);
            countText.setVisibility(View.GONE);
        } else {
            updateCount();
            if (requiredCount == 1) {
                countText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelSize(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(nameEdit.getText())) {
            return false;

        }
        return true;
    }

    protected void updateCount() {
        if (currentCount <= requiredCount) {
            countText.setText(getString(R.string.adding, currentCount, requiredCount));
            nameEdit.setText("");
        } else {
            hide(getActivity());
        }

    }

    public static void showToAdd(FragmentActivity activity, String itemGuid, String variantItemGuid, int reqCount) {
        DialogUtil.show(activity, DIALOG_NAME, VariantSubItemDialogFragment_.builder().itemGuid(itemGuid).requiredCount(reqCount).variantItemGuid(variantItemGuid).build());
    }

    public static void showToEdit(FragmentActivity activity, VariantSubItemModel variantSubItemModel) {
        DialogUtil.show(activity, DIALOG_NAME, VariantSubItemDialogFragment_.builder().variantSubItemModel(variantSubItemModel).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
