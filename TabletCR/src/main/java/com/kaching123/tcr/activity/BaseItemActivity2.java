package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.view.ViewPager;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ItemPagerAdapter;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.StartMode;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EActivity(R.layout.item_activity)
public class BaseItemActivity2 extends ScannerBaseActivity implements ItemProvider{

    @FragmentById
    protected ItemCommonInformationFragment commonInformationFragment;

    @ViewById
    protected SlidingTabLayout tabs;
    @ViewById
    protected ViewPager viewPager;

    @Extra
    protected ItemExModel model;

    @Extra
    protected StartMode mode;

    private ItemPagerAdapter adapter;

    @AfterViews
    protected void init(){
        adapter = new ItemPagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.item_tabs));
        viewPager.setAdapter(adapter);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);
    }

    @Override
    public ItemExModel getModel() {
        return model;
    }

    @Override
    public boolean isCreate() {
        return StartMode.ADD == mode;
    }

    @Override
    protected void onBarcodeReceived(String barcode) {

    }

    public static void start(Context context, ItemExModel model, StartMode mode){
        BaseItemActivity2_.intent(context).model(model).mode(mode).start();
    }
}
