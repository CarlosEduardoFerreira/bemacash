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
import com.kaching123.tcr.commands.store.inventory.UpdateTaxGroup;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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

    protected TaxGroupsAdapter adapter;

    private TaxGroupsLoader loader = new TaxGroupsLoader();

    private DeleteTaxGroupCallback deleteTaxGroupCallback = new DeleteTaxGroupCallback();

    private String tax2DeleteTitle;

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void init() {
        adapter = getAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TaxGroupModel model = adapter.getItem(i);
                showTaxGroupDialog(model);
            }
        });
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    protected TaxGroupsAdapter getAdapter() {
        return new TaxGroupsAdapter(this);
    }

    protected void showTaxGroupDialog(TaxGroupModel model) {
        TaxGroupDialog.show(this, model, changeListener);
    }

    protected TaxGroupDialog.TaxGroupListener changeListener = new TaxGroupDialog.TaxGroupListener() {
        @Override
        public void onTaxGroupChanged(String guid, String title, BigDecimal tax, boolean isDefault) {
            onGroupChanged(guid, title, tax, isDefault);
        }
    };

    private void onGroupChanged(String guid, String title, BigDecimal tax, boolean isDefault) {
        UpdateTaxGroup.start(TaxGroupsActivity.this, new UpdateTaxGroup.BaseUpdateTaxGroupCallback() {
            @Override
            protected void onTaxGroupUpdated() {
                onGroupUpdated();
            }

            @Override
            protected void onTaxGroupUpdateError() {
            }
        }, guid, title, tax, isDefault);
    }

    protected void onGroupUpdated() {
    }

    @OptionsItem
    protected void actionAddSelected() {
        showTaxGroupDialog(null);
    }

    protected void onGroupDeleted() {
        Toast.makeText(TaxGroupsActivity.this, getString(R.string.tax_group_delete_succeded_message,
                tax2DeleteTitle), Toast.LENGTH_SHORT).show();
    }

    private class DeleteTaxGroupCallback extends DeleteTaxGroupCommand.BaseDeleteTaxGroupCallback {

        @Override
        protected void onTaxGroupDeleted() {
            WaitDialogFragment.hide(TaxGroupsActivity.this);
            onGroupDeleted();
        }

        @Override
        protected void onTaxGroupHasItems(String taxName, int itemsCount) {
            WaitDialogFragment.hide(TaxGroupsActivity.this);
            Toast.makeText(TaxGroupsActivity.this, String.format(Locale.US, getString(R.string.tax_group_toast_failed_message), taxName, itemsCount), Toast.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
        }
    }

    protected class TaxGroupsAdapter extends ObjectsArrayAdapter<TaxGroupModel> implements DragSortListView.RemoveListener {

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

        protected void display(ViewHolder holder, TaxGroupModel item) {
            holder.title.setText(item.title);
            UiHelper.showPercent(holder.tax, item.tax);
        }

        @Override
        protected View bindView(View convertView, int position, TaxGroupModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            display(holder, item);
            return convertView;
        }

        @Override
        public void remove(int i) {
            handleRemove(getItem(i));
        }

        class ViewHolder {
            TextView title;
            TextView tax;
        }
    }

    private void handleRemove(final TaxGroupModel item) {
        AlertDialogWithCancelListener.show(this, R.string.tax_group_delete_warning_dialog_title,
                getString(R.string.tax_group_delete_warning_dialog_message, item.title),
                R.string.btn_confirm, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        tax2DeleteTitle = item.title;
                        try2DeleteTaxGroup(item);
                        return true;
                    }
                }, adapter);
    }

    private void try2DeleteTaxGroup(TaxGroupModel model) {
        WaitDialogFragment.show(this, getString(R.string.search_items_wait_dialog_message));
        DeleteTaxGroupCommand.start(TaxGroupsActivity.this, model, deleteTaxGroupCallback);
    }

    private class TaxGroupsLoader implements LoaderCallbacks<List<TaxGroupModel>> {

        @Override
        public Loader<List<TaxGroupModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_TAX_GROUPS)
                    .transformRow(new TaxGroupConverter())
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

    protected static class TaxGroupConverter extends ListConverterFunction<TaxGroupModel> {
        @Override
        public TaxGroupModel apply(Cursor c) {
            super.apply(c);
            return new TaxGroupModel(
                    c.getString(indexHolder.get(ShopStore.TaxGroupTable.GUID)),
                    c.getString(indexHolder.get(ShopStore.TaxGroupTable.TITLE)),
                    _decimal(c, indexHolder.get(ShopStore.TaxGroupTable.TAX), BigDecimal.ZERO),
                    _bool(c, indexHolder.get(ShopStore.TaxGroupTable.IS_DEFAULT)),
                    null
            );
        }
    }

    public static void start(Context context) {
        TaxGroupsActivity_.intent(context).start();
    }

}
