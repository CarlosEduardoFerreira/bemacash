package com.kaching123.tcr.websvc.service;

import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.HelloRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SaleActionRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.HelloResult;
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
 * Created by dot on 08.04.2014.
 */
public final class PAXWebSerivce {

    public static final String PAX_URL = "http://192.168.178.136:6911";

    private PAXWebSerivce(){}

    public static void hello(final HelloRequest data, final HelloResult result, String url) throws IOException {
        String path = RESTWebService.createPath(url);
        Header[] h = new Header[0];
//        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");
        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        WebService.REST.get().post(request, result);
    }

    public static void sale(final SaleActionRequest data, final SaleResult result) throws IOException {
        String path = RESTWebService.createPath(url());
        Header[] h = new Header[1];
        h[0] = new BasicHeader(HTTP.CONTENT_TYPE, "text/json");
        RESTRequest request = new RESTRequest();
        request.parameters = new WebParameter[0];
        request.headers = h;
        request.path = path;
        request.body = GSON.getInstance().getGson().toJson(data);
        WebService.REST.get().post(request, result);
    }

    private static String url() {
        return PAX_URL;
    }

}
