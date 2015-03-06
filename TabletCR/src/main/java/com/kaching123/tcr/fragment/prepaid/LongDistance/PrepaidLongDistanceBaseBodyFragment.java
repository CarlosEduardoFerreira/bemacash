package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.EFragment;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceBaseBodyFragment extends Fragment {

    closeLayoutCallback callback;
    public interface closeLayoutCallback{
        void pageSelected(int position);
    }


}
