package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.pos.printer.BitmapCarl;
import com.kaching123.pos.printer.BitmapPrintedCarl;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ProcessTransResult.ProcessTransResultCode;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by hamsterksu on 24.04.2014.
 */
public class PaxProcessorSaleCommand extends PaxProcessorBaseCommand {


    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String RESULT_TRANSACTION = "RES_TRANSACTION";
    private static final String RESULT_ERROR_REASON = "ERROR";
    private static final String ARG_SALEACTIONRESPONSE = "SaleActionResponse";
    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";

    private PaxTransaction transaction;
    private String errorReason;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sqlCommand;


    public static PaxSignature paxSignature = null;




    public static final TaskHandler startSale(Context context,
                                              PaxModel paxTerminal,
                                              Transaction transaction,
                                              int saleId,
                                              PaxSaleCommandBaseCallback callback) {
        return create(PaxProcessorSaleCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_PURPOSE, saleId)
                .callback(callback)
                .queueUsing(context);
    }

    public static final TaskHandler startSaleFromData(Context context,
                                                      PaxModel paxTerminal,
                                                      Transaction transaction,
                                                      int saleId,
                                                      SaleActionResponse response,
                                                      PaxSaleCommandBaseCallback callback) {
        return create(PaxProcessorSaleCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_SALEACTIONRESPONSE, response)
                .arg(ARG_PURPOSE, saleId)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        TaskResult result = failed();
        try {
            result = super.doInBackground();
        } catch (Exception e) {
            Logger.e("PaxSaleCommand: doInBackground(): failed", e);
        }

        if (isFailed(result)) {
            if (transaction != null)
                transaction.allowReload = true;
            if (TextUtils.isEmpty(errorReason))
                errorReason = ErrorReason.UNKNOWN.getDescription();
        }

        return succeeded()
                .add(RESULT_TRANSACTION, transaction)
                .add(RESULT_ERROR_REASON, errorReason);
    }


    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

    @Override
    protected TaskResult doCommand() {
        PaymentTransactionJdbcConverter jdbcConverter = (PaymentTransactionJdbcConverter) JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME);
        operations = new ArrayList<>();
        sqlCommand = batchInsert(PaymentTransactionModel.class);

        paxSignature = null;

        PaymentResponse response;
        Object possibleData = getArgs().getSerializable(ARG_SALEACTIONRESPONSE);
        if (possibleData != null) {
            response = (PaymentResponse) possibleData;
            //TODO: startSaleFromData (PaymentResponse) When does it get here/?
        }

        int transactionId = getIntArg(ARG_PURPOSE);
        transaction = getArgs().getParcelable(ARG_AMOUNT);

        if(transaction == null) {
           return failed();
        }
        BigDecimal cents = transaction.getAmount();
        String sAmount = String.valueOf((cents.multiply(CalculationUtil.ONE_HUNDRED)).intValue());
        Logger.d("PaxProcessorSaleCommand %d - %s", transactionId, sAmount);
        try {

            PaymentRequest request = new PaymentRequest();
//            request.TransType = TRANS_TYPE_SALE;
            preFillRequest(request, transaction);

            request.Amount = sAmount;
            request.ECRRefNum = ECRREFNUM_DEFAULT;
            //request.OrigRefNum = what goes here??//

            PosLink posLink = createPosLink();
            posLink.PaymentRequest = request;

            ProcessTransResult ptr = posLink.ProcessTrans();

            Logger.d("PaxProcessorSaleCommand response:" + ptr.Msg);

            if (ptr.Code == ProcessTransResultCode.OK) {

                response = posLink.PaymentResponse;

                if (response.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {

                    String paxDigitalSign = null;
                    getApp().paxSignatureCanceledByCustomer = false;
                    getApp().paxMachineHasTransactionSuccessfull = true;

                    if(transaction.getGateway().isTrueCreditCard() && getApp().getDigitalSignature()
                            && getApp().requireSignatureOnTransactionsHigherThan) {
                        try {
                            Thread.sleep(400);
                            paxSignature = new PaxSignature(getPaxModel());
                            Thread.sleep(400);
                            if (paxSignature != null) {
                                paxDigitalSign = paxSignature.signaturePaxFileString;
                                Log.d("Bemacarl", "PaxProcessorSaleCommand 188: paxDigitalSign: " + paxDigitalSign);
                                if(paxDigitalSign == null || paxDigitalSign == ""){
                                    getApp().paxSignatureCanceledByCustomer = true;
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    transaction.updateWith(response, paxDigitalSign);

                    PaymentTransactionModel transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);
                    operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                            .withValues(transactionModel.toValues())
                            .build());
                    sqlCommand.add(jdbcConverter.insertSQL(transactionModel, getAppCommandContext()));

                } else {
                    getApp().paxSignatureCanceledByCustomer = true;
                    getApp().paxMachineHasTransactionSuccessfull = false;

                    transaction.allowReload = true;
                    errorReason = "Result Code: " + response.ResultCode + " (" + response.ResultTxt + ")";
                    Logger.d("Pax Error code: " + response.ResultCode + ", Message: " + response.ResultTxt);
                }


            } else if (ptr.Code == ProcessTransResultCode.TimeOut) {
                transaction.allowReload = true;
                errorReason = "Payment cancelled or connection problem.";
                Logger.d("Pax TimeOUt");

            } else {
                errorReason = "Exception Code: " + ptr.Code + ", Message: " + ptr.Msg;
                Logger.d("Pax Fail");
            }

            // Send initial message to Pax machine.
            new PaxProcessorHelloCommand().sync(getContext(), getPaxModel());

        } catch (Exception ex) {
            transaction.allowReload = true;
            errorReason = "Exception occured." + ex.getMessage();
            Logger.e("Sale Pax", ex);
        }

        return succeeded();
    }


    public String getEntryModeByID(int Card_Entry_ID){
        String PLEntryMode = "";
        switch(Card_Entry_ID){
            case 0: PLEntryMode = "Manual"; break;
            case 1: PLEntryMode = "Swipe"; break;
            case 2: PLEntryMode = "Contactless"; break;
            case 3: PLEntryMode = "Scanner"; break;
            case 4: PLEntryMode = "Chip"; break;
            case 5: PLEntryMode = "Chip Fall Back Swipe"; break;
        }
        return PLEntryMode;
    }


    public String getExtData(String extData, String tag) {
        StringReader sr = new StringReader(extData);
        InputSource is = new InputSource(sr);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            NodeList exeDatas = document.getChildNodes();
            for (int i = 0; i < exeDatas.getLength(); i++) {
                Node node = exeDatas.item(i);
                NodeList datas = node.getChildNodes();
                for (int j = 0; j < datas.getLength(); j++) {
                    if (datas.item(j).getNodeName().equalsIgnoreCase(tag))
                        return datas.item(j).getTextContent();
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    protected boolean validateAppCommandContext() {
        return true;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public static abstract class PaxSaleCommandBaseCallback {

        @OnSuccess(PaxProcessorSaleCommand.class)
        public final void onSuccess(@Param(PaxProcessorSaleCommand.RESULT_TRANSACTION) Transaction result,
                                    @Param(PaxProcessorSaleCommand.RESULT_ERROR_REASON) String errorReason) {
            handleSuccess(result, errorReason);
        }

        protected abstract void handleSuccess(Transaction result, String errorReason);

        @OnFailure(PaxProcessorSaleCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
