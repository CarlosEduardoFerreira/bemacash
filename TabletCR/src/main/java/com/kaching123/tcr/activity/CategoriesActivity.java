package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.commands.store.inventory.AddCategoryCommand.BaseAddCategoryCallback;
import com.kaching123.tcr.commands.store.inventory.DeleteCategoryCommand;
import com.kaching123.tcr.commands.store.inventory.UpdateCategoryOrderCommand;
import com.kaching123.tcr.fragment.categories.CategoriesDialog;
import com.kaching123.tcr.fragment.categories.CategoryDragItemView;
import com.kaching123.tcr.fragment.categories.CategoryDragItemView_;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CategorySimpleView2;
import com.kaching123.tcr.store.ShopStore.CategorySimpleView;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by pkabakov on 13.12.13.
 */
@EActivity(R.layout.categories_activity)
@OptionsMenu (R.menu.categories_activity)
public class CategoriesActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.INVENTORY_MODULE);
    }

    private static final Uri URI_CATEGORIES = ShopProvider.getContentUri(CategorySimpleView.URI_CONTENT);

    @ViewById
    protected DragSortListView list;

    private CategoriesLoader loader = new CategoriesLoader();

    private CategoriesAdapter adapter;

    private BaseAddCategoryCallback callback = new BaseAddCategoryCallback() {
        @Override
        protected void onCategoryAddedSuccess() {
            adapter.updateCategoryOrder();
        }
    };


    @Override
    protected HashSet<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void initViews() {
        adapter = new CategoriesAdapter(this);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CategoryModel model = (CategoryModel) adapterView.getItemAtPosition(i);
                CategoriesDialog.show(CategoriesActivity.this, model, null);
            }
        });

        getSupportLoaderManager().restartLoader(0, null, loader);
    }

    @OptionsItem
    protected void actionAddSelected(){
        CategoriesDialog.show(this, null, callback);
    }

    private class CategoriesAdapter extends ObjectsArrayAdapter<CategoryModel> implements DragSortListView.DropListener, DragSortListView.RemoveListener {

        public CategoriesAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return CategoryDragItemView_.build(getContext());
        }

        @Override
        protected View bindView(View convertView, int position, CategoryModel item) {
            CategoryDragItemView categoryItemView = (CategoryDragItemView) convertView;
            categoryItemView.bind(item.guid,item.title);
            return convertView;
        }

        @Override
        public void drop(int from, int to) {
            CategoryModel dropItem = getItem(from);
            remove(dropItem);
            insert(dropItem, to);

            updateCategoryOrder();
        }

        public void updateCategoryOrder() {
            String[] orderNumbers = new String[getCount()];
            for (int i = 0; i < getCount(); i++) {
                CategoryModel item = getItem(i);
                orderNumbers[i] = item.guid;
            }

            UpdateCategoryOrderCommand.start(getContext(), orderNumbers);
        }

        @Override
        public void remove(int i) {
            handleRemove(getItem(i));
        }

    }

    private void handleRemove(final CategoryModel model) {
        AlertDialogWithCancelListener.show(CategoriesActivity.this, R.string.categories_delete_dialog_title, String.format(Locale.US, getString(R.string.categories_delete_dialog_message), model.title), R.string.btn_confirm, new StyledDialogFragment.OnDialogClickListener() {
            @Override
            public boolean onClick() {
                try2DeleteCategory(model);
                return true;
            }
        }, adapter);


    }

    private void try2DeleteCategory(CategoryModel model) {
        WaitDialogFragment.show(CategoriesActivity.this, getString(R.string.search_items_wait_dialog_message));
        DeleteCategoryCommand.start(this, model, new DeleteCategoryCommand.DeleteCategoryCommandCallback() {
            @Override
            protected void onCategoryDeleted(String categoryName) {
                WaitDialogFragment.hide(CategoriesActivity.this);
                Toast.makeText(CategoriesActivity.this, String.format(Locale.US, getString(R.string.categories_toast_success_message), categoryName), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onCategoryHasItems(String categoryName, int itemsCount) {
                WaitDialogFragment.hide(CategoriesActivity.this);
                Toast.makeText(CategoriesActivity.this, String.format(Locale.US, getString(R.string.categories_toast_failed_message), categoryName, itemsCount), Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class CategoriesLoader implements LoaderManager.LoaderCallbacks<List<CategoryModel>> {

        @Override
        public Loader<List<CategoryModel>> onCreateLoader(int arg0, Bundle arg1) {
            return CursorLoaderBuilder.forUri(URI_CATEGORIES)
                    .orderBy(CategorySimpleView2.CategoryTable.ORDER_NUM + ", " + CategorySimpleView2.CategoryTable.TITLE)
                    .transformRow(new CategoryConverter())
                    .build(CategoriesActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<CategoryModel>> loader, List<CategoryModel> categoryModelList) {
            adapter.changeCursor(categoryModelList);
        }

        @Override
        public void onLoaderReset(Loader<List<CategoryModel>> loader) {
            adapter.changeCursor(null);
        }
    }

    private static class CategoryConverter extends ListConverterFunction<CategoryModel> {

        @Override
        public CategoryModel apply(Cursor c) {
            super.apply(c);
            return new CategoryModel(
                    c.getString(indexHolder.get(CategorySimpleView2.CategoryTable.GUID)),
                    c.getString(indexHolder.get(CategorySimpleView2.CategoryTable.DEPARTMENT_GUID)),
                    c.getString(indexHolder.get(CategorySimpleView2.CategoryTable.TITLE)),
                    c.getString(indexHolder.get(CategorySimpleView2.CategoryTable.IMAGE)),
                    c.getInt(indexHolder.get(CategorySimpleView2.CategoryTable.ORDER_NUM)),
                    _bool(c, c.getColumnIndex(CategorySimpleView2.CategoryTable.ELIGIBLE_FOR_COMMISSION)),
                    _decimal(c, c.getColumnIndex(CategorySimpleView2.CategoryTable.COMMISSION), BigDecimal.ZERO),
                    null
            );
        }

    }

    public static void start(Context context){
        CategoriesActivity_.intent(context).start();
    }

}

