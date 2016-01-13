package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BaseItemExAdapter;
import com.kaching123.tcr.commands.store.inventory.EditVariantMatrixItemCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.store.ShopSchema2;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;


/**
 * Created by aakimov on 18.05.2015.
 */
@EFragment(R.layout.choose_child_item_dialog_fragment)
public class ChooseChildItemDialogFragment extends StyledDialogFragment implements BarcodeReceiver {

    private static final String DIALOG_NAME = "ChooseChildItem";

    @ColorRes(android.R.color.white)
    protected int whiteColor;

    @ViewById
    protected AutoCompleteTextView itemChooser;

    @FragmentArg
    protected ItemMatrixModel itemMatrixModel;

    @FragmentArg
    protected String childGuid;

    protected ItemsAdapter itemsAdapter;

    public static void show(FragmentActivity activity,
                            ItemMatrixModel itemMatrixModel) {
        DialogUtil.show(activity, DIALOG_NAME, ChooseChildItemDialogFragment_
                .builder()
                .itemMatrixModel(itemMatrixModel)
                .build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews() {
        itemsAdapter = new ItemsAdapter(getActivity());

        itemChooser.setAdapter(itemsAdapter);
        itemChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                childGuid = ((ItemsAdapter) parent.getAdapter()).getItem(position).guid;
                enablePositiveButton(true, whiteColor);
            }
        });
        enablePositiveButton(false, whiteColor);
        itemChooser.addTextChangedListener(textWatcher);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            enablePositiveButton(false, whiteColor);

        }
    };

    @Override
    protected int getDialogContentLayout() {
        return R.layout.choose_child_item_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.variants_add_child_item_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_add;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (childGuid != null) {
                    itemMatrixModel.childItemGuid = childGuid;
                    EditVariantMatrixItemCommand.start(getActivity(), itemMatrixModel);
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    private class ItemsAdapter extends BaseItemExAdapter {

        public ItemsAdapter(Context context) {
            super(context);
        }

        @Override
        protected String getCustomSelection() {
            return ShopSchema2.ItemExtView2.ItemTable.ITEM_REF_TYPE + "=?" + " AND " + ShopSchema2.ItemExtView2.ItemMatrixTable.PARENT_GUID + " IS NULL";
        }

        @Override
        protected String[] getCustomSelectionArgs() {
            return new String[]{String.valueOf(ItemRefType.Simple.ordinal())};
        }

        @Override
        protected void publishResults(FluentIterable<ItemExModel> cursor) {
            // set dropdown height for itemChooser to be upper the keyboard
            //fixed by overriding onKeyPreIme() in ItemAutocompleteTextView.class
            /*if (cursor == null || cursor.size() == 0) {
                itemChooser.setDropDownHeight(0);
            } else {
                int height;
                if (cursor.size() == 1) {
                    height = UiHelper.dpToPx(getContext(), 65);
                } else {
                    height = (int) ((cursor.size() > 3 ? 3.5 : cursor.size()) * UiHelper.dpToPx(getContext(), 52));
                }
                itemChooser.setDropDownHeight(height);
            }*/

        }
    }
}