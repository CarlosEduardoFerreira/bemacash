package com.kaching123.tcr.fragment.categories;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

@EViewGroup(R.layout.categories_item_drag_view)
public class CategoryDragItemView extends FrameLayout {
    @ViewById
    protected TextView title;
    @ViewById
    protected ImageView drag;
    private String guid;

    public CategoryDragItemView(Context context) {
        super(context);
    }

    public void bind(String guid, String title) {
        this.guid = guid;
        this.title.setText(title);
    }
}