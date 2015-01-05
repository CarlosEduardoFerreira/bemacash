package com.kaching123.tcr.fragment.prepaid.SunPass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrepaidActivity.PrepaidSunPassActivity;

/**
 * Created by teli.yin on 12/4/2014.
 */
@EFragment
public class PrepaidSunPassCategoryFragment extends Fragment implements PrepaidSunPassActivity.PrepaidSunPassInterface {

    public static final int ACTIVE_OR_REPLENISH_SUNPASS_TRANSPONDER = 0 ;
    public static final int PAY_YOUR_DOCUMENT = 1 ;

    @ViewById
    protected ImageView transponder;
    @ViewById
    protected ImageView payYourDocument;

    private SunPassCategoryCallback categoryCallback;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sunpass_category_choose, container, false);
    }

    @AfterViews
    protected void init()
    {

    }

    @Click
    protected void transponder(){
        categoryCallback.onCategoryChosen(ACTIVE_OR_REPLENISH_SUNPASS_TRANSPONDER);
    }

    @Click
    protected void payYourDocument(){
        categoryCallback.onCategoryChosen(PAY_YOUR_DOCUMENT);
    }

    public void setCallBack(SunPassCategoryCallback callBack)
    {
        this.categoryCallback = callBack;
    }

    @Override
    public void onBackPressed() {
        categoryCallback.onBackButtonPressed();
    }

    public interface SunPassCategoryCallback{
        void onBackButtonPressed();

        void onCategoryChosen(int mode);
    }
}
