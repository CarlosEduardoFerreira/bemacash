package com.kaching123.tcr.processor;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.commands.device.DeletePaxCommand;
import com.kaching123.tcr.commands.display.DisplayOrderCommand;
import com.kaching123.tcr.commands.display.DisplayPartialTenderCommand;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.commands.local.EndTransactionCommand;
import com.kaching123.tcr.commands.local.StartTransactionCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.WebCommand;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackGateway;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorGiftCardReloadCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorHelloCommand;
import com.kaching123.tcr.commands.print.pos.PrintSignatureOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.commands.store.saleorder.SuccessOrderCommand;
import com.kaching123.tcr.commands.store.settings.EditPaxCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.employee.EmployeeTipsFragmentDialog;
import com.kaching123.tcr.fragment.employee.EmployeeTipsFragmentDialog.IAddTipsListener;
import com.kaching123.tcr.fragment.tendering.CustomerPickerExtremeFragment;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.TransactionPendingFragmentDialogBase.ISaleProgressListener;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment;
import com.kaching123.tcr.fragment.tendering.pax.PayPAXPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pax.PayPAXPendingFragmentDialog.IPaxSaleProgressListener;
import com.kaching123.tcr.fragment.tendering.payment.ApplyTipsFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.ApplyTipsFragmentDialog.IApplyTipsListener;
import com.kaching123.tcr.fragment.tendering.payment.CloseTransPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.CloseTransPendingFragmentDialog.ICloseProgressListener;
import com.kaching123.tcr.fragment.tendering.payment.CloseTransactionsFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.CloseTransactionsFragmentDialog.CloseTransactionsListener;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.IPaymentDialogListener;
import com.kaching123.tcr.fragment.tendering.payment.PayChargeFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayChargeFragmentDialog.ISaleChargeListener;
import com.kaching123.tcr.fragment.tendering.payment.PayCreditReceiptFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayCreditReceiptFragmentDialog.ICreditReceiptPaymentListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayOtherFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PaySilentCashFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PaySwipePendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PaySwipePendingFragmentDialog.ISaleSwipeListener;
import com.kaching123.tcr.fragment.tendering.payment.PayTenderUnitedFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.PayTransPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.SettlementNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.SettlementTransPendingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.payment.SettlementTransPendingFragmentDialog.ISettlementProgressListener;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.model.payment.PaymentMethod;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.general.card.CreditCard;
import com.kaching123.tcr.model.payment.general.transaction.BlackStoneTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.general.transaction.TransactionType;
import com.kaching123.tcr.print.processor.GiftCardBillingResult;
import com.kaching123.tcr.processor.MoneybackProcessor.IVoidCallback;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import junit.framework.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * @author Ivan v. Rikhmayer
 */
public class PaymentProcessor implements BaseCashierActivity.PrepaidBillingCallback {

    //public static final BigDecimal CREDIT_RECEIPT_THRESHOLD = new BigDecimal(2000);

    private String orderGuid;
    private OrderType orderType;
    private KitchenPrintStatus kitchenPrintStatus;
    private String[] salesmanGuids;
    private CustomerModel customer;

    private BigDecimal orderTotal;
    private BigDecimal orderPayed;
    private BigDecimal orderChange;
    private IPaymentProcessor callback;
    private boolean singleTenderEnabled;

    private PaymentTransactionModel lastTransaction;

    private boolean prepaidMode = false;

    private static final Handler handler = new Handler();

    private PrintSignatureOrderCommand.ReceiptType gateWay;

    private ArrayList<PrepaidReleaseResult> ReleaseResultList;
    private ArrayList<PrepaidReleaseResult> failReleaseResultList;

    private ArrayList<GiftCardBillingResult> successGiftCardResultList;
    private ArrayList<GiftCardBillingResult> failGiftCardResultList;
    private final int PREPAID_RELEASE_BILLING_SUCC = 200;

    private static final Uri URI_SALE_ITEMS_PREPAID = ShopProvider.getContentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);

    // we will use the list of successed credit card transaction to print signature receipt.
    // This artifact is created in sake of saving the last four CC number digits, as it is againts the law and RO wish to save them anyway
    private ArrayList<PaymentTransactionModel> successfullCCtransactionModels = new ArrayList<PaymentTransactionModel>();

    private String currentTransactionId;
    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);

    private PaymentProcessor(String orderGuid, OrderType orderType, KitchenPrintStatus kitchenPrintStatus, String[] salesmanGuids) {
        this.currentTransactionId = UUID.randomUUID().toString();
        this.orderGuid = orderGuid;
        this.orderType = orderType;
        this.kitchenPrintStatus = kitchenPrintStatus;
        this.salesmanGuids = salesmanGuids;
    }

    private PaymentProcessor() {
    }

    /**
     * Create the working instance
     */
    public static PaymentProcessor create(String orderGuid, OrderType orderType, KitchenPrintStatus kitchenPrintStatus) {
        return new PaymentProcessor(orderGuid, orderType, kitchenPrintStatus, null);
    }

    public static PaymentProcessor create(String orderGuid, OrderType orderType, KitchenPrintStatus kitchenPrintStatus, String[] salesmanGuids) {
        return new PaymentProcessor(orderGuid, orderType, kitchenPrintStatus, salesmanGuids);
    }

    public static PaymentProcessor create() {
        return new PaymentProcessor();
    }

    public PaymentProcessor setPrepaidMode() {
        prepaidMode = true;
        return this;
    }

    public PaymentProcessor setCustomer(CustomerModel customer){
        this.customer = customer;
        return this;
    }


    /**
     * Set the working context
     */
    public PaymentProcessor init(final FragmentActivity context) {
        this.singleTenderEnabled = true;
        if (TcrApplication.get().payWithCustomerEnabled() && customer == null) {
            proceedToCpf(context);
        } else {
            proceedToTender(context, 0, singleTenderEnabled);
        }
        return this;
    }

    private void proceedToCpf(final FragmentActivity context) {
        CustomerPickerExtremeFragment.show(context, orderGuid, new CustomerPickerExtremeFragment.ExtremeCallback() {

            @Override
            public void onCancel() {
                hide();
                callback.onCancel();
            }

            @Override
            public void onChosen(String email) {
                hide();
                proceedToTender(context, 0, singleTenderEnabled);
            }

            private void hide() {
                CustomerPickerExtremeFragment.hide(context);
            }
        });
    }

    public PaymentProcessor callback(IPaymentProcessor callback) {
        this.callback = callback;
        return this;
    }

    public PaymentProcessor closePreauth(FragmentActivity context, boolean isOrderTipped, ArrayList<PaymentTransactionModel> preauthTransactions, BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
        proceedToTransactionSelection(context, isOrderTipped, preauthTransactions);
        return this;
    }

    public void doSettlement(final FragmentActivity context) {
        AlertDialogFragment.showAlert(context, R.string.blackstone_settlement_alert_title, context.getString(R.string.blackstone_settlement_alert_message), R.string.btn_ok, new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                proceedToDoSettlement(context);
                return true;
            }
        });
    }


    /*Preauth flow*/

    private void proceedToTransactionSelection(final FragmentActivity context, final boolean isOrderTipped, ArrayList<PaymentTransactionModel> preauthTransactions) {
        if (preauthTransactions.size() > 1) {
            CloseTransactionsFragmentDialog.show(context, preauthTransactions, new CloseTransactionsListener() {
                @Override
                public void onCancel() {

                }

                @Override
                public void onTransactionSelected(PaymentTransactionModel transaction) {
                    proceedToTips(context, isOrderTipped, transaction);
                }
            });
            return;
        }

        PaymentTransactionModel transaction = preauthTransactions.get(0);
        proceedToTips(context, isOrderTipped, transaction);
    }

    private void proceedToTips(final FragmentActivity context, boolean isOrderTipped, final PaymentTransactionModel transaction) {
        if (!isOrderTipped) {
            EmployeeTipsFragmentDialog.show(context, new IAddTipsListener() {
                @Override
                public void onTipsConfirmed(BigDecimal amount, List<String> employeeGuids, String notes) {
                    String employeeGuid = employeeGuids.get(0);
                    proceedToClosePreauth(context, transaction, new TipsModel(
                            null,
                            null,
                            employeeGuid,
                            null,
                            PaymentProcessor.this.orderGuid,
                            null,
                            null,
                            amount,
                            notes,
                            null
                    ));
                }
            }, false, orderTotal, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    proceedToClosePreauth(context, transaction, null);
                    return true;
                }
            });
            return;
        }
        proceedToClosePreauth(context, transaction, null);
    }

    private void proceedToClosePreauth(final FragmentActivity context, final PaymentTransactionModel transactionModel, final TipsModel tips) {
        proceedToClosePreauth(context, transactionModel, tips, false, null);
    }

    private void proceedToClosePreauth(final FragmentActivity context, final PaymentTransactionModel transactionModel, final TipsModel tips, final boolean tipsOnFly, final SaleActionResponse reloadResponse) {
        CloseTransPendingFragmentDialog.show(context, transactionModel, tips, reloadResponse, new ICloseProgressListener() {

            @Override
            public void onComplete(TransactionStatusCode responseCode, ErrorReason errorReason) {
                Logger.d("proceedToClosePreauth onComplete");
                hide();

                boolean codeNotNull = responseCode != null;
                boolean success = codeNotNull && responseCode.success();
                boolean allowRetry = codeNotNull && responseCode.retryMayHelp();

                //only for this situation
                boolean abortSwitchToClose = TransactionStatusCode.REFERENCED_PREAUTHORIZATION_COMPLETED == responseCode;

                proceedToClosePreauthNotification(context, allowRetry, false, success, abortSwitchToClose, getErrorMessage(success, codeNotNull, responseCode, errorReason), transactionModel, tips, tipsOnFly);
            }

            @Override
            public void onComplete(TransactionStatusCode responseCode, PaxGateway.Error error) {
                Logger.d("proceedToClosePreauth onComplete");
                hide();

                boolean codeNotNull = responseCode != null;
                boolean success = codeNotNull && responseCode.success();
                boolean allowRetry = codeNotNull && responseCode.retryMayHelp();
                boolean allowReload = !success && PaxGateway.Error.SERVICE != error;

                //only for this situation
                boolean abortSwitchToClose = TransactionStatusCode.REFERENCED_PREAUTHORIZATION_COMPLETED == responseCode;

                proceedToClosePreauthNotification(context, allowRetry, allowReload, success, abortSwitchToClose, getErrorMessage(success, codeNotNull, responseCode, error), transactionModel, tips, tipsOnFly);
            }


            @Override
            public void onFailure(String errorMessage) {
                hide();

                SpannableString errorSpannable = new SpannableString(errorMessage);
                errorSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, errorSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                proceedToClosePreauthNotification(context, false, false, false, false, errorSpannable, transactionModel, tips, tipsOnFly);
            }

            private Spannable getErrorMessage(boolean success, boolean codeNotNull, TransactionStatusCode responseCode, ErrorReason errorReason) {
                final String message;
                final Spannable messageSpannable;
                if (success) {
                    message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                    messageSpannable = new SpannableString(message);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return messageSpannable;
                }

                final int messageClarification;
                if (codeNotNull) {
                    message = responseCode.getDescription();
                    messageClarification = R.string.blackstone_pay_failure_body_2nd;
                } else if (errorReason != null) {
                    message = errorReason.getDescription();
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
                return messageSpannable;
            }

            private Spannable getErrorMessage(boolean success, boolean codeNotNull, TransactionStatusCode responseCode, PaxGateway.Error error) {
                final String message;
                final Spannable messageSpannable;
                if (success) {
                    message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                    messageSpannable = new SpannableString(message);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return messageSpannable;
                }

                final int messageClarification;
                if (codeNotNull) {
                    message = responseCode.getDescription();
                    messageClarification = R.string.blackstone_pay_failure_body_2nd;
                } else if (error != null) {
                    switch (error) {
                        case CONNECTIVITY:
                            message = context.getString(R.string.pax_timeout);
                            break;
                        case PAX:
                        case PAX404:
                            message = context.getString(R.string.blackstone_pax_failure_reason_pax);
                            break;
                        default:
                            message = ErrorReason.UNKNOWN.getDescription();
                            break;
                    }
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
                return messageSpannable;
            }

            private void hide() {
                CloseTransPendingFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToClosePreauthNotification(final FragmentActivity context,
                                                   boolean allowRetry,
                                                   boolean allowReload,
                                                   final boolean success,
                                                   boolean abortSwitchToClose,
                                                   Spannable message,
                                                   final PaymentTransactionModel transactionModel,
                                                   final TipsModel tips) {
        proceedToClosePreauthNotification(context, allowRetry, allowReload, success, abortSwitchToClose, message, transactionModel, tips, false);
    }

    private void proceedToClosePreauthNotification(final FragmentActivity context,
                                                   boolean allowRetry,
                                                   boolean allowReload,
                                                   final boolean success,
                                                   boolean abortSwitchToClose,
                                                   Spannable message,
                                                   final PaymentTransactionModel transactionModel,
                                                   final TipsModel tips,
                                                   final boolean tipsOnFly) {
        final Transaction transaction = transactionModel.toTransaction();
        final BigDecimal amount = transactionModel.availableAmount;
        PayNotificationFragmentDialog.show(context, allowRetry, success, abortSwitchToClose, message, allowReload, new INotificationConfirmListener() {

            @Override
            public void onReload(final Object UIFragent) {
                PaxReloadProcessor.get().reload(context, transaction, amount, new PaxReloadProcessor.IPAXReloadCallback() {
                    @Override
                    public void onStart() {
                        ((PayNotificationFragmentDialog) UIFragent).startSwirl(true);
                    }

                    @Override
                    public void onComplete(SaleActionResponse reloadResponse) {
                        hide();
                        proceedToClosePreauth(context, transactionModel, tips, tipsOnFly, reloadResponse);
                    }

                    @Override
                    public void onError(String errorMessage, boolean allowFurther) {
                        ((PayNotificationFragmentDialog) UIFragent).startSwirl(false);
                        ((PayNotificationFragmentDialog) UIFragent).enableReload(allowFurther, errorMessage);
                    }
                });
            }

            @Override
            public void onRetry() {
                hide();
                proceedToClosePreauth(context, transactionModel, tips, tipsOnFly, null);
            }

            @Override
            public void onCancel() {
                hide();
                if (tipsOnFly)
                    proceedToFinish(context, successfullCCtransactionModels);
            }

            @Override
            public void onConfirmed() {
                hide();
                if (tipsOnFly)
                    proceedToFinish(context, successfullCCtransactionModels);
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }


    /*Do Settlement flow*/

    private void proceedToDoSettlement(final FragmentActivity context) {
        SettlementTransPendingFragmentDialog.show(context, new ISettlementProgressListener() {

            @Override
            public void onComplete(TransactionStatusCode responseCode, ErrorReason errorReason, boolean transactionsClosed) {
                Logger.d("proceedToDoSettlement onComplete");
                hide();

                boolean codeNotNull = responseCode != null;
                boolean success = codeNotNull && responseCode.success();
                boolean allowRetry = (success && !transactionsClosed) || (!success && codeNotNull && responseCode.retryMayHelp());

                proceedToDoSettlementNotification(context, allowRetry, success, transactionsClosed, getErrorMessage(success, transactionsClosed, codeNotNull, errorReason));
            }

            @Override
            public void onComplete(TransactionStatusCode responseCode, PaxGateway.Error error, boolean transactionsClosed) {
                Logger.d("proceedToDoSettlement (PAX) onComplete Blackstone");
                hide();

                boolean codeNotNull = responseCode != null;
                boolean success = codeNotNull && responseCode.success();
                boolean allowRetry = (success && !transactionsClosed) || (!success && codeNotNull && responseCode.retryMayHelp());

                proceedToDoSettlementNotification(context, allowRetry, success, transactionsClosed, getErrorMessage(success, transactionsClosed, codeNotNull, error));
            }


            private Spannable getErrorMessage(boolean success, boolean transactionsClosed, boolean codeNotNull, ErrorReason errorReason) {
                final String message;
                final Spannable messageSpannable;
                if (success && transactionsClosed) {
                    message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                    messageSpannable = new SpannableString(message);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return messageSpannable;
                }

                final int messageClarification;
                if (success && !transactionsClosed) {
                    message = context.getString(R.string.blackstone_settlement_failure_reason_db_update);
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                } else if (codeNotNull) {
                    message = context.getString(R.string.blackstone_settlement_failure);
                    messageClarification = R.string.blackstone_settlement_failure_body_2nd;
                } else if (errorReason != null) {
                    message = errorReason.getDescription();
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                } else {
                    message = ErrorReason.UNKNOWN.getDescription();
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                }
                String mainString = context.getResources().getString(R.string.blackstone_settlement_failure_body_1st);
                messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_settlement_failure_body_constructor,
                        mainString, context.getResources().getString(messageClarification), message));
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, mainString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), messageSpannable.length() - message.length(),
                        messageSpannable.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return messageSpannable;
            }

            private Spannable getErrorMessage(boolean success, boolean transactionsClosed, boolean codeNotNull, PaxGateway.Error error) {
                final String message;
                final Spannable messageSpannable;
                if (success && transactionsClosed) {
                    message = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.getDescription();
                    messageSpannable = new SpannableString(message);
                    messageSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return messageSpannable;
                }

                final int messageClarification;
                if (success && !transactionsClosed) {
                    message = context.getString(R.string.blackstone_settlement_failure_reason_db_update);
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                } else if (codeNotNull) {
                    message = context.getString(R.string.blackstone_settlement_failure);
                    messageClarification = R.string.blackstone_settlement_failure_body_2nd;
                } else if (error != null) {
                    switch (error) {
                        case CONNECTIVITY:
                            message = context.getString(R.string.pax_timeout);
                            break;
                        case PAX:
                        case PAX404:
                            message = context.getString(R.string.blackstone_pax_failure_reason_pax);
                            break;
                        default:
                            message = ErrorReason.UNKNOWN.getDescription();
                            break;
                    }
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                } else {
                    message = ErrorReason.UNKNOWN.getDescription();
                    messageClarification = R.string.blackstone_settlement_failure_body_3nd;
                }
                String mainString = context.getResources().getString(R.string.blackstone_settlement_failure_body_1st);
                messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_settlement_failure_body_constructor,
                        mainString, context.getResources().getString(messageClarification), message));
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, mainString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED), messageSpannable.length() - message.length(),
                        messageSpannable.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return messageSpannable;
            }

            private void hide() {
                SettlementTransPendingFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToDoSettlementNotification(final FragmentActivity context, boolean allowRetry, boolean success, boolean transactionClose, Spannable message) {
        Assert.assertNotNull(context);
        SettlementNotificationFragmentDialog.show(context, allowRetry, success, transactionClose, message, new INotificationConfirmListener() {

            @Override
            public void onReload(Object UIFragent) {
                // ignore
            }

            @Override
            public void onRetry() {
                hide();
                proceedToDoSettlement(context);
            }

            @Override
            public void onCancel() {
                hide();
            }

            @Override
            public void onConfirmed() {
                hide();
            }

            private void hide() {
                SettlementNotificationFragmentDialog.hide(context);
            }
        });
    }


    /**
     * Ask the user for the payment method
     */
    private void proceedToTender(final FragmentActivity context, int animation) {
        proceedToTender(context, animation, singleTenderEnabled);
    }

    private void proceedToTender(final FragmentActivity context, int animation, Boolean singleTenderEnabled) {
        if (getDisplayBinder(context) != null)
            getDisplayBinder(context).startCommand(new DisplayOrderCommand(orderGuid));

        PayTenderUnitedFragmentDialog.show(context, orderGuid, orderType, new IPaymentDialogListener.IPayTenderUnitedListener() {

            @Override
            public void onUnitedPaymentAmountSelected(PaymentMethod method, BigDecimal orderTotal, BigDecimal amount) {
                Logger.d("We have finished step 2 : %s credits!", amount);
                hide();
                PaymentProcessor.this.orderTotal = orderTotal;
                PaymentProcessor.this.onPaymentAmountSelected(context, method, amount);
            }

            @Override
            public void onUnitedCancel() {
                Logger.d("We have cancelled step 2. This may happen only on first cycle, so we dont care much");
                hide();
                proceedToTender(context, 0);
            }


            @Override
            public void onPaymentMethodSelected(PaymentMethod method, BigDecimal orderTotal, BigDecimal pendingAmount, boolean singleTender) {
                PaymentProcessor.this.orderTotal = orderTotal;
                hide();
                if (singleTender) {
                    PaymentProcessor.this.onPaymentAmountSelected(context, method, pendingAmount);
                } else {
                    proceedToCharge(context, method, orderTotal, pendingAmount);
                }
            }

            @Override
            public void onVoidRequested(List<PaymentTransactionModel> transactions) {
                Logger.d("We have requested a void");
                if (transactions.isEmpty()) {
                    Logger.d("Can not proceed with voids - no such entities exist");
                } else {
                    Logger.d("check is ok");
                    hide();
                    proceedToVoid(context, transactions);
                }
            }

            @Override
            public void onDataLoaded(BigDecimal alreadyPayed, BigDecimal orderTotal, ArrayList<PaymentTransactionModel> ignore) {
                PaymentProcessor.this.orderPayed = alreadyPayed;
                PaymentProcessor.this.orderTotal = orderTotal;
                if (orderTotal.compareTo(alreadyPayed) <= 0) {//if we payed more that required go to the finish
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hide();
                            if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                                proceedToTipsApply(context, successfullCCtransactionModels);
                        }
                    });
                } else if (getDisplayBinder(context) != null)
                    getDisplayBinder(context).startCommand(new DisplayPartialTenderCommand(orderTotal, orderTotal.subtract(alreadyPayed)));
            }

            @Override
            public void onSingleTenderCheck(boolean singleTenderEnabled) {
                PaymentProcessor.this.singleTenderEnabled = singleTenderEnabled;
            }

            @Override
            public void onOtherMethodsShown(boolean otherMethodsShown) {

            }

            @Override
            public void onCancel() {
                if (callback != null)
                    callback.onCancel();

                hide();
            }

            private void hide() {
                PayTenderUnitedFragmentDialog.hide(context);
            }

        }, animation, singleTenderEnabled);
    }

    /**
     * Ask the user for the biller to charge
     */
    private void proceedToCharge(final FragmentActivity context, final PaymentMethod method, BigDecimal amount, BigDecimal pendingAmount) {

        PayChargeFragmentDialog.show(context, amount, pendingAmount, new ISaleChargeListener() {

            @Override
            public void onPaymentAmountSelected(BigDecimal amount) {
                Logger.d("We have finished step 2 : %s credits!", amount);
                hide();
                PaymentProcessor.this.onPaymentAmountSelected(context, method, amount);
            }

            @Override
            public void onCancel() {
                Logger.d("We have cancelled step 2. This may happen only on first cycle, so we dont care much");
                hide();
                proceedToTender(context, 0);
            }

            private void hide() {
                PayChargeFragmentDialog.hide(context);
            }

        });
    }

    public void onPaymentAmountSelected(final FragmentActivity context, final PaymentMethod method, BigDecimal amount) {
        Transaction transaction = null;
        amount = amount.setScale(2, RoundingMode.HALF_UP);// for remove the last two digit decimal
        Logger.d("Price send to Processor" + amount.toString());
        switch (method) {
            case CASH: {
                transaction = PaymentGateway.CASH.gateway().createTransaction(context, orderTotal, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.CASH);
                proceedToCashPayment(context, amount, transaction);
                break;
            }
            case CREDIT_CARD: {
                boolean usePreauthTransactions = TcrApplication.get().isTipsEnabled();
                PaxGateway paxGateway = (PaxGateway) PaymentGateway.PAX.gateway();
                if (TcrApplication.get().isPaxConfigured() && paxGateway.acceptPaxCreditEnabled()) {
                    transaction = !usePreauthTransactions ? paxGateway.createTransaction(context, amount, orderGuid) : paxGateway.createPreauthTransaction(context, amount, orderGuid);
                    transaction.cashBack = BigDecimal.ZERO;
                    transaction.setType(TransactionType.PAX);
                    proceedToPAXPayment(context, transaction);
                } else {
                    BlackGateway gateway = (BlackGateway) PaymentGateway.BLACKSTONE.gateway();
                    transaction = !usePreauthTransactions ? gateway.createTransaction(context, amount, orderGuid) : gateway.createPreauthTransaction(context, amount, orderGuid);
                    transaction.cashBack = BigDecimal.ZERO;
                    transaction.setType(TransactionType.CREDIT);
                    proceedToCardSwipe(context, transaction);

                    if (getDisplayBinder(context) != null) {
                        getDisplayBinder(context).startCommand(new DisplayTenderCommand(amount, null));
                    }
                }
                break;
            }
            case PAX_DEBIT: {
                transaction = PaymentGateway.PAX_DEBIT.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                gateWay = PrintSignatureOrderCommand.ReceiptType.DEBIT;
                transaction.setType(TransactionType.PAX_DEBIT);
                proceedToPAXPayment(context, transaction);
                break;
            }
            case PAX_EBT_FOODSTAMP: {
                transaction = PaymentGateway.PAX_EBT_FOODSTAMP.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                gateWay = PrintSignatureOrderCommand.ReceiptType.EBT;
                transaction.setType(TransactionType.PAX_EBT_FOODSTAMP);
                proceedToPAXPayment(context, transaction);
                break;
            }
            case PAX_EBT_CASH: {
                gateWay = PrintSignatureOrderCommand.ReceiptType.EBT_CASH;
                transaction = PaymentGateway.PAX_EBT_CASH.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.PAX_EBT_CASH);
                proceedToPAXPayment(context, transaction);
                break;
            }
            case GIFT_CARD: {//used for Poslink pax
                transaction = PaymentGateway.PAX_GIFT_CARD.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.PAX_GIFT_CARD);
                proceedToPAXPayment(context, transaction);
                break;
            }
            case CREDIT_RECEIPT: {
                transaction = PaymentGateway.CREDIT.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.CREDIT_CHECK);
                proceedToCreditReceiptPayment(context, amount, transaction);
                break;
            }
            case OFFLINE_CREDIT: {
                transaction = PaymentGateway.OFFLINE_CREDIT.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.OFFLINE_CREDIT);
                proceedToOfflineCreditPayment(context, amount, transaction);

                if (getDisplayBinder(context) != null) {
                    getDisplayBinder(context).startCommand(new DisplayTenderCommand(amount, null));
                }
                break;
            }
            case CHECK: {
                transaction = PaymentGateway.CHECK.gateway().createTransaction(context, amount, orderGuid);
                transaction.cashBack = BigDecimal.ZERO;
                transaction.setType(TransactionType.CHECK);
                proceedToCheckPayment(context, amount, transaction);

                if (getDisplayBinder(context) != null) {
                    getDisplayBinder(context).startCommand(new DisplayTenderCommand(amount, null));
                }
                break;
            }
            default:
                Logger.d("Sorry, provided method does not exist. This is logic failure");
        }
        lastTransaction = new PaymentTransactionModel(TcrApplication.get().getShiftGuid(), transaction);
        lastTransaction.status = PaymentStatus.SUCCESS;
    }


    private void proceedToCreditReceiptPayment(final FragmentActivity context, final BigDecimal amount, final Transaction transaction) {
        PayCreditReceiptFragmentDialog.show(context, transaction, new ICreditReceiptPaymentListener() {
            @Override
            public void onPaymentCompleted(BigDecimal amount) {
                applyAmount(amount);
                if (orderTotal.compareTo(orderPayed) == 0) {
                    Logger.d("Payment complete! just closing");
                    if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                        proceedToTipsApply(context, successfullCCtransactionModels);
                } else {
                    proceedToTender(context, 0);
                }
            }

            @Override
            public void onCancel() {
                proceedToTender(context, 0);
            }
        });
    }

    private void proceedToPAXPayment(final FragmentActivity context, final Transaction transaction) {
        proceedToPAXPayment(context, transaction, null);
    }

    private void proceedToPAXPayment(final FragmentActivity context, final Transaction transaction, final SaleActionResponse reloadResponse) {
        PayPAXPendingFragmentDialog.show(context, transaction, reloadResponse, new IPaxSaleProgressListener() {

            @Override
            public void onComplete(Transaction transaction, String reason) {
                if (reason != null && reason.equals(WebCommand.ErrorReason.DUE_TO_PAX_IP_CHANGED.getDescription())) {
                    return;
                }
                hide();
                WaitDialogFragment.hide(context);
                proceedToNotificationBlock(context, transaction, reason);
            }

            @Override
            public void onCancel() {
                WaitDialogFragment.hide(context);
                hide();
                proceedToTender(context, 0);
            }

            @Override
            public void onSearchNeed(final PaxModel model) {
                WaitDialogFragment.hide(context);
                EditPaxCommand.start(context, model, new EditPaxCommand.PaxEditCommandBaseCallback() {
                    @Override
                    protected void handleSuccess() {
                        hide();
                        Toast.makeText(context, context.getString(R.string.pax_configured), Toast.LENGTH_LONG).show();
                        DeletePaxCommand.start(context, model, true);
                        proceedToPAXPayment(context, transaction);
                    }

                    @Override
                    protected void handleError() {
                        Toast.makeText(context, context.getString(R.string.pax_not_configured), Toast.LENGTH_LONG).show();
                        hide();
                        proceedToTender(context, 0);
                    }
                });
            }

            @Override
            public void onCloseRequest() {
                WaitDialogFragment.show(context, context.getString(R.string.pax_wait_dialog_title));
            }

            private void hide() {
                PayPAXPendingFragmentDialog.hide(context);
            }
        });
    }

    public PaxProcessorHelloCommand.PaxHelloCommandBaseCallback helloCallBack = new PaxProcessorHelloCommand.PaxHelloCommandBaseCallback() {

        @Override
        protected void handleSuccess(String details) {

        }

        @Override
        protected void handleError(String error) {

        }
    };

    private final void proceedToNotificationBlock(final FragmentActivity context, Transaction transaction, String reason) {
        final Spannable messageSpannable;
        String message = reason;
        boolean success;
        if (success = reason == null || reason.length() == 0) {
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
        if (success && transaction != null && (transaction.lastFour != null || transaction.isPreauth)) {
            PaymentTransactionModel transactionModel = new PaymentTransactionModel(TcrApplication.get().getShiftGuid(), transaction);
            transactionModel.availableAmount = transactionModel.amount;
            transactionModel.balance = transaction.balance;
            Logger.d("PaymentProcessor.proceedToNotificationBlock(): successful transaction: " + transactionModel);
            PaymentProcessor.this.successfullCCtransactionModels.add(transactionModel);
        }
        proceedToCardPaymentNotification(context, false, success, messageSpannable, transaction, null);
    }

    /**
     * Follow with the cash payment
     */
    private void proceedToCashPayment(final FragmentActivity context, final BigDecimal amount, final Transaction transaction) {
        PaySilentCashFragmentDialog.show(context, orderPayed, amount, transaction, new PaySilentCashFragmentDialog.ISaleCashListener() {

            @Override
            public void onPaymentAmountSelected(BigDecimal amount, BigDecimal changeAmount) {
                applyAmount(amount);
                PaymentProcessor.this.orderChange = changeAmount;
                hide();
                if (orderTotal.compareTo(orderPayed) == 0) {
                    Logger.d("Payment complete! just closing");
                    if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                        proceedToTipsApply(context, successfullCCtransactionModels);
                } else {
                    proceedToTender(context, 0);
                }
            }

            @Override
            public void onCancel() {
                hide();
                proceedToTender(context, 0);
            }

            private void hide() {
                PaySilentCashFragmentDialog.hide(context);
            }

        });
    }

    /**
     * Follow with the card payment
     */
    private void proceedToCardSwipe(final FragmentActivity context, final Transaction transaction) {
        PaySwipePendingFragmentDialog.show(context, true, new ISaleSwipeListener() {

            @Override
            public void onSwiped(String track) {
                hide();
                CreditCard card = new CreditCard(track);
                proceedToCCardPayment(context, transaction, card);
            }

            @Override
            public void onCardInfo(String number, String expireDate, String cvn, String zip) {
                hide();
                CreditCard card = new CreditCard(zip, number, expireDate, null, cvn, null);
                proceedToCCardPayment(context, transaction, card);
            }

            @Override
            public void onCancel() {
                hide();
                proceedToTender(context, 0);
            }

            private void hide() {
                PaySwipePendingFragmentDialog.hide(context);
            }
        });
    }

    /**
     * Show waiting swirl, probably is going to get extended
     */
    private void proceedToCCardPayment(final FragmentActivity context, final Transaction transaction, final CreditCard card) {
        TcrApplication app = (TcrApplication) context.getApplicationContext();
        PayTransPendingFragmentDialog.show(context, transaction, card,
                app.getBlackStoneUser(), new ISaleProgressListener<ResponseBase>() {

                    @Override
                    public void onComplete(Transaction transaction, ResponseBase code, ErrorReason reason) {
                        Logger.d("proceedToCCardPayment onComplete");
                        hide();
                        TransactionStatusCode rCode = null;
                        boolean responseNotNull;
                        boolean codeNotNull = false;
                        boolean success = (responseNotNull = (code != null)) && (codeNotNull = (rCode = code.getResponseCode()) != null) && rCode.success();
                        boolean allowRetry = rCode != null && rCode.retryMayHelp();

                        final String message;
                        final int messageClarification;
                        final Spannable messageSpannable;
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
                        PaymentTransactionModel ptm = transaction == null ? null : new PaymentTransactionModel(TcrApplication.get().getShiftGuid(), transaction);
                        if (ptm != null) {
                            ptm.availableAmount = ptm.amount;
                        }

                        if (ptm != null && ptm.status.isSuccessful() && (ptm.lastFour != null || ptm.isPreauth)) {
                            PaymentProcessor.this.successfullCCtransactionModels.add(ptm);
                        }
                        proceedToCardPaymentNotification(context, allowRetry, success, messageSpannable, transaction, card);
                    }

                    @Override
                    public void onCancel() {
                        Logger.d("proceedToCCardPayment onCancel");
                        // TODO its impossible, remove this
                        hide();
                        proceedToTender(context, 0);
                    }

                    private void hide() {
                        PayTransPendingFragmentDialog.hide(context);
                    }
                }
        );

    }

    private void proceedToOfflineCreditPayment(final FragmentActivity context, final BigDecimal amount, final Transaction transaction) {
        PayOtherFragmentDialog.show(context, transaction, new PayOtherFragmentDialog.ISaleProgressListener() {

            @Override
            public void onComplete() {
                applyAmount(amount);
                hide();
                if (orderTotal.compareTo(orderPayed) == 0) {
                    Logger.d("Payment complete! just closing");
                    if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                        proceedToTipsApply(context, successfullCCtransactionModels);
                } else {
                    proceedToTender(context, 0);
                }
            }

            @Override
            public void onFail() {
                proceedToTender(context, 0);
            }

            private void hide() {
                PayOtherFragmentDialog.hide(context);
            }

        }, PayOtherFragmentDialog.Type.OFFLINE_CREDIT);
    }

    private void proceedToCheckPayment(final FragmentActivity context, final BigDecimal amount, final Transaction transaction) {
        PayOtherFragmentDialog.show(context, transaction, new PayOtherFragmentDialog.ISaleProgressListener() {

            @Override
            public void onComplete() {
                applyAmount(amount);
                hide();
                if (orderTotal.compareTo(orderPayed) == 0) {
                    Logger.d("Payment complete! just closing");
                    if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                        proceedToTipsApply(context, successfullCCtransactionModels);
                } else {
                    proceedToTender(context, 0);
                }
            }

            @Override
            public void onFail() {
                proceedToTender(context, 0);
            }

            private void hide() {
                PayOtherFragmentDialog.hide(context);
            }

        }, PayOtherFragmentDialog.Type.CHECK);
    }

    /**
     * Notify the user about the result and wait for further commands
     */
    private void proceedToCardPaymentNotification(final FragmentActivity context,
                                                  boolean allowRetry,
                                                  final boolean success,
                                                  Spannable message,
                                                  final Transaction transaction,
                                                  final CreditCard card) {
        if (success) {
            applyAmount(transaction.getAmount());
        }
        Assert.assertNotNull(context);
        boolean show = transaction != null && transaction instanceof PaxTransaction && ((PaxTransaction) transaction).allowReload;


        PayNotificationFragmentDialog.show(context, allowRetry, success, false, message, show, new INotificationConfirmListener() {

            @Override
            public void onReload(final Object UIFragent) {
                if (TcrApplication.get().isBlackstonePax()) {
                    PaxReloadProcessor.get().reload(context, transaction, null, new PaxReloadProcessor.IPAXReloadCallback() {
                        @Override
                        public void onStart() {
                            ((PayNotificationFragmentDialog) UIFragent).startSwirl(true);
                        }

                        @Override
                        public void onComplete(SaleActionResponse reloadResponse) {
                            hide();
                            proceedToPAXPayment(context, transaction, reloadResponse);
                        }

                        @Override
                        public void onError(String errorMessage, boolean allowFurtherReload) {
                            ((PayNotificationFragmentDialog) UIFragent).startSwirl(false);
                            ((PayNotificationFragmentDialog) UIFragent).enableReload(allowFurtherReload, errorMessage);
                        }
                    });
                } else {
                    proceedToPAXPayment(context, transaction);
                }
            }

            @Override
            public void onRetry() {
                hide();
                proceedToCCardPayment(context,
                        BlackStoneTransactionFactory.create(TcrApplication.get().getOperatorGuid(), transaction.getAmount(), transaction.getOrderGuid(), transaction.isPreauth).setType(TransactionType.CREDIT), // Recreating the transaction
                        card);
            }

            @Override
            public void onCancel() {
                hide();
                proceedToTender(context, 0);
            }

            @Override
            public void onConfirmed() {
                hide();
                if (success && orderTotal.compareTo(orderPayed) <= 0) {
                    Logger.d("Payment complete! just closing");
                    if (!needPPD_GCBilling(context, successfullCCtransactionModels))
                        proceedToTipsApply(context, successfullCCtransactionModels);
                } else {
                    proceedToTender(context, 0);
                }
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }

    //todo billing for prepaid and giftcard
    private boolean needPPD_GCBilling(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions) {
        boolean hasPrepaidItem = false;
        boolean hasGiftCardItem = false;
        Cursor cursor = ProviderAction.query(URI_SALE_ITEMS_PREPAID)
                .where(ShopSchema2.SaleOrderItemsView2.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(context);

        List<SaleOrderItemViewModel> saleItemsList = _wrap(cursor,
                new SaleOrderItemViewModelWrapFunction(context));

        List<SaleOrderItemViewModel> prepaidList = getSaleOrderItems(saleItemsList, true);
        if (prepaidList.size() == 0) {

        } else {
            hasPrepaidItem = true;
        }

        List<SaleOrderItemViewModel> giftCardItemsList = getSaleOrderItems(saleItemsList, false);
        if (giftCardItemsList.size() == 0) {

        } else {
            hasGiftCardItem = true;
        }
        if (hasPrepaidItem || hasGiftCardItem)
            callback.onBilling(transactions, prepaidList, giftCardItemsList);

        return (hasPrepaidItem || hasGiftCardItem);
    }

    private List<SaleOrderItemViewModel> getSaleOrderItems(List<SaleOrderItemViewModel> saleItemsList, boolean isPrepaid) {
        List<SaleOrderItemViewModel> list = new ArrayList<SaleOrderItemViewModel>();
        for (SaleOrderItemViewModel item : saleItemsList) {
            if (isPrepaid) {
                if (item.isPrepaidItem)
                    list.add(item);
            } else {
                if (item.isGiftCard)
                    list.add(item);
            }
        }
        return list;
    }


    private void startRefund() {
//        final HistoryDetailedOrderItemListFragment.RefundAmount amount = orderItemsListFragment.getReturnAmount();

    }

    public void proceedToPrepaidCheck(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions, ArrayList<PrepaidReleaseResult> list, PrepaidCheckCallBack callBack) {
        failReleaseResultList = new ArrayList<PrepaidReleaseResult>();
        ReleaseResultList = list;
        for (PrepaidReleaseResult result : list) {
            if (Integer.parseInt(result.error) != PREPAID_RELEASE_BILLING_SUCC)
                failReleaseResultList.add(result);
        }

        callBack.finish();
//        proceedToTipsApply(context, transactions);
    }

    public interface PrepaidCheckCallBack
    {
        void finish();
    }

    public void proceedToGiftCard(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions, ArrayList<GiftCardBillingResult> list) {
        failReleaseResultList = new ArrayList<PrepaidReleaseResult>();
        failGiftCardResultList = new ArrayList<GiftCardBillingResult>();
        successGiftCardResultList = list;
        for (GiftCardBillingResult result : list) {
            if (!result.msg.equalsIgnoreCase(PaxProcessorGiftCardReloadCommand.SUCCESS))
                failGiftCardResultList.add(result);
        }

        proceedToTipsApply(context, transactions);
    }

    private ArrayList<SaleOrderItemViewModel> getOderItems(ArrayList<PrepaidReleaseResult> failPrepaidResultList, ArrayList<GiftCardBillingResult> failGiftCardResultList) {
        ArrayList<SaleOrderItemViewModel> orderItems = new ArrayList<SaleOrderItemViewModel>(failPrepaidResultList.size());
        for (PrepaidReleaseResult result : failPrepaidResultList) {
            orderItems.add(result.model);
        }
        for(GiftCardBillingResult gResult : failGiftCardResultList)
        {
            orderItems.add(gResult.model);
        }
        return orderItems;
    }

    public HistoryDetailedOrderItemListFragment.RefundAmount getReturnAmount() {
        BigDecimal pickedValue = BigDecimal.ZERO;
        ArrayList<SaleOrderItemViewModel> orderItems = getOderItems(failReleaseResultList, failGiftCardResultList);
        ArrayList<MoneybackProcessor.RefundSaleItemInfo> refundItems = new ArrayList<MoneybackProcessor.RefundSaleItemInfo>(failReleaseResultList.size());

        for (SaleOrderItemViewModel model : orderItems) {
            final MoneybackProcessor.RefundSaleItemInfo info = new MoneybackProcessor.RefundSaleItemInfo(model.itemModel.saleItemGuid, BigDecimal.ONE);
            refundItems.add(info);
            pickedValue = pickedValue.add(CalculationUtil.getSubTotal(BigDecimal.ONE, model.finalPrice));
        }

        final HistoryDetailedOrderItemListFragment.RefundAmount refundAmount = new HistoryDetailedOrderItemListFragment.RefundAmount(pickedValue, BigDecimal.ZERO);
        OrderTotalPriceCalculator.calculate(orderItems, null, new OrderTotalPriceCalculator.Handler() {


            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty, BigDecimal itemPriceWithAddons, BigDecimal itemSubTotal, BigDecimal itemTotal, BigDecimal itemEbtTotal, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {

            }

            @Override
            public void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue) {
                refundAmount.orderValue = totalOrderPrice;
            }

        });
        refundAmount.itemsInfo = refundItems;
        return refundAmount;
    }


    public void proceedToTipsApply(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions) {
        boolean tipsEnabled = ((TcrApplication) context.getApplicationContext()).isTipsEnabled();
        boolean tipsOnFlyEnabled = ((TcrApplication) context.getApplicationContext()).getShopInfo().tipsOnFlyEnabled;
        if (transactions == null || !tipsEnabled || !tipsOnFlyEnabled) {
            proceedToFinish(context, transactions);
            return;
        }

        PaymentTransactionModel preauthTransaction = null;
        for (PaymentTransactionModel tr : transactions) {
            if (tr.isPreauth) {
                preauthTransaction = tr;
                break;
            }
        }

        if (preauthTransaction == null) {
            proceedToFinish(context, transactions);
            return;
        }

        final PaymentTransactionModel selectedTransaction = preauthTransaction;
        ApplyTipsFragmentDialog.show(context, orderGuid, new IApplyTipsListener() {

            @Override
            public void onSkip() {
                hide();
                proceedToFinish(context, transactions);
            }

            @Override
            public void onTipsConfirmed(BigDecimal amount, String employeeGuid, String notes) {
                hide();
                proceedToClosePreauth(context, selectedTransaction, new TipsModel(
                        null,
                        null,
                        employeeGuid,
                        null,
                        PaymentProcessor.this.orderGuid,
                        null,
                        null,
                        amount,
                        notes,
                        null
                ), true, null);
            }


            private void hide() {
                ApplyTipsFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToFinish(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions) {
        if (getDisplayBinder(context) != null) {
            getDisplayBinder(context).startCommand(new DisplayWelcomeMessageCommand());
        }

        assert transactions != null && !transactions.isEmpty();
        WaitDialogFragment.hide(context);
        updateOrderStatus(context);
        if (!prepaidMode) {
            PayPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

                @Override
                public void onConfirmed() {

                    callback.onPrintComplete();
                    if ((failReleaseResultList != null && failReleaseResultList.size() != 0) || (failGiftCardResultList != null && failGiftCardResultList.size() != 0)) {
                        final HistoryDetailedOrderItemListFragment.RefundAmount amount = getReturnAmount();
                        if (BigDecimal.ZERO.compareTo(amount.pickedValue) == 0) {
                            Logger.d("just notify");
                            AlertDialogFragment.showAlert(context, R.string.refund_nothing_selected_title, context.getString(R.string.refund_nothing_selected_body));
                        } else {
                            Logger.d("doing refund");
                            //todo onrefund
                            callback.onRefund(amount);
                        }
                    } else
                        finish();
                }
            }, transactions, kitchenPrintStatus, orderChange, gateWay, isPrinterTwoCopiesReceipt(), ReleaseResultList, successGiftCardResultList);
        } else {
//            if (transactions != null && !transactions.isEmpty()) {
//                callback.onPrintValues(orderGuid, transactions, orderChange);
//            } else {
//                ArrayList<PaymentTransactionModel> transactionModels = new ArrayList<PaymentTransactionModel>();
//                lastTransaction.availableAmount = lastTransaction.amount;
//                transactionModels.add(lastTransaction);
//                callback.onPrintValues(orderGuid, transactionModels, orderChange);
//            }
            Cursor cursor = ProviderAction.query(URI_SALE_ITEMS)
                    .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
                    .perform(context);
        }
    }

    public void initRefund(final FragmentActivity context, final ArrayList<PaymentTransactionModel> transactions, final HistoryDetailedOrderItemListFragment.RefundAmount amount) {
        PaymentMethod returnMethod = calcReturnMethod(transactions);

        StartTransactionCommand.start(context);
        MoneybackProcessor.create(orderGuid, OrderType.SALE).callback(new MoneybackProcessor.IRefundCallback() {

            @Override
            public void onRefundComplete(SaleOrderModel childOrderModel) {
                EndTransactionCommand.start(context, true);
                callback.onUpdateOrderList();
            }

            @Override
            public void onRefundCancelled() {
                // ignore
                EndTransactionCommand.start(context);
                callback.onUpdateOrderList();
            }

            @Override
            public void onRefundFailure() {
                EndTransactionCommand.start(context);
                callback.onUpdateOrderList();
            }
        }).initRefund(context, returnMethod, transactions, amount, getApp().getBlackStoneUser(), false);
    }

    private TcrApplication getApp() {
        return (TcrApplication.get());
    }

    private PaymentMethod calcReturnMethod(ArrayList<PaymentTransactionModel> transactions) {
        int cashGatewayCount = 0;
        int creditReceiptCount = 0;
        int creditCardCount = 0;

        for (PaymentTransactionModel t : transactions) {
            if (t.gateway.isCreditCard()) {
                creditCardCount++;
            } else if (t.gateway == PaymentGateway.CASH) {
                cashGatewayCount++;
            } else if (t.gateway == PaymentGateway.CREDIT) {
                creditReceiptCount++;
            }
        }
        if (cashGatewayCount == transactions.size())
            return PaymentMethod.CASH;
        if (creditCardCount == transactions.size())
            return PaymentMethod.CREDIT_CARD;
        if (creditReceiptCount == transactions.size())
            return PaymentMethod.CREDIT_RECEIPT;
        return null;
    }

    private boolean isPrinterTwoCopiesReceipt() {
        return TcrApplication.get().getPrinterTwoCopiesReceipt();
    }

    private void proceedToVoid(final FragmentActivity context, final List<PaymentTransactionModel> transactions) {
        if (getDisplayBinder(context) != null) {
            getDisplayBinder(context).startCommand(new DisplayWelcomeMessageCommand());
        }

        //we leave void for payment process, we can cancel transaction during multiple tendering
        MoneybackProcessor.create(orderGuid, orderType).callback(new IVoidCallback() {


            @Override
            public void onVoidComplete(List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel) {
                orderPayed = BigDecimal.ZERO;

                if (callback != null)
                    callback.onCancel();

//                proceedToTender(context, R.style.DialogAnimation);
            }

            @Override
            public void onVoidFailure(BigDecimal ignore) {
                proceedToTender(context, 0);
            }

            @Override
            public void onVoidCancelled() {
                if (callback != null)
                    callback.onCancel();
            }
        }).childOrderModel(new SaleOrderModel(orderGuid))
                .initVoid(context,
                        null,
                        transactions,
                        TcrApplication.get().getBlackStoneUser(), true, true);
    }

    private void updateOrderStatus(final FragmentActivity context) {
        SuccessOrderCommand.start(context, this.orderGuid, this.salesmanGuids);
    }

    /**
     * 0...max value, depending on what has already been payed
     */
    private BigDecimal getPendingAmount() {
        if (this.orderPayed == null) {
            this.orderPayed = BigDecimal.ZERO;
        }
        return orderTotal.subtract(orderPayed);
    }

    /**
     * Add or subtract some biller from the total
     */
    private void applyAmount(BigDecimal value) {
        if (this.orderPayed == null) {
            this.orderPayed = BigDecimal.ZERO;
        }
        if (value != null) {
            this.orderPayed = this.orderPayed.add(value);
        }
    }

    private boolean finish() {
        if (callback == null) {
            return false;
        }
        callback.onSuccess();
        return true;
    }

    private IDisplayBinder getDisplayBinder(FragmentActivity activity) {
        if (activity instanceof IDisplayBinder) {
            return (IDisplayBinder) activity;
        }
        return null;
    }

    @Override
    public void onBillingSuccess(FragmentActivity context, PrepaidReleaseResult releaseResult) {
        Logger.d("onBillingSuccess");
    }

    @Override
    public void onBillingFailure() {

    }

    public interface IPaymentProcessor {

        public void onSuccess();

        public void onCancel();

        public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount);

        public void onBilling(ArrayList<PaymentTransactionModel> successfullCCtransactionModels, List<SaleOrderItemViewModel> prepaidList, List<SaleOrderItemViewModel> giftCardList);

        public void onRefund(final HistoryDetailedOrderItemListFragment.RefundAmount amount);

        public void onUpdateOrderList();

        public void onPrintComplete();
    }


}
