package com.kaching123.tcr.fragment.quickservice;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 25.11.13.
 */

@EViewGroup (R.layout.quickservice_category_view)
public class QuickCategoryItemView extends FrameLayout {

    @ViewById
    protected ImageView image;

    @ViewById
    protected TextView title;

    private String guid;

    public QuickCategoryItemView(Context context) {
        super(context);
    }

    public QuickCategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuickCategoryItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bind(String guid, String title, String imageName){
        this.guid = guid;
        this.title.setText(title);

        Integer level = getLevel(imageName);
        if (level != null){
            image.setImageDrawable(null);
            image.getBackground().setLevel(level);
        }else{
            int drawableResourceId = 0;
            if(!TextUtils.isEmpty(imageName)){
                drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
            }
            this.image.setImageResource(drawableResourceId == 0 ? R.drawable.categories_placeholder : drawableResourceId);
            image.getBackground().setLevel(0);
        }
    }

    private static Integer getLevel(String str){
        try{
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe){
            return null;
        }
    }

}
