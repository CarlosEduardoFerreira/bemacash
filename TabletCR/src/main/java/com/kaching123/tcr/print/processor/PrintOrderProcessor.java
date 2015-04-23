package com.kaching123.tcr.print.processor;

import android.content.Context;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery.PrintHandler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintOrderProcessor extends BasePrintProcessor<ITextPrinter> {

    private boolean reprint;

    private final String TRANSACTION_FEE = "Transaction Fee";

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtotal(String subtotal) {
        this.subTotal = subtotal;
    }

    public void setDiscountTotal(String discountTotal) {
        this.discountTotal = discountTotal;
    }

    public void setTaxTotal(String taxTotal) {
        this.taxTotal = taxTotal;
    }

    public void setPaxTransactions(ArrayList<PaymentTransactionModel> transactions) {
        this.transactions = transactions;
    }

    public void setAmountTotal(String amountTotal) {
        this.amountTotal = amountTotal;
    }


    public PrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public PrintOrderProcessor(String orderGuid, boolean reprint, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.reprint = reprint;
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        if (reprint) {
            printerWrapper.subTitle(context.getString(R.string.print_order_copy_header));
            printerWrapper.emptyLine();
        }

        super.printHeader(context, app, printerWrapper);
    }

    @Override
    protected void printMidTid(ITextPrinter printer, String label, String value, boolean bold) {
        printer.addWithTab2(label, value, true, bold);
    }

    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        final String changeText = context.getString(R.string.print_order_change_label);
        final String itemDiscountText = context.getString(R.string.print_order_item_discount);
        final List<PaymentTransactionModel> payments = (transactions != null && transactions.size() != 0) ? transactions : ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);
        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new PrintHandler() {
            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty,
                                   BigDecimal itemSubtotal, BigDecimal itemDiscount,
                                   BigDecimal itemTax, BigDecimal singleItemPrice, List<Unit> units, ArrayList<SaleOrderItemViewModel.AddonInfo> addons, BigDecimal transactionFee, BigDecimal itemFullPrice, String note) {
                List<String> unitAsStrings = new ArrayList<String>(units.size());
                for (Unit unit : units) {
                    unitAsStrings.add(unit.serialCode);
                }

                BigDecimal itemPrice = itemFullPrice;
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        if (addon.addon.type != ModifierType.MODIFIER)
                            itemPrice = itemPrice.subtract(addon.addon.extraCost);
                    }
                itemSubtotal = CalculationUtil.getSubTotal(qty, itemPrice);
                printerWrapper.add(description, qty, itemSubtotal, unitAsStrings);
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        String title = addon.addonTitle;
                        if (addon.addon.type == ModifierType.OPTIONAL)
                            title = "NO " + addon.addonTitle;
                        BigDecimal newBD = addon.addon.extraCost.setScale(2);
                        printerWrapper.addAddsOn(title, newBD);
                    }
                if (transactionFee.compareTo(BigDecimal.ZERO) > 0) {
                    printerWrapper.addAddsOn(TRANSACTION_FEE, transactionFee);
                }
                if (itemDiscount.compareTo(BigDecimal.ZERO) == 1) {
                    printerWrapper.addItemDiscount(itemDiscountText, negative(itemDiscount));
                }
                printerWrapper.addNotes(note);
            }

            @Override
            public void handleTotal(BigDecimal totalSubtotal, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal tipsAmount, BigDecimal transactionFee) {
                BigDecimal totalCashBack = BigDecimal.ZERO;

                printerWrapper.drawLine();
                for (PaymentTransactionModel p : payments) {
                    totalCashBack = totalCashBack.add(p.cashBack.negate());
                }
                if (totalCashBack.compareTo(BigDecimal.ZERO) > 0)
                    printerWrapper.orderFooter(context.getString(R.string.printer_cash_back), totalCashBack);

                if (BigDecimal.ZERO.compareTo(totalDiscount) != 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_discount), negative(totalDiscount));
                }
                if (subTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_subtotal), totalSubtotal.subtract(totalDiscount).add(transactionFee).add(totalCashBack));
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_subtotal), (new BigDecimal(subTotal)).subtract(new BigDecimal(discountTotal).add(totalCashBack)));

                if (taxTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_tax), totalTax);
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_tax), new BigDecimal(taxTotal));

                if (BigDecimal.ZERO.compareTo(tipsAmount) != 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_tips), tipsAmount);
                }

                BigDecimal totalOrderPrice = totalSubtotal.add(totalTax).subtract(totalDiscount);
                if (amountTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), totalOrderPrice.add(tipsAmount).add(transactionFee).add(totalCashBack), true);
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), new BigDecimal(amountTotal).add(transactionFee).add(totalCashBack), true);
                printerWrapper.drawLine();
            }
        });


        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;
            printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName, isChanged ? p.amount.add(p.changeAmount).add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));
//            if (p.gateway.name().equalsIgnoreCase(PaymentGateway.PAX_DEBIT.name())) {
//                if (p.lastFour != null)
//                    printerWrapper.add(UiHelper.formatLastFour(p.lastFour));
//                if (p.authorizationNumber != null)
//                    printerWrapper.addWithTab2(context.getString(R.string.printer_auth_number), p.authorizationNumber, true, false);
//            }

            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            if (p.balance != null && p.lastFour != null) {
                printerWrapper.orderFooter(context.getString(R.string.printer_balance) + " (" + p.lastFour + ")", p.balance, true);
            }
        }
    }

}
