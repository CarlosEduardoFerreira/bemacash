package com.kaching123.tcr.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.EmployeePagerAdapter;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.employee.EmployeePermissionFragment;
import com.kaching123.tcr.fragment.employee.EmployeePersonalInfoFragment;
import com.kaching123.tcr.fragment.employee.EmployeeProvider;
import com.kaching123.tcr.fragment.employee.EmployeeView;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by vkompaniets on 23.12.13.
 */
@EActivity
public abstract class BaseEmployeeActivity extends SuperBaseActivity implements EmployeeProvider {

    protected static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);
    public static final int REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_INDEX = 1;

    @Extra
    protected EmployeeModel model;
    private EmployeeModel initEmployeeModel;

    @FragmentById(R.id.personal_info_fragment)
    protected EmployeePersonalInfoFragment personalInfoFragment;

    protected EmployeePermissionFragment permissionFragment;

    @ViewById protected SlidingTabLayout tabs;
    @ViewById protected ViewPager viewPager;

    protected EmployeeMode mode;

    protected EmployeePagerAdapter adapter;
    protected long statusValue;
    protected Long presedValue;
    protected boolean statusChanged = false;
    protected boolean presedChanged = false;

    protected abstract void callCommand(EmployeeModel model, ArrayList<Permission> permissions);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEmployeeModel = new EmployeeModel(model);
    }

    @Override
    public EmployeeModel getEmployee() {
        return model;
    }

    @Override
    public EmployeeMode getMode() {
        return mode;
    }

    protected void init() {
        adapter = new EmployeePagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.employee_tabs));

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i) instanceof EmployeePermissionFragment) {
                permissionFragment = (EmployeePermissionFragment)adapter.getItem(i);
                break;
            }
        }
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(adapter);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);

    }

    @Click
    protected void btnSaveClicked() {
        if (!validateForm()) {
            return;
        }
        if (personalInfoFragment.isLoginEnabled())
            model.isSynced = false;
        bindModel();
        doCommand();
    }

    protected void bindModel() {
        personalInfoFragment.collectDataToModel(model);
        for (int i = 0; i < adapter.getCount(); i++) {
            ((EmployeeView)adapter.getItem(i)).collectDataToModel(model);
        }
    }

    protected void doCommand() {
        WaitDialogFragment.show(BaseEmployeeActivity.this, getString(R.string.employee_edit_wait_msg));
        callCommand(model, permissionFragment.getPermissions());
    }

    @Override
    public void onBackPressed() {
        if(employeeHasChanges() || permissionFragment.permissionsHasChanges()) {
            final FragmentActivity actv = this;
            AlertDialogFragment.showAlert(
                    this,
                    R.string.dlg_title_back_button,
                    getApplicationContext().getResources().getString(R.string.dlg_text_back_button),
                    R.string.btn_yes,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            onBackPressedDialog();
                            return false;
                        }
                    }, new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            DialogUtil.hide(actv, "errorDialogFragment");
                            return false;
                        }
                    }
            );
        }else{
            onBackPressedDialog();
        }
    }

    protected boolean validateForm() {
        for (int i = 0; i < adapter.getCount(); i++) {
            EmployeeView fragment = ((EmployeeView) adapter.getItem(i));
            if(!fragment.validateView()){
                return false;
            }
        }
        return true;
    }

    private boolean employeeHasChanges(){
        if (mode == EmployeeMode.EDIT) {
            if (personalInfoFragment.hasChanges(initEmployeeModel)) {
                return true;
            }
            for (int i = 0; i < adapter.getCount(); i++) {
                if(((EmployeeView) adapter.getItem(i)).hasChanges(initEmployeeModel)) {
                    return true;
                }
            }
            statusChanged = initEmployeeModel.status.ordinal() != personalInfoFragment.getStatus().getSelectedItemId();

            presedValue = permissionFragment.getPresedValue();
            presedChanged = presedValue != permissionFragment.getPreset().getSelectedItemId();

            if (statusChanged) {
                Log.d("BemaCarl3", "BaseEmployeeActivity.employeeHasChanges.statusChanged");
                return true;
            }
            if (presedChanged) {
                Log.d("BemaCarl3", "BaseEmployeeActivity.employeeHasChanges.presedChanged");
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    private void onBackPressedDialog(){
        super.onBackPressed();
        disableForceLogOut();
    }

    protected void disableForceLogOut() {
        setResult(REQUEST_CODE, new Intent().putExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, false));
    }

    @OnActivityResult(PERMISSIONS_REQUEST_INDEX)
    protected void onResult(Intent data) {
        if (data.getExtras() == null)
            return;
        List<Permission> result = (List<Permission>) data.getExtras().getSerializable(PermissionActivity.EXTRA_PERMISSIONS);
        permissionFragment.try2SetupPermissions(result);
    }

    public enum EmployeeMode {
        CREATE,
        EDIT
    }


}
