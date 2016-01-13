package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.inventory.VariantsListFragment;
import com.kaching123.tcr.fragment.inventory.VariantsMatrixFragment;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;


/**
 * Created by aakimov on 27/04/15.
 */
@EActivity(R.layout.inventory_item_variants_activity)
public class VariantsActivity extends SuperBaseActivity {
    @Extra
    ItemExModel model;

    public static void start(Context context, ItemExModel model) {
        VariantsActivity_.intent(context).model(model).start();
    }

    @AfterViews
    protected void init() {
        fill();
        setTitle(R.string.variants);
    }

    @OptionsItem
    protected void actionSeeMatrixSelected() {
        fill();
    }

    private void fill() {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
        if (f != null && f instanceof VariantsListFragment) {
            VariantsMatrixFragment.replace(getSupportFragmentManager(), model);
        } else {
            VariantsListFragment.replace(getSupportFragmentManager(), model);
        }
    }

}
