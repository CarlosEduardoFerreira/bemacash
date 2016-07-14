package com.kaching123.tcr.fragment.permissions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.Permission.Group;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by vkompaniets on 26.12.13.
 */
@EFragment (R.layout.permissions_list)
public class PermissionsFragment extends Fragment {

    @FragmentArg
    protected HashSet<Permission> groupPermissions;

    @FragmentArg
    protected HashSet<Permission> selectedPermissions;

    @FragmentArg
    protected Group group;

    @ViewById
    protected ListView list;

    @ViewById
    protected ImageView checkboxAll;

    @ViewById
    protected TextView title;

    @ViewById
    protected TextView titleCounter;

    private PermissionsAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SortedSet<Permission> groupPermissions2 = null;
        if (Group.SYSTEM_CONFIGURATION == group){
            groupPermissions2 = systemConfigurationDirtySort(groupPermissions);
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.itemClicked(i);
                setSelectAll(adapter.isAllSelected());
            }
        });

        title.setText(getString(group.getLabelId()));

        adapter = new PermissionsAdapter(getActivity());
        adapter.changeCursor(new ArrayList<>(groupPermissions2 == null ? groupPermissions : groupPermissions2));

        list.setAdapter(adapter);
    }

    private static SortedSet<Permission> systemConfigurationDirtySort(Set<Permission> permissions) {
        TreeSet<Permission> result = new TreeSet<>();
        if (permissions.contains(Permission.CUSTOMER_MANAGEMENT) && permissions.contains(Permission.CUSTOMER_LOYALTY_POINTS_ADJUST)){
            result.add(Permission.CUSTOMER_MANAGEMENT);
            result.add(Permission.CUSTOMER_LOYALTY_POINTS_ADJUST);
            for (Permission p : permissions){
                if (p != Permission.CUSTOMER_MANAGEMENT && p != Permission.CUSTOMER_LOYALTY_POINTS_ADJUST)
                    result.add(p);
            }
            return result;
        }else{
            return new TreeSet<>(permissions);
        }
    }

    private void updateCounter(){
        int count = adapter.getSelectedCount();
        if(count == 0){
            titleCounter.setText(null);
        }else{
            titleCounter.setText(String.format(Locale.US, "(%d)", count));
        }
    }

    @Click
    protected void checkboxAllClicked(){
        Boolean value = Boolean.TRUE.equals(checkboxAll.getTag()) ? Boolean.FALSE : Boolean.TRUE;
        adapter.setSelectAll(value);
        setSelectAll(value);
    }

    private void setSelectAll(boolean selected) {
        checkboxAll.setTag(selected);
        checkboxAll.setActivated(selected);
        updateCounter();
    }

    public void setSelectedItems(List<Permission> permissions) {
        adapter.setSelectedPermissions(permissions);
        setSelectAll(adapter.isAllSelected());
    }

    public Collection<? extends Permission> getSelectedItems() {
        return adapter.getSelectedPermissions();
    }
}
