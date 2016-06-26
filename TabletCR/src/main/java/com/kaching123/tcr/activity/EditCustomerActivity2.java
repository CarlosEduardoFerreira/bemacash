package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.view.ViewPager;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.CustomerPagerAdapter;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.customer.CustomerPersonalInfoFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.Permission;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EActivity(R.layout.edit_customer_activity2)
@OptionsMenu(R.menu.edit_customer_activity)
public class EditCustomerActivity2 extends SuperBaseActivity {

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

    @Extra
    protected CustomerModel model;

    private Mode mode;

    @AfterViews
    protected void init(){
        mode = model == null ? Mode.CREATE : Mode.UPDATE;
        setTitle();

        CustomerPagerAdapter adapter = new CustomerPagerAdapter(this, getSupportFragmentManager(), getResources().getStringArray(R.array.customer_tabs));
        viewPager.setAdapter(adapter);

        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);
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

    private enum Mode {
        CREATE, UPDATE
    }

    public static void start(Context context, CustomerModel model) {
        EditCustomerActivity2_.intent(context).model(model).start();
    }

    public static void startForResult(Context context, CustomerModel model, int requestCode) {
        EditCustomerActivity2_.intent(context).model(model).startForResult(requestCode);
    }
}
