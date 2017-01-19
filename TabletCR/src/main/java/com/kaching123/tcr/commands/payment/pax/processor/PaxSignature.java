package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;

import com.kaching123.pos.printer.BitmapCarl;
import com.kaching123.pos.printer.BitmapPrintedCarl;
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


    public Bitmap SignatureBitmapObject;
    public byte[] signatureBitmapBytes;

    public PaxSignature(PaxModel paxModel) {
        if(TcrApplication.get().PAX_SIGNATURE_EMULATOR) {
            try {
                SignatureBitmapObject = this.ConvertPaxSignatureToBitmapObject("");
                Bitmap bmp = SignatureBitmapObject;
                if (bmp == null) {
                    Logger.d("bemacarl.BasePrintProcessor.bmp (109): " + bmp);
                } else {
                    Thread.sleep(200);
                    BitmapCarl bitmapCarl = new BitmapCarl();
                    Thread.sleep(200);
                    BitmapPrintedCarl printedBitmapCarl = bitmapCarl.toPrint(bmp);
                    Thread.sleep(200);
                    signatureBitmapBytes = printedBitmapCarl.toPrint();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            this.paxModel = paxModel;
            this.doCommand();
        }
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

        String alldata = "";

        if(TcrApplication.get().PAX_SIGNATURE_EMULATOR){
            // Carlos
            alldata = "0,65535^35,81^33,78^32,75^31,71^30,68^29,65^28,62^27,58^27,55^26,52^25,48^24,44^" +
                    "24,41^24,38^24,35^25,31^28,28^30,31^29,35^28,38^28,41^28,44^28,48^28,51^28,54^" +
                    "28,57^28,61^29,64^29,67^30,70^31,74^32,77^32,80^33,84^36,85^38,82^39,79^40,75^40,7" +
                    "2^40,69^41,66^43,62^44,59^45,56^48,53^52,56^52,59^53,62^55,66^56,69^57,72^59,75^" +
                    "62,75^62,71^62,68^62,65^62,62^61,58^61,55^61,52^60,48^58,45^55,43^52,41^49,44^" +
                    "48,48^48,51^49,54^52,54^54,51^56,48^59,49^61,53^62,56^63,59^64,62^64,66^65,69^67,7" +
                    "2^70,72^72,69^72,66^72,62^72,59^72,56^72,53^72,49^71,46^70,43^73,40^76,44^76,47^" +
                    "78,50^79,53^80,57^81,60^83,63^84,66^88,69^91,66^92,62^93,59^94,56^95,52^95,48^" +
                    "95,45^95,42^95,39^94,35^93,32^92,29^92,26^90,22^89,19^87,16^87,19^88,22^88,26^88,2" +
                    "9^89,32^90,35^91,39^91,42^92,45^92,48^93,52^94,55^96,58^96,62^98,65^101,68^104,70^" +
                    "108,66^108,63^108,60^109,57^109,53^109,50^109,47^110,44^111,40^112,44^113,47^" +
                    "115,50^118,51^120,48^122,44^122,41^122,38^122,35^121,31^120,28^119,25^117,22^114," +
                    "20^111,22^109,26^108,29^106,32^105,35^104,39^105,42^108,42^112,41^115,39^118,36^" +
                    "121,33^124,30^127,26^130,24^132,27^134,31^135,34^136,37^136,40^136,44^136,47^" +
                    "134,50^132,53^131,57^128,60^125,62^123,65^120,66^~";
        }else {
            File file = new File(imageLocation);
            BufferedReader sr = null;
            try {
                FileInputStream in = new FileInputStream(file);

                String index;
                for (sr = new BufferedReader(new InputStreamReader(in)); (index = sr.readLine()) != null; index = "") {
                    alldata = alldata + index;
                }
            } catch (Exception var11) {
                var11.printStackTrace();
            }
            sr.close();
        }


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
