package com.kaching123.tcr.processor;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.local.EndTransactionCommand;
import com.kaching123.tcr.commands.local.StartTransactionCommand;
import com.kaching123.tcr.commands.store.saleorder.AddBillPaymentOrderCommand.BaseAddBillPaymentOrderCallback;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderCommand;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.PrepaidTypeChoosingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.PrepaidTypeChoosingFragmentDialog.PrepaidTypeChoosingFragmentDialogCallback;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.PaymentFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidBillPaymentPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidSunPassPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidWirelessPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.TransactionMode;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.processor.MoneybackProcessor.IVoidCallback;
import com.kaching123.tcr.processor.PaymentProcessor.IPaymentProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PrepaidProcessor {

    private String cashierId;
    private PrepaidUser user;
    private Broker broker;
    private BigDecimal changeAmount;

    public static long generateId() {
        return (long) ((Math.random() * (Integer.MAX_VALUE - 1)) + 1);
    }

    public static PrepaidProcessor create() {
        return new PrepaidProcessor();
    }

    protected void exit(Context context) {
        exit(context, false);
    }

    protected void exit(Context context, boolean isSuccess) {
        EndTransactionCommand.start(context, isSuccess);
    }

    private PrepaidProcessor() {
    }

    public void init(final FragmentActivity context) {
        user = TcrApplication.get().getPrepaidUser();
        cashierId = TcrApplication.get().getOperator().login;

        StartTransactionCommand.start(context);
        startCycle(context);
    }

    protected void startCycle(final FragmentActivity context) {
        PrepaidTypeChoosingFragmentDialog.show(context, new PrepaidTypeChoosingFragmentDialogCallback() {
            @Override
            public void onCancel() {
                hide();
                exit(context);
            }

            @Override
            public void onTypeSelected(Broker broker) {
                hide();
                PrepaidProcessor.this.broker = broker;
                switch (broker) {
                    case LONG_DISTANCE:
                        proceedToLongDistance(context);
                        break;
                    case BILL_PAYMENT:
                        proceedToBillPayment(context);
                        break;
                    case SUNPASS:
                        proceedToSunpass(context);
                        break;
                    case WIRELESS_RECHARGE:
                        proceedToWireless(context, false);
                        break;
                    case INTERNATIONAL_TOPUP:
                        proceedToWireless(context, true);
                        break;
                    case PINLESS:
                        proceedToPinless(context);
                        break;
                }
            }

            private void hide() {
                PrepaidTypeChoosingFragmentDialog.hide(context);
            }
        });
    }

    protected void proceedToLongDistance(final FragmentActivity context) {
        WirelessProcessor.create(user, getPrepaidTransactionMode(), cashierId, false, broker).longDistanceMode().callback(callbackImpl).init(context);
    }

    protected void proceedToPinless(final FragmentActivity context) {
        WirelessProcessor.create(user, getPrepaidTransactionMode(), cashierId, false, broker).pinlessMode().callback(callbackImpl).init(context);
    }

    protected void proceedToBillPayment(final FragmentActivity context) {
        BillPaymentProcessor.create(user, getPrepaidTransactionMode(), cashierId).callback(callbackImpl).init(context);
    }

    protected void proceedToSunpass(final FragmentActivity context) {
        SunpassProcessor.create(user, getPrepaidTransactionMode(), cashierId).callback(callbackImpl).init(context);
    }

    protected void proceedToWireless(final FragmentActivity context, boolean international) {
        WirelessProcessor.create(user, getPrepaidTransactionMode(), cashierId, international, broker).callback(callbackImpl).init(context);
    }

    private String getPrepaidTransactionMode() {
        return TcrApplication.get().isTrainingMode() ? TransactionMode.getTransactionMode(true) : TcrApplication.get().getShopInfo().prepaidTransactionMode;
    }

    protected void proceedToPayment(final FragmentActivity context, BigDecimal amount, final String description, final PrepaidType type, final SubProcessorDelegate delegate, BigDecimal transactionFee) {

        PaymentFragmentDialog.show(context, Boolean.TRUE, amount, description, type, PrepaidProcessor.this.broker, transactionFee, new BaseAddBillPaymentOrderCallback() {

            @Override
            protected void handleSuccess(final String orderGuid, final long prepaidOrderId) {
                hide();
                PaymentProcessor.create(orderGuid, OrderType.PREPAID, null).callback(new IPaymentProcessor() {
                    @Override
                    public void onSuccess() {
                        delegate.onComplete(context, null, null, prepaidOrderId);
                    }

                    @Override
                    public void onCancel() {
                        RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                        startCycle(context);
                    }

                    @Override
                    public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount) {
                        PrepaidProcessor.this.changeAmount = changeAmount;
                        delegate.onComplete(context, order, list, prepaidOrderId);
                    }

                    @Override
                    public void onBilling(ArrayList<PaymentTransactionModel> successfullCCtransactionModels, List<SaleOrderItemViewModel> prepaidList) {

                    }

                    @Override
                    public void onRefund(HistoryDetailedOrderItemListFragment.RefundAmount amount) {

                    }

                }).setPrepaidMode().init(context);

            }

            @Override
            protected void handleFailure() {
                Logger.e("PrepaidProcessor: AddBillPaymentOrderCommand failed!");
                hide();
                startCycle(context);
            }

            private void hide() {
                PaymentFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info) {
        PrepaidPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                exit(context, true);
            }
        }, transactions, info, changeAmount);
    }

    private void proceedToVoid(final FragmentActivity context, final String orderGuid, final List<PaymentTransactionModel> transactions) {
        //we leave void for payment process, we can cancel transaction during multiple tendering
        MoneybackProcessor.create(orderGuid, OrderType.PREPAID).callback(new IVoidCallback() {


            @Override
            public void onVoidComplete(List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel) {
                RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                exit(context);
            }

            @Override
            public void onVoidFailure(BigDecimal ignore) {
                startCycle(context);
            }

            @Override
            public void onVoidCancelled() {
                startCycle(context);
            }
        }).childOrderModel(new SaleOrderModel(orderGuid))
                .initVoid(context,
                        null,
                        transactions,
                        TcrApplication.get().getBlackStoneUser(), true, true);
    }

    protected final PrepaidProcessorCallback callbackImpl = new PrepaidProcessorCallback() {
        @Override
        public void onComplete(FragmentActivity context) {
            exit(context, true);
        }

        @Override
        public void onCancel(FragmentActivity context) {
            startCycle(context);
        }

        @Override
        public void onPaymentRequested(FragmentActivity context, BigDecimal amount, String description, PrepaidType type, SubProcessorDelegate delegate, BigDecimal transactionFee) {
            proceedToPayment(context, amount, description, type, delegate, transactionFee);
        }

        @Override
        public void onPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info) {
            onCompleteWithPrint(context, orderGuid, transactions, info);
        }

        @Override
        public void onVoidRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions) {
            onVoid(context, orderGuid, transactions);
        }

        @Override
        public void onSunPassPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunReplenishmentRequest request, SunpassType type, BalanceResponse response, BigDecimal transactionFee,String orderNum, String total) {

            onCompleteWithSunPassPrint(context, orderGuid, transactions, info, type, request, response, transactionFee, orderNum, total);
        }

        @Override
        public void onSunPassPYDPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunPassDocumentPaymentRequest request, SunpassType type, BalanceResponse response, BigDecimal transactionFee, String orderNum, String total) {
            exit(context, true);
        }

        @Override
        public void onWirelessPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, WirelessItem chosenCategory, BigDecimal transactionFee, String orderNum, String total) {
            proceedToWirelessPrintFinish(context, orderGuid, transactions, info, chosenCategory, transactionFee, orderNum, total);

        }

        @Override
        public void onBillPaymentPrintRequested(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, Category chosenCategory, BillPaymentRequest request, BigDecimal amount, BigDecimal transactionFee, String orderNum, String total) {
            proceedToBillPaymentPrintFinish(context, orderGuid, transactions, info, chosenCategory, request, amount, transactionFee, orderNum, total);
        }
    };

    protected void onCompleteWithPrint(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info) {
        proceedToPrintFinish(context, orderGuid, transactions, info);
    }

    protected void onVoid(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions) {
        proceedToVoid(context, orderGuid, transactions);
    }

    protected void onCompleteWithSunPassPrint(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunpassType type, SunReplenishmentRequest request, BalanceResponse response, BigDecimal transactionFee,String orderNum, String total) {
        proceedToSunPassPrintFinish(context, orderGuid, transactions, info, type, request, response, transactionFee, orderNum, total);
    }

    private void proceedToSunPassPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunpassType type, SunReplenishmentRequest request, BalanceResponse response, BigDecimal transactionFee, String orderNum, String total) {
        PrepaidSunPassPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                exit(context, true);
            }
        }, transactions, info, type, request, response, changeAmount, transactionFee, orderNum, total, null, null);
    }

    private void proceedToWirelessPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, WirelessItem chosenCategory, BigDecimal transactionFee, String orderNum, String total) {
        PrepaidWirelessPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                exit(context, true);
            }
        }, transactions, info, chosenCategory, changeAmount, transactionFee, orderNum, total);
    }

    private void proceedToBillPaymentPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, Category chosenCategory, BillPaymentRequest request, BigDecimal amount, BigDecimal transactionFee, String orderNum, String total) {
//        PrepaidBillPaymentPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {
//
//            @Override
//            public void onConfirmed() {
//                exit(context, true);
//            }
//        }, transactions, info, chosenCategory, request, amount, changeAmount, transactionFee, orderNum, total);
    }
}
