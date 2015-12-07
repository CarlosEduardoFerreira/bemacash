package com.kaching123.tcr.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.unitlabel.RemoveUnitLabelCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.unitlabel.UnitLabelAddEditDialog;
import com.kaching123.tcr.fragment.unitlabel.UnitLabelListFragment;
import com.kaching123.tcr.model.UnitLabelModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

import java.util.List;

/**
 * Created by alboyko 07.12.2015
 */

@EActivity(R.layout.unit_labels_activity)
public class UnitLabelActivity extends SuperBaseActivity implements UnitLabelListFragment.IUnitLabelCallback {

    @FragmentById
    protected UnitLabelListFragment listFragment;

    public static void start(Context context) {
        UnitLabelActivity_.intent(context).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void init() {
        listFragment.setCallback(self());
    }

    @Override
    protected UnitLabelActivity self() {
        return this;
    }

    @Override
    public void onAdd() {
        UnitLabelAddEditDialog.show(
                self(),
                null,
                new UnitLabelAddEditDialog.UnitLabelCallback() {

                    @Override
                    public void handleSuccess() {
                        Toast.makeText(self(), R.string.unit_label_add_completed, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void handleError(String message) {
                        showErrorDialog(message);
                    }
                });
    }

    @Override
    public void onEdit(UnitLabelModel item) {
        UnitLabelAddEditDialog.show(
                self(),
                item,
                new UnitLabelAddEditDialog.UnitLabelCallback() {

                    @Override
                    public void handleSuccess() {
                        Toast.makeText(self(), R.string.unit_label_edit_completed, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void handleError(String message) {
                        showErrorDialog(message);
                    }
            });
    }

    @Override
    public void onDelete(final List<UnitLabelModel> items) {
        if (items.size() == 0) {
            listFragment.hideProgress();
            return;
        }
        UnitLabelModel unit = items.remove(0);
        RemoveUnitLabelCommand.start(self(), unit, new RemoveUnitLabelCommand.UnitLabelCallback() {
            @Override
            protected void handleSuccess() {
                onDelete(items);
            }

            @Override
            protected void handleError() {
                onDelete(items);
            }
        });
    }

    private void showErrorDialog(String message) {
        AlertDialogFragment.showAlert(self(),
                R.string.unit_label_shortcut_already_exists_dialog_title,
                message);
    }

}










