package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.KDSModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.KDSTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by gdubina on 04.12.13.
 */
public abstract class KDSCommand extends PublicGroundyTask {

    protected Socket socket;

    public enum KDSError {NOT_CONFIGURED, OFFLINE, IP_NOT_FOUND}

    public static final String EXTRA_ERROR_KDS = "EXTRA_ERROR_KDS";

    public static final String EXTRA_NEED_SYNC = "EXTRA_NEED_SYNC";

    protected static final Uri URI_KDS = ShopProvider.getContentWithLimitUri(KDSTable.URI_CONTENT, 1);

//    protected KDSModel getKds() {
//        Cursor c = ProviderAction.query(URI_KDS)
//                .where(KDSTable.ALIAS_GUID + " IS NULL")
//                .perform(getContext());
//        KDSModel kdsModel = null;
//        if (c.moveToFirst()) {
//            kdsModel = new KDSModel(c);
//        }
//        c.close();
//        return kdsModel;
//    }

    @Override
    protected TaskResult doInBackground() {

        if (TextUtils.isEmpty(getApp().getShopPref().kdsRouterIp().getOr(""))) {
            Logger.e("PrinterCommand: printer doesn't configured");
            return failed().add(EXTRA_ERROR_KDS, KDSError.NOT_CONFIGURED);
        }
        socket = null;
        TaskResult result;
        try {
             socket = connectToKds();
             result = execute();
        } catch (IOException e){
            result = failed().add(EXTRA_ERROR_KDS, KDSError.IP_NOT_FOUND);
        } catch (Exception e) {
            Logger.e("PrinterCommand execute error: ", e);
            result = failed();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Logger.e("PrinterCommand close error: ", e);
                }
            }
        }
        return result;
    }

    private Socket connectToKds() throws IOException {
        return new Socket(getApp().getShopPref().kdsRouterIp().getOr(""), getApp().getShopPref().kdsRouterPort().getOr(4000));
    }

    protected abstract TaskResult execute() throws IOException;

    public byte[] buildXmlCommand(String strXml)
    {
        byte[] bytes =  strXml.getBytes();
        int ncount = 7 + bytes.length;

        byte[] ar = new byte[ncount];
        ar[0] = 0x02; //start
        ar[1] = 0x16; //command
        //data length
        int nlength = bytes.length;
        byte b0 = (byte)(nlength & 0xffL);
        byte b1 = (byte)((nlength & 0xff00L) >> 8);
        byte b2 = (byte)((nlength & 0xff0000L) >> 16);
        byte b3 = (byte)((nlength & 0xff000000L) >> 24);
        //HTL
        ar[2] = b3;
        ar[3] = b2;
        ar[4] = b1;
        ar[5] = b0;

        for (int i = 0; i < nlength; i++)
        {
            ar[6 + i] = bytes[i];
        }
        ar[6 + nlength] = 0x03;

        return ar;
    }

    public String removeTags(String str){
        str = str.replace("<items>","");
        str = str.replace("</items>","");
        str = str.replace("<modifiers>","");
        str = str.replace("</modifiers>","");
        return str;
    }

    public static abstract class BaseKdsCallback {

        @OnSuccess(KDSCommand.class)
        public void handleSuccess() {
            onPrintSuccess();
        }

        @OnFailure(KDSCommand.class)
        public void handleFailure(
                @Param(EXTRA_ERROR_KDS)
                KDSError kdsError) {

            if (kdsError != null && kdsError == KDSError.NOT_CONFIGURED) {
                onKdsNotConfigured();
                return;
            }
            onPrintError(kdsError);
        }

        protected abstract void onPrintSuccess();

        protected abstract void onPrintError(KDSError error);

        protected abstract void onKdsNotConfigured();
    }

}
