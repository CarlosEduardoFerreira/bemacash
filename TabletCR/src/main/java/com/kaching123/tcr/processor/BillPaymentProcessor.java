package com.kaching123.tcr.processor;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.BillingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.BillpaymentConfirmationPageDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.CategorySelectionFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.CategorySelectionFragmentDialog.CategorySelectionFragmentDialogCallback;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.CredentialsInputFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.CredentialsInputFragmentDialog.CredentialsInputFragmentDialogCallback;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.websvc.api.prepaid.BillPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.MasterBiller;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
public class BillPaymentProcessor {

    private PrepaidProcessorCallback callback;
    private String transactionMode;
    private String cashierId;
    private PrepaidUser user;
    private String orderGuid;
    private ArrayList<PaymentTransactionModel> transactions;
    private PaymentOption mChosenOption;

    public static BillPaymentProcessor create(PrepaidUser user, String transactionMode, String cashierId) {
        return new BillPaymentProcessor(user, transactionMode, cashierId);
    }

    public BillPaymentProcessor callback(PrepaidProcessorCallback callback) {
        this.callback = callback;
        return this;
    }

    public BillPaymentProcessor(PrepaidUser user, String transactionMode, String cashierId) {
        this.transactionMode = transactionMode;
        this.user = user;
        this.cashierId = cashierId;
    }

    public void init(final FragmentActivity context) {
        proceedToBillPayment(context);
    }

    protected void proceedToBillPayment(final FragmentActivity context) {
        CategorySelectionFragmentDialog.show(context, transactionMode, cashierId, user, new CategorySelectionFragmentDialogCallback() {

            @Override
            public void onCancel() {
                hide();
                callback.onCancel(context);
            }

            @Override
            public void onError(String message) {
                hide();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                callback.onCancel(context);
            }

            @Override
            public void onConditionSelected(Category chosenCategory, MasterBiller chosenBiller, PaymentOption chosenOption, BigDecimal amount, BillerLoadRecord bilelrData, String accountNumber) {
                hide();
                mChosenOption = chosenOption;
                proceedToDataInput(context, chosenCategory, chosenBiller, chosenOption, amount, bilelrData, accountNumber, BigDecimal.valueOf(chosenOption.feeAmount));
            }


            private void hide() {
                CategorySelectionFragmentDialog.hide(context);
            }
        });
    }

    protected void proceedToConfirmationPage(final FragmentActivity context,
                                             final Category chosenCategory,
                                             final MasterBiller chosenBiller,
                                             final PaymentOption chosenOption,
                                             final BigDecimal amount,
                                             final BillerLoadRecord billerData,
                                             final String accountNumber,
                                             final BillPaymentRequest formedRequest,
                                             final BigDecimal total,
                                             final BigDecimal transactionFee) {
        BillpaymentConfirmationPageDialog.show(context, chosenCategory, chosenBiller,
                chosenOption, amount, billerData, accountNumber, formedRequest, total, new BillpaymentConfirmationPageDialog.BillpaymentConfirmationPageDialogCallback() {
                    @Override
                    public void onCancel() {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onError(String message) {
                        hide();
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onComplete() {
                        hide();
                        callback.onPaymentRequested(context, total, billerData.vendorName, PrepaidType.BILL_PAYMENT, new SubProcessorDelegate() {
                            @Override
                            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
                                BillPaymentProcessor.this.orderGuid = orderGuid;
                                BillPaymentProcessor.this.transactions = transactions;
                                formedRequest.setOrderId(prepaidOrderId);
//                                proceedToBilling(context, formedRequest, prepaidOrderId, chosenCategory, amount, transactionFee, chosenBillPaymentItem);
                            }
                        }, transactionFee);

                    }

                    private void hide() {
                        BillpaymentConfirmationPageDialog.hide(context);
                    }
                }
        );
    }

    protected void proceedToDataInput(final FragmentActivity context,
                                      final Category chosenCategory,
                                      final MasterBiller chosenBiller,
                                      final PaymentOption chosenOption,
                                      final BigDecimal amount,
                                      final BillerLoadRecord billerData,
                                      final String accountNumber,
                                      final BigDecimal transactionFee) {
        CredentialsInputFragmentDialog.show(context,
                chosenCategory,
                chosenBiller,
                chosenOption,
                cashierId,
                user,
                transactionMode,
                amount,
                billerData,
                accountNumber,
                new CredentialsInputFragmentDialogCallback() {
                    @Override
                    public void onCancel() {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onError(String message) {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onComplete(final BillPaymentRequest formedRequest, BigDecimal total) {
                        hide();
                        proceedToConfirmationPage(context, chosenCategory, chosenBiller, chosenOption, amount, billerData, accountNumber, formedRequest, total, transactionFee);
//                        callback.onPaymentRequested(context, total, billerData.vendorName, PrepaidType.BILL_PAYMENT, new SubProcessorDelegate() {
//                            @Override
//                            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
//                                BillPaymentProcessor.this.orderGuid = orderGuid;
//                                BillPaymentProcessor.this.transactions = transactions;
//                                formedRequest.setOrderId(prepaidOrderId);
//                                proceedToBilling(context, formedRequest, prepaidOrderId, chosenCategory, amount);
//                            }
//                        });
                    }

                    private void hide() {
                        CredentialsInputFragmentDialog.hide(context);
                    }
                }
        );
    }

    protected void proceedToBilling(final FragmentActivity context,
                                    final BillPaymentRequest formedRequest, final long prepaidOrderId, final Category chosenCategory, final BigDecimal amount, final BigDecimal transactionFee, BillPaymentItem chosenBillPaymentItem) {
        BillingFragmentDialog.show(context, formedRequest, transactionFee, orderGuid,chosenBillPaymentItem, new BillingFragmentDialog.BillingFragmentDialogCallback() {

            @Override
            public void onError(String message) {
                hide();
                final Spannable messageSpannable;
                String mainString = context.getResources().getString(R.string.blackstone_pay_failure_body_1st);
                messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_pay_failure_body_constructor,
                        mainString, "The pre-paid gateway has responded with message : ", message));
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED),
                        0,
                        mainString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED),
                        messageSpannable.length() - message.length(),
                        messageSpannable.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                proceedToNotification(context, false, messageSpannable);
            }

            @Override
            public void onComplete(BillPaymentResponse response, BigDecimal transactionFee, String orderNum, String total, BillPaymentItem chosenBillPaymentItem) {
                hide();
//                IPrePaidInfo info = new BillPaymentInfo(mChosenOption);
                callback.onBillPaymentPrintRequested(context, orderGuid, transactions, null, chosenCategory, formedRequest, amount, transactionFee, orderNum, total);

            }
            private void hide() {
                BillingFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToNotification(final FragmentActivity context,
                                       final boolean success,
                                       final Spannable message) {

        PayNotificationFragmentDialog.show(context, false, success, false, message, false, new INotificationConfirmListener() {

            @Override
            public void onRetry() {
                // ignore
            }

            @Override
            public void onCancel() {
                hide();
                callback.onVoidRequested(context, orderGuid, transactions);
            }

            @Override
            public void onConfirmed() {
                hide();
                callback.onVoidRequested(context, orderGuid, transactions);
            }

            @Override
            public void onReload(Object UIFragent) {
                // ignore
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }
}
