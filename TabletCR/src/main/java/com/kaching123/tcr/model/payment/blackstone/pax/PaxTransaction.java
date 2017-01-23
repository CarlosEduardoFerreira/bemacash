package com.kaching123.tcr.model.payment.blackstone.pax;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxSignature;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.LastTrasnactionResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is a transaction related variables
 */
public class PaxTransaction extends Transaction<PaxTransaction> {

    public boolean allowReload;
    private final static String TAG = "PaxTransaction";

    public PaxTransaction(String userTransactionNumber, BigDecimal amount) {
        super(userTransactionNumber, amount);
    }

    public PaxTransaction(String userTransactionNumber, SaleActionResponse data) {
        super(userTransactionNumber, null);
        cardName = "Credit";
        type = TransactionType.PAX;
        paymentType = PaymentType.SALE;
        if (data != null) updateWith(data);
    }

    public void updateWith(SaleActionResponse data) {
        cardName = data.getDetails().getSale().getType();
        amount = new BigDecimal(data.getDetails().getAmount());
        serviceTransactionNumber = data.getDetails().getTransactionNumber();
        authorizationNumber = data.getDetails().getSale().getAuthNumber();
        lastFour = data.getDetails().getDigits();
        userTransactionNumber = data.getDetails().getTransactionNumber();
        String balanceStr = data.getDetails().getSale().getBalance();
        if (balanceStr != null) {
            balance = new BigDecimal(balanceStr);
        }
    }

    public void updateWith(LastTrasnactionResponse data) {
        cardName = data.getDetails().getDetails().getSale().getType();
        amount = new BigDecimal(data.getDetails().getDetails().getAmount());
        serviceTransactionNumber = data.getDetails().getDetails().getTransactionNumber();
        authorizationNumber = data.getDetails().getDetails().getSale().getAuthNumber();
        lastFour = data.getDetails().getDetails().getDigits();
        userTransactionNumber = data.getDetails().getDetails().getTransactionNumber();
        String balanceStr = data.getDetails().getDetails().getSale().getBalance();
        if (balanceStr != null) {
            balance = new BigDecimal(balanceStr);
        }
    }

    public void updateWith(com.pax.poslink.PaymentResponse response, byte[] paxDigitalSign) {
        cardName = TextUtils.isEmpty(response.CardType) ? cardName : response.CardType;

        //fixme
        // hack for temporary detecting ebt card
        if (getGateway().equals(PaymentGateway.PAX_EBT_CASH) || getGateway().equals(PaymentGateway.PAX_EBT_FOODSTAMP)) {
            cardName = "EBT CARD";
        }

        if (getGateway().equals(PaymentGateway.PAX_GIFT_CARD)) {
            cardName = "GIFT CARD";
        }
        //end of hack, needs to be replaced by real detection

        amount = new BigDecimal(response.ApprovedAmount).divide(CalculationUtil.ONE_HUNDRED);
        serviceTransactionNumber = response.RefNum;

        //added for Heartland requirements
        resultCode = response.ResultCode;

        Logger.d("bemacarl.updateWith: " + response.ExtData);

        setExtData("<extData>" + response.ExtData + "</extData>");
        //TODO PosLink need change to HostCode later.
//        userTransactionNumber = response.RefNum;

        // PaxSignature paxSign = new PaxSignature();

        if(response.RemainingBalance == ""){
            response.RemainingBalance = "0";
        }

        try {
            balance = new BigDecimal(response.RemainingBalance).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        lastFour = response.BogusAccountNum;
        int Card_Entry_ID = Integer.parseInt(getExtData(response.ExtData,"PLEntryMode"));
        entryMethod = getEntryModeByID(Card_Entry_ID);
        applicationIdentifier = getExtData(response.ExtData,"AID");
        applicationCryptogramType = getExtData(response.ExtData,"CID");
        authorizationNumber = response.AuthCode;
        paxDigitalSignature = paxDigitalSign;

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


    public void setExtData(String extData) {
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
                    System.out.println(datas.item(j).getNodeName()
                            + ":" + datas.item(j).getTextContent());
                    Logger.d("bemacarl.setExtData: " + datas.item(j).getNodeName() + ": " + datas.item(j).getTextContent());
                    if (datas.item(j).getNodeName().equalsIgnoreCase("PLEntryMode"))
                        entryMethod = datas.item(j).getTextContent();
                    if (datas.item(j).getNodeName().equalsIgnoreCase("ARC"))
                        applicationCryptogramType = datas.item(j).getTextContent();
                    if (datas.item(j).getNodeName().equalsIgnoreCase("AID"))
                        applicationIdentifier = datas.item(j).getTextContent();
                    if (datas.item(j).getNodeName().equalsIgnoreCase("PLNameOnCard"))
                        customerName = datas.item(j).getTextContent();
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEntryMethod(String exData) {

        return null;
    }

    private PaxTransaction() {
        super();
    }

    public PaxTransaction(PaymentTransactionModel mdoel) {
        super(mdoel);
    }

    public static Creator<PaxTransaction> CREATOR = new Creator<PaxTransaction>() {

        @Override
        public PaxTransaction createFromParcel(Parcel source) {
            return new PaxTransaction().initFromParcelableSource(source);
        }

        @Override
        public PaxTransaction[] newArray(int size) {
            return new PaxTransaction[size];
        }
    };

    public PaxTransaction setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
        return this;
    }

    @Override
    public PaymentGateway getGateway() {
        return gateway;
    }

}

