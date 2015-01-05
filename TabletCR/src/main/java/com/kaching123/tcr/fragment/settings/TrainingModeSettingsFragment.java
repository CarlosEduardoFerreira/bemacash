package com.kaching123.tcr.fragment.settings;

import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.store.settings.SetTrainingModeCommand;
import com.kaching123.tcr.commands.store.settings.SetTrainingModeCommand.SetTrainingModeBaseCallback;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.Permission;

@EFragment(R.layout.settings_training_mode_fragment)
@OptionsMenu(R.menu.settings_training_mode_fragment)
public class TrainingModeSettingsFragment extends SuperBaseFragment {

    @ViewById
    protected TextView modeValue;

    @AfterViews
    protected void initViews() {
        showCurrentModeValue();
    }

    private void showCurrentModeValue() {
        boolean isTrainingMode = getApp().isTrainingMode();
        modeValue.setText(isTrainingMode ? R.string.training_mode_on_label : R.string.training_mode_off_label);
    }

    @OptionsItem
    protected void actionTrainingModeSelected(){
        boolean permitted = TcrApplication.get().hasPermission(Permission.TRAINING_MODE);
        if (!permitted) {
            PermissionFragment.showCancelable(getActivity(), new SuperBaseActivity.BaseTempLoginListener(getActivity()) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    actionTrainingModeSelected();
                }
            }, Permission.TRAINING_MODE);
            return;
        }

        TrainingModeSelectionFragment.show(getActivity(), new TrainingModeSelectionFragment.TrainingModeSelectionListener() {
            @Override
            public void onSelected(final boolean isTrainingMode) {
                boolean isTrainingModeCurrent = getApp().isTrainingMode();
                if (isTrainingMode == isTrainingModeCurrent)
                    return;

                if (isTrainingMode) {
                    AlertDialogFragment.showConfirmation(getActivity(), R.string.training_mode_selection_fragment_warning_title, getString(R.string.training_mode_selection_fragment_warning_message), new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            setTrainingMode(isTrainingMode);
                            return true;
                        }
                    });
                    return;
                }

                setTrainingMode(isTrainingMode);
            }
        });
    }

    private void setTrainingMode(final boolean isTrainingMode) {
        setTrainingMode(isTrainingMode, null);
    }

    private void setTrainingMode(final boolean isTrainingMode, Boolean shouldCopyToTrainingDatabase) {
        if (isTrainingMode && shouldCopyToTrainingDatabase == null) {
            TrainingModeOptionsFragment.show(getActivity(), new TrainingModeOptionsFragment.TrainingModeOptionsListener() {
                @Override
                public void onOptionSelected(boolean shouldCopyToTrainingDatabase) {
                    setTrainingMode(isTrainingMode, shouldCopyToTrainingDatabase);
                }
            });
            return;
        }

        SyncWaitDialogFragment.show(getActivity(), getString(R.string.loading_message));
        new SetTrainingModeCommand().start(getActivity(), isTrainingMode, shouldCopyToTrainingDatabase == null ? false : shouldCopyToTrainingDatabase, trainingModeCallback);
    }

    private SetTrainingModeBaseCallback trainingModeCallback = new SetTrainingModeBaseCallback() {
        @Override
        protected void handleSuccess() {
            if (getActivity() == null)
                return;

            if (getApp().isTipsEnabledWasChanged()) {
                getApp().setTipsEnabledWasChanged(false);
                Toast.makeText(getActivity(), getApp().isTipsEnabled() ? R.string.warning_message_tips_enabled : R.string.warning_message_tips_disabled, Toast.LENGTH_LONG).show();
            }

            SyncWaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showComplete(getActivity(), R.string.training_mode_confirm_title, getString(R.string.training_mode_confirm_message));
            showCurrentModeValue();
        }

        @Override
        protected void handleError() {
            if (getActivity() == null)
                return;

            SyncWaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.training_mode_error_title, getString(R.string.training_mode_error_message));
            showCurrentModeValue();
        }
    };

    public static Fragment instance() {
        return TrainingModeSettingsFragment_.builder().build();
    }
}
