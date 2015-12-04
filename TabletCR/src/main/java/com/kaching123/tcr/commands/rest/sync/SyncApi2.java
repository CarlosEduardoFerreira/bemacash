package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.model.PlanOptionsResponse;

import org.json.JSONObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by gdubina on 19/03/14.
 */
public interface SyncApi2 {

    /*@FormUrlEncoded
    @POST("/apiv2/data/download_employees")
    GetResponse downloadEmployees(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_inventory")
    GetResponse downloadInventory(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_sales")
    GetResponse downloadSales(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);*/

    @FormUrlEncoded
    @POST("/apiv2/data/download")
    GetPagedArrayResponse download(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_unitslimited")
    GetPagedArrayResponse downloadUnitsLimited(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_movementgroups")
    GetPagedArrayResponse downloadMovementGroups(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_oldactiveorders")
    GetArrayResponse downloadOldActiveOrders(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_oldorders")
    GetArrayResponse downloadOldOrders(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials, @Field("entity") JSONObject entity);

    @FormUrlEncoded
    @POST("/apiv2/data/download_shopinfo")
    GetResponse downloadShopInfo(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);

    @FormUrlEncoded
    @POST("/apiv2/service/freemium_options")
    PlanOptionsResponse getFreemiumOptions(@Field("api_key") String apiKey, @Field("credentials") JSONObject credentials);
}
