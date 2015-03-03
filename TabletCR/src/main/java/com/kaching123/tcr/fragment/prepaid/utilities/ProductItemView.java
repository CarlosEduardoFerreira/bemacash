package com.kaching123.tcr.fragment.prepaid.utilities;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

/**
 * Created by teli.yin on 10/31/2014.
 */
@EViewGroup(R.layout.icon_spinner_header_item)
public class ProductItemView extends FrameLayout {

    @ViewById
    protected ImageView icon;

    @ViewById
    protected TextView title;

    public ProductItemView(Context context) {
        super(context);
    }

    public void bind(String text,
                     String url) {
        this.title.setText(text);
//        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
//        RelativeLayout.LayoutParams layout_title = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        layout_title.setMargins((int) (210 * metrics.density), (int) (45 * metrics.density), 0, (int) (40 * metrics.density));
//        this.title.setLayoutParams(layout_title);
//        this.icon.setVisibility(VISIBLE);
        UrlImageViewHelper.setUrlDrawable(icon, url, R.drawable.operator_default_icon, 60000);
    }

}
