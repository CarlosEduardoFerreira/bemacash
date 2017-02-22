package com.kaching123.tcr.fragment.inventory;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.inventory.AddVariantItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantItemCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.UUID;

/**
 * Created by aakimov on 30/04/15.
 */

@EFragment
public class VariantItemDialogFragment extends StyledDialogFragment {

    private static final Uri URI_VARIANT_SUB_ITEM = ShopProvider.contentUri(ShopStore.VariantSubItemTable.URI_CONTENT);

    private static final String DIALOG_NAME = VariantItemDialogFragment.class.getName();
    
    @ColorRes(android.R.color.white)
    protected int whiteBtnColor;
    
    @ViewById
    protected EditText nameEdit;
    @ViewById
    protected EditText countEdit;

    @FragmentArg
    protected ItemExModel itemExModel;

    @FragmentArg
    protected VariantItemModel variantItemModel;

    @FragmentArg
    protected int variantSubItemsCount;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.variant_item_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.variant;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @AfterViews
    protected void init() {
        enablePositiveButton(false, whiteBtnColor);
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty() || countEdit.getText().toString().isEmpty()) {
                    enablePositiveButton(false, whiteBtnColor);
                } else {
                    enablePositiveButton(true, whiteBtnColor);

                }
            }
        });
        countEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.toString().isEmpty() || nameEdit.getText().toString().isEmpty())) {
                    enablePositiveButton(false, whiteBtnColor);
                } else if(Integer.valueOf(s.toString()) > 0) {
                    enablePositiveButton(true, whiteBtnColor);
                } else {
                    enablePositiveButton(false, whiteBtnColor);
                }
            }
        });
        if (variantItemModel != null) {
            nameEdit.setText(variantItemModel.name);
            countEdit.setVisibility(View.GONE);
            getVariantSubItemsCount();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelSize(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (validateFields()) {
                    if (variantItemModel == null) {
                        String guid = UUID.randomUUID().toString();
                        AddVariantItemCommand.start(getActivity(),
                                new VariantItemModel(guid, nameEdit.getText().toString(),
                                        itemExModel.guid, TcrApplication.get().getShopId(), null));
                        VariantSubItemDialogFragment.showToAdd(getActivity(), itemExModel.guid,
                                guid, Integer.parseInt(countEdit.getText().toString()));
                    } else {
                        String changedName = nameEdit.getText().toString();
                        if (!variantItemModel.name.equals(changedName)) {
                            variantItemModel.name = changedName;
                            EditVariantItemCommand.start(getActivity(), variantItemModel);
                        }
                    }
                    hide(getActivity());
                    return false;
                } else {
                    return false;
                }
            }
        };
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(nameEdit.getText())) {
            return false;

        }
        if (TextUtils.isEmpty(countEdit.getText())) {
            return false;
        }
        return true;
    }

    @Background
    protected void getVariantSubItemsCount() {
        Cursor c = ProviderAction.query(URI_VARIANT_SUB_ITEM).where(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID + "=?", variantItemModel.guid).perform(getActivity());
        int count = c.getCount();
        c.close();
        setCount(count);
    }

    @UiThread
    protected void setCount(int count) {
        countEdit.setText(String.valueOf(variantSubItemsCount = count));
    }


    public static void showToAdd(FragmentActivity activity, ItemExModel itemExModel) {
       DialogUtil.show(activity, DIALOG_NAME, VariantItemDialogFragment_.builder().itemExModel(itemExModel).build());
    }


    public static void showToEdit(FragmentActivity activity, VariantItemModel variantItemModel) {
        DialogUtil.show(activity, DIALOG_NAME, VariantItemDialogFragment_.builder().variantItemModel(variantItemModel).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
