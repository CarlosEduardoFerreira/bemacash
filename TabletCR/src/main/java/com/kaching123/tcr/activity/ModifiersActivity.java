package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog.ActionType;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog.OnEditListener;
import com.kaching123.tcr.fragment.editmodifiers.EditModifiersFragment;
import com.kaching123.tcr.fragment.editmodifiers.ModifiersCopyDialog;
import com.kaching123.tcr.fragment.editmodifiers.ModifiersCopyDialog.OnClosedListener;
import com.kaching123.tcr.fragment.editmodifiers.SearchFragment;
import com.kaching123.tcr.fragment.editmodifiers.SearchFragment.IItemListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.KeyboardUtils;

import java.util.HashSet;
import java.util.UUID;

/**
 * Created by vkompaniets on 09.12.13.
 */
@EActivity (R.layout.inventory_item_modifiers_activity)
@OptionsMenu (R.menu.inventory_item_modifier_activity)
public class ModifiersActivity extends SuperBaseActivity/* implements LoaderManager.LoaderCallbacks<String>*/{

    private final static Uri URI_ITEM = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);

    public static final String EXTRA_DEF_MODIFIER = "EXTRA_DEF_MODIFIER";

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    @Extra
    protected String itemGuid;

    @Extra
    protected String itemName;

    @Extra
    protected String defaultModifier;

    @FragmentById
    protected EditModifiersFragment modifiers;

    @FragmentById
    protected EditModifiersFragment addons;

    @FragmentById
    protected EditModifiersFragment optionals;

    @FragmentById
    protected SearchFragment searchFragment;

    private MenuItem searchItem;

    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    private OnEditListener onEditListener = new OnEditListener() {
        @Override
        public void onDefaultModifierChanged(String modifierId, boolean useAsDefault, boolean resetDefaultModifier) {
            Intent bundle = new Intent();
            if(useAsDefault){
                modifiers.setDefaultModifierGuid(modifierId);
                bundle.putExtra(EXTRA_DEF_MODIFIER, modifierId);
                setResult(RESULT_OK, bundle);
            }else if(resetDefaultModifier){
                modifiers.setDefaultModifierGuid(null);
                bundle.putExtra(EXTRA_DEF_MODIFIER, (String) null);
                setResult(RESULT_OK, bundle);
            }
        }
    };

    @AfterViews
    protected void init(){

        String itemName = TextUtils.isEmpty(this.itemName) ? getString(R.string.modifiers_activity_new_item_title) : this.itemName;
        getActionBar().setTitle(String.format(getString(R.string.modifiers_activity_title), itemName));

        getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
        searchFragment.setListener(new IItemListener() {
            @Override
            public void onItemSelected(long id, ItemExModel model) {
                ModifiersCopyDialog.show(ModifiersActivity.this, itemGuid, model, new OnClosedListener() {
                    @Override
                    public void onDialogSuccessClosed() {
                        closeSearch();
                    }
                });
            }
        });

        //getSupportLoaderManager().initLoader(0, null, this);
        initFragments();

    }

    private void initFragments() {
        modifiers.setArgs(itemGuid, ModifierType.MODIFIER, onEditListener);
        modifiers.setDefaultModifierGuid(defaultModifier);
        addons.setArgs(itemGuid, ModifierType.ADDON, null);
        optionals.setArgs(itemGuid, ModifierType.OPTIONAL, null);
    }

    @OptionsItem
    protected void actionNewSelected(){
        hideSearchFragment();
        EditDialog.show(this, "", new ModifierModel(UUID.randomUUID().toString(), itemGuid, null, null, null), ActionType.CREATE, onEditListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        searchItem = menu.findItem(R.id.action_search);
        initSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideSearchFragment();
                return true;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchFragment();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                KeyboardUtils.hideKeyboard(ModifiersActivity.this, searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSearchFragment(newText);
                return true;
            }
        });
    }

    /**
     * search fragment
     */
    protected void showSearchFragment() {
        getSupportFragmentManager().beginTransaction().show(searchFragment).commit();
    }

    private void hideSearchFragment() {
        getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
    }

    private void closeSearch() {
        if (searchItem == null)
            return;
        searchItem.collapseActionView();
    }

    private void filterSearchFragment(String text) {
        searchFragment.setSearchText(text);
    }


    /*** search fragment end ***/
/*
<<<<<<< .working
    public static void start(Context context, String itemGuid, String itemName){
        ModifiersActivity_.intent(context).itemGuid(itemGuid).itemName(itemName).start();
=======
*/

    public static void start(Context context, int reqCode, String itemGuid, String itemName, String defaultModifier) {
        ModifiersActivity_.intent(context).itemGuid(itemGuid).itemName(itemName).defaultModifier(defaultModifier).startForResult(reqCode);
    }

    /*@Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder
                .forUri(URI_ITEM)
                .projection(ShopStore.ItemTable.DEFAULT_MODIFIER_GUID)
                .where(ShopStore.ItemTable.GUID + " = ?", itemGuid)
                .wrap(new Function<Cursor, String>() {
                    @Override
                    public String apply(Cursor c) {
                        if(c.moveToFirst()){
                            return c.isNull(0) ? "" : c.getString(0);
                        }
                        return "";
                    }
                }).build(this);
    }

    @Override
    public void onLoadFinished(Loader<String> stringLoader, String itemDefaultModifierGuid) {
        modifiers.setDefaultModifierGuid(itemDefaultModifierGuid);
        addons.setDefaultModifierGuid(itemDefaultModifierGuid);
        optionals.setDefaultModifierGuid(itemDefaultModifierGuid);
    }

    @Override
    public void onLoaderReset(Loader<String> stringLoader) {

    }*/
}
