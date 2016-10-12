package com.kaching123.tcr.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.DeleteKDSCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteKDSAliasCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.printeralias.AddEditDialog;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.StartMode;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.List;

/**
 * Created by long.jiao on 6.7.16.
 */
@EActivity (R.layout.printer_alias_activity)
@OptionsMenu(R.menu.printer_alias_activity)
public class KDSAliasActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri URI_KDS_ALIAS = ShopProvider.getContentUri(ShopStore.KDSAliasTable.URI_CONTENT);

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
                AddEditDialog.show(KDSAliasActivity.this, adapter.getItem(i), StartMode.EDIT);
            }
        });
        getSupportLoaderManager().restartLoader(0, null, new KDSAliasLoader());
    }

    @OptionsItem
    protected void actionAddSelected(){
        AddEditDialog.show(this, new KDSAliasModel(), StartMode.ADD);
    }

    private class Adapter extends ObjectsCursorAdapter<KDSAliasModel> implements DragSortListView.RemoveListener {

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
        protected View bindView(View convertView, int position, KDSAliasModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (item == null) {
                return convertView;
            }

            holder.title.setText(item.alias);

            return convertView;
        }

        @Override
        public void remove(int i) {
            handleRemove((KDSAliasModel) getItem(i));
        }

        private class ViewHolder {
            TextView title;
        }
    }
    private static final Uri KDS_URI = ShopProvider.getContentUri(ShopStore.ItemKDSTable.URI_CONTENT);

    private void handleRemove(final KDSAliasModel model) {
        DeleteKDSCommand.sync(KDSAliasActivity.this, model.guid);

        ContentValues cv = new ContentValues();
        cv.put(ShopStore.ItemKDSTable.IS_DELETED, 1);
        ProviderAction.update(KDS_URI)
                .values(cv)
                .where(ShopStore.ItemKDSTable.KDS_ALIAS_GUID + " = ?", model.guid)
                .perform(getApplicationContext());


        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.printer_alias_dialog_title_delete,
                getString(R.string.printer_alias_dialog_delete_msg, model.alias),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteKDSAliasCommand.start(KDSAliasActivity.this, model);
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

    private class KDSAliasLoader implements LoaderCallbacks<List<KDSAliasModel>> {

        @Override
        public Loader<List<KDSAliasModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_KDS_ALIAS)
                    .transform(new KDSAliasConverter())
                    .build(KDSAliasActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<KDSAliasModel>> listLoader, List<KDSAliasModel> printerAliasModels) {
            adapter.changeCursor(printerAliasModels);
        }

        @Override
        public void onLoaderReset(Loader<List<KDSAliasModel>> listLoader) {
            adapter.changeCursor(null);
        }

    }

    public static class KDSAliasConverter extends ListConverterFunction<KDSAliasModel>{
        @Override
        public KDSAliasModel apply(Cursor c) {
            super.apply(c);
            return new KDSAliasModel(
                    c.getString(indexHolder.get(ShopStore.KDSAliasTable.GUID)),
                    c.getString(indexHolder.get(ShopStore.KDSAliasTable.ALIAS))
            );
        }
    }

    public static void start(Context context){
        KDSAliasActivity_.intent(context).start();
    }

}
