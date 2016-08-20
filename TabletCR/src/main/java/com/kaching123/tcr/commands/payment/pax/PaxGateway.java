package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;
import android.os.Parcel;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneAddTipsCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneRefundCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneSaleCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneSettlementCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorAddTipsCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBalanceCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorRefundCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSaleCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSettlementCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorVoidCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransactionFactory;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.telly.groundy.GroundyManager;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
public class PaxGateway implements IPaymentGateway<PaxTransaction, Void> {

    private static final BigDecimal MIN_VALUE = new BigDecimal("0.01");


    private final int saleId;
    private final int refundId;
    private final PaxMethod self;

    public static PaxGateway credit() {
        int sId;
        int rId;
        if (TcrApplication.get().isBlackstonePax()) {
            sId = PaxBlackstoneSaleCommand.TRANSACTION_ID_CREDIT_SALE;
            rId = PaxBlackstoneSaleCommand.TRANSACTION_ID_CREDIT_REFUND;
        } else {
            sId = PaxProcessorSaleCommand.TRANSACTION_ID_CREDIT_SALE;
            rId = PaxProcessorSaleCommand.TRANS_TYPE_RETURN;
        }


        return new PaxGateway(sId, rId, PaxMethod.CREDIT);
    }

    public static PaxGateway debit() {
        int sId;
        int rId;
        if (TcrApplication.get().isBlackstonePax()) {
            sId = PaxBlackstoneSaleCommand.TRANSACTION_ID_DEBIT_SALE;
            rId = PaxBlackstoneSaleCommand.TRANSACTION_ID_DEBIT_REFUND;
        } else {
            sId = PaxProcessorSaleCommand.TRANSACTION_ID_DEBIT_SALE;
            rId = PaxProcessorSaleCommand.TRANS_TYPE_RETURN;
        }
        return new PaxGateway(sId, rId, PaxMethod.DEBIT);
    }

    public static PaxGateway ebtFoodstamp() {
        int sId;
        int rId;
        if (TcrApplication.get().isBlackstonePax()) {
            sId = PaxBlackstoneSaleCommand.TRANSACTION_ID_EBT_FOODSTAMP_SALE;
            rId = PaxBlackstoneSaleCommand.TRANSACTION_ID_EBT_REFUND;
        } else {
            sId = PaxProcessorSaleCommand.TRANSACTION_ID_EBT_FOODSTAMP_SALE;
            rId = PaxProcessorSaleCommand.TRANS_TYPE_RETURN;
        }
        return new PaxGateway(sId, rId, PaxMethod.EBT_FOODSTAMP);

    }

    public static PaxGateway ebtCash() {
        int sId;
        int rId;
        if (TcrApplication.get().isBlackstonePax()) {
            sId = PaxBlackstoneSaleCommand.TRANSACTION_ID_EBT_CASH_SALE;
            rId = 0;
        } else {
            sId = PaxProcessorSaleCommand.TRANSACTION_ID_EBT_CASH_SALE;
            rId = PaxProcessorSaleCommand.TRANS_TYPE_RETURN;
        }
        return new PaxGateway(sId, rId, PaxMethod.EBT_CASH);

    }

    public static PaxGateway giftCard() {
        int sId;
        int rId;

        sId = PaxProcessorSaleCommand.TRANSACTION_ID_GIFT;
        rId = PaxProcessorSaleCommand.TRANS_TYPE_RETURN;

        return new PaxGateway(sId, rId, PaxMethod.GIFT);

    }

    public PaxGateway(int saleId, int refundId, PaxMethod self) {
        this.saleId = saleId;
        this.refundId = refundId;
        this.self = self;
    }

    @Override
    public TaskHandler sale(Context context,
                            Object callback,
                            User user,
                            Void ignore,
                            Transaction transaction) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);

        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorSaleCommand.startSale(
                    context,
                    PaxModel.get(),
                    transaction,
                    saleId,
                    (PaxProcessorSaleCommand.PaxSaleCommandBaseCallback) callback);
        }

        return PaxBlackstoneSaleCommand.startSale(
                context,
                PaxModel.get(),
                transaction,
                saleId,
                (PaxBlackstoneSaleCommand.PaxSaleCommandBaseCallback) callback);
    }

    public TaskHandler sale(Context context,
                            Object callback,
                            User user,
                            Void ignore,
                            Transaction transaction,
                            Object reloadResponse) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);

        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorSaleCommand.startSaleFromData(
                    context,
                    PaxModel.get(),
                    transaction,
                    saleId,
                    (SaleActionResponse) reloadResponse,
                    (PaxProcessorSaleCommand.PaxSaleCommandBaseCallback) callback);
        }
        return PaxBlackstoneSaleCommand.startSaleFromData(
                context,
                PaxModel.get(),
                transaction,
                saleId,
                (SaleActionResponse) reloadResponse,
                (PaxBlackstoneSaleCommand.PaxSaleCommandBaseCallback) callback);

    }

    public TaskHandler reload(Context context,
                            Object callback,
                            User user,
                            Void ignore,
                            Transaction transaction,
                            Object reloadResponse) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);

            return PaxProcessorGiftCardReloadCommand.startSaleFromData(
                    context,
                    PaxModel.get(),
                    transaction,
                    saleId,
                    (SaleActionResponse) reloadResponse,
                    (PaxProcessorGiftCardReloadCommand.PaxGiftCardReloadCallback) callback);

    }

    @Override
    public TaskHandler refund(Context context,
                              Object callback,
                              User user,
                              Void ignore,
                              PaymentTransactionModel transaction,
                              BigDecimal amount,
                              SaleOrderModel childOrderModel, boolean refundTips, boolean isManualReturn) {
        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, refundTips, isManualReturn,
                    (PaxProcessorRefundCommand.PaxREFUNDCommandBaseCallback) callback);
        }
        return PaxBlackstoneRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, refundTips, isManualReturn,
                (PaxBlackstoneRefundCommand.PaxREFUNDCommandBaseCallback) callback);
    }

    public TaskHandler refund(Context context,
                              Object callback,
                              User user,
                              Void ignore,
                              PaymentTransactionModel transaction,
                              BigDecimal amount,
                              SaleActionResponse reloadResponse,
                              SaleOrderModel childOrderModel,
                              boolean refundTips,
                              boolean isManualReturn) {
        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, reloadResponse, refundTips, isManualReturn,
                    (PaxProcessorRefundCommand.PaxREFUNDCommandBaseCallback) callback);
        }
        return PaxBlackstoneRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, reloadResponse, refundTips, isManualReturn,
                (PaxBlackstoneRefundCommand.PaxREFUNDCommandBaseCallback) callback);
    }

    public PaxProcessorVoidCommand.VoidResponse Void_PosLink(
            Context context,
            Object callback,
            User user,
            Void ignore,
            PaymentTransactionModel transaction,
            BigDecimal amount,
            SaleActionResponse reloadResponse,
            SaleOrderModel childOrderModel,
            boolean refundTips,
            boolean isManualReturn
    ) {
        //todo do void first
        PaxProcessorVoidCommand.VoidResponse response = new PaxProcessorVoidCommand().Sync(refundId, transaction, amount, refundTips, isManualReturn, transaction);
        if (response.getErrorReason().equalsIgnoreCase("")) {
            return response;
        }
        return null;
    }

    @Override
    public TaskHandler voidMe(Context context,
                              Object callback,
                              User user,
                              PaymentTransactionModel transaction,
                              SaleOrderModel childOrderModel,
                              boolean needToCancel) {

        transaction.status = PaymentStatus.SUCCESS;
        transaction.paymentType = PaymentType.REFUND;
        transaction.declineReason = PaymentStatus.SUCCESS.toString();
        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorRefundCommand.startReturn(context, PaxModel.get(), transaction, transaction.amount, childOrderModel, refundId, false, false,
                    (PaxProcessorRefundCommand.PaxREFUNDCommandBaseCallback) callback);
        }
        return PaxBlackstoneRefundCommand.startReturn(context, PaxModel.get(), transaction, transaction.amount, childOrderModel, refundId, false, false,
                (PaxBlackstoneRefundCommand.PaxREFUNDCommandBaseCallback) callback);
    }

    public TaskHandler closePreauth(Context context,
                                    Object callback,
                                    PaymentTransactionModel transactionModel,
                                    BigDecimal tipsAmount,
                                    String tipsComments,
                                    String tippedEmployeeId,
                                    SaleActionResponse reloadResponse) {

        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorAddTipsCommand.start(context, PaxModel.get(), transactionModel, tipsAmount, tipsComments, tippedEmployeeId, reloadResponse, (PaxProcessorAddTipsCommand.PaxTipsCommandBaseCallback) callback);
        }
        return PaxBlackstoneAddTipsCommand.start(context, PaxModel.get(), transactionModel, tipsAmount, tipsComments, tippedEmployeeId, reloadResponse, (PaxBlackstoneAddTipsCommand.PaxTipsCommandBaseCallback) callback);
    }

    public TaskHandler doSettlement(Context context,
                                    Object callback) {
        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorSettlementCommand.start(context, PaxModel.get(), (PaxProcessorSettlementCommand.SettlementCommandBaseCallback) callback);
        }
        return PaxBlackstoneSettlementCommand.start(context, PaxModel.get(), (PaxBlackstoneSettlementCommand.SettlementCommandBaseCallback) callback);
    }

    public TaskHandler doBalance(Context context,
                                 Object callback) {
        if (!TcrApplication.get().isBlackstonePax()) {
            return PaxProcessorBalanceCommand.start(context, PaxModel.get(), (PaxProcessorBalanceCommand.PaxBalanceCommandBaseCallback) callback);
        }
        return PaxBlackstoneBalanceCommand.start(context, PaxModel.get(), (PaxBlackstoneBalanceCommand.PaxBalanceCommandBaseCallback) callback);
    }

    public TaskHandler doGiftCardBalance(Context context,
                                         Object callback) {
        return PaxProcessorBalanceCommand.start(context, PaxModel.get(), (PaxProcessorBalanceCommand.PaxBalanceCommandBaseCallback) callback);
    }

    @Override
    public BigDecimal minimalAmount() {
        return MIN_VALUE;
    }

    @Override
    public PaxTransaction createTransaction(Context context, BigDecimal amount, String orderGuid) {
        PaymentGateway gateway = PaymentGateway.PAX;
        switch (self) {
            case CREDIT:
                gateway = PaymentGateway.PAX;
                break;
            case DEBIT:
                gateway = PaymentGateway.PAX_DEBIT;
                break;
            case EBT_FOODSTAMP:
                gateway = PaymentGateway.PAX_EBT_FOODSTAMP;
                break;
            case EBT_CASH:
                gateway = PaymentGateway.PAX_EBT_CASH;
                break;
        }
        return PaxTransactionFactory.create(TcrApplication.get().getOperatorGuid(), orderGuid, gateway, false).setAmount(amount);
    }

    public PaxTransaction createPreauthTransaction(Context context, BigDecimal amount, String orderGuid) {
        boolean isPreauth = false;
        PaymentGateway gateway = PaymentGateway.PAX;
        switch (self) {
            case CREDIT:
                gateway = PaymentGateway.PAX;
                isPreauth = true;
                break;
            case DEBIT:
                gateway = PaymentGateway.PAX_DEBIT;
                break;
            case EBT_FOODSTAMP:
                gateway = PaymentGateway.PAX_EBT_FOODSTAMP;
                break;
            case EBT_CASH:
                gateway = PaymentGateway.PAX_EBT_CASH;
                break;
        }
        return PaxTransactionFactory.create(TcrApplication.get().getOperatorGuid(), orderGuid, gateway, isPreauth).setAmount(amount);
    }

    public boolean acceptPaxEbtEnabled() {
        return TcrApplication.get().getShopPref().acceptEbtCards().get();
    }

    public boolean acceptPaxDebitEnabled() {
        return TcrApplication.get().getShopPref().acceptDebitCards().get();
    }

    public boolean acceptPaxCreditEnabled() {
        return TcrApplication.get().getShopPref().acceptCreditCards().get();
    }

    private enum PaxMethod {
        CREDIT,
        DEBIT,
        EBT_FOODSTAMP,
        EBT_CASH,
        GIFT
    }

    public enum Error {
        UNDEFINED, SERVICE, PAX, PAX404, CONNECTIVITY, TIMEOUT
    }

}
