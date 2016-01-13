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
import com.kaching123.tcr.adapter.BaseItemMatrixAdapter;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

/**
 * Created by aakimov on 20.05.2015.
 */
@EFragment(R.layout.choose_parent_item_dialog_fragment)
public class ChooseParentItemDialogFragment extends StyledDialogFragment implements BarcodeReceiver {

    private static final String DIALOG_NAME = "ChooseParentItem";

    @ColorRes(android.R.color.white)
    protected int whiteColor;

    @ViewById
    protected AutoCompleteTextView itemChooser;
    @ViewById
    protected AutoCompleteTextView itemMatrixChooser;

    @FragmentArg
    protected ItemMatrixModel itemMatrixModel;

    @FragmentArg
    protected ItemExModel parentModel;

    @FragmentArg
    protected String childGuid;

    protected ItemsAdapter itemsAdapter;
    protected ItemMatrixAdapter itemMatrixAdapter;
    private OnItemChosenListener onChosenClickListener;

    public interface OnItemChosenListener {
        void onItemChosen(ItemExModel parentItem, ItemMatrixModel parentItemMatrix);
    }

    public static void show(FragmentActivity activity,
                            String childGuid, OnItemChosenListener onChosenClickListener) {
        DialogUtil.show(activity, DIALOG_NAME, ChooseParentItemDialogFragment_
                .builder()
                .childGuid(childGuid)
                .build()).setOnChosenClickListener(onChosenClickListener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public ChooseParentItemDialogFragment setOnChosenClickListener(OnItemChosenListener onChosenClickListener) {
        this.onChosenClickListener = onChosenClickListener;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews() {
        enablePositiveButton(false, whiteColor);
        itemMatrixChooser.setEnabled(false);
        itemsAdapter = new ItemsAdapter(getActivity());
        itemChooser.setAdapter(itemsAdapter);
        itemChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentModel = ((ItemsAdapter) parent.getAdapter()).getItem(position);
                itemMatrixChooser.setEnabled(true);
                enablePositiveButton(true, whiteColor);
            }
        });
        itemChooser.addTextChangedListener(textWatcher);
        itemMatrixAdapter = new ItemMatrixAdapter(getActivity());
        itemMatrixChooser.setAdapter(itemMatrixAdapter);
        itemMatrixChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemMatrixModel = ((ItemMatrixAdapter) parent.getAdapter()).getItem(position);
                enablePositiveButton(true, whiteColor);
            }
        });
        itemMatrixChooser.addTextChangedListener(textWatcher);
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
        return R.layout.choose_parent_item_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.variants_add_parent_item_title;
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
                if (itemMatrixModel != null) {
                    itemMatrixModel.childItemGuid = childGuid;
                }
                onChosenClickListener.onItemChosen(parentModel, itemMatrixModel);
                return true;
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
            return ShopSchema2.ItemExtView2.ItemTable.ITEM_REF_TYPE + "=?";
        }

        @Override
        protected String[] getCustomSelectionArgs() {
            return new String[]{String.valueOf(ItemRefType.Reference.ordinal())};
        }

        @Override
        protected void publishResults(FluentIterable<ItemExModel> cursor) {


        }
    }

    private class ItemMatrixAdapter extends BaseItemMatrixAdapter {
        public ItemMatrixAdapter(Context context) {
            super(context);
        }

        @Override
        protected void publishResults(FluentIterable<ItemMatrixModel> cursor) {
            // set dropdown height for itemMatrixChooser to be upper the keyboard
            if (cursor == null || cursor.size() == 0) {
                itemMatrixChooser.setDropDownHeight(0);
            } else {
                int height;
                if (cursor.size() == 1) {
                    height = UiHelper.dpToPx(getContext(), 60);
                } else {
                    height = (cursor.size() > 3 ? 3 : cursor.size()) * UiHelper.dpToPx(getContext(), 52);
                }
                itemMatrixChooser.setDropDownHeight(height);
            }
        }


        @Override
        protected String getSelection() {
            String selection = ShopStore.ItemMatrixTable.PARENT_GUID + "=?" + " AND "
                    + ShopStore.ItemMatrixTable.NAME + " LIKE ?"
                    + " AND " + ShopStore.ItemMatrixTable.CHILD_GUID + " IS NULL";
            return selection;
        }

        @Override
        protected String[] getSelectionArgs(CharSequence constraint) {
            return new String[]{parentModel.guid, constraint + "%"};
        }
    }
}