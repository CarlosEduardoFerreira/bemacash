package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EViewGroup(R.layout.icon_spinner_header_item)
public class WirelessItemHeaderView extends FrameLayout {

    @ViewById
    protected ImageView icon;

    @ViewById
    protected TextView title;

    public WirelessItemHeaderView(Context context) {
        super(context);
    }

    public void bind(String text,
                     String url) {
        this.title.setText(text);
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        RelativeLayout.LayoutParams layout_title =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layout_title.setMargins((int)(210*metrics.density),(int)(45*metrics.density),0,(int)(40*metrics.density));
        this.title.setLayoutParams(layout_title);
        this.icon.setVisibility(VISIBLE);
        UrlImageViewHelper.setUrlDrawable(icon, url, R.drawable.operator_default_icon, 60000);
    }

    public void bindTextOnly(String text) {
        this.title.setText(text);
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        RelativeLayout.LayoutParams layout_title =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layout_title.setMargins((int)(10*metrics.density),(int)(45*metrics.density),0,0);
        this.title.setLayoutParams(layout_title);
        this.icon.setVisibility(INVISIBLE);
    }


}