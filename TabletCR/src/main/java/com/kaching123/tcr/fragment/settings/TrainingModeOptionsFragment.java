package com.kaching123.tcr.fragment.settings;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;

/**
 * Created by pkabakov on 07.07.2014.
 */
@EFragment
public class TrainingModeOptionsFragment extends AlertDialogFragment {

    protected static final String DIALOG_NAME = TrainingModeOptionsFragment.class.getSimpleName();

    private TrainingModeOptionsListener trainingModeOptionsListener;

    public void setTrainingModeOptionsListener(TrainingModeOptionsListener trainingModeOptionsListener) {
        this.trainingModeOptionsListener = trainingModeOptionsListener;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.training_mode_options_fragment_button_current;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.training_mode_options_fragment_button_new;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.pref_training_mode_header_title;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (trainingModeOptionsListener != null)
                    trainingModeOptionsListener.onOptionSelected(false);
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (trainingModeOptionsListener != null)
                    trainingModeOptionsListener.onOptionSelected(true);
                return true;
            }
        };
    }

    public static void show(FragmentActivity activity, TrainingModeOptionsListener trainingModeOptionsListener) {
        DialogUtil.show(activity, DIALOG_NAME, TrainingModeOptionsFragment_.builder().dialogType(DialogType.CONFIRM).errorMsg(activity.getString(R.string.training_mode_options_fragment_message)).build()).setTrainingModeOptionsListener(trainingModeOptionsListener);
    }

    public interface TrainingModeOptionsListener {
        void onOptionSelected(boolean shouldCopyToTrainingDatabase);
    }

}
