package com.kaching123.tcr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ItemPagerAdapter;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand;
import com.kaching123.tcr.commands.store.inventory.AddReferenceItemCommand;
import com.kaching123.tcr.commands.store.inventory.AddVariantMatrixItemsCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditReferenceItemCommand;
import com.kaching123.tcr.commands.store.inventory.EditVariantMatrixItemCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand.UnitCallback;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.item.ItemAdditionalInformationFragment_;
import com.kaching123.tcr.fragment.item.ItemBaseFragment;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment;
import com.kaching123.tcr.fragment.item.ItemMonitoringFragment_;
import com.kaching123.tcr.fragment.item.ItemPriceFragment_;
import com.kaching123.tcr.fragment.item.ItemPrintFragment_;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.fragment.item.ItemSpecialPricingFragment_;
import com.kaching123.tcr.fragment.item.ItemVariantsFragment_;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemRefType;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static android.view.View.GONE;
import static com.kaching123.tcr.activity.BaseItemActivity2_.MODEL_EXTRA;
import static com.kaching123.tcr.activity.BaseItemActivity2_.MODE_EXTRA;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EActivity(R.layout.item_activity)
@OptionsMenu(R.menu.items_actions)
public class BaseItemActivity2 extends ScannerBaseActivity implements ItemProvider{

    public static final int TAG_RESULT_SERIAL = 1;
    public static final int TAG_RESULT_COMPOSER = 2;
    public static final int TAG_RESULT_MODIFIER = 3;

    public static final String DUPLICATE_EXTRA = "DUPLICATE_TAG";
    public static final String SOURCE_GUID_EXTRA = "SOURCE_GUID_EXTRA";

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

    @ViewById
    protected Button btnDuplicate;

    @ViewById
    protected View animObj;

    @Extra
    protected ItemExModel model;

    protected ItemExModel originalModel;

    @Extra
    protected StartMode mode;

    private FragmentPagerAdapter adapter;

    private ItemQtyInfo qtyInfo;

    private boolean changesInSubActivitiesDone;
    private boolean duplicateRequest;

    private ItemExModel parentItem;
    private ItemMatrixModel parentItemMatrix;

    private  AlphaAnimation animationFadeOut;

    @AfterViews
    protected void init(){
        animationFadeOut = new AlphaAnimation(0.0f, 1.0f);
        animationFadeOut.setDuration(500);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animObj.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        originalModel = new ItemExModel(model);
        String sourceGuid = getIntent().getExtras().getString(SOURCE_GUID_EXTRA);
        if (!TextUtils.isEmpty(sourceGuid)) {
            originalModel.guid = sourceGuid;
        }

        duplicateRequest = getIntent().getExtras().getBoolean(DUPLICATE_EXTRA, false);
        if(mode == StartMode.EDIT && !duplicateRequest && model.refType != ItemRefType.Reference) {
            btnDuplicate.setVisibility(View.VISIBLE);
        }

        qtyInfo = new ItemQtyInfo();
        qtyInfo.setAvailableQty(model.availableQty);

        adapter = createAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(10);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(viewPager);

        if (!isCreate()) {
            if (model.codeType == null){
                loadComposersInfo();
            }else{
                loadUnitsInfo();
            }
        }
    }

    private FragmentPagerAdapter createAdapter(){
        String[] tabTitles = getResources().getStringArray(R.array.item_tabs);
        ArrayList<TabHolder> tabHolders = new ArrayList<>();
        tabHolders.add(new TabHolder(tabTitles[0], new ItemPriceFragment_()));
        tabHolders.add(new TabHolder(tabTitles[1], new ItemAdditionalInformationFragment_()));
        tabHolders.add(new TabHolder(tabTitles[2], new ItemPrintFragment_()));
        tabHolders.add(new TabHolder(tabTitles[3], new ItemSpecialPricingFragment_()));
        if (model.refType == ItemRefType.Simple){
            tabHolders.add(new TabHolder(tabTitles[4], new ItemMonitoringFragment_()));
        }else{
            tabHolders.add(new TabHolder(tabTitles[5], new ItemVariantsFragment_()));
        }

        String[] titles = new String[tabHolders.size()];
        ItemBaseFragment[] fragments = new ItemBaseFragment[tabHolders.size()];
        for (int i = 0; i < tabHolders.size(); i++){
            titles[i] = tabHolders.get(i).name;
            fragments[i] = tabHolders.get(i).fragment;
        }

        return new ItemPagerAdapter(getSupportFragmentManager(), fragments, titles);
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
    public boolean isDuplicate() {
        return duplicateRequest;
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
    public void setParentItem(ItemExModel parent) {
        this.parentItem = parent;
    }

    @Override
    public void setParentMatrixItem(ItemMatrixModel parentMatrixItem) {
        this.parentItemMatrix = parentMatrixItem;
    }

    @Override
    protected void onBarcodeReceived(String barcode) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BarcodeReceiver) {
                BarcodeReceiver orderListFragment = (BarcodeReceiver) fragment;
                orderListFragment.onBarcodeReceived(barcode);
            }
        }
    }

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
        onBarcodeReceived(barcode);
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
        if (!validateData())
            return;

        collectData();
        saveReference();
        saveModel();
        exit();
    }

    @Click
    protected void btnDuplicateClicked(){
        alphaAnim();
        duplicateRequest = true;

        model = new ItemExModel(originalModel);

        model = model.duplicate();

        parentItem = null;
        model.referenceItemGuid = null;

        commonInformationFragment.duplicate();
        for (int i = 0; i < adapter.getCount(); i++) {
            ((ItemBaseFragment)adapter.getItem(i)).duplicate();
        }

        btnDuplicate.setVisibility(GONE);
    }

    private void alphaAnim(){
        animObj.setVisibility(View.VISIBLE);
        animObj.startAnimation(animationFadeOut);
    }

    private void exit() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MODEL_EXTRA, model);
        resultIntent.putExtra(MODE_EXTRA, mode);
        resultIntent.putExtra(DUPLICATE_EXTRA, isDuplicate());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void saveModel(){
        if (StartMode.ADD == mode || (StartMode.EDIT == mode && duplicateRequest)){
            if (model.isReferenceItem()){
                AddReferenceItemCommand.start(self(), model, null);
            }else{
                AddItemCommand.start(self(), model, null);
            }
        }else{
            if (model.isReferenceItem()){
                EditReferenceItemCommand.start(self(), model, null);
            }else{
                EditItemCommand.start(self(), model, null);
            }
        }
    }

    private void saveReference() {
        if (parentItem != null){ //item was linked to reference item
            if (parentItemMatrix == null){ //matrix variant wasn't selected, create new one
                parentItemMatrix = new ItemMatrixModel(
                        UUID.randomUUID().toString(),
                        model.description,
                        parentItem.guid,
                        model.guid, null
                );
                ArrayList<ItemMatrixModel> matrix = new ArrayList<>(1);
                matrix.add(parentItemMatrix);
                AddVariantMatrixItemsCommand.start(self(), matrix);
            }else{ //matrix variant was selected, update it
                EditVariantMatrixItemCommand.start(self(), parentItemMatrix);
            }
            model.referenceItemGuid = null;  //surprise!
        }
    }

    private boolean validateData(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fr : fragments){
            if (fr instanceof ItemBaseFragment)
                if (!((ItemBaseFragment) fr).validateData())
                    return false;
        }
        return true;
    }

    private void collectData(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fr : fragments){
            if (fr instanceof ItemBaseFragment)
                ((ItemBaseFragment) fr).collectData();
        }
    }

    private void updateQtyBlock() {
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
            actionComposer.setVisible(!model.isSerializable() && !model.isAComposer && model.refType == ItemRefType.Simple);
            actionSerial.setVisible(model.isSerializable() && PlanOptions.isSerializableAllowed() && model.refType == ItemRefType.Simple);
            actionModifier.setVisible(model.refType == ItemRefType.Simple);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (changesInSubActivitiesDone){
            Toast.makeText(self(), R.string.save_item_first_msg, Toast.LENGTH_LONG).show();
            return;
        }else{
            super.onBackPressed();
        }
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
            counterTextPanel.setVisibility(GONE);
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
                qtyInfo.setAvailableQty(new BigDecimal(availableUnitsCount));

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
                qtyInfo.setAvailableQty(restrictComposersCount == 0 ? getModel().availableQty : qty);

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
        public BigDecimal availableQty = BigDecimal.ZERO;
        public BigDecimal cost;
        public int unitsCount;
        public int availableUnitsCount;
        public int composersCount;
        public int restrictComposersCount;

        public void setAvailableQty(BigDecimal availableQty){
            this.availableQty = availableQty == null ? BigDecimal.ZERO : availableQty;
        }
    }

    public static void start(Context context, ItemExModel model, ItemRefType refType, StartMode mode){
        boolean isCommonItem = refType == ItemRefType.Simple;
        if (StartMode.ADD == mode){
            model.isSalable = isCommonItem;
            model.isActiveStatus = true;
            model.isDiscountable = true;
            model.refType = refType;
        }
        BaseItemActivity2_.intent(context).model(model).mode(mode).start();
    }

    public static Intent getIntentToStart(Context context, ItemExModel model, ItemRefType refType, StartMode mode){
        return getIntentToStart(context, model, refType, mode, false, null);
    }
    public static Intent getIntentToStart(Context context, ItemExModel model, ItemRefType refType, StartMode mode, boolean duplicate, String sourceGuid){
        Intent intent = new Intent(context, BaseItemActivity2_.class);
        boolean isCommonItem = refType == ItemRefType.Simple;
        if (StartMode.ADD == mode){
            model.isSalable = isCommonItem;
            model.isActiveStatus = true;
            model.isDiscountable = true;
            model.refType = refType;
        }
        intent.putExtra(MODEL_EXTRA, model);
        intent.putExtra(MODE_EXTRA, mode);
        intent.putExtra(DUPLICATE_EXTRA, duplicate);
        intent.putExtra(SOURCE_GUID_EXTRA, sourceGuid);
        return intent;
    }

    private class TabHolder{
        String name;
        ItemBaseFragment fragment;

        public TabHolder(String name, ItemBaseFragment fragment) {
            this.name = name;
            this.fragment = fragment;
        }
    }
}
