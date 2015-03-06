package com.kaching123.tcr.fragment.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.ConfigurePrinterCommand;
import com.kaching123.tcr.commands.device.ConfigurePrinterCommand.PrinterConfigureBaseCallback;
import com.kaching123.tcr.commands.store.settings.EditPrinterCommand;
import com.kaching123.tcr.component.IpAddressFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.util.Validator;

import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment
public class PrinterConfigFragment extends StyledDialogFragment {

    public static final String DIALOG_NAME = "PRINTER_CONFIG_FRAGMENT";

    @FragmentArg
    protected PrinterModel model;

    @ViewById
    protected EditText ipText;
    @ViewById
    protected EditText portText;
    @ViewById
    protected EditText subnetText;
    @ViewById
    protected EditText gatewayText;
    @ViewById
    protected CheckBox dhcpCheckbox;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_printer_config_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.printer_config_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_configure;
    }

    @Override
    protected boolean hasSkipButton() {
        return true;
    }

    @Override
    protected int getSkipButtonTitle() {
        return R.string.btn_save;
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                model.dhcp = dhcpCheckbox.isChecked();
                if(model.dhcp){
                    model.ip = "";
                    model.subNet = "";
                    model.gateway = "";
                }else{
                    model.ip = ipText.getText().toString();
                    model.subNet = subnetText.getText().toString();
                    model.gateway = gatewayText.getText().toString();
                }
                EditPrinterCommand.start(getActivity(), model);
                return true;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.printer_config_dialog_width),
                getDialog().getWindow().getAttributes().height);

        ipText.requestFocusFromTouch();

        ipText.setText(model.ip);
        portText.setText(String.valueOf(model.port));
        subnetText.setText(model.subNet);
        gatewayText.setText(model.gateway);


        InputFilter[] ipFilter = new InputFilter[]{new IpAddressFormatInputFilter()};
        ipText.setFilters(ipFilter);
        subnetText.setFilters(ipFilter);
        gatewayText.setFilters(ipFilter);
        dhcpCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ipText.setEnabled(!isChecked);
                subnetText.setEnabled(!isChecked);
                gatewayText.setEnabled(!isChecked);
            }
        });
        dhcpCheckbox.setChecked(model.dhcp);
        validatePositiveButton();
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                collectData();

                WaitDialogFragment.show(getActivity(), getString(R.string.printer_config_update_wait));
                ConfigurePrinterCommand.start(
                        getActivity(),
                        model,
                        callback
                );
                return false;
            }
        };
    }

    private void collectData() {
        boolean useDhcp = dhcpCheckbox.isChecked();

        model.dhcp = useDhcp;
        if (!useDhcp) {
            String ip = ipText.getText().toString();
            int port = toInt(portText.getText().toString(), 0);
            String subnet = subnetText.getText().toString();
            String gateway = gatewayText.getText().toString();

            model.ip = ip;
            model.port = port;
            model.subNet = subnet;
            model.gateway = gateway;
        }

    }

    private boolean isValid() {
        boolean useDhcp = dhcpCheckbox.isChecked();
        if(useDhcp)
            return true;

        String ip = ipText.getText().toString();
        int port = toInt(portText.getText().toString(), 0);
        String subNet = subnetText.getText().toString();
        String gateway = gatewayText.getText().toString();

        return Validator.isIp(ip) && port > 0 && Validator.isIp(subNet) && Validator.isIp(gateway);
    }

    private boolean isValidUpdate() {
        boolean useDhcp = dhcpCheckbox.isChecked();
        if(useDhcp)
            return true;

        String ip = ipText.getText().toString();
        String sPort = portText.getText().toString();
        int port = toInt(sPort, 0);
        String subNet = subnetText.getText().toString();
        String gateway = gatewayText.getText().toString();

        return (TextUtils.isEmpty(ip) || Validator.isIp(ip)) && (TextUtils.isEmpty(sPort) || port > 0)
                && (TextUtils.isEmpty(subNet) || Validator.isIp(subNet))
                && (TextUtils.isEmpty(gateway) || Validator.isIp(gateway));
    }

    private void validatePositiveButton() {
        enableSkipButtons(isValidUpdate());
        enablePositiveButtons(isValid());
    }

    @AfterTextChange
    protected void ipTextAfterTextChanged(Editable s) {
        validatePositiveButton();
    }

    @AfterTextChange
    protected void portTextAfterTextChanged(Editable s) {
        validatePositiveButton();
    }

    @AfterTextChange
    protected void subnetTextAfterTextChanged(Editable s) {
        validatePositiveButton();
    }

    @AfterTextChange
    protected void gatewayTextAfterTextChanged(Editable s) {
        validatePositiveButton();
    }

    public static void show(FragmentActivity activity, PrinterModel model) {
        DialogUtil.show(activity, DIALOG_NAME, PrinterConfigFragment_.builder().model(model).build());
    }

    public PrinterConfigureBaseCallback callback = new PrinterConfigureBaseCallback() {

        @Override
        protected void handleSuccess() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.show(getActivity(), DialogType.COMPLETE, R.string.printer_config_dialog_title,
                    getString(R.string.printer_config_update_ok), R.string.btn_ok, new OnDialogClickListener() {

                @Override
                public boolean onClick() {
                    return true;
                }
            });
            dismiss();
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.printer_config_update_error));
        }
    };
}
