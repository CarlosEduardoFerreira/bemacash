package com.kaching123.tcr.fragment.itempick;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.googlecode.androidannotations.annotations.EBean;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryView;
import com.kaching123.tcr.util.ColumnIndexHolder;

/**
 * Created by vkompaniets on 12.11.13.
 */

@EBean
public class CategoriesAdapter extends CursorAdapter {

    private ColumnIndexHolder indexHolder = new ColumnIndexHolder();

    public CategoriesAdapter (Context context){
        super (context, null, false);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return CategoryItemView_.build(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CategoryItemView categoryItemView = (CategoryItemView) view;
        categoryItemView.bind(
                cursor.getString(indexHolder.get(CategoryTable.GUID)),
                cursor.getString(indexHolder.get(CategoryTable.TITLE)),
                cursor.getInt(indexHolder.get(CategoryView.ITEM_COUNT)),
                true);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        indexHolder.update(cursor);
        super.changeCursor(cursor);
    }

    public String getCategoryGuid (int pos){
        Cursor c = (Cursor)getItem(pos);
        return c.getString(indexHolder.get(ShopStore.CategoryTable.GUID));
    }
}
