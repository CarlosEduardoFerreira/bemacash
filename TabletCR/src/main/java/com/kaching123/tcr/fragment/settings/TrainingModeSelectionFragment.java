package com.kaching123.tcr.fragment.settings;

import android.support.v4.app.FragmentActivity;
import android.widget.RadioButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by pkabakov on 07.07.2014.
 */
@EFragment
public class TrainingModeSelectionFragment extends StyledDialogFragment {

    protected static final String DIALOG_NAME = TrainingModeSelectionFragment.class.getSimpleName();

    @ViewById
    protected RadioButton offButton;
    @ViewById
    protected RadioButton onButton;

    private TrainingModeSelectionListener trainingModeSelectionListener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_training_mode_selection_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.pref_training_mode_header_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @AfterViews
    protected void initViews() {
        boolean isTrainingMode = getApp().isTrainingMode();
        if (isTrainingMode)
            onButton.setChecked(true);
        else
            offButton.setChecked(true);
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onTrainingModeSelected();
                return true;
            }
        };
    }

    private void onTrainingModeSelected() {
        boolean isTrainingMode = onButton.isChecked();

        if (trainingModeSelectionListener != null)
            trainingModeSelectionListener.onSelected(isTrainingMode);
    }

    public TrainingModeSelectionFragment setTrainingModeSelectionListener(TrainingModeSelectionListener trainingModeSelectionListener) {
        this.trainingModeSelectionListener = trainingModeSelectionListener;
        return this;
    }

    public static void show(FragmentActivity activity, TrainingModeSelectionListener trainingModeSelectionListener) {
        DialogUtil.show(activity, DIALOG_NAME, TrainingModeSelectionFragment_.builder().build()).setTrainingModeSelectionListener(trainingModeSelectionListener);
    }

    public interface TrainingModeSelectionListener {
        void onSelected(boolean isTrainingMode);
    }

}
