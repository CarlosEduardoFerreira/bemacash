package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.IPaymentGateway;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.pax.PaxAddTipsCommand.PaxTipsCommandBaseCallback;
import com.kaching123.tcr.commands.payment.pax.PaxRefundCommand.PaxREFUNDCommandBaseCallback;
import com.kaching123.tcr.commands.payment.pax.PaxSettlementCommand.SettlementCommandBaseCallback;
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
        return new PaxGateway(PaxSaleCommand.TRANSACTION_ID_CREDIT_SALE, PaxSaleCommand.TRANSACTION_ID_CREDIT_REFUND, PaxMethod.CREDIT);
    }

    public static PaxGateway debit() {
        return new PaxGateway(PaxSaleCommand.TRANSACTION_ID_DEBIT_SALE, PaxSaleCommand.TRANSACTION_ID_DEBIT_REFUND, PaxMethod.DEBIT);
    }

    public static PaxGateway ebtFoodstamp() {
        return new PaxGateway(PaxSaleCommand.TRANSACTION_ID_EBT_FOODSTAMP_SALE, PaxSaleCommand.TRANSACTION_ID_EBT_REFUND, PaxMethod.EBT_FOODSTAMP);
    }

    public static PaxGateway ebtCash() {
        return new PaxGateway(PaxSaleCommand.TRANSACTION_ID_EBT_CASH_SALE, 0, PaxMethod.EBT_CASH);
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
        return PaxSaleCommand.startSale(
                context,
                PaxModel.get(),
                transaction,
                saleId,
                (PaxSaleCommand.PaxSaleCommandBaseCallback) callback);
    }

    public TaskHandler sale(Context context,
                            Object callback,
                            User user,
                            Void ignore,
                            Transaction transaction,
                            SaleActionResponse reloadResponse) {
        transaction.setCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        return PaxSaleCommand.startSaleFromData(
                context,
                PaxModel.get(),
                transaction,
                saleId,
                reloadResponse,
                (PaxSaleCommand.PaxSaleCommandBaseCallback) callback);
    }

    @Override
    public TaskHandler refund(Context context,
                              Object callback,
                              User user,
                              Void ignore,
                              PaymentTransactionModel transaction,
                              BigDecimal amount,
                              SaleOrderModel childOrderModel, boolean refundTips, boolean isManualReturn) {
        return PaxRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, refundTips, isManualReturn,
                (PaxREFUNDCommandBaseCallback) callback);
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
        return PaxRefundCommand.startReturn(context, PaxModel.get(), transaction, amount, childOrderModel, refundId, reloadResponse, refundTips, isManualReturn,
                (PaxREFUNDCommandBaseCallback) callback);
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
        return PaxRefundCommand.startReturn(context, PaxModel.get(), transaction, transaction.amount, childOrderModel, refundId, false, false,
                (PaxREFUNDCommandBaseCallback) callback);
    }
    public TaskHandler closePreauth(Context context,
                                    PaxTipsCommandBaseCallback callback,
                                    PaymentTransactionModel transactionModel,
                                    BigDecimal tipsAmount,
                                    String tipsComments,
                                    String tippedEmployeeId,
                                    SaleActionResponse reloadResponse) {
        return PaxAddTipsCommand.start(context, PaxModel.get(), transactionModel, tipsAmount, tipsComments, tippedEmployeeId, reloadResponse, callback);
    }

    public TaskHandler doSettlement(Context context,
                                    SettlementCommandBaseCallback callback) {
        return PaxSettlementCommand.start(context, PaxModel.get(), callback);
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
        EBT_CASH
    }
}
