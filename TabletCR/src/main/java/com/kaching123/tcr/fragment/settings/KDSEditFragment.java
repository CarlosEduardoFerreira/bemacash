package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.settings.EditKDSCommand;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.IpAddressFormatInputFilter;
import com.kaching123.tcr.component.PortFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.KDSAliasTable;
import com.kaching123.tcr.util.Validator;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Arrays;

import static com.kaching123.tcr.R.layout.spinner_item_dark;
import static com.kaching123.tcr.util.Util.toInt;

/**
 * Created by long.jiao on 06.21.16.
 */
@EFragment
public class KDSEditFragment extends KeyboardDialogFragment implements LoaderCallbacks<Cursor> {

    private static final Uri URI_ALIAS = ShopProvider.getContentUri(ShopStore.KDSAliasTable.URI_CONTENT);
    public static final String KDS_EDIT_FRAGMENT = "KDS_EDIT_FRAGMENT";

    @FragmentArg
    protected KDSModel model;

    public DialogInterface.OnDismissListener getListener() {
        return listener;
    }

    public void setListener(DialogInterface.OnDismissListener listener) {
        this.listener = listener;
    }

    protected DialogInterface.OnDismissListener listener;

    @ViewById(R.id.alias)
    protected Spinner alias;
    @ViewById(R.id.ip)
    protected CustomEditBox ipText;
    @ViewById(R.id.port)
    protected CustomEditBox portText;

    private AliasAdapter aliasAdapter;

    private MatrixCursor defCursor = new MatrixCursor(new String[]{"_id", KDSAliasTable.GUID, KDSAliasTable.ALIAS});

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_kds_edit_fragment;
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
        String aliasGuid = aliasAdapter.getGuid(alias.getSelectedItemPosition());

        model.ip = ip;
        model.port = port;
        model.aliasGuid = aliasGuid;
        EditKDSCommand.start(getActivity(), model, new EditKDSCommand.Callback() {
            @Override
            protected void handleSuccess() {
                dismiss();
            }
        });
        return true;
    }

    private boolean isValidIp(){
        return Validator.isIp(ipText.getText().toString());
    }

    private boolean isValidPort(){
        return toInt(portText.getText().toString(), 0) > 0;
    }

    private boolean isValidAll() {
        return isValidIp() && isValidPort() && aliasAdapter.getCount() != 0;
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
            model = new KDSModel(null, "", 3000, null);
        }
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

        aliasAdapter = new AliasAdapter(getActivity());
        alias.setAdapter(aliasAdapter);

        ipText.setText(model.ip);
        portText.setText(String.valueOf(model.port));

        enablePositiveButtons(isValidAll());
        getLoaderManager().initLoader(0, null, this);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), URI_ALIAS, new String[]{"0 as _id", KDSAliasTable.GUID, KDSAliasTable.ALIAS}, null, null, KDSAliasTable.ALIAS);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        int selected = 0;
        if (!TextUtils.isEmpty(model.aliasGuid)) {
            int i = defCursor.getCount();
            while (c.moveToNext()) {
                String guid = c.getString(1);
                if (guid.equals(model.aliasGuid)) {
                    selected = i;
                    break;
                }
                i++;
            }
        }
        aliasAdapter.changeCursor(new MergeCursor(new Cursor[]{defCursor, c}));
        alias.setSelection(selected);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onDismiss(dialog);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        aliasAdapter.changeCursor(defCursor);
    }

    public static KDSEditFragment show(FragmentActivity activity, KDSModel model) {
        KDSEditFragment fragment = KDSEditFragment_.builder().model(model).build();
        DialogUtil.show(activity, KDS_EDIT_FRAGMENT, fragment);
        return fragment;
    }

    public static void hide (FragmentActivity activity){
        DialogUtil.hide(activity, KDS_EDIT_FRAGMENT);
    }


    private class AliasAdapter extends ResourceCursorAdapter {

        public AliasAdapter(Context context) {
            super(context, spinner_item_dark, null, false);
            setDropDownViewResource(R.layout.spinner_dropdown_item);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ((TextView) view).setText(c.getString(2));
        }

        public String getGuid(int pos) {
            return ((Cursor) getItem(pos)).getString(1);
        }
    }
}
