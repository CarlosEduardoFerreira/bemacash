package com.kaching123.tcr.fragment.tendering;

import android.os.Bundle;
import android.widget.CheckBox;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 26.12.13.
 */
@EFragment
public abstract class PrintAndFinishFragmentDialogBase extends StyledDialogFragment {

    protected IFinishConfirmListener listener;

    @ViewById
    protected CheckBox printBox;

    @FragmentArg
    protected String orderGuid;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width), getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        setCancelable(false);
    }

    protected abstract BigDecimal calcTotal();

    protected boolean enableSignatureCheckbox() {
        return true;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_abort;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_finish;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {

        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                onConfirm();
                return false;
            }
        };
    }

    protected boolean onConfirm() {
        if (printBox.isChecked()) {
            printOrder(false, false);
        } else {
            completeProcess();
        }
        return false;
    }

    protected void completeProcess() {
        if (listener != null) {
            listener.onConfirmed();
        }
        dismiss();
    }

    public PrintAndFinishFragmentDialogBase setListener(IFinishConfirmListener listener) {
        this.listener = listener;
        return this;
    }

    public static interface IFinishConfirmListener {

        void onConfirmed();
    }

    protected abstract void printOrder(boolean skipPaperWarning, boolean searchByMac);

}
