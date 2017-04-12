package com.kaching123.tcr.commands;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by Carlos on 17/03/2017.
 */

public class AtomicUpload {

    public enum UploadType{
        LOCAL, WEB, BOTH
    }

    public enum UploadObject{
        DATA, EMPLOYEE
    }

    Context mContext;

    public AtomicUpload() {
        this.mContext = TcrApplication.get().getApplicationContext();
    }

    public void upload(BatchSqlCommand sql, UploadType type){
        upload(sql, type, UploadObject.DATA);
    }

    public void upload(BatchSqlCommand sql, UploadType type, UploadObject object) {

        mContext = TcrApplication.get().getApplicationContext();

        if (type.equals(UploadType.LOCAL) || type.equals(UploadType.BOTH)) {
            Log.d("BemaCarl7", "AtomicUpload.upload.type|sql1: " + type + "|" + sql.toJson());
            ContentValues values = getContentValues(sql, System.currentTimeMillis(), true);
            mContext.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandHostTable.URI_CONTENT), values);

        }

        if (type.equals(UploadType.WEB) || type.equals(UploadType.BOTH)) {
            Log.d("BemaCarl7", "AtomicUpload.upload.type|sql2: " + type + "|" + sql.toJson());
            ContentValues values = getContentValues(sql, System.currentTimeMillis(), false);
            mContext.getContentResolver().insert(ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT), values);
        }

        if(hasInternetConnection()) {
            if (object.equals(UploadObject.DATA)) {
                Log.d("BemaCarl7", "AtomicUpload.upload.object: " + object);
                OfflineCommandsService.startUpload(mContext);
            } else {
                Log.d("BemaCarl7", "AtomicUpload.upload.object: " + object);
                OfflineCommandsService.startemployeeTableUpload(mContext);
            }
        }

    }

    public boolean hasNetworkConnection(){

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if(isConnected) {
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Internet is connected.");
            //Toast.makeText(context,"Internet is connected",Toast.LENGTH_SHORT).show();
        }else {
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Internet is not connected.");
            //Toast.makeText(context, "Internet is not connected, please check your settings", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public boolean hasInternetConnection(){
        String servidorBemacash = mContext.getResources().getString(R.string.api_server_url);
        String url = fixURL(servidorBemacash);
        Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection.servidorBemacash: " + url);
        HttpGet httpGet = new HttpGet(url);
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 2000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 3000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        try{
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Checking Connection to " + url);
            httpClient.execute(httpGet);
            Log.d("BemaCarl7", "AtomicUpload.hasInternetConnection: Connection to " + url + " is ok.");
            return true;
        }
        catch(ClientProtocolException e){
            Log.e("BemaCarl7", "AtomicUpload.hasInternetConnection: ClientProtocolException: Connection to " + url + " is NOT ok.");
            //e.printStackTrace();
        }
        catch(IOException e){
            Log.e("BemaCarl7", "AtomicUpload.hasInternetConnection: IOException: Connection to " + url + " is NOT ok.");
            //e.printStackTrace();
        }
        Log.e("BemaCarl7", "AtomicUpload.hasInternetConnection: Connection to " + url + " is NOT ok.");
        return false;
    }


    private String fixURL(String url){
        if(url.contains("@")){
            String[] urls = url.split("@");
            String newUrl = urls[1];
            if(newUrl.substring(newUrl.length() - 1).equals("/")){
                newUrl = newUrl.substring(0, newUrl.length() - 1);
            }
            return "http://" + newUrl;
        }
        return url;
    }

}
