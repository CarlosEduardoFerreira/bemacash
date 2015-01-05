package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.VectorDocument;

import java.math.BigDecimal;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;


/**
 * Created by teli.yin on 6/5/2014.
 */
public class PrepaidPaymentCompleteReceiptListViewModel {
    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);

    static final String[] LIST_SUNPASS_TRANSPONDER = {"Transponder Number", "Last Known Balance", "Minimum recharge amount", "Amount", "Fee", "Total"};
    static final String[] LIST_CONFIRMATION_SUNPASS_TRANSPONDER = {"Transponder Number", "Last Known Balance", "Minimum recharge amount", "Amount", "Fee", "Total"};
    static final String[] LIST_PYD_CONFIRMATION_SUNPASS_DOCUMNETS = {"Document ID", "License Plate Number", "Amount", "Fee", "Total"};

    static public String[] getListForNames() {
        return LIST_SUNPASS_TRANSPONDER;
    }

    static public String[] getListForConfirmationItemNames() {
        return LIST_CONFIRMATION_SUNPASS_TRANSPONDER;
    }

    static public String[] getListForPYDConfirmationItemNames() {
        return LIST_PYD_CONFIRMATION_SUNPASS_DOCUMNETS;
    }

    static private BigDecimal minAmount = BigDecimal.TEN;

    static public String[] getListForContents(SunReplenishmentRequest request, BalanceResponse response, String orderNum, String total) {
        String[] contents = new String[6];
        contents[0] = request.accountNumber;
        contents[1] = commaPriceFormat(new BigDecimal(response.currentBalance));
        BigDecimal min = BigDecimal.valueOf(response.minimumReplenishmentAmount);
        if (min != null && BigDecimal.ZERO.compareTo(min) < 0) {
            minAmount = min;
        }
        contents[2] = commaPriceFormat(minAmount);
        contents[3] = commaPriceFormat(request.amount);
        contents[4] = commaPriceFormat(new BigDecimal(request.feeAmount));
        contents[5] = (commaPriceFormat(new BigDecimal(total).add(new BigDecimal(request.feeAmount))));
        return contents;
    }

    static public String[] getConfiramtionListForContents(String accountNumber, BalanceResponse response, String amount, String feeAmount) {
        String[] contents = new String[6];
        contents[0] = accountNumber;
        contents[1] = commaPriceFormat(new BigDecimal(response.currentBalance));
        BigDecimal min = new BigDecimal(response.minimumReplenishmentAmount);
        if (min != null && BigDecimal.ZERO.compareTo(min) < 0) {
            minAmount = min;
        }
        contents[2] = commaPriceFormat(minAmount);
        contents[3] = commaPriceFormat(new BigDecimal(amount));
        contents[4] = commaPriceFormat(new BigDecimal(feeAmount));
        contents[5] = (commaPriceFormat(new BigDecimal(amount).add(new BigDecimal(feeAmount)))); // service fee is not ccounted currently.
        return contents;
    }

    static public String[] getPYDConfiramtionListForContents(SunPassDocumentPaymentRequest request, DocumentInquiryResponse response, String feeAmount) {


        String[] contents = new String[5];
        contents[0] = request.accountNumber;
        contents[1] = request.licensePateleNumber;
        contents[2] = commaPriceFormat(new BigDecimal(getPaidDocumentsAmount(request.paidDocuments)));
        contents[3] = commaPriceFormat(new BigDecimal(feeAmount));
        contents[4] = commaPriceFormat(new BigDecimal(getPaidDocumentsAmount(request.paidDocuments)).add(new BigDecimal(feeAmount)));
        return contents;
    }

    public static String getPaidDocumentsAmount(VectorDocument paidDocuments) {
        double amount = 0;
        for (int i = 0; i < paidDocuments.size(); i++) {
            amount = amount + paidDocuments.get(i).documentPaymentAmount;
        }
        return amount + "";
    }

    public static void trace(String[] names, String[] contents) {
        StringBuilder st = new StringBuilder();
        st.append("Prepaid Receipt: " + "\n");
        for (int i = 0; i < names.length; i++)
            st.append("name: " + names[i] + ", content: " + contents[i] + "\n");
        Logger.d(st.toString());
    }

}
