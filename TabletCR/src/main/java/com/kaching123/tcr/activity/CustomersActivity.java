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
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.export.ExportQuickbooksCustomersCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.util.DateUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.util.KeyboardUtils.hideKeyboard;

/**
 * Created by pkabakov on 10.02.14.
 */
@EActivity(R.layout.customers_activity)
@OptionsMenu(R.menu.customers_activity)
public class CustomersActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    private static final String FILTER_FIELD_CLICKED = "FILTER_FIELD_CLICKED";

    private static final int FILTER_NAME_CLICKED = 1;
    private static final int FILTER_ADDRESS_CLICKED = 2;
    private static final int FILTER_EMAIL_CLICKED = 3;
    private static final int FILTER_PHONE_CLICKED = 4;
    private static final int FILTER_SEX_CLICKED = 5;
    private static final int FILTER_CREATED_CLICKED = 6;
    private static final int FILTER_PROM_CLICKED = 7;

    private boolean nameOrderAsc = true;
    private boolean addressOrderAsc = true;
    private boolean emailOrderAsc = true;
    private boolean phoneOrderAsc = true;
    private boolean sexOrderAsc = true;
    private boolean createdOrderAsc = true;
    private boolean promOrderAsc = true;

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
    protected void init() {
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EditCustomerActivity.start(CustomersActivity.this, adapter.getItem(i));
            }
        });
        list.setAdapter(adapter);
    }

    @Click(R.id.header_name)
    protected void filterNameClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_NAME_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_address)
    protected void filterAddressClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_ADDRESS_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_email)
    protected void filterEmailClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_EMAIL_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_phone)
    protected void filterPhoneClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_PHONE_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_sex)
    protected void filterSexClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_SEX_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_created)
    protected void filterCreatedClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_CREATED_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_promotions)
    protected void filterPromClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_PROM_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);
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
    protected void actionAddSelected() {
        if (!PlanOptions.isEditingCustomersAllowed()) {
            AlertDialogFragment.showAlert(this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            EditCustomerActivity.start(CustomersActivity.this, null);
        }
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

    private class CustomersAdapter extends ObjectsCursorAdapter<CustomerModel> {

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

        private class ViewHolder {
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

        String firstLastName = "first_name_last_name";
        String lastFirstName = "last_name_first_name";

        @Override
        public Loader<List<CustomerModel>> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(CUSTOMERS_URI);
            String coma = ",";

            builder.projection(CustomerTable.GUID + coma + CustomerTable.FISRT_NAME + coma + CustomerTable.LAST_NAME + coma +
                    CustomerTable.STREET + coma + CustomerTable.COMPLEMENTARY + coma + CustomerTable.CITY + coma +
                    CustomerTable.STATE + coma + CustomerTable.COUNTRY + coma + CustomerTable.ZIP + coma +
                    CustomerTable.EMAIL + coma + CustomerTable.CUSTOMER_IDENTIFICATION + coma + CustomerTable.PHONE + coma +
                    CustomerTable.SEX + coma + CustomerTable.BIRTHDAY + coma + CustomerTable.BIRTHDAY_REWARD_APPLY_DATE + coma +
                    CustomerTable.CREATE_TIME + coma + CustomerTable.CONSENT_PROMOTIONS + coma + CustomerTable.NOTES + coma +
                    CustomerTable.LOYALTY_PLAN_ID + coma + CustomerTable.LOYALTY_BARCODE + coma + CustomerTable.TMP_LOYALTY_POINTS + coma +
                    CustomerTable.UPDATE_TIME_LOCAL + coma +
                    "(" + CustomerTable.FISRT_NAME  + " || ' ' || " + CustomerTable.LAST_NAME + ") " + "as " + firstLastName + coma +
                    "(" + CustomerTable.LAST_NAME  + " || ' ' || " + CustomerTable.FISRT_NAME + ") " + "as " + lastFirstName);

            if (bundle == null) {
                builder.orderBy(CustomerTable.FISRT_NAME + ", " + CustomerTable.LAST_NAME);
                nameOrderAsc = !nameOrderAsc;
            } else {
                switch (bundle.getInt(FILTER_FIELD_CLICKED)) {
                    case FILTER_NAME_CLICKED:
                        String nameOrder = CustomerTable.FISRT_NAME + ", " + CustomerTable.LAST_NAME;
                        builder.orderBy(nameOrderAsc ? nameOrder : CustomerTable.FISRT_NAME + " DESC" + ", " + CustomerTable.LAST_NAME + " DESC");
                        nameOrderAsc = !nameOrderAsc;
                        break;
                    case FILTER_ADDRESS_CLICKED:
                        String addressOrder = CustomerTable.STREET + ", " + CustomerTable.COMPLEMENTARY + ", " +
                                CustomerTable.CITY + ", " + CustomerTable.STATE + ", " +
                                CustomerTable.COUNTRY + ", " + CustomerTable.ZIP;
                        builder.orderBy(addressOrderAsc ? addressOrder : CustomerTable.STREET + " DESC" + ", " +
                                CustomerTable.COMPLEMENTARY + " DESC" + ", " +
                                CustomerTable.CITY + " DESC" + ", " + CustomerTable.STATE + " DESC" + ", " +
                                CustomerTable.COUNTRY + " DESC" + ", " + CustomerTable.ZIP + " DESC");
                        addressOrderAsc = !addressOrderAsc;
                        break;
                    case FILTER_EMAIL_CLICKED:
                        builder.orderBy(emailOrderAsc ? CustomerTable.EMAIL : CustomerTable.EMAIL + " DESC");
                        emailOrderAsc = !emailOrderAsc;
                        break;
                    case FILTER_PHONE_CLICKED:
                        builder.orderBy(phoneOrderAsc ? CustomerTable.PHONE : CustomerTable.PHONE + " DESC");
                        phoneOrderAsc = !phoneOrderAsc;
                        break;
                    case FILTER_SEX_CLICKED:
                        builder.orderBy(sexOrderAsc ? CustomerTable.SEX : CustomerTable.SEX + " DESC");
                        sexOrderAsc = !sexOrderAsc;
                        break;
                    case FILTER_CREATED_CLICKED:
                        builder.orderBy(createdOrderAsc ? CustomerTable.CREATE_TIME : CustomerTable.CREATE_TIME + " DESC");
                        createdOrderAsc = !createdOrderAsc;
                        break;
                    case FILTER_PROM_CLICKED:
                        builder.orderBy(promOrderAsc ? CustomerTable.CONSENT_PROMOTIONS : CustomerTable.CONSENT_PROMOTIONS + " DESC");
                        promOrderAsc = !promOrderAsc;
                        break;
                }
            }

            if (!TextUtils.isEmpty(textFilter)) {
                String filter = "%" + textFilter + "%";
                builder.where(firstLastName + " LIKE ? OR " + lastFirstName + " LIKE ? OR " + CustomerTable.EMAIL + " LIKE ? OR " + CustomerTable.PHONE + " LIKE ?",
                        filter, filter, filter, filter);
            }

            return builder.transformRow(new ListConverterFunction<CustomerModel>() {
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

    public static void start(Context context) {
        CustomersActivity_.intent(context).start();
    }
}
