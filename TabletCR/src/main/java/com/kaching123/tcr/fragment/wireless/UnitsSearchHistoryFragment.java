package com.kaching123.tcr.fragment.wireless;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.SearchUnitCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.Unit;

import java.util.ArrayList;

/**
 * Created by mayer
 */
@EFragment
public class UnitsSearchHistoryFragment extends UnitsSearchFragment {


    protected boolean additionalCheckPassed() {
        return selectedStatus != null;
    }

    protected ArrayAdapter adapter;
    protected Unit.Status selectedStatus;

    @FragmentArg
    protected String orderId;

    @FragmentArg
    protected String itemId;

    @ViewById
    protected Spinner status;

    private static final String DIALOG_NAME = "UnitsSearchHistoryFragment";

    @Override
    protected boolean onSubmitForm() {
        final String serialCode = etSerial.getText().toString();
        SearchUnitCommand.start(getActivity(), serialCode, itemId, orderId, new SearchUnitCommand.UnitCallback() {

            @Override
            protected void handleSuccess(ArrayList<Unit> units, ArrayList<SaleOrderViewModel> order) {
                units.get(0).status = selectedStatus;
                callback.handleSuccess(serialCode, units, order);
            }

            @Override
            protected void handleError(String message) {
                callback.handleError(serialCode, message);
            }
        });
        return true;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.search_unit_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_unit_history_search;
    }

    @Override
    protected void configureUI() {
        super.configureUI();
        adapter = new ArrayAdapter<Unit.Status>(getActivity(), android.R.layout.simple_spinner_item);
        adapter.add(Unit.Status.NEW);
        adapter.add(Unit.Status.USED);
        adapter.add(Unit.Status.BROKEN);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        status.setAdapter(adapter);
        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = (Unit.Status) adapter.getItem(position);
                checkPositiveButtonCondition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStatus = null;
                checkPositiveButtonCondition();
            }
        });
    }

    public static void show(FragmentActivity activity, String orderId, String itemId, UnitCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, UnitsSearchHistoryFragment_
                .builder().orderId(orderId).itemId(itemId).build()).setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
