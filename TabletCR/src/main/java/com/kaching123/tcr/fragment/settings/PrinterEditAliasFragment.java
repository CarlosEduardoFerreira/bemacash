package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.settings.EditPrinterCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment
public class PrinterEditAliasFragment extends StyledDialogFragment implements LoaderCallbacks<Cursor> {

    private static final Uri URI_ALIAS = ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT);
    public static final String PRINTER_EDIT_FRAGMENT = "PRINTER_EDIT_ALIAS_FRAGMENT";

    @FragmentArg
    protected PrinterModel model;

    @StringRes(R.string.edit_printer_default)
    protected String defautlLabel;

    @ViewById(R.id.alias)
    protected Spinner alias;

    private AliasAdapter aliasAdapter;

    private MatrixCursor defCursor = new MatrixCursor(new String[]{"_id", PrinterAliasTable.GUID, PrinterAliasTable.ALIAS});

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_printer_edit_alias_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.printer_edit_alias_dialog_title;
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
        String aliasGuid = aliasAdapter.getGuid(alias.getSelectedItemPosition());

        model.aliasGuid = aliasGuid;
        EditPrinterCommand.start(getActivity(), model);
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (model == null) {
            model = new PrinterModel(null, "", 9100, "", null, null, false, null);
        }
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.printer_edit_dialog_width),
                getDialog().getWindow().getAttributes().height);

        defCursor.addRow(new String[]{"0", null, defautlLabel});

        aliasAdapter = new AliasAdapter(getActivity());
        alias.setAdapter(aliasAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), URI_ALIAS, new String[]{"0 as _id", PrinterAliasTable.GUID, PrinterAliasTable.ALIAS}, null, null, PrinterAliasTable.ALIAS);
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
    public void onLoaderReset(Loader<Cursor> loader) {
        aliasAdapter.changeCursor(defCursor);
    }

    public static void show(FragmentActivity activity, PrinterModel model) {
        DialogUtil.show(activity, PRINTER_EDIT_FRAGMENT, PrinterEditAliasFragment_.builder().model(model).build());
    }


    private class AliasAdapter extends ResourceCursorAdapter {

        public AliasAdapter(Context context) {
            super(context, R.layout.spinner_item_dark, null, false);
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
