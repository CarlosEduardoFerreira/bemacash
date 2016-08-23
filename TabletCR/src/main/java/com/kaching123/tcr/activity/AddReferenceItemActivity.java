package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.widget.ImageButton;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.inventory.AddReferenceItemCommand;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemRefType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by aakimov on 27/04/15.
 */

@EActivity(R.layout.inventory_reference_item_activity)
@OptionsMenu(R.menu.items_actions)
public class AddReferenceItemActivity extends BaseReferenceItemActivity {

    @ViewById
    protected ImageButton variantsChange;

    @AfterViews
    @Override
    protected void init() {
        super.init();
        variantsChange.setEnabled(false);
        setFieldsChangeListeners();
    }

    @Override
    protected void callCommand(ItemModel model, final AddItemCallBack callBack) {
        AddReferenceItemCommand.start(this, model, new AddReferenceItemCommand.AddReferenceItemCommandCallback()
        {

            @Override
            protected void handleSuccess() {
                callBack.success();
            }

            @Override
            protected void handleFailure() {

            }
        });
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_remove).setVisible(false);
        menu.findItem(R.id.action_serial).setVisible(false);
        menu.findItem(R.id.action_composer).setVisible(false);
        modifier.setVisible(false);
        return true;
    }

    public static void start(Context context, ItemExModel item) {
        AddReferenceItemActivity_.intent(context).model(item).start();
    }

    @Override
    protected void collectDataToModel(ItemModel model) {
        model.refType = ItemRefType.Reference;
        model.isStockTracking = false;
        super.collectDataToModel(model);
    }
}
