package com.kaching123.tcr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.composer.ComposerItemListFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by idyuzheva on 04.12.2015.
 */
@EActivity(R.layout.composer_activity)
public class ComposerActivity  extends ScannerBaseActivity implements ComposerItemListFragment.IComposerCallback {

    public static final String RESULT_OK = "RESULT_OK";

    @Extra
    protected String itemGuid;

    @Extra
    protected ItemExModel model;

    @ViewById
    protected LinearLayout holderView;

    @FragmentById
    protected ComposerItemListFragment listFragment;

    //FIXME idyuzheva @ViewById
    //FIXME idyuzheva protected SnackBar snack;

    protected boolean redirectBarcodeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(model.description);
    }

    public static void start(Context context, ItemExModel model, int tag) {
        ComposerActivity_.intent(context).model(model).startForResult(tag);
    }

    @AfterViews
    protected void init() {
        listFragment.setCallback(this);
        listFragment.setModel(model);
    }

    private void showSnack(String msg) {
        //SnackUtils.showSnackClose(snack, self(), msg);
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
            addOrEdit(null, null, barcode);
        }
    }

    protected ComposerActivity self() {
        return this;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_OK, model);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void addOrEdit(final ComposerExModel unit, List<ComposerExModel> composers, String serial) {
        redirectBarcodeResult = true;

        /* //FIXME idyuzhevaComposerEditFragment.show(this, model.guid, unit, composers, new ComposerEditFragment.ComposerCallback() {
            @Override
            public void handleSuccess(ComposerModel parent) {
                hide();
                snack();
            }

            @Override
            public void handleSuccess() {
                hide();
                snack();
            }

            @Override
            public void handleError(String message) {
                //FIXME idyuzheva AlertDialogFragment.showAlert(self(), R.string.item_activity_alert_composer_msg, message);
                showSnack(message);
            }

            @Override
            public void handleCancel() {
                hide();
                showSnack(getString(R.string.composer_cancel_success));
            }

            private void hide() {
                //FIXME idyuzheva ComposerEditFragment.hide(self());
            }

            private void snack() {
                showSnack(unit == null ? getString(R.string.composer_add_success) : getString(R.string.composer_edit_success));
            }
        });*/
    }

    @Override
    public void onAdd(List<ComposerExModel> composers) {
        addOrEdit(null, composers, null);
    }

    @Override
    public void onEdit(ComposerExModel item, List<ComposerExModel> composers) {
        addOrEdit(item, composers, null);
    }

    @Override
    public void onDelete(final List<ComposerExModel> units) {
        if (units.size() == 0) {
            listFragment.hideProgress();
            return;
        }

        /*FIXME idyuzheva RemoveComposerCommand.start(self(), units.remove(0), new RemoveComposerCommand.ComposerCallback() {
            @Override
            protected void handleSuccess() {
                onDelete(units);
                showSnack(getString(R.string.composer_remove_success));
            }

            @Override
            protected void handleError(String message) {
                AlertDialogFragment.showAlert(self(), R.string.item_activity_alert_composer_msg, message);
                showSnack(message);
            }
        });*/
    }
}