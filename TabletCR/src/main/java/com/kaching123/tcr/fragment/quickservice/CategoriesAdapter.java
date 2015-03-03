package com.kaching123.tcr.fragment.quickservice;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import org.androidannotations.annotations.EBean;
import com.kaching123.tcr.store.ShopSchema2.CategoryView2.CategoryTable;
import com.kaching123.tcr.util.ColumnIndexHolder;

@EBean
public class CategoriesAdapter extends CursorAdapter{

    private ColumnIndexHolder indexHolder = new ColumnIndexHolder();

    public CategoriesAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return QuickCategoryItemView_.build(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        QuickCategoryItemView categoryItemView = (QuickCategoryItemView) view;
        categoryItemView.bind(
                cursor.getString(indexHolder.get(CategoryTable.GUID)),
                cursor.getString(indexHolder.get(CategoryTable.TITLE)),
                getImageName(cursor)
        );
    }

    @Override
    public void changeCursor(Cursor cursor) {
        indexHolder.update(cursor);
        super.changeCursor(cursor);

    }

    private String getImageName(Cursor cursor){
        return cursor.getString(indexHolder.get(CategoryTable.IMAGE));
    }

    public String getCategoryGuid(int pos) {
        Cursor c = (Cursor) getItem(pos);
        return c.getString(indexHolder.get(CategoryTable.GUID));
    }
}
