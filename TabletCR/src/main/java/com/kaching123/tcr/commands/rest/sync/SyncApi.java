package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.RestCommand.IntegerResponse;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;

import org.json.JSONObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by gdubina on 19/03/14.
 */
public interface SyncApi {

    String SHOP_ID = "shop_id";
    String API_KEY = "api_key";
    String CREDENTIALS = "credentials";
    String ENTITY = "entity";
    String TYPE = "type";
    String AUTH_REG = "auth-register";
    String AUTH_LOGIN = "auth-login";
    String AUTH_PASSWD = "auth-pswd";
    String AUTH_OPERATOR_ID = "auth-id";
    String AUTH_HASH = "auth-hash";
    String AUTH_TIME = "auth-timestamp";
    String FILE_1_TYPE = "files[id1][type]";
    String FILE_1 = "files[id1][file]";
    String FILE_2_TYPE = "files[id2][type]";
    String FILE_2 = "files[id2][file]";
    String MESSAGE = "message";
    String DOWNLOAD_REQUEST_ID = "download_request_id";
    String UPLOAD_REQUEST_ID = "upload_request_id";
    String SALE_ORDER_IDS = "sale_order_ids";
    String FILE = "file";
    String MD5_FILE = "md5_file";
    String FILE_ID = "file_id";


    //@Headers("Accept-Encoding: gzip")
    @FormUrlEncoded
    @POST("/apiv2/data/upload")
    UploadResponseV1 upload(@Field("api_key") String apiKey,
                            @Field("credentials") JSONObject credentials,
                            @Field("entity") JSONObject entity);

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
    GetCurrentTimestampResponse getCurrentTimestamp(
                                                    @Field(API_KEY) String apiKey,
                                                    @Field(CREDENTIALS) JSONObject credentials);
    @FormUrlEncoded
    @POST("/apiv2/data/set_register_last_update")
    RestCommand.Response setRegisterLastUpdate(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    /*
    @FormUrlEncoded
    @POST("/apiv2/service/get_max_history_limit")
    IntegerResponse getMaxHistoryLimit(@Field("api_key") String apiKey,
                                       @Field("credentials")
                                               JSONObject credentials);
    /**/


    @FormUrlEncoded
    @POST("/apiv2/service/get_max_history_limit")
    IntegerResponse getMaxHistoryLimit(@Field(SHOP_ID) String shopId,
                                       @Field(API_KEY) String apiKey,
                                       @Field(CREDENTIALS) JSONObject credentials);

    @Multipart
    @POST("/apiv2/data/upload_batch")
    GetResponseUploadBatch uploadBatch(@Part(SHOP_ID) String shopId,
                                       @Part(API_KEY) String apiKey,
                                       @Part(CREDENTIALS) String credentials,
                                       @Part(MD5_FILE) String md5,
                                       @Part(FILE_ID) String fileId,
                                       @Part(FILE) TypedFile file);

    @FormUrlEncoded
    @POST("/apiv2/data/upload_status")
    GetResponseUploadBatchStatus uploadStatus(@Field(SHOP_ID) String shopId,
                                              @Field(API_KEY) String apiKey,
                                              @Field(CREDENTIALS) JSONObject credentials,
                                              @Field(UPLOAD_REQUEST_ID) String uploadRequestId,
                                              @Field(TYPE) String type);

    @FormUrlEncoded
    @POST("/apiv2/data/upload_cancel")
    GetResponseUploadBatchStatus uploadCancel(@Field(SHOP_ID) String shopId,
                                              @Field(API_KEY) String apiKey,
                                              @Field(CREDENTIALS) JSONObject credentials,
                                              @Field(UPLOAD_REQUEST_ID) String uploadRequestId);

    @FormUrlEncoded
    @POST("/apiv2/data/download_cancel")
    String downloadCancel(@Field(SHOP_ID) String shopId,
                          @Field(API_KEY) String apiKey,
                          @Field(CREDENTIALS) JSONObject credentials,
                          @Field(DOWNLOAD_REQUEST_ID) String filename);

    @FormUrlEncoded
    @POST("/apiv2/data/download_delete")
    String downloadDelete(@Field(SHOP_ID) String shopId,
                          @Field(API_KEY) String apiKey,
                          @Field(CREDENTIALS) JSONObject credentials,
                          @Field(DOWNLOAD_REQUEST_ID) String filename);

    @FormUrlEncoded
    @POST("/apiv2/data/upload")
    UploadResponse upload(@Field(SHOP_ID) String shopId,
                          @Field(API_KEY) String apiKey,
                          @Field(CREDENTIALS) JSONObject credentials,
                          @Field(ENTITY) JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_shopinfo")
    GetResponse downloadShopInfo(@Field(SHOP_ID) String shopId,
                                 @Field(API_KEY) String apiKey,
                                 @Field(CREDENTIALS) JSONObject credentials);


    @FormUrlEncoded
    @POST("/apiv2/data/download_oldactiveorders")
    GetArrayResponse downloadOldActiveOrders(@Field(SHOP_ID) String shopId,
                                             @Field(API_KEY) String apiKey,
                                             @Field(CREDENTIALS) JSONObject credentials,
                                             @Field(ENTITY) JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_locations")
    GetResponse downloadLocations(@Field(SHOP_ID) String shopId,
                                  @Field(API_KEY) String apiKey,
                                  @Field(CREDENTIALS) JSONObject credentials);


    @FormUrlEncoded
    @POST("/apiv2/data/download_oldorders")
    GetArrayResponse downloadOldOrders(@Field(SHOP_ID) String shopId,
                                       @Field(API_KEY) String apiKey,
                                       @Field(CREDENTIALS) JSONObject credentials,
                                       @Field(ENTITY) JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_commands")
    GetPagedArrayResponse downloadCommands(@Field(API_KEY) String apiKey,
                                           @Field(CREDENTIALS) JSONObject credentials);

}
