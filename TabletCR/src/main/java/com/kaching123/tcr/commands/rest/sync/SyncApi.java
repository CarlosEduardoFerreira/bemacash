package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;

import org.json.JSONObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by gdubina on 19/03/14.
 */
public interface SyncApi {

    //@Headers("Accept-Encoding: gzip")
    @FormUrlEncoded
    @POST("/apiv2/data/upload")
    UploadResponseV1 upload(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/auth")
    AuthResponse auth(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    @FormUrlEncoded
    @POST("/apiv2/service/get_local_db_version")
    DBVersionResponse getDBVersion(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    @FormUrlEncoded
    @POST("/apiv2/service/get_shop_unique_pre_paid_transaction_id")
    GetPrepaidOrderIdResponse getPrepaidOrderId(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/service/close_preauth_transactions")
    RestCommand.Response closePreauthTransactions(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    @FormUrlEncoded
    @POST("/apiv2/service/sendemail")
    RestCommand.Response sendEmail(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/get_current_timestamp")
    GetCurrentTimestampResponse getCurrentTimestamp(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    @FormUrlEncoded
    @POST("/apiv2/data/set_register_last_update")
    RestCommand.Response setRegisterLastUpdate(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

}
