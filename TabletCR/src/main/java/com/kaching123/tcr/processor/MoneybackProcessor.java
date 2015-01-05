package com.kaching123.tcr.processor;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.store.history.UpdateSaleOrderItemRefundQtyCommand;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment.RefundAmount;
import com.kaching123.tcr.fragment.tendering.pax.RefundPAXPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.IPaymentDialogListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PaySwipePendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PaySwipePendingFragmentDialog.ISaleSwipeListener;
import com.kaching123.tcr.fragment.tendering.refund.RefundCashFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundCashFragmentDialog.IRefundProgressListener;
import com.kaching123.tcr.fragment.tendering.refund.RefundCreditCardTransactionPickingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundCreditCardTransactionPickingFragmentDialog.ILoader;
import com.kaching123.tcr.fragment.tendering.refund.RefundCreditReceiptFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundOtherFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundTenderFragmentDialog;
import com.kaching123.tcr.fragment.tendering.refund.RefundTransPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.voiding.VoidPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.voiding.VoidProcessingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.voiding.VoidProcessingFragmentDialog.IVoidProgressListener;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.PaymentMethod;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.BlackStoneTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.processor.PaxReloadProcessor.IPAXReloadCallback;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import junit.framework.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan v. Rikhmayer
 *         This class represents all void and refund possible situations
 */
public class MoneybackProcessor {

    private String orderGuid;
    private OrderType orderType;
    private RefundAmount refund;
    private SaleOrderModel childOrderModel;
    private boolean allowImmediateCancel = true;
    private boolean isFakeRefund;


    private Set<IVoidCallback> voidCallback = new HashSet<IVoidCallback>();
    private Set<IRefundCallback> refundCallback = new HashSet<IRefundCallback>();

    private ArrayList<PaymentTransactionModel> fakeTransactions;

    protected static ArrayList<PaymentTransactionModel> completedTransactions = new ArrayList<PaymentTransactionModel>();

    private MoneybackProcessor(String orderGuid, OrderType orderType) {
        this.orderGuid = orderGuid;
        this.orderType = orderType;
    }

    /**
     * Create the working instance
     */
    public static MoneybackProcessor create(String orderGuid, OrderType orderType) {
        return new MoneybackProcessor(orderGuid, orderType);
    }

    public MoneybackProcessor callback(IRefundCallback refundCallback) {
        this.refundCallback.add(refundCallback);
        return this;
    }

    public MoneybackProcessor childOrderModel(SaleOrderModel childOrderModel) {
        this.childOrderModel = childOrderModel;
        return this;
    }

    public MoneybackProcessor removeCallback(IRefundCallback refundCallback) {
        this.refundCallback.remove(refundCallback);
        return this;
    }

    public MoneybackProcessor callback(IVoidCallback callback) {
        this.voidCallback.add(callback);
        return this;
    }

    public MoneybackProcessor initVoid(final FragmentActivity context,
                                       final ArrayList<RefundSaleItemInfo> refundItemsInfo,
                                       final List<PaymentTransactionModel> transactions,
                                       final User user,
                                       final boolean proposeCheckOnSuccess, final boolean needToCancel) {
        final ArrayList<PaymentTransactionModel> transactionModels = new ArrayList<PaymentTransactionModel>(transactions.size());
        BigDecimal returnAmount = BigDecimal.ZERO;
        for (PaymentTransactionModel model : transactions) {
            returnAmount = returnAmount.add(model.amount);
            transactionModels.add(model);
        }
        if (!needToCancel && TcrApplication.get().getShopInfo().useCreditReceipt) {
            RefundCreditReceiptFragmentDialog.show(context, childOrderModel, transactionModels, returnAmount, false, false, new RefundCreditReceiptFragmentDialog.IRefundCreditProgressListener() {

                @Override
                public void onComplete(BigDecimal amountAfterRefund, ArrayList<PaymentTransactionModel> refundChildTransactions, SaleOrderModel childOrderModel) {
                    new VoidCompleteListener(context, refundItemsInfo, transactionModels, user, proposeCheckOnSuccess) {
                        @Override
                        protected void hide() {

                        }
                    }.onComplete(refundChildTransactions, childOrderModel);
                }

                @Override
                public void onFailed() {
                    new VoidCompleteListener(context, refundItemsInfo, transactionModels, user, proposeCheckOnSuccess) {
                        @Override
                        protected void hide() {

                        }
                    }.onCancel();
                }
            });
        } else {
            VoidProcessingFragmentDialog.show(context, transactionModels, user, new VoidCompleteListener(context, refundItemsInfo, transactionModels, user, proposeCheckOnSuccess) {
                @Override
                protected void hide() {
                    VoidProcessingFragmentDialog.hide(context);
                }
            }, childOrderModel, needToCancel);
        }
        return this;
    }

    private abstract class VoidCompleteListener implements IVoidProgressListener {

        final ArrayList<RefundSaleItemInfo> refundItemsInfo;
        final List<PaymentTransactionModel> transactionModels;
        final User user;
        final boolean proposeCheckOnSuccess;
        final FragmentActivity context;

        protected VoidCompleteListener(FragmentActivity context, ArrayList<RefundSaleItemInfo> refundItemsInfo, ArrayList<PaymentTransactionModel> transactionModels, User user, boolean proposeCheckOnSuccess) {
            this.context = context;
            this.refundItemsInfo = refundItemsInfo;
            this.transactionModels = transactionModels;
            this.user = user;
            this.proposeCheckOnSuccess = proposeCheckOnSuccess;
        }

        @Override
        public void onComplete(final List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel) {
            MoneybackProcessor.this.childOrderModel = childOrderModel;
            Logger.d("The voiding onComplete");
            hide();
            if (completed.isEmpty()) {
                notifyFailure(BigDecimal.ZERO);
                return;
            }
            if (completed.size() != transactionModels.size()) {
                BigDecimal orderValue = BigDecimal.ZERO;
                for (PaymentTransactionModel transactionModel : transactionModels) {
                    orderValue = orderValue.add(transactionModel.availableAmount);
                }
                BigDecimal pickedValue = BigDecimal.ZERO;
                for (PaymentTransactionModel transactionModel : completed) {
                    pickedValue = pickedValue.add(transactionModel.amount);
                }

                BigDecimal amountReturned = pickedValue;
                pickedValue = orderValue.subtract(pickedValue);
                final RefundAmount amount = new RefundAmount(pickedValue, orderValue);

                amount.itemsInfo = refundItemsInfo;


                notifyFailure(amountReturned);
                return;
            }
            if (!proposeCheckOnSuccess) {
                completeProcess(context, refundItemsInfo);
                notifyComplete(completed);
                return;
            }
            proceedToVoidPrintAndFinish(context,
                    orderGuid,
                    refundItemsInfo,
                    new ArrayList<PaymentTransactionModel>(completed),
                    new VoidPrintAndFinishFragmentDialog.IFinishConfirmListener() {
                        @Override
                        public void onConfirmed() {
                            MoneybackProcessor.this.completedTransactions.clear();
                            notifyComplete(completed);
                        }
                    }
            );
        }

        @Override
        public void onCancel() {
            hide();
            notifyFailure(BigDecimal.ZERO);
        }

        private void notifyComplete(final List<PaymentTransactionModel> completed) {
            for (IVoidCallback callback : voidCallback) {
                callback.onVoidComplete(completed, childOrderModel);
            }
        }

        private void notifyFailure(BigDecimal returned) {
            for (IVoidCallback callback : voidCallback) {
                callback.onVoidFailure(returned);
            }
        }

        protected abstract void hide();
    }

    public MoneybackProcessor initRefundForFake(final FragmentActivity context,
                                                final RefundAmount refund,
                                                final ArrayList<PaymentTransactionModel> transactions,
                                                final User user,
                                                final boolean allowImmediateCancel) {
        this.allowImmediateCancel &= allowImmediateCancel;
        this.refund = refund;
        isFakeRefund = true;
        this.fakeTransactions = transactions;
        proceedToRefund(context, refund.pickedValue, refund.pickedValue, transactions, user);

        return this;
    }

    public MoneybackProcessor initRefund(final FragmentActivity context,
                                         PaymentMethod method,
                                         final ArrayList<PaymentTransactionModel> transactions,
                                         final RefundAmount refund,
                                         final User user,
                                         final boolean allowImmediateCancel) {
        this.allowImmediateCancel &= allowImmediateCancel;
        this.refund = refund;
        if (method == null) {
            startCycle(context, refund.pickedValue, user);
        } else {
            //need to check shop's return policy
            if (TcrApplication.get().getShopInfo().useCreditReceipt) {
                method = PaymentMethod.CREDIT_RECEIPT;
            }
            proceedToRefund(context, method, transactions, refund.pickedValue, user);
        }

        return this;
    }

    /**
     * Set the working context
     */
    private MoneybackProcessor startCycle(final FragmentActivity context, final BigDecimal amountLeft, final User user) {
        Logger.d("SHELL payment : %s left", UiHelper.valueOf(amountLeft));
        proceedToRefund(context, refund.pickedValue, amountLeft, user);

        return this;
    }

    private void proceedToRefund(final FragmentActivity context,
                                 final BigDecimal totalAmountToReturn,
                                 final BigDecimal pendingAmountToReturn,
                                 final User user) {
        proceedToRefund(context, totalAmountToReturn, pendingAmountToReturn, fakeTransactions, user);
    }

    private void proceedToRefund(final FragmentActivity context,
                                 final BigDecimal totalAmountToReturn,
                                 final BigDecimal pendingAmountToReturn,
                                 final ArrayList<PaymentTransactionModel> transactions,
                                 final User user) {
        RefundTenderFragmentDialog.show(context,
                orderGuid,
                orderType,
                totalAmountToReturn,
                pendingAmountToReturn,
                transactions,
                new IPaymentDialogListener.IRefundListener() {

                    @Override
                    public void onSingleTenderCheck(boolean singleTenderEnabled) {
                    }

                    @Override
                    public void onOtherMethodsShown(boolean otherMethodsShown) {
                    }

                    @Override
                    public void onCancel() {
                        hide();
                        notifyCancel();
                    }

                    @Override
                    public void onRefundMethodSelected(PaymentMethod method, List<PaymentTransactionModel> transactions) {
                        hide();
                        MoneybackProcessor.this.proceedToRefund(context, method, transactions, pendingAmountToReturn, user);
                    }

                    @Override
                    public void onDataLoaded(BigDecimal alreadyPayed, BigDecimal orderTotal, ArrayList<PaymentTransactionModel> transactions) {
                    }

                    private void hide() {
                        RefundTenderFragmentDialog.hide(context);
                    }

                    private void notifyCancel() {
                        for (IRefundCallback callback : refundCallback) {
                            callback.onRefundCancelled();
                        }
                    }
                }, 0, allowImmediateCancel
        );
    }

    private void proceedToRefund(FragmentActivity context, PaymentMethod method, List<PaymentTransactionModel> transactions, BigDecimal pendingAmountToReturn, User user) {
        switch (method) {
            case CASH:
                ArrayList<PaymentTransactionModel> blackstoneTransactions = new ArrayList<PaymentTransactionModel>();
                for (PaymentTransactionModel item : transactions) {
                    blackstoneTransactions.add(item);
                }
                proceedToCashRefund(context, blackstoneTransactions, pendingAmountToReturn, user, false);
                break;
            case CREDIT_CARD:
            case PAX:
            case PAX_DEBIT:
            case PAX_EBT_FOODSTAMP:
                PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
                proceedToCreditCardRefund(context, transactions, pendingAmountToReturn, user, TcrApplication.get().isPaxConfigured() && paxGateway.acceptPaxCreditEnabled());
                break;
            case CREDIT_RECEIPT:
                proceedToCreditReceiptRefund(context, new ArrayList<PaymentTransactionModel>(transactions), pendingAmountToReturn, user);
                break;
            case OFFLINE_CREDIT:
                proceedToOfflineCreditRefund(context, new ArrayList<PaymentTransactionModel>(transactions), pendingAmountToReturn, user);
                break;
        }
    }

    private void proceedToPAXRefund(final FragmentActivity context,
                                    final PaymentTransactionModel transaction,
                                    final BigDecimal amount) {
        proceedToPAXRefund(context, transaction, amount, null);
    }

    private void proceedToPAXRefund(final FragmentActivity context,
                                    final PaymentTransactionModel transaction,
                                    final BigDecimal amount,
                                    final SaleActionResponse reloadResponse) {
        final BigDecimal refundAmount;
        if (amount.compareTo(transaction.availableAmount) == 1) {
            refundAmount = transaction.availableAmount;
        } else {
            refundAmount = amount;
        }
        RefundPAXPendingFragmentDialog.show(context,
                transaction.toTransaction(),
                refundAmount, reloadResponse, childOrderModel, new RefundPAXPendingFragmentDialog.IRefundProgressListener() {

                    @Override
                    public void onComplete(PaymentTransactionModel child,
                                           Transaction parent,
                                           SaleOrderModel childOrderModel,
                                           String reason) {
                        MoneybackProcessor.this.childOrderModel = childOrderModel;
                        Assert.assertNotNull(parent);

                        final Spannable messageSpannable;
                        String message = reason;
                        boolean success;
                        if (success = child != null) {
                            message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                            messageSpannable = new SpannableString(message);
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            String mainString = "";
                            messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_pay_failure_body_constructor,
                                    mainString, "", message));
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, mainString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), messageSpannable.length() - message.length(),
                                    messageSpannable.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        hide();
                        completedTransactions.add(child);

                        proceedToCardPaymentNotification(context, false, parent.allowReload, amount, refundAmount,
                                success, messageSpannable, transaction, parent, null, null);
                    }

                    @Override
                    public void onCancel() {
                        hide();
                        startCycle(context, amount, TcrApplication.get().getBlackStoneUser());
                    }

                    private void hide() {
                        RefundPAXPendingFragmentDialog.hide(context);
                    }
                }, refund.refundTips, isFakeRefund
        );
    }

    private void proceedToCreditCardRefund(final FragmentActivity context,
                                           final List<PaymentTransactionModel> transactions,
                                           final BigDecimal amount,
                                           final User user,
                                           final boolean isPax) {
        List<PaymentTransactionModel> blackstoneTransactions = new ArrayList<PaymentTransactionModel>();

        for (PaymentTransactionModel item : transactions) {
            if (!PaymentType.SALE.equals(item.paymentType)) {
                Logger.d("We skip this unwanted transaction - it is no SALE one");
                continue;
            }
            if (!isPax && PaymentGateway.BLACKSTONE.equals(item.gateway)
                    || isPax && PaymentGateway.PAX.equals(item.gateway)
                    || isPax && PaymentGateway.PAX_EBT_FOODSTAMP.equals(item.gateway)
                    || isPax && PaymentGateway.PAX_DEBIT.equals(item.gateway)) {
                blackstoneTransactions.add(item);
            }
        }

        if (blackstoneTransactions.size() == 1) {
            if (isPax) {
                proceedToPAXRefund(context, blackstoneTransactions.get(0), amount);
            } else {
                proceedToCardSwipe(context, blackstoneTransactions.get(0), user, amount);
            }
        } else if (blackstoneTransactions.size() > 1) {
            RefundCreditCardTransactionPickingFragmentDialog.show(context, blackstoneTransactions, amount, new ILoader() {

                @Override
                public void onCancel() {
                    hide();
                    startCycle(context, amount, user);
                }

                @Override
                public void onItemClicked(PaymentTransactionModel item) {
                    hide();
                    if (isPax) {
                        proceedToPAXRefund(context, item, amount);
                    } else {
                        proceedToCardSwipe(context, item, user, amount);
                    }
                }

                private void hide() {
                    RefundCreditCardTransactionPickingFragmentDialog.hide(context);
                }
            });
        } else {
            Toast.makeText(context, String.format("One or more transactions were not eligible for %s swipe, please make sure the PAX is %s.",
                    isPax ? "MSR" : "PAX", isPax ? "disabled" : "configured"), Toast.LENGTH_LONG).show();
            startCycle(context, refund.pickedValue, user);
        }
    }

    /**
     * Follow with the card payment
     */
    private void proceedToCardSwipe(final FragmentActivity context, final PaymentTransactionModel transaction, final User user, final BigDecimal amount) {
        PaySwipePendingFragmentDialog.show(context,/* true,*/ true, new ISaleSwipeListener() {

            @Override
            public void onSwiped(String track) {
                hide();
                CreditCard card = new CreditCard(track);
                proceedCreditCardRefund(context, transaction, card, user, amount);
            }

            @Override
            public void onCardInfo(String number, String expireDate, String cvn, String zip) {
                hide();
                CreditCard card = new CreditCard(zip, number, expireDate, null, cvn, null);
                proceedCreditCardRefund(context, transaction, card, user, amount);
            }

            @Override
            public void onCancel() {
                hide();
                startCycle(context, amount, user);
            }

            private void hide() {
                PaySwipePendingFragmentDialog.hide(context);
            }
        });
    }

    private void proceedCreditCardRefund(final FragmentActivity context,
                                         final PaymentTransactionModel transactionModel,
                                         final CreditCard card,
                                         final User user,
                                         final BigDecimal amount) {
        BigDecimal refundAmount = amount;
        if (amount.compareTo(transactionModel.availableAmount) == 1) {
            refundAmount = transactionModel.availableAmount;
        }
        RefundTransPendingFragmentDialog.show(context,
                new BlackStoneTransaction(transactionModel),
                card, user, refundAmount, new RefundTransPendingFragmentDialog.IRefundProgressListener() {


                    @Override
                    public void onComplete(PaymentTransactionModel child, RefundResponse result,
                                           ErrorReason reason, Transaction parent, SaleOrderModel childOrderModel) {
                        hide();
                        MoneybackProcessor.this.childOrderModel = childOrderModel;
//                Assert.assertNotNull(parent);

                        String message;
                        final Spannable messageSpannable;

                        final int messageClarification;
                        TransactionStatusCode rCode = result == null ? null : result.getResponseCode();
                        final boolean success = rCode != null && rCode.success();
                        boolean allowRetry = rCode != null && rCode.retryMayHelp();
                        boolean codeNotNull = rCode != null;
                        if (success) {
                            message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                            messageSpannable = new SpannableString(message);
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        } else {
                            if (codeNotNull) {
                                message = rCode.getDescription();
                                messageClarification = R.string.blackstone_pay_failure_body_2nd;
                            } else if (reason != null) {
                                message = reason.getDescription();
                                messageClarification = R.string.blackstone_pay_failure_body_3nd;
                            } else {
                                message = ErrorReason.UNKNOWN.getDescription();
                                messageClarification = R.string.blackstone_pay_failure_body_3nd;
                            }
                            String mainString = context.getResources().getString(R.string.blackstone_pay_failure_body_1st);
                            messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_pay_failure_body_constructor,
                                    mainString, context.getResources().getString(messageClarification), message));
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, mainString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), messageSpannable.length() - message.length(),
                                    messageSpannable.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }


                        completedTransactions.add(child);

                        proceedToCardPaymentNotification(context, allowRetry, false, amount, parent != null ? parent.amount : null, success, messageSpannable, transactionModel, parent, card, user);

                    }

                    @Override
                    public void onCancel() {
                        hide();
                        startCycle(context, amount, user);
                    }

                    private void hide() {
                        RefundTransPendingFragmentDialog.hide(context);
                    }
                }, childOrderModel, refund.refundTips, isFakeRefund
        );
    }

    /**
     * ************************************************************ CASH ********************************************************************
     */

    private void proceedToCreditReceiptRefund(final FragmentActivity context,
                                              final ArrayList<PaymentTransactionModel> transactions,
                                              final BigDecimal amount,
                                              final User user) {
        RefundCreditReceiptFragmentDialog.show(context, childOrderModel, transactions, amount, false, isFakeRefund, new RefundCreditReceiptFragmentDialog.IRefundCreditProgressListener() {

            @Override
            public void onComplete(BigDecimal amountAfterRefund,
                                   ArrayList<PaymentTransactionModel> refundChildTransactions,
                                   SaleOrderModel childOrderModel) {
                MoneybackProcessor.this.childOrderModel = childOrderModel;
                if (BigDecimal.ZERO.compareTo(amountAfterRefund) == 0) {
                    proceedToPrintAndFinish(context,
                            orderGuid,
                            refund.itemsInfo,
                            refund.refundTips,
                            refundChildTransactions);
                } else {
                    startCycle(context, amountAfterRefund, user);
                }
            }

            @Override
            public void onFailed() {
                startCycle(context, amount, user);
            }
        });
    }

    private void proceedToCashRefund(final FragmentActivity context,
                                     final ArrayList<PaymentTransactionModel> transactions,
                                     final BigDecimal amount,
                                     final User user,
                                     final boolean useOnlyCashTransaction) {
        RefundCashFragmentDialog.show(context, transactions, amount, new IRefundProgressListener() {

            @Override
            public void onComplete(BigDecimal amountAfterRefund,
                                   ArrayList<PaymentTransactionModel> refundChildTransactions,
                                   SaleOrderModel childOrderModel) {
                MoneybackProcessor.this.childOrderModel = childOrderModel;
                hide();
                if (BigDecimal.ZERO.compareTo(amountAfterRefund) == 0) {
                    proceedToPrintAndFinish(context,
                            orderGuid,
                            refund.itemsInfo,
                            refund.refundTips,
                            refundChildTransactions);
                } else {
                    startCycle(context, amountAfterRefund, user);
                }
            }

            private void hide() {
                RefundCashFragmentDialog.hide(context);
            }
        }, useOnlyCashTransaction, childOrderModel, refund.refundTips, isFakeRefund);
    }

    private void proceedToOfflineCreditRefund(final FragmentActivity context,
                                              final ArrayList<PaymentTransactionModel> transactions,
                                              final BigDecimal amount,
                                              final User user) {
        RefundOtherFragmentDialog.show(context, transactions, amount, new RefundOtherFragmentDialog.IRefundProgressListener() {

            @Override
            public void onComplete(BigDecimal amountAfterRefund,
                                   ArrayList<PaymentTransactionModel> refundChildTransactions,
                                   SaleOrderModel childOrderModel) {
                MoneybackProcessor.this.childOrderModel = childOrderModel;
                hide();
                if (BigDecimal.ZERO.compareTo(amountAfterRefund) == 0) {
                    proceedToPrintAndFinish(context,
                            orderGuid,
                            refund.itemsInfo,
                            refund.refundTips,
                            refundChildTransactions);
                } else {
                    startCycle(context, amountAfterRefund, user);
                }
            }

            private void hide() {
                RefundOtherFragmentDialog.hide(context);
            }
        }, RefundOtherFragmentDialog.Type.OFFLINE_CREDIT, childOrderModel, isFakeRefund);
    }

    /*************************************************************** NOTIFY *********************************************************************/

    /**
     * Notify the user about the result and wait for further commands
     */
    private void proceedToCardPaymentNotification(final FragmentActivity context,
                                                  final boolean allowRetry,
                                                  final boolean allowReload,
                                                  final BigDecimal amountPending,
                                                  final BigDecimal amountRefunded,
                                                  final boolean success,
                                                  final Spannable message,
                                                  final PaymentTransactionModel transaction,
                                                  final Transaction parent,
                                                  final CreditCard card,
                                                  final User user) {

        PayNotificationFragmentDialog.show(context, allowRetry, success, false, message, allowReload, new INotificationConfirmListener() {

            @Override
            public void onReload(final Object UIFragent) {
                final PaxTransaction paxTransaction = new PaxTransaction(transaction);
                PaxReloadProcessor.get().reload(context, paxTransaction, amountRefunded, new IPAXReloadCallback() {
                    @Override
                    public void onStart() {
                        ((PayNotificationFragmentDialog) UIFragent).startSwirl(true);
                    }

                    @Override
                    public void onComplete(SaleActionResponse reloadResponse) {
                        hide();
                        proceedToPAXRefund(context, transaction, amountPending, reloadResponse);
                    }

                    @Override
                    public void onError(String errorMessage, boolean allowFurtherReload) {
                        ((PayNotificationFragmentDialog) UIFragent).startSwirl(false);
                        ((PayNotificationFragmentDialog) UIFragent).enableReload(allowFurtherReload, errorMessage);
                    }
                });
            }

            @Override
            public void onRetry() {
                hide();
                proceedCreditCardRefund(context, transaction, card, user, amountPending);
            }

            @Override
            public void onCancel() {
                hide();
                AlertDialogFragment.show(context, AlertDialogFragment.DialogType.CONFIRM,
                        R.string.dlg_process_refund_title,
                        context.getResources().getString(R.string.dlg_process_refund_msg),
                        R.string.btn_ok,
                        new StyledDialogFragment.OnDialogClickListener() {

                            @Override
                            public boolean onClick() {
                                startCycle(context, amountPending, user);
                                return true;
                            }
                        },
                        new OnDialogClickListener() {
                            @Override
                            public boolean onClick() {
                                notifyCancel();
                                return true;
                            }
                        },
                        null
                );
            }

            private void notifyCancel() {
                for (IRefundCallback callback : refundCallback) {
                    callback.onRefundCancelled();
                }
            }

            @Override
            public void onConfirmed() {
                hide();
                assert (transaction != null);
                BigDecimal newAmount = amountPending.subtract((amountRefunded));
                if (success && newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    ArrayList<PaymentTransactionModel> list = new ArrayList<PaymentTransactionModel>();
                    list.add(transaction);
                    proceedToPrintAndFinish(context, orderGuid, refund.itemsInfo, refund.refundTips, completedTransactions);
                } else {
                    startCycle(context, newAmount, user);
                }
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }

    /**
     * ************************************************************ PRINT ********************************************************************
     */

    private void proceedToVoidPrintAndFinish(final FragmentActivity context,
                                             String orderGuid,
                                             final ArrayList<RefundSaleItemInfo> refundItemsInfo,
                                             final ArrayList<PaymentTransactionModel> refundTransactions,
                                             VoidPrintAndFinishFragmentDialog.IFinishConfirmListener listener) {
        completeProcess(context, refundItemsInfo);
        VoidPrintAndFinishFragmentDialog.show(context, orderGuid, refundItemsInfo, refundTransactions, listener, childOrderModel, false);

    }

    private void proceedToPrintAndFinish(final FragmentActivity context,
                                         String orderGuid,
                                         final ArrayList<RefundSaleItemInfo> refundItemsInfo,
                                         final boolean refundTips,
                                         final ArrayList<PaymentTransactionModel> refundTransactions) {
        completeProcess(context, refundItemsInfo);
        VoidPrintAndFinishFragmentDialog.show(context, orderGuid, refundItemsInfo, refundTransactions, new VoidPrintAndFinishFragmentDialog.IFinishConfirmListener() {
            @Override
            public void onConfirmed() {
                MoneybackProcessor.this.completedTransactions.clear();
                finish(true);
            }
        }, childOrderModel, true);
    }

    protected void completeProcess(final FragmentActivity context,
                                   final ArrayList<RefundSaleItemInfo> refundItemsInfo) {
        //TODO: interrupted refund not handled
        if ((refundItemsInfo != null && refundItemsInfo.size() > 0)) {
            UpdateSaleOrderItemRefundQtyCommand.start(context, this, refundItemsInfo, UnitItemCache.get().getUnitsList(), childOrderModel);
            UnitItemCache.get().reset();
        }
    }

    private void finish(boolean success) {
        if (success) {
            for (IRefundCallback callback : refundCallback) {
                callback.onRefundComplete(childOrderModel);
            }
            return;
        }
        for (IRefundCallback callback : refundCallback) {
            callback.onRefundFailure();
        }
    }

    public static interface IRefundCallback {

        public void onRefundComplete(SaleOrderModel childOrderModel);

        public void onRefundCancelled();

        public void onRefundFailure();
    }

    public static interface IVoidCallback {

        public void onVoidComplete(List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel);

        public void onVoidFailure(BigDecimal returned);

        public void onVoidCancelled();
    }

    public static class RefundSaleItemInfo implements Serializable {

        public final String saleItemGuid;
        public final BigDecimal qty;

        public RefundSaleItemInfo(String saleItemGuid, BigDecimal qty) {
            this.saleItemGuid = saleItemGuid;
            this.qty = qty;
        }
    }
}
