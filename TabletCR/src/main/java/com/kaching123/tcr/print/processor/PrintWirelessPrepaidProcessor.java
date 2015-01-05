package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessPinInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessTopupInfo;
import com.kaching123.tcr.websvc.api.prepaid.AccessPhone;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by vkompaniets on 20.03.14.
 */
public class PrintWirelessPrepaidProcessor extends PrintPrepaidProcessor<WirelessInfo> {

    public PrintWirelessPrepaidProcessor(String orderGuid, WirelessInfo info, IAppCommandContext appCommandContext) {
        super(orderGuid, info, appCommandContext);
    }

    public PrintWirelessPrepaidProcessor(String orderGuid, boolean reprint, WirelessInfo info, IAppCommandContext appCommandContext) {
        super(orderGuid, reprint, info, appCommandContext);
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {

        super.printHeader(context, app, printerWrapper);
    }

    @Override
    protected void printBody(Context context, TcrApplication app, ITextPrinter printerWrapper) {
//        if(info instanceof BillPaymentInfo)
//        {
//            BillPaymentInfo billPaymentInfo = (BillPaymentInfo) info;
//            printerWrapper.addTransactionFee(new BillPaymentModel(billPaymentInfo.paymentOption.paymentType, new BigDecimal(billPaymentInfo.paymentOption.feeAmount)));
//        }

        super.printBody(context, app, printerWrapper);

        printerWrapper.drawLine();

        if (info.isFieldsEmty())
            return;

        if (info instanceof WirelessPinInfo) {
            WirelessPinInfo wirelessInfo = (WirelessPinInfo) info;

            printerWrapper.add(context.getString(R.string.wireless_prepaid_print_processor_step_one), false, true);
            if (wirelessInfo.localAccessPhones != null) {
                for (AccessPhone phone : wirelessInfo.localAccessPhones) {
                    printerWrapper.addWithTab(trimOpt(phone.city), trimOpt(phone.phone), true, false);
                }
            }

            printerWrapper.add(context.getString(R.string.wireless_prepaid_print_processor_step_two), false, true);
            printerWrapper.add(trimOpt(wirelessInfo.pinNumber), false, false);

            String instructions = context.getString(R.string.wireless_prepaid_print_processor_instructions);
            String[] instructionLines = instructions.split("\n");
            for (String instructionLine: instructionLines)
                printerWrapper.add(instructionLine, false, true);

            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_control_number), trimOpt(wirelessInfo.controlNumber), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_transaction_id), trimOpt(wirelessInfo.transactionID), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_customer_and_service), trimOpt(wirelessInfo.customerServiceEnglish), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_expiration_date), trimOpt(wirelessInfo.expirationDate), true, false);
        } else if (info instanceof WirelessTopupInfo) {
            WirelessTopupInfo wirelessInfo = (WirelessTopupInfo) info;

            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_control_number), trimOpt(wirelessInfo.controlNumber), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_transaction_id), trimOpt(wirelessInfo.transactionID), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_account_number), trimOpt(wirelessInfo.accountNumber), true, false);
            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_reference), trimOpt(wirelessInfo.referenceNumber), true, false);
            if (!TextUtils.isEmpty(wirelessInfo.authorizationCode))
                printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_authorization_code), trimOpt(wirelessInfo.authorizationCode), true, false);

            if (wirelessInfo.localAccessPhones != null && wirelessInfo.localAccessPhones.length > 0) {
                printerWrapper.emptyLine();
                for (AccessPhone phone : wirelessInfo.localAccessPhones) {
                    printerWrapper.addWithTab(trimOpt(phone.city), trimOpt(phone.phone), true, false);
                }
                printerWrapper.emptyLine();
            }

            printerWrapper.addWithTab(context.getString(R.string.wireless_prepaid_print_processor_customer_and_service), trimOpt(wirelessInfo.customerServiceEnglish), true, false);
        }

        printerWrapper.drawLine();
    }
}
