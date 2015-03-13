package com.kaching123.tcr.activity.PrepaidActivity;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.saleorder.AddBillPaymentOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderCommand;
import com.kaching123.tcr.fragment.prepaid.SunPass.PayYourDocumentConfirmationFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.PayYourDocumentConfirmationFragment_;
import com.kaching123.tcr.fragment.prepaid.SunPass.PrepaidSunPassCategoryFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.PrepaidSunPassCategoryFragment_;
import com.kaching123.tcr.fragment.prepaid.SunPass.PrepaidSunPassHeadFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.PrepaidSunPassHeadFragment_;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassPayYourDocumentFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassPayYourDocumentFragment_;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassTransponderConfirmationFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassTransponderConfirmationFragment_;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassTransponderCreditFragment;
import com.kaching123.tcr.fragment.prepaid.SunPass.SunPassTransponderCreditFragment_;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.PaymentFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.sunpass.SunpassBillingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidSunPassPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.SunpassType;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.kaching123.tcr.processor.PaymentProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.kaching123.tcr.websvc.api.prepaid.DocumentPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.ReplenishmentResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by teli.yin on 12/3/2014.
 */
@EActivity(R.layout.prepaid_long_distance_activity)
public class PrepaidSunPassActivity extends PrepaidBaseFragmentActivity {

    private PrepaidSunPassHeadFragment sunPassHeadFragment;
    private PrepaidSunPassCategoryFragment sunPassCategoryFragment;
    private SunPassTransponderCreditFragment sunPassTransponderCreditFragment;
    private SunPassTransponderConfirmationFragment sunPassTransponderConfirmationFragment;
    private SunPassPayYourDocumentFragment sunPassPayYourDocumentFragment;
    private PayYourDocumentConfirmationFragment payYourDocumentConfirmationFragment;
    private PrepaidSunPassInterface callback;
    private final HeadFragmentCallback headFragmentCallback = new HeadFragmentCallback();

    private SunpassType type;
    private String orderGuid;
    private ArrayList<PaymentTransactionModel> transactions;
    private BigDecimal changeAmount;

    @AfterViews
    protected void init() {
        initFragment();
    }

    protected void initFragment() {
        sunPassHeadFragment = PrepaidSunPassHeadFragment_.builder().build();
        sunPassHeadFragment.setCallback(headFragmentCallback);
        sunPassCategoryFragment = PrepaidSunPassCategoryFragment_.builder().build();
        sunPassCategoryFragment.setCallBack(new PrepaidSunPassCategoryCallBack());
        setCallback(sunPassCategoryFragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.long_distance_head, sunPassHeadFragment).replace(R.id.long_distance_body, sunPassCategoryFragment).commit();
    }

    public static void start(Context context) {
        PrepaidSunPassActivity_.intent(context).start();
    }

    private void setCallback(PrepaidSunPassInterface callback) {
        this.callback = callback;
    }

    public interface PrepaidSunPassInterface {
        void onBackPressed();
    }

    protected
    class HeadFragmentCallback implements PrepaidSunPassHeadFragment.LongDistanceHeadInterface {
        @Override
        public void onHomePressed() {
            finish();
        }

        @Override
        public void onBackButtonPressed() {
            callback.onBackPressed();
        }
    }

    class PrepaidSunPassCategoryCallBack implements PrepaidSunPassCategoryFragment.SunPassCategoryCallback {

        @Override
        public void onBackButtonPressed() {
            backToPreviousLayout();
        }

        @Override
        public void onCategoryChosen(int mode) {
            if (mode == PrepaidSunPassCategoryFragment.ACTIVE_OR_REPLENISH_SUNPASS_TRANSPONDER) {
                type = SunpassType.SUNPASS_TRANSPONDER;
                sunPassTransponderCreditFragment = SunPassTransponderCreditFragment_.builder().cashierId(cashierId).user(user).transactionMode(transactionMode).build();
                sunPassTransponderCreditFragment.setCallBack(new PrepaidSunPassResponderCreditCallBack());
                setCallback(sunPassTransponderCreditFragment);
                getSupportFragmentManager().beginTransaction().hide(sunPassCategoryFragment).addToBackStack(null).add(R.id.long_distance_body, sunPassTransponderCreditFragment).commit();
            } else {
                type = SunpassType.SUNPASS_PAY_YOUR_DOCUMENT;
                sunPassPayYourDocumentFragment = SunPassPayYourDocumentFragment_.builder().cashierId(cashierId).user(user).transactionMode(transactionMode).transactionId(transactionId).build();
                sunPassPayYourDocumentFragment.setCallBack(new SunPassPayYourDocumentCallback());
                setCallback(sunPassPayYourDocumentFragment);
                getSupportFragmentManager().beginTransaction().hide(sunPassCategoryFragment).addToBackStack(null).add(R.id.long_distance_body, sunPassPayYourDocumentFragment).commit();

            }
        }
    }

    ;

    class SunPassPayYourDocumentCallback implements SunPassPayYourDocumentFragment.PayYourDocumentCreditCallBack {

        @Override
        public void onComplete(SunPassDocumentPaymentRequest request, DocumentInquiryResponse result, BigDecimal transcationFee) {
            payYourDocumentConfirmationFragment = PayYourDocumentConfirmationFragment_.builder().request(request).response(result).Fee(transcationFee).build();
            payYourDocumentConfirmationFragment.setCallBack(new PrepaidPYDConfirmationCallBack());
            setCallback(payYourDocumentConfirmationFragment);
            getSupportFragmentManager().beginTransaction().hide(sunPassPayYourDocumentFragment).addToBackStack(null).add(R.id.long_distance_body, payYourDocumentConfirmationFragment).commit();
        }

        @Override
        public void headMessage(int mode) {

            sunPassHeadFragment.setInstroTextView(mode);
        }

        @Override
        public void onBackButtonPressed() {
            backToPreviousLayout();
        }

        @Override
        public void onError(String message) {
            Toast.makeText(PrepaidSunPassActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    ;

    class PrepaidPYDConfirmationCallBack implements PayYourDocumentConfirmationFragment.PayYourDocumentConfirmationCallBack {

        @Override
        public void onComplete(SunPassDocumentPaymentRequest request, DocumentInquiryResponse response) {
            proceedToPayment(PrepaidSunPassActivity.this, null, null, request, response, getString(R.string.sunpass_prepaid_item_description), BillPaymentDescriptionModel.PrepaidType.SUNPASS_PAY_YOUR_DOCUMENT);
        }

        @Override
        public void onBackButtonPressed() {
            backToPreviousLayout();
        }

        @Override
        public void headMeassage(int mode) {
            sunPassHeadFragment.setInstroTextView(mode);
        }
    }

    ;

    class PrepaidSunPassResponderCreditCallBack implements SunPassTransponderCreditFragment.SunPassTransponderCreditCallBack {

        @Override
        public void headMessage(int mode) {
            sunPassHeadFragment.setInstroTextView(mode);
        }

        @Override
        public void onBackButtonPressed() {
            backToPreviousLayout();
        }

        @Override
        public void onComplete(String accountNumber, BalanceResponse formedRequest, BigDecimal amount, BigDecimal transcationFee) {
            proceedToConfirmationPage(formedRequest, accountNumber, amount, transcationFee);
        }
    }


    private void proceedToConfirmationPage(final BalanceResponse response, String accountNumber, BigDecimal amount, final BigDecimal transcationFee) {
        sunPassTransponderConfirmationFragment = SunPassTransponderConfirmationFragment_.builder().cashierId(cashierId).user(user).transactionMode(transactionMode).response(response).accountNumber(accountNumber).amount(amount).FEE_AMOUNT(transcationFee).build();
        sunPassTransponderConfirmationFragment.setCallBack(new PrepaidSunPassTranConCallBack());
        setCallback(sunPassTransponderConfirmationFragment);
        getSupportFragmentManager().beginTransaction().hide(sunPassTransponderCreditFragment).addToBackStack(null).add(R.id.long_distance_body, sunPassTransponderConfirmationFragment).commit();
    }

    class PrepaidSunPassTranConCallBack implements SunPassTransponderConfirmationFragment.SunPassTransponderConfirmationCallBack {

        @Override
        public void onComplete(SunReplenishmentRequest request, BalanceResponse response) {
            proceedToPayment(PrepaidSunPassActivity.this, request, response, null, null, getString(R.string.sunpass_prepaid_item_description), BillPaymentDescriptionModel.PrepaidType.SUNPASS_TRANSPONDER);
        }

        @Override
        public void headMeassage(int mode) {
            sunPassHeadFragment.setInstroTextView(mode);
        }

        @Override
        public void onBackButtonPressed() {
            backToPreviousLayout();
        }
    }

    ;

    protected void proceedToPayment(final FragmentActivity context, final SunReplenishmentRequest request, final BalanceResponse response, final SunPassDocumentPaymentRequest dRequest, final DocumentInquiryResponse dResponse, final String description, final BillPaymentDescriptionModel.PrepaidType type) {
        final BigDecimal amount = request == null ? dRequest.amount : request.amount;
        BigDecimal fee = new BigDecimal(request == null ? dRequest.feeAmount : request.feeAmount);
        PaymentFragmentDialog.show(context, Boolean.TRUE, amount, description, type, Broker.SUNPASS, fee, new AddBillPaymentOrderCommand.BaseAddBillPaymentOrderCallback() {

            @Override
            protected void handleSuccess(final String orderGuid, final long prepaidOrderId) {
                hide();
                PaymentProcessor.create(orderGuid, OrderType.PREPAID, null).callback(new PaymentProcessor.IPaymentProcessor() {
                    @Override
                    public void onSuccess() {
                        proceedToBilling(context, request, response, dRequest, dResponse, prepaidOrderId, new BigDecimal(request.feeAmount));
                    }

                    @Override
                    public void onCancel() {
                        RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                    }

                    @Override
                    public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount) {
                        PrepaidSunPassActivity.this.changeAmount = changeAmount;
//                        delegate.onComplete(context, order, list, prepaidOrderId);
                        PrepaidSunPassActivity.this.orderGuid = orderGuid;
                        PrepaidSunPassActivity.this.transactions = list;
                        if (request != null)
                            request.setOrderId(prepaidOrderId);
                        else
                            dRequest.setOrderId(prepaidOrderId);
                        proceedToBilling(context, request, response, dRequest, dResponse, prepaidOrderId, amount);
                    }
                }).setPrepaidMode().init(context);

            }

            @Override
            protected void handleFailure() {
                Logger.e("PrepaidProcessor: AddBillPaymentOrderCommand failed!");
                hide();
                finish();
            }

            private void hide() {
                PaymentFragmentDialog.hide(context);
            }
        });
    }

    protected void proceedToBilling(final FragmentActivity context, final SunReplenishmentRequest request, final BalanceResponse response, final SunPassDocumentPaymentRequest dRequest, final DocumentInquiryResponse dResponse, final long prepaidOrderId, final BigDecimal transactionFee) {
        SunpassBillingFragmentDialog.show(context, request, type, orderGuid, new SunpassBillingFragmentDialog.SunpassBillingFragmentDialogCallback() {

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
                onCompleteWithSunPassPrint(context, orderGuid, transactions, info, type, request, balanceResponse, transactionFee, orderNum, total, dRequest, dResponse);
            }

            @Override
            public void onComplete(DocumentPaymentResponse result, DocumentInquiryResponse dResponse, BigDecimal transactionFee, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new SunpassInfo(dRequest.licensePateleNumber, dRequest.purchaseId, dResponse.referenceNumber);
                IPrePaidInfo info2 = new SunpassInfo();
                onCompleteWithSunPassPrint(context, orderGuid, transactions, info, type, request, response, transactionFee, orderNum, total, dRequest, dResponse);
            }

            private void hide() {
                SunpassBillingFragmentDialog.hide(context);
            }
        }, response, transactionFee, dRequest, dResponse);
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
                proceedToVoid(context, orderGuid, transactions);
            }

            @Override
            public void onConfirmed() {
                hide();
                proceedToVoid(context, orderGuid, transactions);
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToVoid(final FragmentActivity context, final String orderGuid, final List<PaymentTransactionModel> transactions) {
        //we leave void for payment process, we can cancel transaction during multiple tendering
        MoneybackProcessor.create(orderGuid, OrderType.PREPAID).callback(new MoneybackProcessor.IVoidCallback() {


            @Override
            public void onVoidComplete(List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel) {
                RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                finish();
                finish();
            }

            @Override
            public void onVoidFailure(BigDecimal ignore) {
                finish();
            }

            @Override
            public void onVoidCancelled() {
                finish();
            }
        }).childOrderModel(new SaleOrderModel(orderGuid))
                .initVoid(context,
                        null,
                        transactions,
                        TcrApplication.get().getBlackStoneUser(), true, true);
    }

    protected void onCompleteWithSunPassPrint(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunpassType type, SunReplenishmentRequest request, BalanceResponse response, BigDecimal transactionFee, String orderNum, String total, SunPassDocumentPaymentRequest dRequest,
                                              DocumentInquiryResponse dResponse) {
        proceedToSunPassPrintFinish(context, orderGuid, transactions, info, type, request, response, transactionFee, orderNum, total, dRequest, dResponse);
    }

    private void proceedToSunPassPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, SunpassType type, SunReplenishmentRequest request, BalanceResponse response, BigDecimal transactionFee, String orderNum, String total, SunPassDocumentPaymentRequest dRequest,
                                             DocumentInquiryResponse dResponse) {
        PrepaidSunPassPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                finish();
            }
        }, transactions, info, type, request, response, changeAmount, transactionFee, orderNum, total, dRequest, dResponse);
    }

    public void backToPreviousLayout() {
        super.onBackPressed();
    }
    @Override
    public void onBackPressed() {
        callback.onBackPressed();
    }
}
