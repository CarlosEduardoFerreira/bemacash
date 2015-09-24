package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.settings.EditPaxCommand;
import com.kaching123.tcr.commands.store.settings.EditPaxCommand.PaxEditCommandBaseCallback;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.IpAddressFormatInputFilter;
import com.kaching123.tcr.component.PortFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.util.Validator;

import static com.kaching123.tcr.util.Util.toInt;

@EFragment
public class PaxEditFragment extends KeyboardDialogFragment {

    public static final String PRINTER_EDIT_FRAGMENT = "PAX_EDIT_FRAGMENT";

    @FragmentArg
    protected PaxModel model;

    @StringRes(R.string.edit_pax_default)
    protected String defautlLabel;

    @ViewById(R.id.ip)
    protected CustomEditBox ipText;
    @ViewById(R.id.port)
    protected CustomEditBox portText;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_pax_edit_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.pax_edit_dialog_title;
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
                return onEdit();
            }
        };
    }

    private boolean onEdit() {
        String ip = ipText.getText().toString();
        int port = toInt(portText.getText().toString(), 0);

        model.ip = ip;
        model.port = port;
        EditPaxCommand.start(getActivity(), model, new PaxEditCommandBaseCallback() {
            @Override
            protected void handleSuccess() {
                Toast.makeText(getActivity(), getString(R.string.pax_configured), Toast.LENGTH_LONG).show();
                PaxEditFragment.this.dismiss();
            }

            @Override
            protected void handleError() {
                Toast.makeText(getActivity(), getString(R.string.pax_not_configured), Toast.LENGTH_LONG).show();
                PaxEditFragment.this.dismiss();
            }
        });
        Toast.makeText(getActivity(), getString(R.string.pax_configuring), Toast.LENGTH_LONG).show();
        getPositiveButton().setEnabled(false);
        getPositiveButton().setTextColor(getResources().getColor(R.color.gray_dark));
        getNegativeButton().setTextColor(getResources().getColor(R.color.gray_dark));
        ipText.setEnabled(false);
        portText.setEnabled(false);
        getNegativeButton().setEnabled(false);
        InputMethodManager imm = (InputMethodManager)getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ipText.getWindowToken(), 0);
        return false;
    }

    private boolean isValidIp(){
        return Validator.isIp(ipText.getText().toString());
    }

    private boolean isValidPort(){
        return toInt(portText.getText().toString(), 0) > 0;
    }

    private boolean isValidAll() {
        return isValidIp() && isValidPort();
    }

    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        if (v == ipText){
            keyboard.setDotEnabled(true);
        }else{
            keyboard.setDotEnabled(false);
        }
        super.attachMe2Keyboard(v);
    }

    @AfterTextChange
    protected void ipAfterTextChanged(Editable s) {
        keyboard.setEnterEnabled(isValidIp());
        enablePositiveButton(isValidAll(), greenBtnColor);
    }

    @AfterTextChange
    protected void portAfterTextChanged(Editable s) {
        enablePositiveButtons(isValidAll());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (model == null) {
            model = new PaxModel(null, "", 10009, "", null, null, false, null);
        }
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.printer_edit_dialog_width),
                getDialog().getWindow().getAttributes().height);

        ipText.setFilters(new InputFilter[]{new IpAddressFormatInputFilter()});
        portText.setFilters(new InputFilter[]{new PortFormatInputFilter()});

        ipText.setKeyboardSupportConteiner(this);
        portText.setKeyboardSupportConteiner(this);

        ipText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                portText.requestFocusFromTouch();
                return false;
            }
        });
        portText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                return onEdit();
            }
        });

        ipText.setText(model.ip);
        portText.setText(String.valueOf(model.port));

        ipText.requestFocusFromTouch();

        enablePositiveButtons(isValidAll());
    }

    public static void show(FragmentActivity activity, PaxModel model) {
        DialogUtil.show(activity, PRINTER_EDIT_FRAGMENT, PaxEditFragment_.builder().model(model).build());
    }
}
