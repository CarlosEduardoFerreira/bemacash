package com.kaching123.tcr.fragment.employee;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseEmployeeActivity;
import com.kaching123.tcr.activity.EditEmployeeActivity;
import com.kaching123.tcr.activity.PermissionActivity;
import com.kaching123.tcr.adapter.EmployeePagerAdapter;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.LabaledEnum;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PermissionPreset;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.kaching123.tcr.activity.BaseEmployeeActivity.PERMISSIONS_REQUEST_INDEX;

/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment(R.layout.employee_permission_fragment)
public class EmployeePermissionFragment extends EmployeeBaseFragment implements EmployeeView {

    private static final Uri URI_PERMISSIONS = ShopProvider.getContentUri(ShopStore.EmployeePermissionTable.URI_CONTENT);
    protected static final int CUSTOM_PRESET_INDEX = PermissionPreset.values().length - 1;

    @ViewById
    protected Spinner preset;
    @ViewById
    protected ListView permissionList;

    private ArrayList<Permission> permissions = new ArrayList<Permission>();
    protected ArrayList<PresetWrapper> presetDataList;
    private long presedValue;

    public Collection<Permission> customPermissionsBase;
    protected Collection<Permission> customPermissionInitial;

    @Override
    protected void setViews() {
        presetDataList = new ArrayList<PresetWrapper>();
        for (PermissionPreset preset : PermissionPreset.values()) {
            presetDataList.add(new PresetWrapper(preset));
        }

        final ArrayAdapter<PresetWrapper> presetAdapter = new ArrayAdapter<PresetWrapper>(getContext(),
                R.layout.spinner_item_light, presetDataList);
        presetAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        preset.setAdapter(presetAdapter);
        preset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                displayPermissions(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public ArrayList<Permission> getPermissions() {
        return permissions;
    }

    public Spinner getPreset() {
        return preset;
    }

    public long getPresedValue() {
        return presedValue;
    }

    public void try2SetupPermissions(List<Permission> result) {
        if (!updatePreset(result)) {
            PresetWrapper customPreset = this.presetDataList.get(CUSTOM_PRESET_INDEX);
            customPreset.setCustomPermissions(result);
            if (this.preset.getSelectedItemPosition() == CUSTOM_PRESET_INDEX) {
                displayPermissions(customPreset.getPermissions());
            } else {
                this.preset.setSelection(CUSTOM_PRESET_INDEX);
            }
        }
    }

    private boolean updatePreset(Collection<Permission> list) {
        customPermissionsBase = list;
        int len = presetDataList.size();
        for (int i = 0; i < len; i++) {
            EnumWrapper<PermissionPreset> preset = presetDataList.get(i);
            if (preset.getItem().isPreset(list)) {
                this.preset.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private void displayPermissions(Collection<Permission> list) {
        permissions.clear();
        if (list != null) {
            permissions.addAll(list);
        }
        permissionList.setAdapter(new PermissionAdapter(getContext(), permissions));
    }

    private void displayPermissions(int i) {
        displayPermissions(presetDataList.get(i).getPermissions());
    }

    @Override
    protected void setEmployee() {
        if (getMode() == BaseEmployeeActivity.EmployeeMode.EDIT) {
            preset.setOnTouchListener(new android.view.View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        presedValue = preset.getSelectedItemId();
                    }
                    return false;
                }
            });
            this.preset.setSelection(CUSTOM_PRESET_INDEX);

            getLoaderManager().initLoader(0, null, new UserPermissionsLoader());
        }
    }

    public boolean permissionsHasChanges(){
        if(customPermissionInitial!=null && customPermissionsBase!=null) {
            for (Permission a : customPermissionInitial) {
                boolean hasOnBase = customPermissionsBase.contains(a);
                if (!hasOnBase){
                    Log.d("BemaCarl3","EmployeePermissionFragment.permissionsHasChanges.hasOnBase: " + hasOnBase);
                    Log.d("BemaCarl3","EmployeePermissionFragment.permissionsHasChanges.a.getId(): " + a.getId());
                    return true;
                }
            }
            for (Permission b : customPermissionsBase) {
                boolean hasOnInitial = customPermissionInitial.contains(b);
                if (!hasOnInitial){
                    Log.d("BemaCarl3","EmployeePermissionFragment.permissionsHasChanges.hasOnInitial: " + hasOnInitial);
                    Log.d("BemaCarl3","EmployeePermissionFragment.permissionsHasChanges.b.getId(): " + b.getId());
                    return true;
                }
            }
        }
        return false;
    }

    @Click
    protected void btnEditPermissionClicked() {
        PermissionActivity.start(getContext(), PERMISSIONS_REQUEST_INDEX, getPermissions());
    }

    @Override
    public void collectDataToModel(EmployeeModel model) {

    }

    @Override
    public void setFieldsEnabled(boolean enabled) {

    }

    @Override
    public boolean validateView() {
        return true;
    }

    @Override
    public boolean hasChanges(EmployeeModel initModel) {
        return false;
    }

    private class PresetWrapper extends EnumWrapper<PermissionPreset> {

        private Collection<Permission> customPermissions;

        private PresetWrapper(PermissionPreset item) {
            super(item);
            customPermissions = item.getPermissions();
        }

        public Collection<Permission> getPermissions() {
            return customPermissions;
        }

        public void setCustomPermissions(Collection<Permission> customPermissions) {
            if (customPermissions == null) {
                this.customPermissions = Collections.emptyList();
                return;
            }
            this.customPermissions = customPermissions;
        }
    }

    private class EnumWrapper<E extends LabaledEnum> {
        final E item;

        private EnumWrapper(E item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return getString(item.getLabelId());
        }

        public E getItem() {
            return item;
        }
    }


    private class PermissionAdapter extends ObjectsCursorAdapter<Permission> {

        public PermissionAdapter(Context context, List<Permission> permissions) {
            super(context);
            changeCursor(permissions);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.permission_preset_list_item, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.header = (TextView) convertView.findViewById(R.id.header);
            holder.divider = convertView.findViewById(R.id.divider);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, Permission item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Permission i = getItem(position);

            if (i == null) {
                return convertView;
            }

            holder.name.setText(getString(i.getLabelId()));

            if (position == 0 || item.getGroup() != getItem(position - 1).getGroup()) {
                holder.header.setVisibility(View.VISIBLE);
                holder.header.setText(item.getGroup().getLabelId());
            } else {
                holder.header.setVisibility(View.GONE);
            }

            if (position == getCount() - 1 || item.getGroup() != getItem(position + 1).getGroup()) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public synchronized void changeCursor(List<Permission> list) {
            Collections.sort(list);
            super.changeCursor(list);
        }

        private class ViewHolder {
            TextView header;
            TextView name;
            View divider;
        }
    }

    private class UserPermissionsLoader implements LoaderManager.LoaderCallbacks<List<Permission>> {

        @Override
        public Loader<List<Permission>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_PERMISSIONS)
                    .projection(ShopStore.EmployeePermissionTable.PERMISSION_ID)
                    .where(ShopStore.EmployeePermissionTable.USER_GUID + " = ?", getEmployee().guid)
                    .where(ShopStore.EmployeePermissionTable.ENABLED + " = ?", 1)
                    .transform(new Function<Cursor, List<Permission>>() {
                        @Override
                        public List<Permission> apply(Cursor c) {
                            List<Permission> permissions = new ArrayList<Permission>(c.getCount());
                            while (c.moveToNext()) {
                                Permission p = Permission.valueOfOrNull(c.getLong(0));
                                if (p != null) {
                                    permissions.add(p);
                                }
                            }
                            Collections.sort(permissions, new Comparator<Permission>() {
                                @Override
                                public int compare(Permission p1, Permission p2) {
                                    return p1.getGroup().compareTo(p2.getGroup());
                                }
                            });
                            customPermissionInitial = permissions;
                            return permissions;
                        }
                    })
                    .build(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<Permission>> mapLoader, List<Permission> permissions) {
            try2SetupPermissions(permissions);
            getLoaderManager().destroyLoader(0);
            presedValue = preset.getSelectedItemId();
        }

        @Override
        public void onLoaderReset(Loader<List<Permission>> mapLoader) {

        }

    }
}
