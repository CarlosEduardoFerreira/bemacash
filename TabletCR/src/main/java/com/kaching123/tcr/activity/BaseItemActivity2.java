package com.kaching123.tcr.activity;

import android.support.v4.view.ViewPager;

import com.kaching123.tcr.R;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EActivity(R.layout.item_activity)
public class BaseItemActivity2 extends ScannerBaseActivity implements ItemProvider{

//    @FragmentById
    protected ItemCommonInformationFragment commonInformationFragment;

//    @ViewById
    protected SlidingTabLayout tabs;
//    @ViewById
    protected ViewPager viewPager;

    @Extra
    protected ItemExModel model;

    @AfterViews
    protected void init(){

    }

    @Override
    public ItemExModel getModel() {
        return model;
    }

    @Override
    public boolean isCreate() {
        return false;
    }

    @Override
    protected void onBarcodeReceived(String barcode) {

    }
}
