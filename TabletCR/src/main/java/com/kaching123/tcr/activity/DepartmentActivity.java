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
import com.kaching123.tcr.commands.store.inventory.DeleteDepartmentCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteDepartmentCommand.DeleteDepartmentCallback;
import com.kaching123.tcr.fragment.department.DepartmentDialog;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by vkompaniets on 17.12.13.
 */
@EActivity(R.layout.department_activity)
@OptionsMenu(R.menu.department_activity)
public class DepartmentActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri URI_DEPARTMENTS = ShopProvider.getContentUri(ShopStore.DepartmentTable.URI_CONTENT);

    @ViewById
    protected DragSortListView list;

    private DepartmentsAdapter adapter;

    private DepartmentsLoader loader = new DepartmentsLoader();

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void init() {
        adapter = new DepartmentsAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DepartmentModel model = (DepartmentModel) adapterView.getItemAtPosition(i);
                DepartmentDialog.show(DepartmentActivity.this, model);
            }
        });
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @OptionsItem
    protected void actionAddSelected() {
        DepartmentDialog.show(this, null);
    }

    private class DepartmentsAdapter extends ObjectsArrayAdapter<DepartmentModel> implements DragSortListView.RemoveListener {

        public DepartmentsAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.departments_list_item, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, DepartmentModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final DepartmentModel i = getItem(position);

            if (i == null) {
                return convertView;
            }

            holder.title.setText(i.title);

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

    private void handleRemove(final DepartmentModel model) {
        AlertDialogWithCancelListener.show(DepartmentActivity.this, R.string.departments_delete_dialog_title, String.format(Locale.US, getString(R.string.departments_delete_dialog_message), model.title), R.string.btn_confirm, new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                try2DeleteDepartment(model);
                return true;
            }
        }, adapter);

    }

    private void try2DeleteDepartment(DepartmentModel model) {
        WaitDialogFragment.show(DepartmentActivity.this, getString(R.string.categories_departments_wait_dialog_message));
        DeleteDepartmentCommand.start(this, model, new DeleteDepartmentCallback() {
            @Override
            protected void onDepartmentDeleted(String departmentName) {
                WaitDialogFragment.hide(DepartmentActivity.this);
                Toast.makeText(DepartmentActivity.this, String.format(Locale.US, getString(R.string.departments_toast_success_message), departmentName), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onDepartmentHasItems(String departmentName, int itemsCount) {
                WaitDialogFragment.hide(DepartmentActivity.this);
                Toast.makeText(DepartmentActivity.this, String.format(Locale.US, getString(R.string.departments_toast_failed_message), departmentName, itemsCount), Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class DepartmentsLoader implements LoaderCallbacks<List<DepartmentModel>> {

        @Override
        public Loader<List<DepartmentModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_DEPARTMENTS)
                    .transformRow(new DepartmentConverter())
                    .build(DepartmentActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<DepartmentModel>> listLoader, List<DepartmentModel> departmentModels) {
            adapter.changeCursor(departmentModels);
        }

        @Override
        public void onLoaderReset(Loader<List<DepartmentModel>> listLoader) {
            adapter.changeCursor(null);
        }
    }

    private static class DepartmentConverter extends ListConverterFunction<DepartmentModel> {
        @Override
        public DepartmentModel apply(Cursor c) {
            super.apply(c);
            return new DepartmentModel(
                    c.getString(indexHolder.get(DepartmentTable.GUID)),
                    c.getString(indexHolder.get(DepartmentTable.TITLE))
            );
        }
    }

    public static void start(Context context) {
        DepartmentActivity_.intent(context).start();
    }

}
