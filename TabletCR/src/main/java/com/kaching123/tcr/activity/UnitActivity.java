package com.kaching123.tcr.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.AddUnitsCommand;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.fragment.wireless.UnitItemListFragment;
import com.kaching123.tcr.fragment.wireless.UnitsEditFragment;
import com.kaching123.tcr.fragment.wireless.UnitsRecursiveAddFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;

import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
@EActivity(R.layout.unit_activity)
@OptionsMenu(R.menu.unit_activity)
public class UnitActivity extends ScannerBaseActivity implements UnitItemListFragment.IUnitCallback {

    public static final String RESULT_OK = "RESULT_OK";

    @Extra
    protected String itemGuid;

    @Extra
    protected ItemExModel model;

    @FragmentById
    protected UnitItemListFragment listFragment;

    protected boolean redirectBarcodeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(self(), R.array.unit_action_list, R.layout.wireless_spinner_dropdown_item);
        getActionBar().setListNavigationCallbacks(spinnerAdapter, navigationListener);
        setTitle(model.description);
    }

    public static void start(Context context, ItemExModel model, int tag) {
        UnitActivity_.intent(context).model(model).startForResult(tag);
    }

    @AfterViews
    protected void init() {
        listFragment.setCallback(this);
        listFragment.setModel(model);
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        if (redirectBarcodeResult) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(1);
            if (fragment != null && fragment instanceof BarcodeReceiver) {
                BarcodeReceiver editFragment = (BarcodeReceiver)fragment;
                editFragment.onBarcodeReceived(barcode);
            }
        } else {
            addRemoveOrEdit(null, barcode);
        }
    }

    ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {
            if (listFragment == null) {
                return true;
            }
            if (position == 0) {
                listFragment.setStatus(null);
            } else {
                listFragment.setStatus(Unit.Status.valueOf(position - 1));
            }
            return true;
        }
    };



    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_OK, model);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void addRemoveOrEdit(Unit unit, String serial) {

        redirectBarcodeResult = true;
        UnitsEditFragment.show(self(), model, unit, model.codeType, serial, new UnitsEditFragment.UnitCallback() {
            @Override
            public void handleSuccess(final boolean add, final ItemExModel parent) {
                hide();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        self().model = parent;
                    }
                });
                Toast.makeText(self(), getString(R.string.unit_completed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleSuccess() {
                hide();
                Toast.makeText(self(), getString(R.string.unit_edit_completed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleError(String message) {
                Toast.makeText(self(), message, Toast.LENGTH_LONG).show();
                hide();
            }

            @Override
            public void handleCancel() {
                redirectBarcodeResult = false;
            }

            @Override
            public void handleScannedSwitch(boolean on) {
                if (on) {
                    tryReconnectScanner();
                } else {
                    disconnectScanner();
                }
            }

            private void hide() {
                redirectBarcodeResult = false;
                UnitsEditFragment.hide(self());
            }

        });
    }

    @Override
    public void onAdd() {
        addRemoveOrEdit(null, null);
    }

    @Override
    public void onMultiAdd() {
        redirectBarcodeResult = true;
        UnitsRecursiveAddFragment.show(self(), model, null, model.codeType, new UnitsRecursiveAddFragment.UnitCallback() {
            @Override
            public void handleSuccess(ItemExModel parent) {
                self().model = parent;
                hide();
            }

            @Override
            public void handleError(String message) {
                hide();
                Toast.makeText(self(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void turnScanner(boolean on) {
                if (on) {
                    tryReconnectScanner();
                } else {
                    disconnectScanner();
                }
            }

            private void hide() {
                redirectBarcodeResult = false;
                UnitsRecursiveAddFragment.hide(self());
            }
        });
    }

    @Override
    public void onEdit(Unit item) {
        addRemoveOrEdit(item, null);
    }

    @Override
    public void onDelete(final List<Unit> units) {
        if (units.size() == 0) {
            listFragment.hideProgress();
            return;
        }
        Unit unit = units.remove(0);
        AddUnitsCommand.start(self(), false, unit, model, new AddUnitsCommand.UnitCallback() {
            @Override
            protected void handleSuccess(ItemExModel model) {
                self().model = model;
                onDelete(units);
            }

            @Override
            protected void handleError(String message) {
                onDelete(units);
            }
        });
    }

    private UnitActivity self() {
        return this;
    }
}