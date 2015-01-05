package com.kaching123.tcr.commands.rest.sync.v1;

import org.json.JSONObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by gdubina on 19/03/14.
 */
public interface SyncApiV1 {

    //@Headers("Accept-Encoding: gzip")
    @FormUrlEncoded
    @POST("/api/upload")
    UploadResponseV1 upload(@Field("api_key") String apiKey, @Field("entity") JSONObject entity);
}
