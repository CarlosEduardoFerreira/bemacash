package com.kaching123.tcr.fragment.detailedpick;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup (R.layout.detailed_qservice_category_view)
public class DetailedQServiceCategoryItemView extends FrameLayout {

    @ViewById
    protected TextView title;

    @ViewById
    protected ImageView image;

    public DetailedQServiceCategoryItemView(Context context) {
        super(context);
    }

    public void bind(String guid, String title, String imageName) {
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
