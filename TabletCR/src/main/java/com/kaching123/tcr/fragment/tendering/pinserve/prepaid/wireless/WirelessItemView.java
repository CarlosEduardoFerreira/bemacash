package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EViewGroup(R.layout.icon_spinner_item)
public class WirelessItemView extends FrameLayout {

    @ViewById
    protected ImageView icon;

    @ViewById
    protected TextView title;

    public WirelessItemView(Context context) {
        super(context);
    }

//    public void bind(String text,
//                     String url) {
//        this.title.setText(text);
//        RelativeLayout.LayoutParams layout_title =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//        layout_title.setMargins(210,45,0,40);
//        this.title.setLayoutParams(layout_title);
//        this.icon.setVisibility(VISIBLE);
//        UrlImageViewHelper.setUrlDrawable(icon, url, R.drawable.operator_default_icon, 60000);
//    }

    public void bindTextOnly(String text) {
        this.title.setText(text);
//        RelativeLayout.LayoutParams layout_title =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//        layout_title.setMargins(10,45,0,0);
//        this.title.setLayoutParams(layout_title);
//        this.icon.setVisibility(INVISIBLE);
    }


}