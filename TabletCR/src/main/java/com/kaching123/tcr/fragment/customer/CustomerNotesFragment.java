package com.kaching123.tcr.fragment.customer;

import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PlanOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 27.06.2016.
 */
@EFragment(R.layout.customer_notes_fragment)
public class CustomerNotesFragment extends CustomerBaseFragment implements CustomerView{

    @ViewById protected EditText notes;

    @Override
    @AfterViews
    protected void init(){
        super.init();
    }

    @Override
    protected void setViews() {
        super.setViews();
        setFieldsEnabled(PlanOptions.isEditingCustomersAllowed());
    }

    @Override
    protected void setCustomer() {
        CustomerModel model = getCustomer();
        notes.setText(model.notes);
    }

    @Override
    public void collectDataToModel(CustomerModel model) {
        model.notes = notes.getText().toString();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {
        notes.setEnabled(enabled);
    }

    @Override
    public boolean validateView() {
        return true;
    }
}
