package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.commands.store.export.ExportInventoryCommand;
import com.kaching123.tcr.commands.store.export.ExportInventoryCommand.ExportCommandBaseCallback;
import com.kaching123.tcr.commands.store.export.ExportQuickbooksInventoryCommand;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand.BaseImportCommandCallback;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand.ImportType;
import com.kaching123.tcr.commands.store.inventory.ImportInventoryCommand.WrongImportInfo;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.AlertDialogListFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.FileChooseListener;
import com.kaching123.tcr.fragment.filemanager.FileChooserFragment.Type;
import com.kaching123.tcr.fragment.filemanager.ImportTypeFragment;
import com.kaching123.tcr.fragment.filemanager.ImportTypeFragment.OnTypeChosenListener;
import com.kaching123.tcr.fragment.inventory.CategoriesFragment;
import com.kaching123.tcr.fragment.inventory.ItemsFragment;
import com.kaching123.tcr.fragment.itempick.DrawerCategoriesFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.kaching123.tcr.util.KeyboardUtils.hideKeyboard;

/**
 * Created by vkompaniets on 27.11.13.
 */

@EActivity(R.layout.inventory_activity)
@OptionsMenu(R.menu.inventory_activity)
public class InventoryActivity extends ScannerBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri ITEMS_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    @FragmentById
    protected CategoriesFragment categoriesFragment;

    @FragmentById
    protected ItemsFragment itemsFragment;

    @Extra
    protected boolean extraOnlyNearTheEnd;

    private MenuItem searchItem;

    private MenuItem sortItem;

    private boolean sortByName;

    private String selectedDeartmentGuid;

    private String selectedCategoryGuid;

    private ImportCallback importCallback = new ImportCallback();
    private ExportCallback exportCallback = new ExportCallback();
    private ExportQuickbooksCallback exportQuickbooksCallback = new ExportQuickbooksCallback();

    private int itemsCount;

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    public static void start(Context context, boolean nearTheEnd) {
        InventoryActivity_.intent(context).extraOnlyNearTheEnd(nearTheEnd).start();
    }

    @AfterViews
    protected void init() {
        categoriesFragment.setListener(new DrawerCategoriesFragment.ICategoryListener() {

            @Override
            public void onCategoryChanged(long id, String depGuid, String catGuid) {
                //Logger.d("[INVENTORY] onCategoryChanged %d, %s, %s", id, depGuid, catGuid);
                if (id == AdapterView.INVALID_POSITION) {
                    itemsFragment.setCategory(ItemsFragment.LOAD_ALL_CATEGORIES);
                    selectedDeartmentGuid = null;
                    selectedCategoryGuid = null;
                    if (sortItem != null){
                        sortItem.setVisible(true);
                    }
                } else {
                    itemsFragment.setCategory(catGuid);
                    itemsFragment.setDepartment(depGuid);
                    selectedCategoryGuid = catGuid;
                    selectedDeartmentGuid = depGuid;
                    if (sortItem != null){
                        sortItem.setVisible(false);
                    }
                }
            }

        });

        itemsFragment.setListener(new BaseItemsPickFragment.IItemListener() {
            @Override
            public void onItemSelected(long id, ItemExModel model) {
                EditItemActivity.start(InventoryActivity.this, model);
            }
        });

        getSupportLoaderManager().initLoader(0, null, itemsCountLoader);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(new NavigationSpinnerAdapter(this), new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                itemsFragment.setUseOnlyNearTheEnd(NavigationSpinnerAdapter.NAVIGATION_NEAR_THE_END == itemPosition);
                categoriesFragment.setUseOnlyNearTheEnd(NavigationSpinnerAdapter.NAVIGATION_NEAR_THE_END == itemPosition);
                return true;
            }
        });
        getActionBar().setSelectedNavigationItem(extraOnlyNearTheEnd ? NavigationSpinnerAdapter.NAVIGATION_NEAR_THE_END : NavigationSpinnerAdapter.NAVIGATION_ALL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean b = super.onCreateOptionsMenu(menu);
        searchItem = menu.findItem(R.id.action_search);
        sortItem = menu.findItem(R.id.action_sort);
        assert searchItem != null;
        initSearchView();
        return b;
    }

    @OptionsItem
    protected void actionImportSelected() {
        FileChooserFragment.show(this, Type.FILE, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                ImportTypeFragment.show(InventoryActivity.this, new OnTypeChosenListener() {
                    @Override
                    public void onTypeChosen(ImportInventoryCommand.ImportType type) {
                        if (type == ImportType.ALL && !checkMaxItemsCount()) {
                            AlertDialogFragment.showAlert(InventoryActivity.this, R.string.error_dialog_title, getString(R.string.error_message_max_items_count));
                            return;
                        }

                        importCallback.type = type;
                        WaitDialogFragment.show(InventoryActivity.this, getString(R.string.inventory_import_wait));
                        ImportInventoryCommand.start(InventoryActivity.this, type, file.getAbsolutePath(), importCallback);
                    }
                });
            }
        });
    }

    @OptionsItem
    protected void actionExportSelected() {
        FileChooserFragment.show(this, Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(InventoryActivity.this, getString(R.string.inventory_export_wait_msg));
                ExportInventoryCommand.start(InventoryActivity.this, file.getAbsolutePath(), exportCallback);
            }
        });
    }

    /*@OptionsItem
    protected void actionQuickbooksExportSelected() {
        FileChooserFragment.show(this, Type.FOLDER, new FileChooseListener() {
            @Override
            public void fileChosen(final File file) {
                WaitDialogFragment.show(InventoryActivity.this, getString(R.string.inventory_export_wait_msg));
                ExportQuickbooksInventoryCommand.start(InventoryActivity.this, file.getAbsolutePath(), exportQuickbooksCallback);
            }
        });
    }*/

    @OptionsItem
    protected void actionSortSelected() {
        int level = sortItem.getIcon().getLevel();
        if (level == 0){
            level = 1;
        }else {
            level = 0;
        }
        sortByName = level == 1;
        itemsFragment.sortByName(sortByName);
        sortItem.getIcon().setLevel(level);
    }

    @OptionsItem
    protected void actionAddItemSelected() {
        if (!checkMaxItemsCount()) {
            AlertDialogFragment.showAlert(this, R.string.error_dialog_title, getString(R.string.error_message_max_items_count));
            return;
        }

        ItemExModel model = new ItemExModel();
        model.categoryId = selectedCategoryGuid;
        model.departmentGuid = selectedDeartmentGuid;

        AddItemActivity.start(InventoryActivity.this, model);
    }

    private boolean checkMaxItemsCount() {
        return itemsCount < getApp().getShopInfo().maxItemsCount;
    }

    @OptionsItem
    protected void actionManageCategoriesSelected() {
        CategoriesActivity.start(this);
    }


    @OptionsItem
    protected void actionManageDepartmentSelected() {
        DepartmentActivity.start(this);
    }

    @OptionsItem
    protected void actionManageTaxGroupsSelected() {
        TaxGroupsActivity.start(this);
    }

    @OptionsItem
    protected void actionManagePrinterAliasSelected(){
        PrinterAliasActivity.start(this);
    }

    private void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard(InventoryActivity.this, searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSearchFragment(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setQuery(null, true);
                return true;
            }
        });
    }

    private void filterSearchFragment(String newText) {
        itemsFragment.setTextFilter(newText);
    }

    @Override
    protected void onBarcodeReceived(String barcode) {
        searchItem.expandActionView();
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQuery(barcode, true);
    }


    private class NavigationSpinnerAdapter extends ArrayAdapter<String> {

        public static final int NAVIGATION_ALL = 0;
        public static final int NAVIGATION_NEAR_THE_END = 1;

        public NavigationSpinnerAdapter(Context context) {
            super(context, R.layout.actionbar_spinner, new String[]{context.getString(R.string.inventory_navigation_filter_all), context.getString(R.string.inventory_navigation_filter_near_the_end)});
            setDropDownViewResource(R.layout.actionbar_spinner_drodown_item);
        }
    }

    public class ImportCallback extends BaseImportCommandCallback {

        ArrayList<WrongImportInfo> wrongItems;
        ImportType type;
        boolean isMaxItemsCountError;

        @Override
        protected void handleStart() {
            wrongItems = new ArrayList<WrongImportInfo>();
            isMaxItemsCountError = false;
        }

        @Override
        protected void handleInvalidData(WrongImportInfo info) {
            wrongItems.add(info);
        }

        @Override
        protected void handleSuccess(final int count) {
            WaitDialogFragment.hide(InventoryActivity.this);

            if (isMaxItemsCountError) {
                AlertDialogFragment.show(InventoryActivity.this, DialogType.ALERT, R.string.error_dialog_title, getString(R.string.error_message_max_items_count), R.string.btn_ok, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        if (wrongItems != null && wrongItems.isEmpty()) {
                            AlertDialogFragment.showAlert(InventoryActivity.this, R.string.error_dialog_title,
                                    getString(type == ImportType.DELETE ? R.string.inventory_import_delete_success_msg : R.string.inventory_import_success_msg, count));
                        } else {
                            String message = getString(R.string.inventory_import_failed_items_max_count_msg_list, count);
                            AlertDialogListFragment.show(InventoryActivity.this, message, new WrongImportInfoAdapter(InventoryActivity.this, wrongItems));
                            wrongItems = null;
                        }
                        return true;
                    }
                });
                return;
            }

            if (wrongItems != null && wrongItems.isEmpty()) {
                AlertDialogFragment.showNotification(InventoryActivity.this, R.string.inventory_import_success_title,
                        getString(type == ImportType.DELETE ? R.string.inventory_import_delete_success_msg : R.string.inventory_import_success_msg, count));
            } else {
                AlertDialogListFragment.show(InventoryActivity.this, new WrongImportInfoAdapter(InventoryActivity.this, wrongItems));
                wrongItems = null;
            }

        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(InventoryActivity.this);
            AlertDialogFragment.showAlert(InventoryActivity.this, R.string.error_dialog_title, getString(R.string.inventory_import_failed_msg));
        }

        @Override
        protected void handleMaxItemsCountError() {
            isMaxItemsCountError = true;
        }
    }

    public class ExportCallback extends ExportCommandBaseCallback {

        @Override
        protected void handleSuccess(int count) {
            WaitDialogFragment.hide(InventoryActivity.this);
            AlertDialogFragment.showComplete(InventoryActivity.this, R.string.inventory_export_success_title, getString(R.string.inventory_export_success_msg, count));
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(InventoryActivity.this);
            AlertDialogFragment.showAlert(InventoryActivity.this, R.string.inventory_export_error_title, getString(R.string.inventory_export_error_msg));
        }
    }

    public class ExportQuickbooksCallback extends ExportQuickbooksInventoryCommand.ExportCommandBaseCallback {

        @Override
        protected void handleSuccess(int count) {
            WaitDialogFragment.hide(InventoryActivity.this);
            AlertDialogFragment.showComplete(InventoryActivity.this, R.string.inventory_export_success_title, getString(R.string.inventory_export_success_msg, count));
        }

        @Override
        protected void handleFailure() {
            WaitDialogFragment.hide(InventoryActivity.this);
            AlertDialogFragment.showAlert(InventoryActivity.this, R.string.inventory_export_error_title, getString(R.string.inventory_export_error_msg));
        }
    }

    private static class WrongImportInfoAdapter extends ObjectsArrayAdapter<WrongImportInfo> {

        public WrongImportInfoAdapter(Context context, ArrayList<WrongImportInfo> list) {
            super(context, list);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return View.inflate(getContext(), R.layout.dialog_alert_list_item, null);
        }

        @Override
        protected View bindView(View v, int position, WrongImportInfo item) {
            ((TextView) v.findViewById(android.R.id.text1)).setText(item.description);
            ((TextView) v.findViewById(android.R.id.text2)).setText(item.productCode);
            return v;
        }
    }

    private LoaderCallbacks<Integer> itemsCountLoader = new LoaderCallbacks<Integer>() {

        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder
                    .forUri(ITEMS_URI)
                    .projection("count(" + ItemTable.GUID + ")")
                    .wrap(new Function<Cursor, Integer>() {
                        @Override
                        public Integer apply(Cursor c) {
                            if (c.moveToFirst()) {
                                return c.getInt(0);
                            }
                            return 0;
                        }
                    }).build(InventoryActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Integer> integerLoader, Integer value) {
            InventoryActivity.this.itemsCount = value == null ? 0 : value;
        }

        @Override
        public void onLoaderReset(Loader<Integer> integerLoader) {

        }
    };
}