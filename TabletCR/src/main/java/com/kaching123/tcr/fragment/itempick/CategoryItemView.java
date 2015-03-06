package com.kaching123.tcr.fragment.itempick;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by vkompaniets on 13.11.13.
 */

@EViewGroup(R.layout.itempick_category_view)
public class CategoryItemView extends FrameLayout {

    @ViewById
    protected TextView title;

    @ViewById
    protected TextView count;

    @ViewById
    protected ImageView arrow;

    private String guid;

    public CategoryItemView(Context context) {
        super(context);
    }

    public void bind(String guid, String title, int itemCount, boolean showCount) {
        this.guid = guid;
        this.title.setText(title);
        if (showCount){
            this.count.setVisibility(View.VISIBLE);
            this.count.setText(getContext().getString(R.string.inventory_categories_item_count, itemCount));
        }else {
            this.count.setVisibility(View.INVISIBLE);
        }
    }

}
