package com.kaching123.tcr.fragment.item;

import android.content.Context;

import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment
public abstract class ItemBaseFragment extends SuperBaseFragment implements ItemView{

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getActivity() instanceof ItemProvider))
            throw new IllegalStateException("host should implement ItemProvider interface");
    }

    protected ItemExModel getModel(){
        return ((ItemProvider) getActivity()).getModel();
    }

    protected ItemProvider getItemProvider(){
        return (ItemProvider) getActivity();
    }

    @AfterViews
    protected void init(){
        setViews();
        if (!getItemProvider().isCreate()){
            setModel();
        }
    }

    protected abstract void setViews();
    protected abstract void setModel();
}
