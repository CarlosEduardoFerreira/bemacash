package com.kaching123.tcr.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Fullscreen;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.fragment.user.LoginFragment.Mode;
import com.kaching123.tcr.fragment.user.LoginOuterFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;
import com.kaching123.usb.SysBusUsbDevice;
import com.kaching123.usb.SysBusUsbManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by gdubina on 04.12.13.
 */
@EActivity
@Fullscreen
public class SuperBaseActivity extends FragmentActivity {

    @App
    protected TcrApplication app;

    private TempLoginActionProvider tempLoginActionProvider;

    private boolean isDestroyed;

    public TcrApplication getApp() {
        return app;
    }

    protected Set<Permission> getPermissions() {
        return null;
    }

    private Permission[] getPermissionsArray() {
        Set<Permission> permissions = getPermissions();
        if (permissions == null)
            return new Permission[0];
        return permissions.toArray(new Permission[permissions.size()]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDestroyed = false;
        tempLoginActionProvider = new TempLoginActionProvider(this);
        //ViewServer.get(this).addWindow(this);

        setMintPos();
        checkUsbMsr();
    }

    private void setMintPos() {
        getApp().getShopPref().disableBSMSR().put(true); // mint
        if (!getApp().getShopPref().notFirstTimeLoaded().getOr(false)) {
            getApp().getShopPref().displayAddress().put(FindDeviceFragment.INTEGRATED_DISPLAYER);
            getApp().getShopPref().displayName().put(FindDeviceFragment.SERIAL_PORT);
            getApp().getShopPref().notFirstTimeLoaded().put(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        //ViewServer.get(this).removeWindow(this);
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void onResume() {
        super.onResume();
        //ViewServer.get(this).setFocusedWindow(this);
    }

    private void checkUsbMsr() {
        SysBusUsbManager mUsbManagerLinux = new SysBusUsbManager();
        HashMap<String, SysBusUsbDevice> mLinuxUsbDeviceList = mUsbManagerLinux.getUsbDevices();
        Iterator<SysBusUsbDevice> deviceIterator = mLinuxUsbDeviceList.values().iterator();
        Logger.d("trace UsbDevice length: " + mLinuxUsbDeviceList.size());
        while (deviceIterator.hasNext()) {
            SysBusUsbDevice device = deviceIterator.next();
            Logger.d("trace UsbDevice " + device.getVID() + " /n" + device.getPID());
            if (device.getVID().equalsIgnoreCase("1667") && device.getPID().equalsIgnoreCase("9")) {
                getApp().getShopPref().usbMSRName().put(FindDeviceFragment.USB_MSR_NAME);
                break;
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem lockItem = menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, getResources().getInteger(R.integer.menu_order_last), R.string.action_lock_label);
        lockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LoginOuterFragment.show(SuperBaseActivity.this, Mode.UNLOCK);
                return true;
            }
        });

//        PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX_EBT_CASH.gateway();
//
//        if (getApp().isPaxConfigured() && paxGateway.acceptPaxEbtEnabled()) {
//
//            MenuItem ebtItem =  menu.add(
//                    Menu.CATEGORY_ALTERNATIVE,
//                    Menu.NONE,
//                    getResources().getInteger(R.integer.menu_order_last), R.string.cashier_action_balance);
//            ebtItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    PaxBalanceProcessor.get().checkBalance(SuperBaseActivity.this);
//                    return true;
//                }
//            });
//        }

        if (getApp().hasPrevOperator()) {
            MenuItem tempLoginItem = menu.add(Menu.NONE, Menu.NONE, getResources().getInteger(R.integer.menu_order_first), null);
            tempLoginItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            tempLoginItem.setActionProvider(tempLoginActionProvider);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void checkPermissions() {
        if (!validatePermissions()) {
            PermissionFragment.showRedirecting(this, getOnTempLoginCompleteListener(), getPermissionsArray());
        }
    }

    protected LoginFragment.OnLoginCompleteListener getOnTempLoginCompleteListener() {
        return new LoginFragment.OnLoginCompleteListener() {
            @Override
            public void onLoginComplete() {
                onTempLogin();
                invalidateOptionsMenu();
            }

            @Override
            public boolean onLoginComplete(String lastUncompletedSaleOrderGuid) {
                return false;
            }
        };
    }

    protected void onTempLogin() {
    }

    protected void onTempLogout() {

    }

    private boolean validatePermissions() {
        Set<Permission> permissions = getPermissions();
        if (permissions == null || permissions.isEmpty())
            return true;

        Set<Permission> operatorPermissions = getApp().getOperatorPermissions();
        return operatorPermissions == null ? false : operatorPermissions.containsAll(permissions);
    }

    private class TempLoginActionProvider extends ActionProvider {

        private Context context;

        public TempLoginActionProvider(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public View onCreateActionView() {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.temp_login_item, null);
            TextView nameLabel = (TextView) view.findViewById(R.id.name_label);
            nameLabel.setText(getApp().getOperator().fullName());
            view.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getApp().hasPrevOperator()) {
                        getApp().restorePrevOperator();
                        onTempLogout();
                        invalidateOptionsMenu();
                        checkPermissions();
                        return;
                    }
                }
            });
            return view;

        }
    }

    public static class BaseTempLoginListener implements LoginFragment.OnLoginCompleteListener {

        private WeakReference<Activity> activityReference;

        public BaseTempLoginListener(Activity activity) {
            activityReference = new WeakReference<Activity>(activity);
        }

        protected Activity getActivity() {
            return activityReference.get();
        }

        @Override
        public void onLoginComplete() {
            Activity activity = getActivity();
            if (activity != null)
                activity.invalidateOptionsMenu();
        }

        @Override
        public boolean onLoginComplete(String lastUncompletedSaleOrderGuid) {
            return false;
        }
    }

}
