package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.export.ExportQuickbooksCustomersCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.util.DateUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.util.KeyboardUtils.hideKeyboard;

/**
 * Created by pkabakov on 10.02.14.
 */
@EActivity (R.layout.customers_activity)
@OptionsMenu (R.menu.customers_activity)
public class CustomersActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.CUSTOMER_MANAGEMENT);
    }

    private static final Uri CUSTOMERS_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    @ViewById
    protected ListView list;

    private MenuItem searchItem;

    private String textFilter;

    private CustomersAdapter adapter;

    private CustomersLoader loader = new CustomersLoader();

    private ExportQuickbooksCallback exportQuickbooksCallback = new ExportQuickbooksCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new CustomersAdapter(this);
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void init(){
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EditCustomerActivity.start(CustomersActivity.this, adapter.getItem(i));
            }
        });
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean b = super.onCreateOptionsMenu(menu);
        searchItem = menu.findItem(R.id.action_search);
        assert searchItem != null;
        initSearchView();
        return b;
    }

    private void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard(CustomersActivity.this, searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setFilter(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setQuery(null, true);
                return true;
            }

        });
    }

    private void setFilter(String newText) {
        textFilter = newText;
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @OptionsItem
    protected void actionAddSelected(){
        EditCustomerActivity.start(CustomersActivity.this, null);
    }

    /*@OptionsItem
    protected void actionQuickbooksExportSelected() {
        FileChooserFragment.show(this, Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(CustomersActivity.this, getString(R.string.customers_export_wait_msg));
                ExportQuickbooksCustomersCommand.start(CustomersActivity.this, file.getAbsolutePath(), exportQuickbooksCallback);
            }
        });
    }*/

    private class ExportQuickbooksCallback extends ExportCommandBaseCallback {

        @Override
        protected void handleSuccess(int count) {
            WaitDialogFragment.hide(CustomersActivity.this);
            AlertDialogFragment.showComplete(CustomersActivity.this, R.string.customers_export_success_title, getString(R.string.customers_export_success_msg, count));
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(CustomersActivity.this);
            AlertDialogFragment.showAlert(CustomersActivity.this, R.string.customers_export_error_title, getString(R.string.customers_export_error_msg));
        }
    }

    private class CustomersAdapter extends ObjectsCursorAdapter<CustomerModel>{

        public CustomersAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.customers_list_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.phone = (TextView) convertView.findViewById(R.id.phone);
            holder.sex = (TextView) convertView.findViewById(R.id.sex);
            holder.createDate = (TextView) convertView.findViewById(R.id.create_date);
            holder.consentPromotions = (TextView) convertView.findViewById(R.id.consent_promotions);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, CustomerModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.name.setText(item.getFullName());

            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(item.street))
                builder.append(item.street).append(", ");
            if (!TextUtils.isEmpty(item.complementary))
                builder.append(item.complementary).append(", ");
            if (!TextUtils.isEmpty(item.city))
                builder.append(item.city).append(", ");
            if (!TextUtils.isEmpty(item.state))
                builder.append(item.state).append(", ");
            if (!TextUtils.isEmpty(item.country))
                builder.append(item.country).append(", ");
            if (!TextUtils.isEmpty(item.zip))
                builder.append(item.zip).append(", ");
            if (builder.length() > 0)
                builder.delete(builder.length() - 2, builder.length());
            holder.address.setText(builder);

            holder.email.setText(item.email);
            showPhone(holder.phone, item.phone);
            holder.sex.setText(item.sex ? R.string.sex_male : R.string.sex_female);
            holder.createDate.setText(DateUtils.dateOnlyFormat(item.createTime));
            holder.consentPromotions.setText(item.consentPromotions ? R.string.yes : R.string.no);

            return convertView;
        }

        private class ViewHolder{
            TextView name;
            TextView address;
            TextView email;
            TextView phone;
            TextView sex;
            TextView createDate;
            TextView consentPromotions;
        }
    }

    private class CustomersLoader implements LoaderCallbacks<List<CustomerModel>> {

        @Override
        public Loader<List<CustomerModel>> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(CUSTOMERS_URI);
            builder.orderBy(CustomerTable.FISRT_NAME + ", " + CustomerTable.LAST_NAME);
            if (!TextUtils.isEmpty(textFilter)){
                String filter = "%" + textFilter + "%";
                builder.where(CustomerTable.FISRT_NAME + " LIKE ? OR " + CustomerTable.LAST_NAME + " LIKE ? OR " + CustomerTable.EMAIL + " LIKE ? OR " + CustomerTable.PHONE + " LIKE ?",
                        filter, filter, filter, filter);
            }

            return builder.transform(new ListConverterFunction<CustomerModel>() {
                @Override
                public CustomerModel apply(Cursor cursor) {
                    return new CustomerModel(cursor);
                }
            }).build(CustomersActivity.this);

        }

        @Override
        public void onLoadFinished(Loader<List<CustomerModel>> listLoader, List<CustomerModel> list) {
            adapter.changeCursor(list);
        }

        @Override
        public void onLoaderReset(Loader<List<CustomerModel>> listLoader) {
            adapter.changeCursor(null);
        }
    }

    public static void start (Context context){
        CustomersActivity_.intent(context).start();
    }
}
