package com.kaching123.tcr.fragment.department;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddDepartmentCommand;
import com.kaching123.tcr.commands.store.inventory.EditDepartmentCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.DepartmentModel;

/**
 * Created by vkompaniets on 17.12.13.
 */
@EFragment (R.layout.department_dialog_fragment)
public class DepartmentDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = "department_dialog";

    @FragmentArg
    protected DepartmentModel model;

    @ViewById
    protected EditText title;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.department_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.department_dialog_height);

        if (model != null){
            title.setText(model.title);
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
        return R.layout.department_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return model == null ? R.string.department_dialog_title_create : R.string.department_dialog_title_edit;
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
        if (fieldsValide()){
            String title = this.title.getText().toString().trim();
            if (model != null){
                model.title = title;
                EditDepartmentCommand.start(getActivity(), model);
            }else{
                AddDepartmentCommand.start(getActivity(), title);
            }
            return true;
        }
        return false;
    }

    private boolean fieldsValide() {
        if (TextUtils.isEmpty(title.getText().toString().trim()))
            return false;

        return true;
    }

    public static void show (FragmentActivity activity, DepartmentModel model){
        DialogUtil.show(activity, DIALOG_NAME, DepartmentDialog_.builder().model(model).build());
    }


}
