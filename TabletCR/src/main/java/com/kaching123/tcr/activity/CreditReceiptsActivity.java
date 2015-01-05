package com.kaching123.tcr.activity;

import android.content.Context;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.creditreceipt.CreditReceiptFilterFragment;
import com.kaching123.tcr.fragment.creditreceipt.CreditReceiptListFragment;
import com.kaching123.tcr.model.Permission;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gdubina on 27/02/14.
 */
@EActivity(R.layout.creditreceipt_activity)
public class CreditReceiptsActivity extends ScannerBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.REPORTS);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @FragmentById(R.id.creditreceipt_list_fragment)
    protected CreditReceiptListFragment listFragment;

    @FragmentById(R.id.creditreceipt_filter_fragment)
    protected CreditReceiptFilterFragment filterFragment;


    public static void start(Context context) {
        CreditReceiptsActivity_.intent(context).start();
    }


    @AfterViews
    protected void init() {
        filterFragment.setListener(listFragment);
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (filterFragment != null)
            filterFragment.setCreditReceiptNum(barcode);
    }

}
