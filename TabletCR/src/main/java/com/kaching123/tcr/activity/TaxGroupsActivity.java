package com.kaching123.tcr.activity;

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
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.commands.store.inventory.DeleteTaxGroupCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.taxgroup.TaxGroupDialog;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.TaxGroupModel;
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

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by pkabakov on 25.12.13.
 */
@EActivity(R.layout.tax_groups_activity)
@OptionsMenu(R.menu.tax_groups_activity)
public class TaxGroupsActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri URI_TAX_GROUPS = ShopProvider.getContentUri(ShopStore.TaxGroupTable.URI_CONTENT);

    @ViewById
    protected DragSortListView list;

    private TaxGroupsAdapter adapter;

    private TaxGroupsLoader loader = new TaxGroupsLoader();

    private DeleteTaxGroupCallback deleteTaxGroupCallback = new DeleteTaxGroupCallback();

    private String tax2DeleteTitle;

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void init() {
        adapter = new TaxGroupsAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TaxGroupModel model = adapter.getItem(i);
                //adapter.getItems();
                TaxGroupDialog.show(TaxGroupsActivity.this, model);
            }
        });

        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @OptionsItem
    protected void actionAddSelected() {
        TaxGroupDialog.show(this, null);
    }

    private class DeleteTaxGroupCallback extends DeleteTaxGroupCommand.BaseDeleteTaxGroupCallback {

        @Override
        protected void onTaxGroupDeleted() {
            Toast.makeText(TaxGroupsActivity.this, getString(R.string.tax_group_delete_succeded_message, tax2DeleteTitle), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onTaxGroupDeleteError() {
            Toast.makeText(TaxGroupsActivity.this, getString(R.string.tax_group_delete_failed_message, tax2DeleteTitle), Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }
    }

    private class TaxGroupsAdapter extends ObjectsArrayAdapter<TaxGroupModel> implements DragSortListView.RemoveListener {

        public TaxGroupsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.tax_group_list_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.tax = (TextView) convertView.findViewById(R.id.tax);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, TaxGroupModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.title.setText(item.title);
            UiHelper.showPercent(holder.tax, item.tax);

            return convertView;
        }

        @Override
        public void remove(int i) {
            handleRemove(getItem(i));
        }

        private class ViewHolder {
            TextView title;
            TextView tax;
        }
    }

    private void handleRemove(final TaxGroupModel item) {
        AlertDialogWithCancelListener.show(this, R.string.tax_group_delete_warning_dialog_title, getString(R.string.tax_group_delete_warning_dialog_message, item.title), R.string.btn_confirm, new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                tax2DeleteTitle = item.title;
                deleteTaxGroup(item);
                return true;
            }
        }, adapter);
    }

    private void deleteTaxGroup(TaxGroupModel item) {
        DeleteTaxGroupCommand.start(TaxGroupsActivity.this, item.guid, deleteTaxGroupCallback);
    }

    private class TaxGroupsLoader implements LoaderCallbacks<List<TaxGroupModel>> {

        @Override
        public Loader<List<TaxGroupModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_TAX_GROUPS)
                    .transform(new TaxGroupConverter())
                    .build(TaxGroupsActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<TaxGroupModel>> listLoader, List<TaxGroupModel> list) {
            adapter.changeCursor(list);
        }

        @Override
        public void onLoaderReset(Loader<List<TaxGroupModel>> listLoader) {
            adapter.changeCursor(null);
        }
    }

    private int defCount;

    private class DefaultTaxGroupsLoader implements LoaderCallbacks<List<TaxGroupModel>> {

        @Override
        public Loader<List<TaxGroupModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_TAX_GROUPS)
                    .where(ShopStore.TaxGroupTable.IS_DEFAULT + " = ? ", 1)
                    .transform(new TaxGroupConverter())
                    .build(TaxGroupsActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<TaxGroupModel>> listLoader, List<TaxGroupModel> list) {
            defCount = list.size();
        }

        @Override
        public void onLoaderReset(Loader<List<TaxGroupModel>> listLoader) {
            defCount = 0;
        }
    }

    private static class TaxGroupConverter extends ListConverterFunction<TaxGroupModel> {
        @Override
        public TaxGroupModel apply(Cursor c) {
            super.apply(c);
            return new TaxGroupModel(
                    c.getString(indexHolder.get(ShopStore.TaxGroupTable.GUID)),
                    c.getString(indexHolder.get(ShopStore.TaxGroupTable.TITLE)),
                    _decimal(c, indexHolder.get(ShopStore.TaxGroupTable.TAX)),
                    _bool(c, indexHolder.get(ShopStore.TaxGroupTable.IS_DEFAULT))
            );
        }
    }

    public static void start(Context context) {
        TaxGroupsActivity_.intent(context).start();
    }

}
