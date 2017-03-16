package com.kaching123.tcr.fragment.printeralias;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddKDSAliasCommand;
import com.kaching123.tcr.commands.store.inventory.AddPrinterAliasCommand;
import com.kaching123.tcr.commands.store.inventory.EditKDSAliasCommand;
import com.kaching123.tcr.commands.store.inventory.EditPrinterAliasCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.AliasModel;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.model.StartMode;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 12.02.14.
 */
@EFragment (R.layout.printer_alias_dialog_fragment)
public class AddEditDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = "add_edit_dialog";

    @FragmentArg
    protected AliasModel model;

    @FragmentArg
    protected StartMode mode;

    @ViewById
    protected EditText title;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.printer_alias_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.printer_alias_dialog_height);

        if (model != null){
            model.guid = model.toValues().getAsString("guid");
            model.alias = model.toValues().getAsString("alias");
            title.setText(model.alias);
        }

        title.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(EditorInfo.IME_ACTION_DONE == i){
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
        return R.layout.printer_alias_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return mode == StartMode.ADD ? R.string.printer_alias_dialog_title_create : R.string.printer_alias_dialog_title_edit;
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
        if (valideForm()){
            String title = this.title.getText().toString().trim();
            if (mode == StartMode.EDIT){
                model.alias = title;
                if(model instanceof PrinterAliasModel)
                    EditPrinterAliasCommand.start(getActivity(), new PrinterAliasModel(((PrinterAliasModel) model).guid, title));
                else
                    EditKDSAliasCommand.start(getActivity(), new KDSAliasModel(((KDSAliasModel) model).guid, title));
            }else{
                if(model instanceof PrinterAliasModel)
                    AddPrinterAliasCommand.start(getActivity(), title);
                else
                    AddKDSAliasCommand.start(getActivity(), title);
            }
            return true;
        }
        return false;
    }

    private boolean valideForm() {
        if (TextUtils.isEmpty(title.getText().toString().trim()))
            return false;

        return true;
    }

    public static void show(FragmentActivity activity, AliasModel model, StartMode mode){
        DialogUtil.show(activity, DIALOG_NAME, AddEditDialog_.builder().model(model).mode(mode).build());
    }


}
