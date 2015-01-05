package com.kaching123.tcr.fragment.filemanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioGroup;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand.ImportType;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by gdubina on 15/01/14.
 */
@EFragment
public class ImportTypeFragment extends StyledDialogFragment {

    public static final String DIALOG_NAME = "IMPORT_TYPE_DIALOG";

    private OnTypeChosenListener listener;

    @ViewById
    protected RadioGroup importTypeGroup;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        importTypeGroup.check(R.id.import_full);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.filemanager_importtype_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.inventory_import_type_title;
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
                if (listener != null) {
                    int selected = importTypeGroup.getCheckedRadioButtonId();
                    listener.onTypeChosen(selected == R.id.import_qty ? ImportInventoryCommand.ImportType.QTY :
                            selected == R.id.import_price ? ImportInventoryCommand.ImportType.PRICE :
                            selected == R.id.batch_delete ? ImportType.DELETE :
                            ImportInventoryCommand.ImportType.ALL);
                }
                return true;
            }
        };
    }

    public void setListener(OnTypeChosenListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentActivity activity, OnTypeChosenListener callback) {
        DialogUtil.show(activity, DIALOG_NAME, ImportTypeFragment_.builder().build()).setListener(callback);
    }

    public static interface OnTypeChosenListener {
        void onTypeChosen(ImportInventoryCommand.ImportType type);
    }
}
