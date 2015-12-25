package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddItemCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.inventory.ItemCodeChooserAlertDialogFragment;
import com.kaching123.tcr.function.NextProductCodeQuery;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by vkompaniets on 05.12.13.
 */

@EActivity(R.layout.inventory_item_activity)
@OptionsMenu(R.menu.items_actions)
public class AddItemActivity extends BaseItemActivity {

    @AfterViews
    @Override
    protected void init() {
        super.init();
        if (!TextUtils.isEmpty(model.tmpBarcode)) {
            ItemCodeChooserAlertDialogFragment.show(AddItemActivity.this, filterBarcodes(model.tmpBarcode));
        }
        updateStockTrackingBlock(false);
        new GetNextProductCodeTask().execute();
        setFieldsChangeListeners();
    }

    private String filterBarcodes(String barcodes) {
        return barcodes.replace("-","").replace("#","");
    }

    @Override
    protected void onSerializableSet(boolean isSerializable) {
        if (isSerializable) {
            availableQty.setFocusable(false);
            availableQty.setFocusableInTouchMode(false);
            availableQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialogFragment.showAlert(AddItemActivity.this, R.string.new_item_warning_dialog_title, "Item needs to be saved prior to quantity adjustment.");
                }
            });
        } else {
            availableQty.setFocusable(true);
            availableQty.setFocusableInTouchMode(true);
            availableQty.setOnClickListener(null);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_remove).setVisible(false);
        menu.findItem(R.id.action_serial).setVisible(model.isSerializable());
        menu.findItem(R.id.action_serial).setEnabled(false);
        composer.setVisible(false);
        modifier.setVisible(false);
        return true;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        super.onLoadFinished(cursorLoader, cursor);
        switch (cursorLoader.getId()) {
            case DEPARTMENT_LOADER_ID:
                department.setSelection(departmentAdapter.getPosition4Id(model.departmentGuid));
                break;
            case CATEGORY_LOADER_ID:
                category.setSelection(categoryAdapter.getPosition4Id(model.categoryId));
                break;
        }
    }

    @Override
    protected void callCommand(ItemModel model) {
        AddItemCommand.start(AddItemActivity.this, model);
    }

    public static void start(Context context, String barcode) {
        ItemExModel item = new ItemExModel();
        item.tmpBarcode = barcode;
        start(context, item);
    }

    public static void start(Context context, ItemExModel item) {
        AddItemActivity_.intent(context).model(item).start();
    }

    class GetNextProductCodeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return NextProductCodeQuery.getCode(AddItemActivity.this);
        }

        @Override
        protected void onPostExecute(String code) {
            productCode.setText(code);
        }
    }

    private class ProductCodeLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .projection(ShopStore.ItemTable.GUID)
                    .where(ShopStore.ItemTable.PRODUCT_CODE + " = ?", productCode.getText().toString())
                    .where(ShopStore.ItemTable.GUID + " <> ?", model.guid)
                    .build(AddItemActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            duplicateProductCode = cursor.getCount() > 0;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }

}
