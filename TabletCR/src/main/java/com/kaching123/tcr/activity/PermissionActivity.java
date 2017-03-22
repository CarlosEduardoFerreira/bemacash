package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.permissions.PermissionsFragment;
import com.kaching123.tcr.fragment.permissions.PermissionsFragment_;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.Permission.Group;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by vkompaniets on 26.12.13.
 */
@EActivity(R.layout.permission_activity)
public class PermissionActivity extends SuperBaseActivity {

    public static final String EXTRA_PERMISSIONS = "EXTRA_PERMISSIONS";

    @Extra
    public List<Permission> userPermissions;

    private HashMap<Group, Integer> group2Resource = new HashMap<Group, Integer>();

    @AfterViews
    protected void init() {

        group2Resource.put(Group.SALES_MODULE, R.id.sales);
        group2Resource.put(Group.DASHBOARD, R.id.dashboard);
        group2Resource.put(Group.SYSTEM_CONFIGURATION, R.id.system);

        for (Entry<Group, Integer> entry : group2Resource.entrySet()) {
            HashSet<Permission> permissions = new HashSet<Permission>();
            for (Permission p : Permission.values()) {
                if (entry.getKey() == p.getGroup()) {
                    permissions.add(p);
                }
            }
            final PermissionsFragment fragment = PermissionsFragment_.builder().group(entry.getKey()).groupPermissions(permissions).build();
            getSupportFragmentManager().beginTransaction().replace(entry.getValue(), fragment).commit();

        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        HashMap<Group, List<Permission>> result = new HashMap<Group, List<Permission>>();
        for (Permission p : userPermissions) {
            List<Permission> permissions = result.get(p.getGroup());
            if (permissions == null) {
                result.put(p.getGroup(), permissions = new ArrayList<Permission>());
            }
            permissions.add(p);
        }
        for (Entry<Group, List<Permission>> e : result.entrySet()) {
            if (!group2Resource.containsKey(e.getKey())) {
                continue;
            }
            PermissionsFragment f = (PermissionsFragment) getSupportFragmentManager().findFragmentById(group2Resource.get(e.getKey()));
            f.setSelectedItems(e.getValue());
        }
    }

    private void collectResult() {
        ArrayList<Permission> permissions = new ArrayList<Permission>();
        for (Entry<Group, Integer> e : group2Resource.entrySet()) {
            PermissionsFragment f = (PermissionsFragment) getSupportFragmentManager().findFragmentById(group2Resource.get(e.getKey()));
            permissions.addAll(f.getSelectedItems());
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        setResult(RESULT_OK, intent);
    }

    @Click
    protected void btnSaveClicked(){
        collectResult();
        finish();
    }

    @Override
    public void onBackPressed() {
        collectResult();
        finish();
    }

    public static void start(Context context, int reqCode, List<Permission> userPermissions) {
        PermissionActivity_.intent(context).userPermissions(userPermissions).startForResult(reqCode);
    }
}
