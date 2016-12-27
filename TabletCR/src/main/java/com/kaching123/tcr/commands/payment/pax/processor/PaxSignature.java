package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class PaxSignature extends PaxProcessorBaseCommand {

    public static final String RESULT_DETAILS = "RESULT_DETAILS";
    public static final String RESULT_CODE = "RESULT_CODE";
    private PaxModel paxModel;



    public String filesDir = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath();
    public String sigSavePath = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath() + "/img/receipt";

    public byte[] bitmapdata;
    public Bitmap SignatureBitmapObject;

    public PaxSignature(PaxModel paxModel) {
        this.paxModel = paxModel;
        this.doCommand();
    }

    public static void start(Context context, PaxModel paxTerminal, PaxSignatureCallback callback) {
        create(PaxSignature.class).arg(ARG_DATA_PAX, paxTerminal).allowNonUiCallbacks().callback(callback).queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public TaskResult sync(Context context, PaxModel model) {
        Bundle b = new Bundle();
        b.putParcelable(ARG_DATA_PAX, model);
        //no need in commands cache (creds on start)
        paxModel = model;
        return super.sync(context, b, null);
    }

    @Override
    protected PaxModel getPaxModel() {
        return paxModel == null ? (PaxModel) getArgs().getParcelable(ARG_DATA_PAX) : paxModel;
    }

    @Override
    protected TaskResult doCommand() {
        String errorMsg = null;
        int errorCode = 0;

        PosLink posLink = null;

        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = TransactionStatusCode.EMPTY_REQUEST;
        try {

            /** SigSavePath *************************/
            File dirSigSavePath = new File(sigSavePath);
            if (!dirSigSavePath.exists() || !dirSigSavePath.isDirectory()) {
                try {
                    String command = "mkdir " + sigSavePath;
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(command);
                } catch (IOException e) {
                    System.out.println("mkdir failed! " + sigSavePath);
                }
            }
            if(!dirSigSavePath.canWrite() || !dirSigSavePath.canRead()) {
                try {
                    String command = "chmod 777 " + sigSavePath;
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(command);
                } catch (IOException e) {
                    System.out.println("chmod 777 failed!");
                }
            }
            /************************* SigSavePath **/

            ManageRequest manageRequest = new ManageRequest();
            manageRequest.TransType = 5;
            Logger.d("manageRequest.SigSavePath 1: " + manageRequest.SigSavePath);
            manageRequest.SigSavePath = sigSavePath;
            Logger.d("manageRequest.SigSavePath 2: " + manageRequest.SigSavePath);

            posLink = createPosLink();
            posLink.appDataFolder = filesDir;
            posLink.ManageRequest = manageRequest;

            ProcessTransResult ptr = posLink.ProcessTrans();
            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                ManageResponse response = posLink.ManageResponse;

                Logger.d("bemacark.response.SigFileName: " + response.SigFileName);

                PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                responseCode = paxResp.getStatusCode();

                if (response.ResultCode.equalsIgnoreCase(RESULT_CODE_SUCCESS)) {
                    Logger.d("PaxSignatureCommand ResultCode: " + response.ResultCode);
                    TcrApplication.get().getShopPref().paxUrl().put(paxTerminal.ip);
                    TcrApplication.get().getShopPref().paxPort().put(paxTerminal.port);

                    // Convert signature to bitmap object
                    SignatureBitmapObject = this.ConvertPaxSignatureToBitmapObject(response.SigFileName);

                    return succeeded().add(RESULT_DETAILS, response.ResultCode).add(RESULT_CODE, errorCode);
                } else {
                    Logger.e("PaxSignatureCommand failed, pax error code(not RESULT_CODE_SUCCESS): " + ptr.Code);
                    error = PaxGateway.Error.PAX;
                    return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
                }

            } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                error = PaxGateway.Error.TIMEOUT;
                Logger.e("PaxSignatureCommand failed, pax error code(TimeOut): " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            } else {
                error = PaxGateway.Error.SERVICE;
                Logger.e("PaxSignatureCommand failed, pax error code(Error.SERVICE): " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            }
        } catch (Exception e) {
            Logger.e("PaxSignatureCommand failed", e);
            error = PaxGateway.Error.CONNECTIVITY;
        }
        return failed().add(RESULT_DETAILS, errorMsg).add(RESULT_CODE, errorCode);
    }



    public Bitmap ConvertPaxSignatureToBitmapObject(String imageLocation) throws IOException {


        File file = new File(imageLocation);
        BufferedReader sr = null;
        String alldata = "";

        try {
            FileInputStream in = new FileInputStream(file);

            String index;
            for(sr = new BufferedReader(new InputStreamReader(in)); (index = sr.readLine()) != null; index = "") {
                alldata = alldata + index;
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
        sr.close();


        System.out.println("bemacarl.alldata: " + alldata);

        String div = "\\^";
        String[] signature_divide = alldata.split(div);
        int margin_x = 180;
        int margin_y =  20;
        ArrayList xVal = new ArrayList(signature_divide.length);
        ArrayList yVal = new ArrayList(signature_divide.length);

        String x;
        String y;
        String x_y;
        int i;
        for (int image = 0; image < signature_divide.length - 1; ++image) {
            try {
                x_y = signature_divide[image];
                i = x_y.indexOf(",");
                x = x_y.substring(0, i);
                y = x_y.substring(i + 1);
                if (Integer.parseInt(y) != '\uffff') {
                    xVal.add(Integer.valueOf(Integer.parseInt(x)));
                    yVal.add(Integer.valueOf(Integer.parseInt(y)));
                }
                //both[image] = x.getBytes()[0];
            } catch (Exception var22) {
                //
            }
        }

        Collections.sort(yVal);
        Collections.sort(xVal);
        System.out.println("bemacarl.yVal: " + yVal);
        System.out.println("bemacarl.xVal: " + xVal);
        int minx = Integer.parseInt(((Integer) xVal.get(0)).toString());
        int miny = Integer.parseInt(((Integer) yVal.get(0)).toString());
        int BitmapOriginalWidth  = Integer.parseInt(((Integer) xVal.get(xVal.size() - 1)).toString()) - minx + 1 + margin_x * 2;
        int BitmapOriginalHeight = Integer.parseInt(((Integer) yVal.get(yVal.size() - 1)).toString()) - miny + 1 + margin_y * 2;

        System.out.println("bemacarl.minx: " + minx);
        System.out.println("bemacarl.miny: " + miny);
        System.out.println("bemacarl.BitmapOriginalWidth: " + BitmapOriginalWidth);
        System.out.println("bemacarl.BitmapOriginalHeight: " + BitmapOriginalHeight);

        Bitmap bmp = Bitmap.createBitmap(BitmapOriginalWidth , BitmapOriginalHeight, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE); // fundo branco
        bmp.setDensity(Bitmap.DENSITY_NONE);
        Canvas graph = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // cor do pixel

        for (i = 1; i < signature_divide.length - 1; ++i) {
            x_y = signature_divide[i - 1];
            int pos = signature_divide[i - 1].indexOf(",");
            y = x_y.substring(pos + 1);
            x = x_y.substring(0, pos);
            if (Integer.parseInt(y) != '\uffff') {
                int p1x = Integer.parseInt(x) + margin_x - minx;
                int p1y = Integer.parseInt(y) + margin_y - miny;
                x_y = signature_divide[i];
                pos = signature_divide[i].indexOf(",");
                y = x_y.substring(pos + 1);
                x = x_y.substring(0, pos);
                if (Integer.parseInt(y) != '\uffff') {
                    int p2x = Integer.parseInt(x) + margin_x - minx;
                    int p2y = Integer.parseInt(y) + margin_y - miny;
                    graph.drawLine(p1x, p1y, p2x, p2y, paint);
                }
            }
        }
        System.out.println("bemacarl.imageBitmap: " + bmp);

        return bmp;

        /*
        File f = new File(filesDir, "sign.bmp");
        f.createNewFile();

        Bitmap bitmap = bmp;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        /**/

    }



    public static abstract class PaxSignatureCallback {

        @OnSuccess(PaxSignature.class)
        public final void onSuccess(@Param(RESULT_DETAILS) String details) {
            handleSuccess(details);
        }

        protected abstract void handleSuccess(String details);

        @OnFailure(PaxSignature.class)
        public final void onFailure(@Param(RESULT_DETAILS) String error) {
            handleError(error);
        }

        protected abstract void handleError(String error);
    }
}
