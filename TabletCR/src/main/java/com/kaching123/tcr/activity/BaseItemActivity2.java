package com.kaching123.tcr.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ItemPagerAdapter;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment_;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.StartMode;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EActivity(R.layout.item_activity)
@OptionsMenu(R.menu.items_actions)
public class BaseItemActivity2 extends ScannerBaseActivity implements ItemProvider{

    public static final int TAG_RESULT_SERIAL = 1;
    public static final int TAG_RESULT_COMPOSER = 2;
    public static final int TAG_RESULT_MODIFIER = 3;

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @OptionsMenuItem protected MenuItem actionComposer;
    @OptionsMenuItem protected MenuItem actionModifier;
    @OptionsMenuItem protected MenuItem actionSerial;
    @OptionsMenuItem protected MenuItem actionRemove;

    @FragmentById
    protected ItemCommonInformationFragment commonInformationFragment;

    @ViewById
    protected SlidingTabLayout tabs;

    @ViewById
    protected ViewPager viewPager;

    @Extra
    protected ItemExModel model;

    //holds actual database model
    protected ItemExModel model2;


    @Extra
    protected StartMode mode;

    private ItemPagerAdapter adapter;

    @AfterViews
    protected void init(){
        String[] tabNames = getResources().getStringArray(R.array.item_tabs);
        adapter = new ItemPagerAdapter(getSupportFragmentManager(), tabNames);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tabNames.length);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);

    }

    @Override
    public ItemExModel getModel() {
        return model;
    }

    @Override
    public ItemExModel getModel2() {
        return model2;
    }

    @Override
    public boolean isCreate() {
        return StartMode.ADD == mode;
    }

    @Override
    public void updateQtyBlock() {
        ItemMonitoringFragment fr = (ItemMonitoringFragment) getFragment(ItemMonitoringFragment_.class);
        if (fr != null){
            fr.updateQty();
        }
    }

    @Override
    protected void onBarcodeReceived(String barcode) {

    }

    @Override
    public void onResume() {
        super.onResume();
        reloadItem();
    }

    public void reloadItem(){
        getSupportLoaderManager().restartLoader(0, null, new ItemLoader());
    }

    /** Options menu **/

    @OptionsItem
    protected void actionSerialSelected() {
        UnitActivity.start(this, model, TAG_RESULT_SERIAL);
    }

    @OptionsItem
    protected void actionComposerSelected() {
        ComposerActivity.start(this, model, TAG_RESULT_COMPOSER);
    }

    @OptionsItem
    protected void actionModifierSelected() {
        ModifierActivity.start(this, model, TAG_RESULT_MODIFIER);
    }

    @OptionsItem
    protected void actionRemoveSelected() {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.item_activity_hide_item_dialog_title,
                getString(R.string.item_activity_hide_item_dialog_message),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteItemCommand.start(self(), model.guid);
                        finish();
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCreate()){
            actionComposer.setVisible(false);
            actionSerial.setVisible(false);
            actionModifier.setVisible(false);
            actionRemove.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isCreate()){
            actionComposer.setIcon(buildCounterDrawable(self(), model.composersCount, android.R.drawable.ic_dialog_dialer));
            actionComposer.setVisible(!model.isSerializable() && !model.isAComposer);
            actionSerial.setVisible(model.isSerializable() && PlanOptions.isSerializableAllowed());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /******************/

    private class ItemLoader implements LoaderCallbacks<List<ItemExModel>>{
        @Override
        public Loader<List<ItemExModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUriGroupBy(ItemExtView.URI_CONTENT, ItemTable.GUID))
                    .projection(ItemExFunction.PROJECTION)
                    .where(ItemTable.GUID + " = ?", model.guid)
                    .transform(new ItemExFunction())
                    .build(self());
        }

        @Override
        public void onLoadFinished(Loader<List<ItemExModel>> loader, List<ItemExModel> data) {
            if (data.isEmpty())
                return;

            model2 = data.get(0);
            model.availableQty = model2.availableQty;
            model.updateQtyFlag = model2.updateQtyFlag;
            model.isAComposer = model2.isAComposer;
            model.isAComposisiton = model2.isAComposisiton;
            model.composersCount = model2.composersCount;
            model.restrictComposersCount = model2.restrictComposersCount;
            model.unitCount = model2.unitCount;
            model.availableUnitCount = model2.availableUnitCount;

            if (model.codeType != null)
                model.availableQty = new BigDecimal(model.availableUnitCount);

            invalidateOptionsMenu();
            updateQtyBlock();
        }

        @Override
        public void onLoaderReset(Loader<List<ItemExModel>> loader) {

        }
    }

    private Fragment getFragment(Class clazz){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fr : fragments){
            if (fr.getClass() == clazz)
                return fr;
        }
        return null;
    }

    private static Drawable buildCounterDrawable(Context context, int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.badge, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static void start(Context context, ItemExModel model, StartMode mode){
        if (StartMode.ADD == mode){
            model.isSalable = true;
            model.isActiveStatus = true;
            model.isStockTracking = true;
            model.isDiscountable = true;
        }
        BaseItemActivity2_.intent(context).model(model).mode(mode).start();
    }
}
