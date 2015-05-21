package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ResourceCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.PrinterAliasActivity;
import com.kaching123.tcr.commands.device.DeletePrinterCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PrinterView2.AliasTable;
import com.kaching123.tcr.store.ShopSchema2.PrinterView2.PrinterTable;
import com.kaching123.tcr.store.ShopStore.PrinterView;
import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment (R.layout.settings_printers_list_fragment)
@OptionsMenu(R.menu.discover_printer_activity)
public class PrinterListFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterView.URI_CONTENT);
    public static final int LOADER_ID = 123;

    private PrinterListAdapter adapter;

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected Switch printLogoSwitch;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        printLogoSwitch.setChecked(((TcrApplication) getActivity().getApplicationContext()).printLogo());
        printLogoSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TcrApplication.get().getShopPref().printLogo().put(isChecked);
            }
        });
        list.setEmptyView(getView().findViewById(android.R.id.empty));
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                PrinterModel model = adapter.getModel(i);
                PrinterStatusFragment.show(getActivity(), model.ip, model.port, model.mac, model.aliasGuid);
            }
        });
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @OptionsItem
    protected void actionAddSelected() {
        PrinterEditFragment.show(getActivity(), null);
    }

    @OptionsItem
    protected void actionSearchSelected() {
        FindPrinterFragment.show(getActivity());
    }

    @OptionsItem
    protected void actionManagePrinterAliasSelected(){
        PrinterAliasActivity.start(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), URI_PRINTER, new String[]{
                "0 as _id",
                PrinterTable.GUID,
                PrinterTable.IP,
                PrinterTable.PORT,
                PrinterTable.MAC,
                PrinterTable.SUBNET,
                PrinterTable.GATEWAY,
                PrinterTable.DHCP,
                PrinterTable.ALIAS_GUID,
                AliasTable.ALIAS
        }, null, null, AliasTable.ALIAS);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new PrinterListAdapter(getActivity());
        adapter.changeCursor(data);
        list.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        list.setAdapter(null);
    }

    private class PrinterListAdapter extends ResourceCursorAdapter implements DragSortListView.RemoveListener {

        public PrinterListAdapter(Context context) {
            super(context, R.layout.settings_printers_list_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            View edit = v.findViewById(R.id.btn_edit);
            edit.setOnClickListener(editItemClickListener);

            View config = v.findViewById(R.id.btn_config);
            config.setOnClickListener(configItemClickListener);

            v.setTag(new UiHolder(
                    (TextView) v.findViewById(android.R.id.text1),
                    (TextView) v.findViewById(android.R.id.text2),
                    edit,
                    config)
            );
            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {
            String aliasGuid = c.getString(c.getColumnIndex(PrinterTable.ALIAS_GUID));
            String alias = c.getString(c.getColumnIndex(AliasTable.ALIAS));

            UiHolder holder = (UiHolder) v.getTag();
            holder.pos = c.getPosition();
            holder.editView.setTag(holder.pos);
            holder.configView.setTag(holder.pos);

            holder.text1.setText(TextUtils.isEmpty(aliasGuid) ? getString(R.string.edit_printer_default) : TextUtils.isEmpty(alias) ? getString(R.string.edit_printer_unknown) : alias);
            holder.text2.setText(c.getString(c.getColumnIndex(PrinterTable.IP)) + ":" + c.getString(c.getColumnIndex(PrinterTable.PORT)));

            holder.configView.setEnabled(!TextUtils.isEmpty(c.getString(4)));
        }

        public PrinterModel getModel(int position) {
            Cursor c = (Cursor) getItem(position);
            return new PrinterModel(
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getInt(7) == 1,
                    c.getString(8));
        }

        @Override
        public void remove(int i) {
            handleRemove(getModel(i).getGuid());
        }

    }

    private void handleRemove(final String printerGuid) {
        AlertDialogFragment.show(
                getActivity(),
                DialogType.CONFIRM_NONE,
                R.string.printer_delete_dialog_title,
                getString(R.string.printer_delete_dialog_msg),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeletePrinterCommand.start(getActivity(), printerGuid);
                        return true;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                }, null
        );
    }

    /*private View.OnClickListener statusItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            UiHolder holder = (UiHolder)v.getTag();
            PrinterModel model = adapter.getModel(holder.pos);
            PrinterStatusFragment.show(getActivity(), model.ip, model.port);
        }
    };*/

    private View.OnClickListener editItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Integer pos = (Integer)v.getTag();
            PrinterEditAliasFragment.show(getActivity(), adapter.getModel(pos));
        }
    };

    private View.OnClickListener configItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Integer pos = (Integer)v.getTag();
            final PrinterModel model = adapter.getModel(pos);
            if(TextUtils.isEmpty(model.mac)){
                /*AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.printer_config_mac_error),
                        R.string.btn_configure,
                        new OnDialogClickListener() {

                            @Override
                            public boolean onClick() {
                                PrinterEditFragment.show(getActivity(), model);
                                return true;
                            }
                        },
                        new OnDialogClickListener() {
                            @Override
                            public boolean onClick() {
                                return true;
                            }
                        }
                );*/
                return;
            }
            PrinterConfigFragment.show(getActivity(), model);
        }
    };

    private static class UiHolder {

        View editView;
        View configView;

        TextView text1;

        TextView text2;

        int pos;

        private UiHolder(TextView text1, TextView text2, View editView, View configView) {
            this.text1 = text1;
            this.text2 = text2;
            this.editView = editView;
            this.configView = configView;
        }
    }

    public static Fragment instance() {
        return PrinterListFragment_.builder().build();
    }
}
