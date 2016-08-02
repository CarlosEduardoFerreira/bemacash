package com.kaching123.tcr.fragment.loyalty;

import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveExModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 05.07.2016.
 */
@EFragment
public class LoyaltyFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = LoyaltyFragmentDialog.class.getSimpleName();

    @ViewById protected ViewGroup content;
    @ViewById protected TextView availableLabel;
    @ViewById protected TextView typeLabel;
    @ViewById protected TextView rewardLabel;

    @FragmentArg
    protected String customerGuid;

    @FragmentArg
    protected IncentiveExModel incentive;

    private LoyaltyDialogListener listener;

    public void setListener(LoyaltyDialogListener listener) {
        this.listener = listener;
    }

    @AfterViews
    protected void init(){
        setCancelable(false);

        typeLabel.setText(getString(R.string.loyalty_incentive_dialog_type_label, getString(incentive.type.getLabel())));
        rewardLabel.setText(getString(R.string.loyalty_incentive_dialog_reward_label, incentive.name));
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.loyalty_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.loyalty_incentive_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_no;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_yes;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null)
                    listener.onApplyRequested(incentive);
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null)
                    listener.onSkipRequested(incentive);
                return true;
            }
        };
    }


    public static void show(FragmentActivity activity,IncentiveExModel incentive, LoyaltyDialogListener listener){
        DialogUtil.show(activity, DIALOG_NAME, LoyaltyFragmentDialog_.builder().incentive(incentive).build()).setListener(listener);
    }

    public interface LoyaltyDialogListener{
        void onApplyRequested(IncentiveExModel incentive);
        void onSkipRequested(IncentiveExModel incentive);
    }
}
