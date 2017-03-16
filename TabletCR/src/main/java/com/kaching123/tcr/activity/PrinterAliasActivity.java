package com.kaching123.tcr.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.inventory.DeletePrinterAliasCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.printeralias.AddEditDialog;
import com.kaching123.tcr.model.AliasModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.model.StartMode;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.List;

/**
 * Created by vkompaniets on 11.02.14.
 */
@EActivity (R.layout.printer_alias_activity)
@OptionsMenu(R.menu.printer_alias_activity)
public class PrinterAliasActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri URI_PRINTER_ALIAS = ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT);

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @ViewById
    protected DragSortListView list;

    private Adapter adapter;


    @AfterViews
    protected void init(){
        adapter = new Adapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("BemaCarl2","PrinterAliasActivity.onItemClick.adapter.getItem(i).toValues(): " + adapter.getItem(i).toValues());
                Log.d("BemaCarl2","PrinterAliasActivity.onItemClick.adapter.getItem(i).guid: " + adapter.getItem(i).guid);
                Log.d("BemaCarl2","PrinterAliasActivity.onItemClick.adapter.getItem(i).alias: " + adapter.getItem(i).alias);
                PrinterAliasModel pam = new PrinterAliasModel(adapter.getItem(i).guid , adapter.getItem(i).alias);
                AddEditDialog.show(PrinterAliasActivity.this, pam, StartMode.EDIT);
            }
        });
        getSupportLoaderManager().restartLoader(0, null, new PrinterAliasLoader());
    }

    @OptionsItem
    protected void actionAddSelected(){
        AddEditDialog.show(this, new PrinterAliasModel(), StartMode.ADD);
    }

    private class Adapter extends ObjectsCursorAdapter<PrinterAliasModel> implements DragSortListView.RemoveListener {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.printer_alias_list_item, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, PrinterAliasModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (item == null) {
                return convertView;
            }

            holder.title.setText(item.alias);

            return convertView;
        }

        @Override
        public void remove(int i) {
            handleRemove(getItem(i));
        }

        private class ViewHolder {
            TextView title;
        }
    }

    private void handleRemove(final PrinterAliasModel model) {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.printer_alias_dialog_title_delete,
                getString(R.string.printer_alias_dialog_delete_msg, model.alias),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeletePrinterAliasCommand.start(PrinterAliasActivity.this, model);
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

    private class PrinterAliasLoader implements LoaderCallbacks<List<PrinterAliasModel>> {

        @Override
        public Loader<List<PrinterAliasModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_PRINTER_ALIAS)
                    .transformRow(new PrinterAliasConverter())
                    .build(PrinterAliasActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<PrinterAliasModel>> listLoader, List<PrinterAliasModel> printerAliasModels) {
            adapter.changeCursor(printerAliasModels);
        }

        @Override
        public void onLoaderReset(Loader<List<PrinterAliasModel>> listLoader) {
            adapter.changeCursor(null);
        }

    }

    public static class PrinterAliasConverter extends ListConverterFunction<PrinterAliasModel>{
        @Override
        public PrinterAliasModel apply(Cursor c) {
            super.apply(c);
            return new PrinterAliasModel(
                    c.getString(indexHolder.get(PrinterAliasTable.GUID)),
                    c.getString(indexHolder.get(PrinterAliasTable.ALIAS))
            );
        }
    }

    public static void start(Context context){
        PrinterAliasActivity_.intent(context).start();
    }

}
