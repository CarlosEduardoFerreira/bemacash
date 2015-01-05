package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;
import com.kaching123.tcr.model.payment.blackstone.prepaid.SignatureFactory;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.ResponseCode;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.IWsdl2CodeEvents;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to be parent to all web commands
 */
public abstract class SOAPWebCommand<TypeRequest extends RequestBase> extends WebCommand {

    private static final int RETRY_COUNT = 3;

    protected abstract Long doCommand(Broker brokerApi, TypeRequest request);

    protected abstract TypeRequest getRequest();

    protected TypeRequest request;

    private boolean isRequestSent;

    protected String getSign(String MID, String TID, String Password, BigDecimal Amount, long OrderID) {
        try {
            return SignatureFactory.getSignature(MID,TID,Password,Amount,OrderID);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected boolean performAction() {
        request = getRequest();

        int retryCount = RETRY_COUNT;
        Long responseCode = null;
        do {
            Logger.d(this.getClass().getSimpleName() + ": performAction(): attempt #" + (RETRY_COUNT - retryCount + 1));
            isRequestSent = true;
            responseCode = doCommand(getBrokerApi(), request);
            if (responseCode == null) {
                Logger.e(this.getClass().getSimpleName() + ": performAction(): attempt #" + (RETRY_COUNT - retryCount + 1) + " failed: could not get response");
                Logger.d(this.getClass().getSimpleName() + ": performAction(): attempt #" + (RETRY_COUNT - retryCount + 1) + " failed: could not get response; request: " + request);
            }
            retryCount--;
        } while ((allowRetries() || !isRequestSent) && responseCode == null && retryCount > 0);
        boolean result = responseCode != null && ResponseCode.TRANSACTION_APPROVED.equals(ResponseCode.valueOf(responseCode));
        if (!result && responseCode != null) {
            Logger.e(this.getClass().getSimpleName() + ": attempt #" + (RETRY_COUNT - retryCount) + " failed, response code: " + responseCode);
            Logger.d(this.getClass().getSimpleName() + ": attempt #" + (RETRY_COUNT - retryCount) + " failed, response code: " + responseCode + "; request: " + request);
        }
        return result;
    }

    protected boolean allowRetries() {
        return true;
    }

    private Broker getBrokerApi() {
        return new Broker(eventsHandler).setUrl(getApp().getShopInfo().prepaidUrl);
    }

    @Override
    protected TaskResult afterAction() {
        return succeeded();
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }
    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }


    private EventsHandler eventsHandler = new EventsHandler() {

        @Override
        public void Wsdl2CodeFinishedWithException(Exception ex) {
            isRequestSent = isRequestSent(ex);
            Logger.e("SOAPWebCommand: request sent: " + isRequestSent + "; finished with Exception: ", ex);
        }

        private boolean isRequestSent(Exception ex) {
            if (!(ex instanceof SSLException))
                return true;

            String message = ex.getMessage();
            if (TextUtils.isEmpty(message))
                return true;

            return !(message.contains("Write error") && message.contains("during system call") && message.contains("Connection reset by peer"));
        }

    };

    private static abstract class EventsHandler implements IWsdl2CodeEvents {

        @Override
        public void Wsdl2CodeStartedRequest() {

        }

        @Override
        public void Wsdl2CodeFinished(String methodName, Object Data) {

        }

        @Override
        public void Wsdl2CodeFinishedWithException(Exception ex) {

        }

        @Override
        public void Wsdl2CodeEndedRequest() {

        }
    }
}
