package com.kaching123.tcr.commands.payment.pax;

import android.content.ContentProviderOperation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.AddTipsRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.HelloRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.LastTransactionRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.MIDownloadRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.MIRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SaleActionRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SerialRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SettlementRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.AddTipsResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.HelloResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.LastTrasnactionResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MIResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SerialResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SettlementResponse;
import com.squareup.okhttp.OkHttpClient;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by hamsterksu on 24.04.2014.
 */
public abstract class PaxBaseCommand extends AsyncCommand {

    public static final int CONNECTION_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(2);

    protected static final String ARG_DATA_PAX = "ARG_DATA_PAX";

    protected static final String RESULT_DATA = "RESULT_DATA";
    protected static final String RESULT_ERROR = "RESULT_ERROR";
    protected static final String RESULT_ERROR_CODE = "RESULT_ERROR_CODE";

    protected PaxModel paxTerminal;

    @Override
    protected TaskResult doCommand() {
        if (paxTerminal == null)
            paxTerminal = getArgs().getParcelable(ARG_DATA_PAX);
        return doCommand(getApi());
    }

    protected abstract TaskResult doCommand(PaxWebApi api);

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

    private PaxWebApi getApi() {
        RestAdapter.Builder adapterBuilder = new RestAdapter.Builder().setEndpoint(String.format("http://%s:%s", paxTerminal.ip, paxTerminal.port));
        adapterBuilder.setErrorHandler(new PaxErrorHandler());
        adapterBuilder.setConverter(new GsonConverter(createGson()));

        OkClient okClient = new OkClient(new OkHttpClient()) {

            @Override
            protected HttpURLConnection openConnection(Request request) throws IOException {
                HttpURLConnection connection = super.openConnection(request);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                return connection;
            }
        };
        adapterBuilder.setClient(okClient);
        //adapterBuilder.setLogLevel(LogLevel.FULL).setLog(new AndroidLog("PAX"));
        return adapterBuilder.build().create(PaxWebApi.class);
    }

    private static Gson createGson() {
        GsonBuilder gson = new GsonBuilder();
        gson.excludeFieldsWithoutExposeAnnotation();
        gson.serializeNulls();
        gson.setPrettyPrinting();
        return gson.create();
    }

    private static class PaxErrorHandler implements ErrorHandler {

        @Override
        public Throwable handleError(RetrofitError cause) {
            Response r = cause.getResponse();
            if (r != null && r.getStatus() == 404) {
                return new Pax404Exception(getPaxHeader(r.getHeaders()), cause);
            }
            return cause;
        }

        private static String getPaxHeader(List<Header> headers) {
            if (headers == null || headers.isEmpty())
                return null;
            for (Header h : headers) {
                if ("X-PAX-Reason".equals(h.getName())) {
                    return h.getValue();
                }
            }
            return null;
        }
    }

    public enum Error {
        UNDEFINED, SERVICE, PAX, PAX404, CONNECTIVITY
    }

    public static class Pax404Exception extends RuntimeException {

        public Pax404Exception(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    public static interface PaxWebApi {

        @POST("/")
        HelloResponse hello(@Body HelloRequest request);

        @POST("/")
        SerialResponse serial(@Body SerialRequest request);

        @POST("/")
        MIResponse midownload(@Body MIDownloadRequest request);

        @POST("/")
        MIResponse mi(@Body MIRequest request);

        @POST("/")
        SaleActionResponse sale(@Body SaleActionRequest request);

        @POST("/")
        SaleActionResponse refund(@Body SaleActionRequest request);

        @POST("/")
        AddTipsResponse addTips(@Body AddTipsRequest request);

        @POST("/")
        SettlementResponse settlement(@Body SettlementRequest request);

        @POST("/")
        LastTrasnactionResponse last(@Body LastTransactionRequest request);
    }
}
