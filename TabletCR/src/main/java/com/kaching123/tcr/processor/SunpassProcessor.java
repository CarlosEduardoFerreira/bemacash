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
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunCredentialsFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunPassChoiceDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunPayYourDocumentCredentialsFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassAmountFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassBillingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassBillingFragmentDialog.SunpassBillingFragmentDialogCallback;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassConfirmationPageDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassPYDConfirmationPageDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassPayYourDocumentAmountFragmentDialog;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.ReplenishmentResponse;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
public class SunpassProcessor {

    private PrepaidProcessorCallback callback;
    private String transactionMode;
    private String cashierId;
    private PrepaidUser user;
    private String orderGuid;
    private ArrayList<PaymentTransactionModel> transactions;

    private SunpassType type;

    public static SunpassProcessor create(PrepaidUser user, String transactionMode, String cashierId) {
        return new SunpassProcessor(user, transactionMode, cashierId);
    }

    public SunpassProcessor callback(PrepaidProcessorCallback callback) {
        this.callback = callback;
        return this;
    }

    public SunpassProcessor(PrepaidUser user, String transactionMode, String cashierId) {
        this.transactionMode = transactionMode;
        this.user = user;
        this.cashierId = cashierId;
    }

    public void init(final FragmentActivity context) {
        proceedToSunpass(context);
    }

    protected void proceedToSunpass(final FragmentActivity context) {
        SunPassChoiceDialog.show(context, new SunPassChoiceDialog.SunPassChoiceCallback() {
            @Override
            public void onTrensponder(FragmentActivity context) {
                hide();
                type = SunpassType.SUNPASS_TRANSPONDER;
                SunPass_setOnTransponder(context);

            }

            @Override
            public void onPayYourDocument(FragmentActivity context) {
                hide();
                type = SunpassType.SUNPASS_PAY_YOUR_DOCUMENT;
                SunPass_setOnPayYourDocument(context);
            }

            @Override
            public void cancel() {
                hide();
                callback.onCancel(context);
            }

            private void hide() {
                SunPassChoiceDialog.hide(context);
            }
        });
    }

    private void SunPass_setOnPayYourDocument(final FragmentActivity context) {
        SunPayYourDocumentCredentialsFragmentDialog.show(context, cashierId, user, transactionMode,
                new SunPayYourDocumentCredentialsFragmentDialog.SunDocumentCredentialsFragmentDialogCallback() {
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
                    public void onComplete(String accountNumber, DocumentInquiryResponse response, String licensePlateNumber, BigDecimal transcationFee) {
                        hide();
                        proceedToPayYourDocumentAmountInput(context, response, accountNumber, licensePlateNumber, transcationFee);
                    }

                    private void hide() {
                        SunCredentialsFragmentDialog.hide(context);
                    }
                }
        );
    }

    private void SunPass_setOnTransponder(final FragmentActivity context) {
        SunCredentialsFragmentDialog.show(context, cashierId, user, transactionMode,
                new SunCredentialsFragmentDialog.SunCredentialsFragmentDialogCallback() {
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
                    public void onComplete(String accountNumber, BalanceResponse response, String amount, BigDecimal transcationFee) {
                        hide();
                        proceedToConfirmationPage(context, response, accountNumber, amount, transcationFee);
                    }

                    private void hide() {
                        SunCredentialsFragmentDialog.hide(context);
                    }
                }
        );
    }

    private void proceedToPayYourDocumentAmountInput(final FragmentActivity context, final DocumentInquiryResponse response, final String accountNumber, String licensePlateNumber, BigDecimal transcationFee) {
        SunpassPayYourDocumentAmountFragmentDialog.show(context, accountNumber, transactionMode,
                cashierId, user, response, licensePlateNumber, new SunpassPayYourDocumentAmountFragmentDialog.SunpassPayYourDocumentAmountFragmentDialogCallback() {
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
                    public void onComplete(final SunPassDocumentPaymentRequest request, BigDecimal transcationFee) {
                        hide();
                        proceedPYDToConfirmationPage(context, response, accountNumber, request, transcationFee);
                    }


                    private void hide() {
                        SunpassAmountFragmentDialog.hide(context);
                    }
                }
        );
    }

    private void proceedPYDToConfirmationPage(final FragmentActivity context, final DocumentInquiryResponse response, String accountNumber, SunPassDocumentPaymentRequest request, final BigDecimal transcationFee) {
        SunpassPYDConfirmationPageDialog.show(context, accountNumber, transactionMode,
                cashierId, user, request, response, type, new SunpassPYDConfirmationPageDialog.SunpassPYDConfirmationPageDialogCallback() {
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
                    public void onComplete(final SunPassDocumentPaymentRequest request) {
                        hide();
                        callback.onPaymentRequested(context, request.amount, context.getString(R.string.sunpass_prepaid_item_description), PrepaidType.SUNPASS, new SubProcessorDelegate() {

                            @Override
                            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
                                SunpassProcessor.this.orderGuid = orderGuid;
                                SunpassProcessor.this.transactions = transactions;
                                request.setOrderId(prepaidOrderId);
                                proceedToBilling(context, request, null, response, prepaidOrderId, transcationFee);
                            }
                        }, transcationFee);
                    }

                    private void hide() {
                        SunpassPYDConfirmationPageDialog.hide(context);
                    }
                }
        );
    }

    private void proceedToConfirmationPage(final FragmentActivity context, final BalanceResponse response, String accountNumber, String amount, final BigDecimal transcationFee) {
        SunpassConfirmationPageDialog.show(context, accountNumber, transactionMode,
                cashierId, user, response, amount, type, new SunpassConfirmationPageDialog.SunpassConfirmationPageDialogCallback() {
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
                    public void onComplete(final SunReplenishmentRequest request) {
                        hide();
                        callback.onPaymentRequested(context, request.amount, context.getString(R.string.sunpass_prepaid_item_description), PrepaidType.SUNPASS, new SubProcessorDelegate() {

                            @Override
                            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
                                SunpassProcessor.this.orderGuid = orderGuid;
                                SunpassProcessor.this.transactions = transactions;
                                request.setOrderId(prepaidOrderId);
                                proceedToBilling(context, request, null, response, prepaidOrderId, transcationFee);
                            }
                        }, transcationFee);
                    }

                    private void hide() {
                        SunpassConfirmationPageDialog.hide(context);
                    }
                }
        );
    }
//    private void proceedToAmountInput(final FragmentActivity context, final BalanceResponse response, String accountNumber, String amount) {
//        SunpassAmountFragmentDialog.show(context, accountNumber, transactionMode,
//                cashierId, user, response, amount, new SunpassAmountFragmentDialogCallback() {
//                    @Override
//                    public void onCancel() {
//                        hide();
//                        callback.onCancel(context);
//                    }
//
//                    @Override
//                    public void onError(String message) {
//                        hide();
//                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//                        callback.onCancel(context);
//                    }
//
//                    @Override
//                    public void onComplete(final SunReplenishmentRequest request) {
//                        hide();
//                        callback.onPaymentRequested(context, request.amount, context.getString(R.string.sunpass_prepaid_item_description), PrepaidType.SUNPASS, new SubProcessorDelegate() {
//
//                            @Override
//                            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
//                                SunpassProcessor.this.orderGuid = orderGuid;
//                                SunpassProcessor.this.transactions = transactions;
//                                request.setOrderId(prepaidOrderId);
//                                proceedToBilling(context, request, null,response, prepaidOrderId);
//                            }
//                        });
//                    }
//
//                    private void hide() {
//                        SunpassAmountFragmentDialog.hide(context);
//                    }
//                });
//    }

    protected void proceedToBilling(final FragmentActivity context, final SunReplenishmentRequest request, final SunPassDocumentPaymentRequest dRequest, final BalanceResponse response, final long prepaidOrderId, final BigDecimal transactionFee) {
        SunpassBillingFragmentDialog.show(context, request, type, orderGuid, new SunpassBillingFragmentDialogCallback() {

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
            public void onComplete(ReplenishmentResponse response, BalanceResponse balanceResponse, BigDecimal transactionFee, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new SunpassInfo(request.accountNumber, request.purchaseId, response.referenceNumber);
                IPrePaidInfo info2 = new SunpassInfo();

//                callback.onPrintRequested(context, orderGuid, transactions, info);
                callback.onSunPassPrintRequested(context, orderGuid, transactions, info, request, type, balanceResponse, transactionFee, orderNum, total);
            }

            @Override
            public void onComplete(DocumentPaymentResponse result, DocumentInquiryResponse balanceResponse, BigDecimal transactionFee, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new SunpassInfo(request.accountNumber, request.purchaseId, response.referenceNumber);
                IPrePaidInfo info2 = new SunpassInfo();

//                callback.onPrintRequested(context, orderGuid, transactions, info);
                callback.onSunPassPYDPrintRequested(context, orderGuid, transactions, info, dRequest, type, balanceResponse, transactionFee, orderNum, total);
            }


            private void hide() {
                SunpassBillingFragmentDialog.hide(context);
            }
        }, response, transactionFee, null, null);
    }

    private void proceedToNotification(final FragmentActivity context,
                                       final boolean success,
                                       final Spannable message) {

        PayNotificationFragmentDialog.show(context, false, success, false, message, false, new INotificationConfirmListener() {

            @Override
            public void onReload(Object UIFragent) {
                // ignore
            }

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

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }
}
