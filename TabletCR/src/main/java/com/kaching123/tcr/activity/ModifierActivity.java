package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.DeleteModifierCommand;
import com.kaching123.tcr.commands.store.inventory.DeleteModifierGroupCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.modify.ModificationItemListFragment;
import com.kaching123.tcr.fragment.modify.ModifierEditFragment;
import com.kaching123.tcr.fragment.modify.ModifierGroupEditFragment;
import com.kaching123.tcr.fragment.modify.ModifierItemListFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;


/**
 * Created by alboyko on 01.12.2015.
 */

@EActivity(R.layout.modifier_activity)
@OptionsMenu(R.menu.modifier_activity)
public class ModifierActivity extends ScannerBaseActivity implements ModifierItemListFragment.ItemDataProvider, ModifierItemListFragment.IModifierCallback  {

    public static final String RESULT_OK = "RESULT_OK";

    @Extra
    protected String itemGuid;

    @Extra
    protected ItemExModel model;

    @ViewById(R.id.pager)
    protected ViewPager viewPager;

    @ViewById
    protected LinearLayout holderView;

    protected boolean redirectBarcodeResult;

    protected ModificationPagerAdapter pagerAdapter;

    private ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                if (viewPager == null) {
                    return;
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };
        actionBar.addTab(actionBar.newTab().setText(R.string.dlg_section_modifier).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.dlg_section_addon).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.dlg_section_optional).setTabListener(tabListener));
    }
        setTitle(model.description);
    }

    public static void start(Context context, ItemExModel model, int tag) {
        ModifierActivity_.intent(context).model(model).startForResult(tag);
    }

    @AfterViews
    protected void init() {
        pagerAdapter = new ModificationPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (actionBar.getTabCount() > position) {
                            actionBar.setSelectedNavigationItem(position);
                        }
                    }
                });
    }

    @OptionsItem
    protected void actionCopySelected(){
        CopyModifiersActivity.start(self(), model.guid, model.description);
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (redirectBarcodeResult) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(1);
            if (fragment != null && fragment instanceof BarcodeReceiver) {
                BarcodeReceiver editFragment = (BarcodeReceiver)fragment;
                editFragment.onBarcodeReceived(barcode);
            }
        } else {
            manageModifier(null, barcode, null, null);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_OK, model);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

     private void showSnack(String msg) {
        // SnackUtils.showSnackClose(snack, self(), msg);
         Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void manageModifier(final ModifierExModel unit, String serial, ModifierType modType, String itemGroupGuid) {
        redirectBarcodeResult = true;
        ModifierEditFragment.show(this, model.guid, itemGroupGuid, modType, unit, new ModifierEditFragment.ModifierCallback() {
            @Override
            public void handleSuccess(ComposerModel parent) {
                hide();
                snack();
            }

            @Override
            public void handleSuccess() {
                hide();
                snack();
            }

            @Override
            public void handleError(String message) {
                AlertDialogFragment.showAlert(self(), R.string.item_activity_alert_composer_msg, message);
                showSnack(message);
            }

            @Override
            public void handleCancel() {
                hide();
                showSnack(getString(R.string.modifier_cancel_success));
            }

            private void hide() {
                ModifierEditFragment.hide(self());
            }


            private void snack() {
                showSnack(unit == null ? getString(R.string.modifier_add_success) : getString(R.string.modifier_edit_success));
            }
        });
    }

    private void manageGroup(final ModifierGroupModel unit) {
        ModifierGroupEditFragment.show(this, model.guid, unit, new ModifierGroupEditFragment.ModifierGroupCallback() {

            @Override
            public void handleSuccess(ModifierGroupModel parent) {
                hide();
                snack();
            }


            @Override
            public void handleError(String message) {
                AlertDialogFragment.showAlert(self(), R.string.item_activity_alert_composer_msg, message);
                showSnack(message);
            }

            @Override
            public void handleCancel() {
                hide();
                showSnack(getString(R.string.modifier_cancel_success));
            }

            private void hide() {
                ModifierGroupEditFragment.hide(self());
            }


            private void snack() {
                showSnack(unit == null ? getString(R.string.modifier_add_success) : getString(R.string.modifier_edit_success));
            }
        });
    }

    @Override
    public ItemExModel getItem() {
        return model;
    }

    @Override
    public void onAdd(ModifierType modType, String itemGroupGuid) {
        manageModifier(null, null, modType, itemGroupGuid);
    }

    @Override
    public void onAddGroup() {
        manageGroup(null);
    }

    @Override
    public void onEdit(ModifierExModel item) {
        manageModifier(item, null, item.type, null);
    }

    @Override
    public void onDefault(ModifierExModel item) {
        showSnack(getString(R.string.modifier_group_default_success));
    }

    @Override
    public void onEdit(ModifierGroupModel item) {
        manageGroup(item);
    }

    @Override
    public void onDeleteModel(final List<ModifierExModel> units) {
        for (ModifierExModel item : units) {
            DeleteModifierCommand.start(self(), item);
        }
        showSnack(getString(R.string.modifier_remove_success));
    }

    @Override
    public void onDeleteGroup(ModifierGroupModel unit) {
        DeleteModifierGroupCommand.start(self(), unit);
        showSnack(getString(R.string.modifier_group_remove_success));
    }

    public class ModificationPagerAdapter extends FragmentStatePagerAdapter {

        public ModificationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            ModifierType type;
            Fragment fragment;
            if (i == 0) {
                type = ModifierType.MODIFIER;
                fragment = ModificationItemListFragment.build(type);
            } else if (i == 1) {
                type = ModifierType.ADDON;
                fragment = ModifierItemListFragment.build(type);
            } else if (i == 2) {
                type = ModifierType.OPTIONAL;
                fragment = ModifierItemListFragment.build(type);
            } else {
                type = null;
                fragment = ModifierItemListFragment.build(type);
            }
            fragment.setArguments(Bundle.EMPTY);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
}