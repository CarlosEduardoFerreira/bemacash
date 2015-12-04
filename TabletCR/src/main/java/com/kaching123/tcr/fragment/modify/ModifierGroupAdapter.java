package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.kaching123.tcr.fragment.itempick.CategoryItemView;
import com.kaching123.tcr.fragment.itempick.CategoryItemView_;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ColumnIndexHolder;

import org.androidannotations.annotations.EBean;

/**
 * Created by  alboyko on 02.12.2015.
 */

@EBean
public class ModifierGroupAdapter extends CursorAdapter {

    private ColumnIndexHolder indexHolder = new ColumnIndexHolder();

    public ModifierGroupAdapter(Context context){
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
                cursor.getString(indexHolder.get(ShopSchema2.ModifierGroupView2.GroupTable.GUID)),
                cursor.getString(indexHolder.get(ShopSchema2.ModifierGroupView2.GroupTable.TITLE)),
                cursor.getInt(indexHolder.get(ShopStore.ModifierGroupView.ITEM_COUNT)),
                true);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        indexHolder.update(cursor);
        super.changeCursor(cursor);
    }
}