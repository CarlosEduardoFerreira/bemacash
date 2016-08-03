package com.kaching123.tcr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ItemPagerAdapter;
import com.kaching123.tcr.commands.store.inventory.DeleteItemCommand;
import com.kaching123.tcr.component.slidingtab.SlidingTabLayout;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.item.ItemCommonInformationFragment;
import com.kaching123.tcr.fragment.item.ItemProvider;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Permission;
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

import java.util.HashSet;
import java.util.List;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EActivity(R.layout.item_activity)
@OptionsMenu(R.menu.items_actions)
public class BaseItemActivity2 extends ScannerBaseActivity implements ItemProvider{

    private static final int TAG_RESULT_SERIAL = 1;
    private static final int TAG_RESULT_COMPOSER = 2;
    private static final int TAG_RESULT_MODIFIER = 3;

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @OptionsMenuItem(R.id.action_composer)
    protected MenuItem composerMenuItem;
    @OptionsMenuItem(R.id.action_modifier)
    protected MenuItem modifierMenuItem;
    @OptionsMenuItem(R.id.action_serial)
    protected MenuItem serialMenuItem;
    @OptionsMenuItem(R.id.action_remove)
    protected MenuItem deleteMenuItem;


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

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, new ItemLoader());
        /*RecalculateHostCompositionMetadataCommand2.start(self(), model.guid, new RecalculateHostCompositionMetadataCommand2.ComposerCallback() {
            @Override
            protected void handleSuccess(List<ComposerModel> unit, BigDecimal qty, BigDecimal cost) {
                Logger.d("success");
            }

            @Override
            protected void handleError() {
                Logger.d("error");
            }
        });*/
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
            composerMenuItem.setVisible(false);
            serialMenuItem.setVisible(false);
            modifierMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /******************/

    private class ItemLoader implements LoaderCallbacks<ItemExModel>{
        @Override
        public Loader<ItemExModel> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUriGroupBy(ItemExtView.URI_CONTENT, ItemTable.GUID))
                    .projection(ItemExFunction.PROJECTION)
                    .where(ItemTable.GUID + " = ?", model.guid)
                    .transform(new ItemExFunction())
                    .wrap(new Function<List<ItemExModel>, ItemExModel>() {
                        @Override
                        public ItemExModel apply(List<ItemExModel> input) {
                            if (!input.isEmpty())
                                return input.get(0);
                            else
                                return null;
                        }
                    }).build(self());
        }

        @Override
        public void onLoadFinished(Loader<ItemExModel> loader, ItemExModel data) {
            Logger.d("success");
        }

        @Override
        public void onLoaderReset(Loader<ItemExModel> loader) {

        }
    }

    public static void start(Context context, ItemExModel model, StartMode mode){
        BaseItemActivity2_.intent(context).model(model).mode(mode).start();
    }
}
