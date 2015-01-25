package com.kaching123.tcr.processor;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.update.CheckForUpdatesCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.update.UpdateMerchantFlagCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.update.UpdateWirelessCommand;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.CheckForUpdateRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.GetProductListRequest;
import com.kaching123.tcr.websvc.api.prepaid.MerchantFlagsResponse;
import com.kaching123.tcr.websvc.api.prepaid.ProductListResponse;
import com.kaching123.tcr.websvc.api.prepaid.ProductListVersionResponse;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 */
public class PrepaidUpdateProcessor {

    private FragmentActivity context;
    private IUpdateCallback callback;
    private String versionNumber;
    private long transactionId;
    private boolean forceUpdate;
    private PrepaidUser user;

    public static Builder create() {
        return new Builder();
    }

    private PrepaidUpdateProcessor(final FragmentActivity context,
                                   final IUpdateCallback callback,
                                   final String versionNumber,
                                   final long transactionId,
                                   final PrepaidUser user) {
        this.context = context;
        this.versionNumber = versionNumber;
        this.callback = callback;
        this.transactionId = transactionId;
        this.user = user;
    }

    public void checkSync(Context context, boolean force) {
        forceUpdate = force;
        ProductListVersionResponse response = new CheckForUpdatesCommand().sync(context, composeCheckUpdateRequest());
        if (response != null && (!response.productListVersion.equals(versionNumber) || forceUpdate)) {
            ProductListResponse responseUpdate = new UpdateWirelessCommand().sync(context, composeUpdateRequest());
            if (responseUpdate != null) {
                TcrApplication.get().setNeedBillPaymentUpdated(true);
                TcrApplication.get().getShopPref().prepaidVersionId().put(responseUpdate.productListVersion);
            }

        }
        MerchantFlagsResponse merchantFlagsResponse = new UpdateMerchantFlagCommand().sync(context, composeCheckUpdateRequest());
        if(merchantFlagsResponse != null)
        {
            TcrApplication.get().setBillPaymentActivated(merchantFlagsResponse.flags.billPaymentActivated);
            TcrApplication.get().setSunpassActivated(merchantFlagsResponse.flags.sunpassActivated);
        }
    }

    private CheckForUpdateRequest composeCheckUpdateRequest() {
        CheckForUpdateRequest request = new CheckForUpdateRequest();
        request.mID = String.valueOf(this.user.getMid());

        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.transactionId = this.transactionId;
        return request;
    }

    private GetProductListRequest composeUpdateRequest() {
        GetProductListRequest request = new GetProductListRequest();
        request.mID = String.valueOf(this.user.getMid());
        request.tID = String.valueOf(this.user.getTid());
        request.password = this.user.getPassword();
        request.transactionId = this.transactionId;
        return request;
    }

    public void check(boolean force) {
        forceUpdate = force;
        WaitDialogFragment.show(this.context, "Checking for update ...");


        CheckForUpdatesCommand.start(this.context, this, composeCheckUpdateRequest());
    }

    ////////////////////////////////////////////////// GENERAL //////////////////////////////////////////////

    @OnSuccess(CheckForUpdatesCommand.class)
    public void onCheckForUpdatesCommandSuccess(@Param(CheckForUpdatesCommand.ARG_RESULT) ProductListVersionResponse result) {
        if (!result.productListVersion.equals(versionNumber) || forceUpdate) {
            TcrApplication.get().setNeedBillPaymentUpdated(true);
            UpdateWirelessCommand.start(context, this, composeUpdateRequest());
        } else {
            WaitDialogFragment.hide(context);
            callback.onComplete(true);
        }
    }

    @OnFailure(CheckForUpdatesCommand.class)
    public void onCheckForUpdatesCommandFail(@Param(CheckForUpdatesCommand.ARG_RESULT) ProductListVersionResponse result) {
        if (forceUpdate) {
            TcrApplication.get().setNeedBillPaymentUpdated(true);
            UpdateWirelessCommand.start(context, this, null);
        } else {
            WaitDialogFragment.hide(context);
            callback.onError(result.resultDescription);
        }
    }

    ////////////////////////////////////////////////// WIRELESS //////////////////////////////////////////////

    @OnSuccess(UpdateWirelessCommand.class)
    public void onUpdateWirelessCommandSuccess(@Param(UpdateWirelessCommand.ARG_RESULT) ProductListResponse result) {
        TcrApplication.get().getShopPref().prepaidVersionId().put(result.productListVersion);
        WaitDialogFragment.hide(context);
        callback.onComplete(false);
    }

    @OnFailure(UpdateWirelessCommand.class)
    public void onUpdateWirelessCommandFail(@Param(UpdateWirelessCommand.ARG_RESULT) ProductListResponse result) {
        callback.onError(result.resultDescription);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void update() {
        WaitDialogFragment.show(context, "Updating Broker info...");
    }

    public static class Builder {

        private FragmentActivity context;
        private IUpdateCallback callback;
        private String versionNumber;
        private long transactionId;
        private PrepaidUser user;

        private Builder() {
        }

        public PrepaidUpdateProcessor build() {
            return new PrepaidUpdateProcessor(context, callback, versionNumber, transactionId, user);
        }

        public Builder setTransactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setUser(PrepaidUser user) {
            this.user = user;
            return this;
        }

        public Builder setVersionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder setContext(FragmentActivity context) {
            this.context = context;
            return this;
        }

        public Builder setCallback(IUpdateCallback callback) {
            this.callback = callback;
            return this;
        }
    }

    public interface IUpdateCallback {

        void onError(String reason);

        void onComplete(boolean upToDate);
    }
}
