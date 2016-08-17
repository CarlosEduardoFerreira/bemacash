package com.kaching123.tcr.fragment.settings;

import android.content.DialogInterface;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.IpAddressFormatInputFilter;
import com.kaching123.tcr.component.PortFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.KDSAliasTable;
import com.kaching123.tcr.util.Validator;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by long.jiao on 06.21.16.
 */
@EFragment
public class KDSRouterEditFragment extends KeyboardDialogFragment{

    private static final Uri URI_ALIAS = ShopProvider.getContentUri(KDSAliasTable.URI_CONTENT);
    public static final String KDS_ROUTER_EDIT_FRAGMENT = "KDS_ROUTER_EDIT_FRAGMENT";

    public DialogInterface.OnDismissListener getListener() {
        return listener;
    }

    public void setListener(DialogInterface.OnDismissListener listener) {
        this.listener = listener;
    }

    protected DialogInterface.OnDismissListener listener;

    @ViewById(R.id.ip)
    protected CustomEditBox ipText;
    @ViewById(R.id.port)
    protected CustomEditBox portText;

    private MatrixCursor defCursor = new MatrixCursor(new String[]{"_id", KDSAliasTable.GUID, KDSAliasTable.ALIAS});

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_kds_router_edit_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.kds_edit_dialog_title;
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

        getApp().getShopPref().kdsRouterIp().put(ip);
        getApp().getShopPref().kdsRouterPort().put(port);
        dismiss();
        return true;
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
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.printer_edit_dialog_width),
                getDialog().getWindow().getAttributes().height);

//        defCursor.addRow(new String[]{"0", null, null});

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

        ipText.requestFocusFromTouch();


        ipText.setText(getApp().getShopPref().kdsRouterIp().getOr(""));
        portText.setText(String.valueOf(getApp().getShopPref().kdsRouterPort().getOr(3000)));

        enablePositiveButtons(isValidAll());
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onDismiss(dialog);
        }
    }

    public static void show(FragmentActivity activity, DialogInterface.OnDismissListener listener) {
        DialogUtil.show(activity, KDS_ROUTER_EDIT_FRAGMENT, KDSRouterEditFragment_.builder().build()).setListener(listener);
    }

    public static void hide (FragmentActivity activity){
        DialogUtil.hide(activity, KDS_ROUTER_EDIT_FRAGMENT);
    }
}
