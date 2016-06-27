package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.CustomerPagerAdapter;
import com.kaching123.tcr.commands.store.user.AddCustomerCommand;
import com.kaching123.tcr.commands.store.user.AddCustomerCommand.BaseAddCustomerCallback;
import com.kaching123.tcr.commands.store.user.DeleteCustomerCommand;
import com.kaching123.tcr.commands.store.user.DeleteCustomerCommand.BaseDeleteCustomerCallback;
import com.kaching123.tcr.commands.store.user.EditCustomerCommand;
import com.kaching123.tcr.commands.store.user.EditCustomerCommand.BaseEditCustomerCallback;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.customer.CustomerPersonalInfoFragment;
import com.kaching123.tcr.fragment.customer.CustomerProvider;
import com.kaching123.tcr.fragment.customer.CustomerView;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EActivity(R.layout.edit_customer_activity)
@OptionsMenu(R.menu.edit_customer_activity)
public class EditCustomerActivity extends SuperBaseActivity implements CustomerProvider {

    private final static HashSet<Permission> permissions = new HashSet<>();

    static {
        permissions.add(Permission.CUSTOMER_MANAGEMENT);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @FragmentById(R.id.personal_info_fragment)
    protected CustomerPersonalInfoFragment personalInfoFragment;

    @ViewById protected SlidingTabLayout tabs;
    @ViewById protected ViewPager viewPager;

    public static final String EXTRA_CUSTOMER = "extra_customer";

    @Extra
    protected CustomerModel model;

    private Mode mode;
    private CustomerPagerAdapter adapter;

    @AfterViews
    protected void init(){
        mode = model == null ? Mode.CREATE : Mode.UPDATE;
        setTitle();

        adapter = new CustomerPagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.customer_tabs));
        viewPager.setAdapter(adapter);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);

//        setFieldsEnabled(PlanOptions.isEditingCustomersAllowed());
    }

    @Override
    public CustomerModel getCustomer() {
        return model;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.action_remove);
        delete.setVisible(mode == Mode.UPDATE);
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem
    protected void actionRemoveSelected() {
        if (!PlanOptions.isEditingCustomersAllowed()) {
            AlertDialogFragment.showAlert(this, R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        } else {
            deleteCustomer();
        }
    }

    private void deleteCustomer() {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.customer_delete_dialog_title,
                getString(R.string.customer_delete_dialog_message),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        WaitDialogFragment.show(self(), getString(R.string.customer_delete_wait_message));
                        DeleteCustomerCommand.start(self(), model, deleteCustomerCallback);
                        return true;
                    }
                }

        );
    }

    private void setTitle() {
        switch (mode) {
            case CREATE:
                setTitle(R.string.add_customer_activity_label);
                break;
            case UPDATE:
                setTitle(model.getFullName());
                break;
        }
    }

    private void setResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_CUSTOMER, model);
        setResult(RESULT_OK, data);
    }

    @Click
    protected void saveButtonClicked() {
        if (!validateView()) {
            return;
        }
        startCommand();
    }

    private void startCommand() {
        updateModel();
        WaitDialogFragment.show(self(), getString(R.string.customer_save_wait_message));
        if (mode == Mode.CREATE) {
            AddCustomerCommand.start(self(), addCustomerCallback, model);
        } else {
            EditCustomerCommand.start(self(), editCustomerCallback, model);
        }

    }

    private boolean validateView() {
        return personalInfoFragment.validateView() && getViewFragment(0).validateView() && getViewFragment(1).validateView();
    }

    private void updateModel() {
        if (mode == Mode.CREATE) {
            model = new CustomerModel(UUID.randomUUID().toString(), new Date());
        }

        personalInfoFragment.collectDataToModel(model);
        getViewFragment(0).collectDataToModel(model);
        getViewFragment(1).collectDataToModel(model);
    }

    private CustomerView getViewFragment(int position){
        return (CustomerView) adapter.getItem(position);
    }

    private void setFieldsEnabled(boolean enabled){
        personalInfoFragment.setFieldsEnabled(enabled);
        getViewFragment(0).setFieldsEnabled(enabled);
        getViewFragment(1).setFieldsEnabled(enabled);
    }

    private BaseAddCustomerCallback addCustomerCallback = new BaseAddCustomerCallback() {

        @Override
        protected void onCustomerAdded() {
            WaitDialogFragment.hide(self());
            setResult();
            finish();
        }

        @Override
        protected void onCustomerAddError() {
            WaitDialogFragment.hide(self());
            AlertDialogFragment.showAlert(self(), R.string.error_dialog_title, getString(R.string.customer_edit_error_msg));
        }

        @Override
        protected void onEmailExists() {
            WaitDialogFragment.hide(self());
            AlertDialogFragment.showAlert(self(), R.string.error_dialog_title, getString(R.string.customer_edit_email_exists_error_msg));
        }

    };

    private BaseEditCustomerCallback editCustomerCallback = new BaseEditCustomerCallback() {

        @Override
        protected void onCustomerUpdated() {
            WaitDialogFragment.hide(self());
            finish();
        }

        @Override
        protected void onCustomerUpdateError() {
            WaitDialogFragment.hide(self());
            AlertDialogFragment.showAlert(self(), R.string.error_dialog_title, getString(R.string.customer_edit_error_msg));
        }

        @Override
        protected void onEmailExists() {
            WaitDialogFragment.hide(self());
            AlertDialogFragment.showAlert(self(), R.string.error_dialog_title, getString(R.string.customer_edit_email_exists_error_msg));
        }
    };

    private BaseDeleteCustomerCallback deleteCustomerCallback = new BaseDeleteCustomerCallback() {
        @Override
        protected void onCustomerDeleted() {
            WaitDialogFragment.hide(self());
            finish();
        }

        @Override
        protected void onCustomerDeleteError() {
            WaitDialogFragment.hide(self());
            AlertDialogFragment.showAlert(self(), R.string.error_dialog_title, getString(R.string.customer_delete_dialog_error_message));
        }
    };

    private enum Mode {
        CREATE, UPDATE
    }

    public static void start(Context context, CustomerModel model) {
        EditCustomerActivity_.intent(context).model(model).start();
    }

    public static void startForResult(Context context, CustomerModel model, int requestCode) {
        EditCustomerActivity_.intent(context).model(model).startForResult(requestCode);
    }

}
