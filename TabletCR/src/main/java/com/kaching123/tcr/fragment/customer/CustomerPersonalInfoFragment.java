package com.kaching123.tcr.fragment.customer;

import android.support.v4.app.Fragment;
import android.widget.EditText;

import com.kaching123.tcr.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EFragment(R.layout.customer_personal_info_fragment)
public class CustomerPersonalInfoFragment extends Fragment {

    @ViewById protected EditText firstName;
    @ViewById protected EditText lastName;
    @ViewById protected EditText email;

}
