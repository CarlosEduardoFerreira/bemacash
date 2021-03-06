package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.OpenDrawerCommand;
import com.kaching123.tcr.commands.device.OpenDrawerCommand.BaseOpenDrawerCallback;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.device.WaitForCloseDrawerCommand;
import com.kaching123.tcr.commands.device.WaitForCloseDrawerCommand.BaseWaitForCloseDrawerCallback;
import com.kaching123.tcr.commands.store.user.AddTipsCommand;
import com.kaching123.tcr.commands.store.user.AddTipsCommand.BaseAddTipsCallback;
import com.kaching123.tcr.commands.store.user.SplitTipsCommand;
import com.kaching123.tcr.commands.store.user.SplitTipsCommand.BaseSplitTipsCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.employee.EmployeeTipsFragmentDialog;
import com.kaching123.tcr.fragment.employee.EmployeeTipsFragmentDialog.IAddTipsListener;
import com.kaching123.tcr.fragment.shift.PutCashFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.kaching123.tcr.util.KeyboardUtils.hideKeyboard;

/**
 * Created by vkompaniets on 20.12.13.
 */
@EActivity(R.layout.employee_activity)
@OptionsMenu(R.menu.employee_activity)
public class EmployeesActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    private static final String FILTER_FIELD_CLICKED = "FILTER_FIELD_CLICKED";

    private static final int FILTER_NAME_CLICKED = 1;
    private static final int FILTER_ADDRESS_CLICKED = 2;
    private static final int FILTER_EMAIL_CLICKED = 3;
    private static final int FILTER_STATUS_CLICKED = 4;
    private static final int FILTER_USER_ID_CLICKED = 5;

    private boolean nameOrderAsc = true;
    private boolean addressOrderAsc = true;
    private boolean emailOrderAsc = true;
    private boolean statusOrderAsc = true;
    private boolean userIdOrderAsc = true;


    static {
        permissions.add(Permission.EMPLOYEE_MANAGEMENT);
    }

    private static final Uri EMPLOYEE_URI = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

    @ViewById
    protected ListView list;

    @StringArrayRes(R.array.employee_status_labels)
    protected String[] statusLabels;

    private EmployeeAdapter adapter;

    private MenuItem searchItem;

    private String textFilter;

    private EmployeeLoader loader = new EmployeeLoader();

    private boolean eligibleForTips;
    private BigDecimal tipsAmount;
    private List<String> tipsEmplGuids;
    private String tipsNotes;

    private int employeeCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EmployeeAdapter(this);
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @AfterViews
    protected void init() {
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EditEmployeeActivity.start(EmployeesActivity.this, adapter.getItem(i));
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
    @Click(R.id.address)
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
    @Click(R.id.status)
    protected void filterStatusClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_STATUS_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }
    @Click(R.id.header_user_id)
    protected void filterUserIdClicked() {
        Bundle args = new Bundle();
        args.putInt(FILTER_FIELD_CLICKED, FILTER_USER_ID_CLICKED);
        getSupportLoaderManager().restartLoader(0, args, loader);

    }

    @OptionsItem
    protected void actionAddSelected() {
        if (PlanOptions.isEmployeeLimited() && (employeeCount >= PlanOptions.getEmployeeLimit())) {
            AlertDialogFragment.showAlert(this,
                    R.string.unavailable_option_title, getString(R.string.unavailable_employee_option_message, PlanOptions.getEmployeeLimit()));
        } else {
            EmployeeModel model = new EmployeeModel(UUID.randomUUID().toString(), null, null, null, null, null, null, null, null, null, null, null, null, true, null, null,
                    EmployeeStatus.ACTIVE,
                    TcrApplication.get().getShopId(), BigDecimal.ZERO, false, false, null, false, false, BigDecimal.ZERO, null, false, false, null);//TcrApplication.get().getShopId());
            AddEmployeeActivity.start(this, model);
        }
    }

    @OptionsItem
    protected void actionAddTipsSelected() {
        if (getApp().hasPermission(Permission.TIPS)) {
            showTipsDialog();
        } else {
            PermissionFragment.showCancelable(EmployeesActivity.this, new BaseTempLoginListener(this) {
                @Override
                public void onLoginComplete() {
                    showTipsDialog();
                }
            }, Permission.TIPS);
        }
    }

    private void showTipsDialog() {
        EmployeeTipsFragmentDialog.show(this, new IAddTipsListener() {
            @Override
            public void onTipsConfirmed(BigDecimal amount, List<String> employeeGuids, String notes) {
                tipsAmount = amount;
                tipsEmplGuids = employeeGuids;
                tipsNotes = notes;
                try2OpenDrawer(false);
            }
        });
    }

    private void try2OpenDrawer(boolean searchByMac) {
        WaitDialogFragment.show(EmployeesActivity.this, getString(R.string.wait_message_open_drawer));
        OpenDrawerCommand.start(EmployeesActivity.this, searchByMac, openDrawerCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean b = super.onCreateOptionsMenu(menu);
        searchItem = menu.findItem(R.id.action_search);
        assert searchItem != null;
        initSearchView();
        MenuItem tipsItem = menu.findItem(R.id.action_add_tips);
        tipsItem.setVisible(getApp().isTipsEnabled());
        tipsItem.setEnabled(getApp().isShiftOpened());
        return b;
    }

    private void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard(EmployeesActivity.this, searchView);
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
        /*if(textFilter == null && newText == null){
            return;
        }*/
        textFilter = newText;
        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    private class EmployeeAdapter extends ObjectsCursorAdapter<EmployeeModel> {

        public EmployeeAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.employee_list_item, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.login = (TextView) convertView.findViewById(R.id.login);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.status = (TextView) convertView.findViewById(R.id.status);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, EmployeeModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            EmployeeModel i = getItem(position);

            if (i == null) {
                return convertView;
            }

            holder.name.setText(i.fullName());
            holder.login.setText(i.login);
            holder.email.setText(i.email);

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

            holder.status.setText(item.status == null ? null : statusLabels[item.status.ordinal()]);

            return convertView;
        }

        private class ViewHolder {
            TextView name;
            TextView login;
            TextView email;
            TextView address;
            TextView status;
        }
    }

    private class EmployeeLoader implements LoaderCallbacks<List<EmployeeModel>> {

        String firstLastName = "first_name_last_name";
        String lastFirstName = "last_name_first_name";

        @Override
        public Loader<List<EmployeeModel>> onCreateLoader(int i, Bundle bundle) {
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(EMPLOYEE_URI);
            String coma = ",";

            builder.projection(EmployeeTable.ID + coma + EmployeeTable.GUID + coma + EmployeeTable.FIRST_NAME + coma + EmployeeTable.LAST_NAME + coma +
                    EmployeeTable.LOGIN + coma +  EmployeeTable.PASSWORD + coma +
                    EmployeeTable.STREET + coma + EmployeeTable.COMPLEMENTARY + coma + EmployeeTable.CITY + coma +
                    EmployeeTable.STATE + coma + EmployeeTable.COUNTRY + coma + EmployeeTable.ZIP + coma +
                    EmployeeTable.EMAIL + coma + EmployeeTable.PHONE + coma +
                    EmployeeTable.SEX + coma + EmployeeTable.UPDATE_TIME_LOCAL + coma +
                    EmployeeTable.HIRE_DATE + coma + EmployeeTable.FIRE_DATE + coma +
                    EmployeeTable.STATUS + coma + EmployeeTable.HOURLY_RATE + coma +
                    EmployeeTable.SHOP_ID + coma + EmployeeTable.TIPS_ELIGIBLE + coma +
                    EmployeeTable.ELIGIBLE_FOR_COMMISSION + coma + EmployeeTable.COMMISSION + coma +
                    EmployeeTable.IS_MERCHANT + coma + EmployeeTable.IS_SYNC + coma + EmployeeTable.OPEN_SHIFT_MANDATORY + coma +
                    EmployeeTable.OVER_STARTS_FROM + coma + EmployeeTable.OVERTIME_RATE + coma + EmployeeTable.CLOCK_IN_MANDATORY + coma +
                    "(" + EmployeeTable.FIRST_NAME  + " || ' ' || " + EmployeeTable.LAST_NAME + ") " + "as " + firstLastName + coma +
                    "(" + EmployeeTable.LAST_NAME  + " || ' ' || " + EmployeeTable.FIRST_NAME + ") " + "as " + lastFirstName);

            if (bundle == null) {
                builder.orderBy(EmployeeTable.FIRST_NAME + "," + EmployeeTable.LAST_NAME);
                nameOrderAsc = !nameOrderAsc;
            } else {
                switch (bundle.getInt(FILTER_FIELD_CLICKED)) {
                    case FILTER_NAME_CLICKED:
                        String nameOrder = EmployeeTable.FIRST_NAME + "," + EmployeeTable.LAST_NAME;
                        builder.orderBy(nameOrderAsc ? nameOrder : EmployeeTable.FIRST_NAME + " DESC, " + EmployeeTable.LAST_NAME + " DESC");
                        nameOrderAsc = !nameOrderAsc;
                        break;
                    case FILTER_ADDRESS_CLICKED:
                        String addressOrder = EmployeeTable.STREET + ", " + EmployeeTable.COMPLEMENTARY + ", " +
                                EmployeeTable.CITY + ", " + EmployeeTable.STATE + ", " +
                                EmployeeTable.COUNTRY + ", " + EmployeeTable.ZIP;
                        builder.orderBy(addressOrderAsc ? addressOrder : EmployeeTable.STREET + " DESC" + ", " +
                                EmployeeTable.COMPLEMENTARY + " DESC" + ", " +
                                EmployeeTable.CITY + " DESC" + ", " + EmployeeTable.STATE + " DESC" + ", " +
                                EmployeeTable.COUNTRY + " DESC" + ", " + EmployeeTable.ZIP + " DESC");
                        addressOrderAsc = !addressOrderAsc;
                        break;
                    case FILTER_EMAIL_CLICKED:
                        builder.orderBy(emailOrderAsc ? EmployeeTable.EMAIL : EmployeeTable.EMAIL + " DESC");
                        emailOrderAsc = !emailOrderAsc;
                        break;
                    case FILTER_STATUS_CLICKED:
                        builder.orderBy(statusOrderAsc ? EmployeeTable.STATUS : EmployeeTable.STATUS + " DESC");
                        statusOrderAsc = !statusOrderAsc;
                        break;
                    case FILTER_USER_ID_CLICKED:
                        builder.orderBy(userIdOrderAsc ? EmployeeTable.LOGIN : EmployeeTable.LOGIN + " DESC");
                        userIdOrderAsc = !userIdOrderAsc;
                        break;
                }
            }
            builder.where(EmployeeTable.IS_MERCHANT + " = ?", 0);
            builder.where(EmployeeTable.LOGIN + " <> ?", "");
            builder.where(EmployeeTable.LOGIN + " IS NOT NULL");

            if (!TextUtils.isEmpty(textFilter)) {
                String filter = "%" + textFilter + "%";
                builder.where(firstLastName + " LIKE ? OR " + lastFirstName + " LIKE ?", filter, filter);
            }

            return builder.transformRow(new ListConverterFunction<EmployeeModel>() {
                @Override
                public EmployeeModel apply(Cursor cursor) {
                    return new EmployeeModel(cursor);
                }
            }).build(EmployeesActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<EmployeeModel>> listLoader, List<EmployeeModel> employeeModels) {
            adapter.changeCursor(employeeModels);
            employeeCount = adapter.getCount();
        }

        @Override
        public void onLoaderReset(Loader<List<EmployeeModel>> listLoader) {
            adapter.changeCursor(null);
            employeeCount = 0;
        }
    }

    /**
     * **************************************** TIPS ********************************************
     */

    private void addTips() {
        if (tipsEmplGuids.size() == 1) {
            AddTipsCommand.start(EmployeesActivity.this, new TipsModel(
                    UUID.randomUUID().toString(),
                    null,
                    tipsEmplGuids.get(0),
                    getApp().getShiftGuid(),
                    null,
                    null,
                    new Date(),
                    tipsAmount,
                    tipsNotes,
                    PaymentType.CASH,
                    null
            ), addTipsCallback);
        } else {
            SplitTipsCommand.start(EmployeesActivity.this, new ArrayList<String>(tipsEmplGuids), tipsAmount, tipsNotes, splitTipsCallback);
        }
    }

    private BaseOpenDrawerCallback openDrawerCallback = new BaseOpenDrawerCallback() {
        @Override
        protected void onDrawerIPnoFound() {
            WaitDialogFragment.hide(EmployeesActivity.this);
            AlertDialogFragment.showAlert(
                    EmployeesActivity.this,
                    R.string.open_drawer_error_title,
                    getString(R.string.error_message_printer_ip_not_found),
                    R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(true);
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onDrawerOpened() {
            WaitDialogFragment.hide(EmployeesActivity.this);
            PutCashFragment.show(EmployeesActivity.this, true);
            WaitForCloseDrawerCommand.start(EmployeesActivity.this, waitForCloseDrawerCallback);
        }

        @Override
        protected void onDrawerOpenError(PrinterError error) {
            WaitDialogFragment.hide(EmployeesActivity.this);
            AlertDialogFragment.showAlert(EmployeesActivity.this, R.string.open_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(false);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    }
            );
        }
    };

    private BaseWaitForCloseDrawerCallback waitForCloseDrawerCallback = new BaseWaitForCloseDrawerCallback() {
        @Override
        protected void onDrawerClosed() {
            PutCashFragment.hide(EmployeesActivity.this);
            addTips();
        }

        @Override
        protected void onDrawerCloseError(PrinterError error) {
            PutCashFragment.hide(EmployeesActivity.this);
            showErrorDialog(R.string.close_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)));
        }

        @Override
        protected void onDrawerTimeoutError() {
            PutCashFragment.hide(EmployeesActivity.this);

            showErrorDialog(R.string.close_drawer_error_title, getString(R.string.close_drawer_error_msg_close_id));
        }

        private void showErrorDialog(int title, String message) {
            AlertDialogFragment.showAlertWithSkip(EmployeesActivity.this, title, message, R.string.btn_try_again,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            PutCashFragment.show(EmployeesActivity.this, true);
                            WaitForCloseDrawerCommand.start(EmployeesActivity.this, waitForCloseDrawerCallback);
                            return true;
                        }
                    },
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    }
            );
        }
    };

    private BaseAddTipsCallback addTipsCallback = new BaseAddTipsCallback() {
        @Override
        public void onTipsAddSuccess() {
            Toast.makeText(EmployeesActivity.this, R.string.employee_tips_add_success_msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTipsAddFailure() {
            Toast.makeText(EmployeesActivity.this, R.string.employee_tips_add_failed_msg, Toast.LENGTH_SHORT).show();
        }
    };

    private BaseSplitTipsCallback splitTipsCallback = new BaseSplitTipsCallback() {
        @Override
        public void onSplitSuccess(int count) {
            Toast.makeText(EmployeesActivity.this, getString(R.string.employee_tips_split_success_msg, count), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSplitFailure() {
            Toast.makeText(EmployeesActivity.this, R.string.employee_tips_add_failed_msg, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * ******************************************************************************************
     */

    public static void start(Context context) {
        EmployeesActivity_.intent(context).startForResult(EditEmployeeActivity.REQUEST_CODE);
    }

    @OnActivityResult(EditEmployeeActivity.REQUEST_CODE)
    protected void onResult(int code, Intent data) {
        if (code == EditEmployeeActivity.REQUEST_CODE && data != null) {
            setResult(DashboardActivity.EXTRA_CODE, new Intent().putExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, data.getBooleanExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, false)));
            if (data.getBooleanExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, false))
                finish();
        }
    }
}
