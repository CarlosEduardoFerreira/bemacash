package com.kaching123.tcr.fragment.customer;

import com.kaching123.tcr.model.CustomerModel;

/**
 * Created by vkompaniets on 27.06.2016.
 */
public interface CustomerView {

    void collectDataToModel(CustomerModel model);
    void setFieldsEnabled(boolean enabled);
    boolean validateView();
}
