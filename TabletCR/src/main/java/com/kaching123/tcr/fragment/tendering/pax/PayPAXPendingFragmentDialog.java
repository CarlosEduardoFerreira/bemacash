package com.kaching123.tcr.fragment.tendering.pax;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.DeletePaxCommand;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.PaxSaleCommand.PaxSaleCommandBaseCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class PayPAXPendingFragmentDialog extends TransactionPendingFragmentDialogBase<PayPAXPendingFragmentDialog, SaleResponse> {

    @FragmentArg
    protected SaleActionResponse reloadResponse;

    @ViewById
    protected TextView message;

    protected IPaxSaleProgressListener listener;

    public PayPAXPendingFragmentDialog setListener(IPaxSaleProgressListener listener) {
        this.listener = listener;
        return this;
    }

    private static final String DIALOG_NAME = "PayPAXPendingFragmentDialog";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        message.setSingleLine(false);
        message.setText(R.string.pax_instructions);
    }

    @Override
    protected void doCommand() {
        PaxGateway gateway = (PaxGateway) transaction.getGateway().gateway();
        gateway.sale(getActivity(), new PaxSaleCommandBaseCallback() {

            @Override
            protected void handleSuccess(Transaction result, String errorReason) {
                listener.onComplete(result, errorReason);
            }

            @Override
            protected void handleError() {
                listener.onComplete(null, WebCommand.ErrorReason.UNKNOWN.getDescription());
            }

            @Override
            protected void handleSearchPort(String ip, int port) {
                PaxModel paxModel = new PaxModel(null, ip, port, "", null, null, false, null);
//                DeletePaxCommand.start(getActivity(), paxModel.getGuid(), true, paxModel.serial);
                listener.onSearchNeed(paxModel);
            }

            @Override
            protected void closePaxWindow() {
                listener.onCloseRequest();
            }
        }, null, null, transaction, reloadResponse);
    }

    public interface IPaxSaleProgressListener {

        void onComplete(Transaction transaction, String errorReason);

        void onCancel();

        void onSearchNeed(PaxModel model);

        void onCloseRequest();
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_wait_title;
    }

    public static void show(FragmentActivity context,
                            final Transaction transaction,
                            SaleActionResponse reloadResponse,
                            IPaxSaleProgressListener listener) {
        DialogUtil.show(context, DIALOG_NAME, PayPAXPendingFragmentDialog_.builder().reloadResponse(reloadResponse).build())
                .setListener(listener).setTransaction(transaction);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
