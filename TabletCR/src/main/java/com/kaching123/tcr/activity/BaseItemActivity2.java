package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ItemPagerAdapter;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditItemCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand.UnitCallback;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.item.ItemBaseFragment;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment_;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.StartMode;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.store.composer.CollectComposersCommand;
import com.kaching123.tcr.store.composer.CollectComposersCommand.ComposerCallback;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
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

    private ItemQtyInfo qtyInfo;

    private boolean changesInSubActivitiesDone;

    @AfterViews
    protected void init(){
        qtyInfo = new ItemQtyInfo();
        qtyInfo.availableQty = model.availableQty;

        String[] tabNames = getResources().getStringArray(R.array.item_tabs);
        adapter = new ItemPagerAdapter(getSupportFragmentManager(), tabNames);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tabNames.length);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);

        if (model.codeType == null){
            loadComposersInfo();
        }else{
            loadUnitsInfo();
        }
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
    public void onStockTypeChanged() {
        if (model.codeType == null){
            loadComposersInfo();
        }else{
            loadUnitsInfo();
        }
    }

    @Override
    public void onPriceTypeChanged() {
        updateQtyBlock();
    }

    @Override
    public ItemQtyInfo getQtyInfo() {
        return qtyInfo;
    }

    @Override
    protected void onBarcodeReceived(String barcode) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == TAG_RESULT_SERIAL){
            changesInSubActivitiesDone |= data.getBooleanExtra(UnitActivity.RESULT_OK, false);
            loadUnitsInfo();
        }else if (resultCode == RESULT_OK && requestCode == TAG_RESULT_COMPOSER){
            changesInSubActivitiesDone |= data.getBooleanExtra(ComposerActivity.RESULT_OK, false);
            loadComposersInfo();
        }
    }

    @Click
    protected void btnSaveClicked(){
        collectDataFromFragments();
        if (StartMode.ADD == mode){
            AddItemCommand.start(self(), model);
        }else{
            EditItemCommand.start(self(), model);
        }
    }

    private void collectDataFromFragments(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fr : fragments){
            ((ItemBaseFragment) fr).collectData();
        }
    }

    public void updateQtyBlock() {
        ItemMonitoringFragment fr = (ItemMonitoringFragment) getFragment(ItemMonitoringFragment_.class);
        if (fr != null){
            fr.updateQty();
        }
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
            actionComposer.setIcon(buildCounterDrawable(self(), qtyInfo.composersCount, android.R.drawable.ic_dialog_dialer));
            actionComposer.setVisible(!model.isSerializable() && !model.isAComposer);
            actionSerial.setVisible(model.isSerializable() && PlanOptions.isSerializableAllowed());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /******************/

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

    private void loadUnitsInfo(){
        CollectUnitsCommand.start(self(), null, getModel().guid, null, null, null, false, false, new UnitCallback() {
            @Override
            protected void handleSuccess(List<Unit> units) {
                qtyInfo.unitsCount = units.size();
                int availableUnitsCount = 0;
                for (Unit unit : units){
                    if (unit.status != Status.SOLD)
                        availableUnitsCount++;
                }
                qtyInfo.availableUnitsCount = availableUnitsCount;
                qtyInfo.availableQty = new BigDecimal(availableUnitsCount);

                qtyInfo.composersCount = 0;
                qtyInfo.restrictComposersCount = 0;
                qtyInfo.cost = null;

                updateQtyBlock();
                invalidateOptionsMenu();
            }

            @Override
            protected void handleError() {

            }
        });
    }

    private void loadComposersInfo(){
        CollectComposersCommand.start(self(), getModel().guid, new ComposerCallback() {
            @Override
            protected void handleSuccess(List<ComposerExModel> composers, BigDecimal qty, BigDecimal cost) {
                qtyInfo.composersCount = composers.size();
                int restrictComposersCount = 0;
                for (ComposerExModel composer : composers){
                    if (composer.restricted)
                        restrictComposersCount++;
                }

                qtyInfo.cost = cost;
                qtyInfo.restrictComposersCount = restrictComposersCount;
                qtyInfo.availableQty = restrictComposersCount == 0 ? getModel().availableQty : qty;

                qtyInfo.unitsCount = 0;
                qtyInfo.availableUnitsCount = 0;

                updateQtyBlock();
                invalidateOptionsMenu();
            }

            @Override
            protected void handleError() {

            }
        });
    }

    public class ItemQtyInfo{
        public BigDecimal availableQty;
        public BigDecimal cost;
        public int unitsCount;
        public int availableUnitsCount;
        public int composersCount;
        public int restrictComposersCount;
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
