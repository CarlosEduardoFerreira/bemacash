package com.kaching123.tcr.fragment.customer;

import android.content.Context;

import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.model.CustomerModel;

/**
 * Created by vkompaniets on 27.06.2016.
 */
public abstract class CustomerBaseFragment extends SuperBaseFragment{

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getActivity() instanceof CustomerProvider))
            throw new IllegalStateException("host should implement CustomerProvider interface");
    }

    protected CustomerModel getCustomer(){
        return ((CustomerProvider) getActivity()).getCustomer();
    }

    protected void init(){
        setViews();
        if (getCustomer() != null)
            setCustomer();
    }

    protected void setViews() {}
    protected void setCustomer() {}
}
