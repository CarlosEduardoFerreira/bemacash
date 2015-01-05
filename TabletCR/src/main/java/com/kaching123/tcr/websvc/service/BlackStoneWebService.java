package com.kaching123.tcr.websvc.service;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.blackstone.payment.request.AutomaticBatchCloseRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ClosePreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoFullRefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoSettlementRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoVoidRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ProcessPreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.RefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.request.SaleRequest;
import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.WebAPI;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.AutomaticBatchCloseResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoFullRefundResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoSettlementResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoVoidResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.PreauthResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.RefundResult;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.SaleResult;
import com.mayer.framework.web.model.WebParameter;
import com.mayer.framework.web.model.rest.RESTRequest;
import com.mayer.framework.web.provider.RESTWebService;
import com.mayer.framework.web.provider.WebService;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to operate blackstone api
 */
public class BlackStoneWebService {

    // TODO debug purposes, remove when UAT starts
    public static final boolean D = true;

    /**
     * SALE REQUEST
     *
     * @throws IOException
     */
    public static void sale(final SaleRequest data, final SaleResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_SALE);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.sale(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void refund(final RefundRequest data, final RefundResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_DOREFUND);
        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");
        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.refund(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void doFullRefund(final DoFullRefundRequest data, final DoFullRefundResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_DOFULLREFUND);
        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");
        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.doFullRefund(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void doVoid(final DoVoidRequest data, final DoVoidResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_DOVOID);
        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");
        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.doVoid(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void processPreauth(final ProcessPreauthRequest data, final PreauthResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_PROCESSPREAUTH);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.processPreauth(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void closePreauth(final ClosePreauthRequest data, final PreauthResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_CLOSEPREAUTH);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.closePreauth(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void doSettlement(final DoSettlementRequest data, final DoSettlementResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_TRANSACTIONS,
                WebAPI.BlackStoneAPI.REQUEST_PATH_DOSETTLEMENT);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.doSettlement(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void setAutomaticBatchClose(AutomaticBatchCloseRequest data, AutomaticBatchCloseResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_ADMINISTRATION,
                WebAPI.BlackStoneAPI.REQUEST_PATH_SETAUTOMATICBATCHCLOSE);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.setAutomaticBatchClose(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    public static void updateAutomaticHourToCloseBatch(AutomaticBatchCloseRequest data, AutomaticBatchCloseResult result) throws IOException {
        String path = RESTWebService.createPath(url(),
                WebAPI.BlackStoneAPI.REQUEST_PATH_API,
                WebAPI.BlackStoneAPI.REQUEST_PATH_ADMINISTRATION,
                WebAPI.BlackStoneAPI.REQUEST_PATH_UPDATEAUTOMATICHOURTOCLOSEBATCH);

        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");

        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        Logger.d("BlackStoneWebService.updateAutomaticHourToCloseBatch(): request: " + request.body);
        WebService.REST.get().post(request, result);
    }

    private static String url() {
        return TcrApplication.get().getShopInfo().blackstonePaymentUrl;
    }
}
