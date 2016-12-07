package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PaxSignature extends PaxProcessorBaseCommand {

    public static final String RESULT_DETAILS = "RESULT_DETAILS";
    public static final String RESULT_CODE = "RESULT_CODE";
    private PaxModel paxModel;

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
        //String top = getApp().getShopInfo().displayWelcomeMsg;
        //String bottom = getApp().getShopInfo().displayWelcomeMsgBottom;
        //String displayMessage = String.format("%s\n%s", top != null ? top : "", bottom != null ? bottom : "");
        TransactionStatusCode responseCode = TransactionStatusCode.EMPTY_REQUEST;
        try {

            String filesDir = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath();

            posLink = createPosLink();
            posLink.appDataFolder = filesDir;

            /** SigSavePath *************************/
            String sigSavePath = filesDir + "/img/receipt;";
            try {
                String command = "mkdir " + sigSavePath;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
            } catch (IOException e) {
                System.out.println("mkdir failed! " + sigSavePath);
            }
            try {
                String command = "chmod 777 " + sigSavePath;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
            } catch (IOException e) {
                System.out.println("chmod 777 failed!");
            }
            /************************* SigSavePath **/

            ManageRequest manageRequest = new ManageRequest();
            manageRequest.TransType = 5;
            Logger.d("manageRequest.SigSavePath 1: " + manageRequest.SigSavePath);
            manageRequest.SigSavePath = sigSavePath;
            Logger.d("manageRequest.SigSavePath 2: " + manageRequest.SigSavePath);

            posLink.ManageRequest = manageRequest;

            ProcessTransResult ptr = posLink.ProcessTrans();
            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                ManageResponse response = posLink.ManageResponse;

                Logger.d("bemacark.response.SigFileName: " + response.SigFileName);

                //File img    = new File(response.SigFileName);
                //String newName = response.SigFileName.substring(0,response.SigFileName.length()-3) + "png";
                //Logger.d("bemacark.response.SigFileName.newName: " + newName);
                //File imgNew = new File(newName);
                //img.renameTo(imgNew);

                PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                responseCode = paxResp.getStatusCode();
                // TODO PosLink ResultCode "0000"?
                if (response.ResultCode.equalsIgnoreCase(RESULT_CODE_SUCCESS)) {
                    Logger.d("PaxSignatureCommand ResultCode: " + response.ResultCode);
                    TcrApplication.get().getShopPref().paxUrl().put(paxTerminal.ip);
                    TcrApplication.get().getShopPref().paxPort().put(paxTerminal.port);
                    manageRequest.ConvertSigToPic(response.SigFileName,"png",sigSavePath);
                    //posLink.ManageRequest.ConvertSigToPic(response.SigFileName,"png",sigSavePath);

                    PaxSignatureConvert(response.SigFileName);

                    //manReq.ConvertSigToPic(String sigpath,String type, String outfile);
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

    public void PaxSignatureConvert(String imageLocation){
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageLocation);
        Logger.d("bemacarl.imageBitmap: " + imageBitmap);
    }
    /**/


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
