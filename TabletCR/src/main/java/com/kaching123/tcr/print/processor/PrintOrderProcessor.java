package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery.PrintHandler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.fragment.UiHelper.integerFormat;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
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

    public void setPrepaidReleaseResults(ArrayList<PrepaidReleaseResult> results) {
        this.prepaidReleaseResults = results;
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
        final List<PaymentTransactionModel> payments = (transactions != null && transactions.size() != 0) ? transactions : ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new PrintHandler() {

            @Override
            public void handleItem(String saleItemGuid, String description, String unitLabel, PriceType priceType, BigDecimal qty,
                                   BigDecimal itemSubtotal, BigDecimal itemDiscount,
                                   BigDecimal itemTax, BigDecimal singleItemPrice, List<Unit> units, ArrayList<SaleOrderItemViewModel.AddonInfo> addons, BigDecimal transactionFee, BigDecimal itemFullPrice, String note, TaxGroupModel model1, TaxGroupModel model2) {
                List<String> unitAsStrings = new ArrayList<String>(units.size());
                Comparator<SaleOrderItemViewModel.AddonInfo> comparator = new Comparator<SaleOrderItemViewModel.AddonInfo>() {
                    @Override
                    public int compare(SaleOrderItemViewModel.AddonInfo lhs, SaleOrderItemViewModel.AddonInfo rhs) {
                        int dif = lhs.addon.type.ordinal() - rhs.addon.type.ordinal();
                        if (dif != 0)
                            return dif;
                        return lhs.groupName.compareTo(rhs.groupName);
                    }
                };
                if (addons != null)
                    Collections.sort(addons, comparator);
                for (Unit unit : units) {
                    unitAsStrings.add(unit.serialCode);
                }

                BigDecimal itemPrice = itemFullPrice;
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        itemPrice = itemPrice.subtract(addon.addon.extraCost);
                    }
                itemSubtotal = CalculationUtil.getSubTotal(qty, itemPrice);
                if (app.getShopPref().printDetailReceipt().get())
                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitLabel, priceType == PriceType.UNIT_PRICE, unitAsStrings);
                else
                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitAsStrings);
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
                if (note != null)
                    printerWrapper.addNotes(note, context.getString(R.string.notes_edit_fragment_title) + ": ");
            }

            @Override
            public void handleTotal(BigDecimal totalSubtotal, Map<TaxGroupModel, BigDecimal> subtotals, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal totalLoyaltyPoints, BigDecimal tipsAmount, BigDecimal transactionFee, Map<TaxGroupModel, BigDecimal> taxes) {
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

                orderInfo.earnedLoyaltyPoints = totalLoyaltyPoints;
            }
        });


        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;
            printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName, isChanged ? p.amount.add(p.changeAmount).add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));
            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            if (p.balance != null && p.gateway.isEbt()) {
                printerWrapper.orderFooter(context.getString(R.string.printer_balance), new BigDecimal(FormatterUtil.priceFormat(p.balance)), true);
            }
        }

        BigDecimal counts = getSaleItemAmount(orderGuid, context);
        if (counts.compareTo(BigDecimal.ZERO) > 0) {
            printerWrapper.header(context.getString(R.string.printer_sale_item_amount), String.valueOf(counts));
        }

        if (prepaidReleaseResults != null)
            for (PrepaidReleaseResult result : prepaidReleaseResults) {
                if (Integer.parseInt(result.error) == 200) {
                    if (result.receipt != null) {
                        String[] prints = getFormattedLine(result.receipt);
                        for (String line : prints) {
                            printerWrapper.add(line);
                        }
                    }
                } else {
                    printerWrapper.header(context.getString(R.string.prepaid_mini_item_description), result.model.description);
                    printerWrapper.header(context.getString(R.string.prepaid_mini_fail_error), result.error);
                    printerWrapper.header(context.getString(R.string.prepaid_mini_fail_error_msg), result.errorMSG);
                }

            }
    }

    @Override
    protected void printFooter(TcrApplication app, ITextPrinter printerWrapper) {
        if (orderInfo.customerLoyaltyPoints != null){
            printerWrapper.header("Total Bonus Points Available", integerFormat(orderInfo.customerLoyaltyPoints));
        }
        if (orderInfo.earnedLoyaltyPoints != null && orderInfo.earnedLoyaltyPoints.compareTo(BigDecimal.ZERO) != 0){
            printerWrapper.header("Bonus Points on this Sale", integerFormat(orderInfo.earnedLoyaltyPoints));
        }
        super.printFooter(app, printerWrapper);
    }

    private String[] getFormattedLine(String receipt) {
        String[] prints = receipt.split("\\n");
        return prints;
    }

    private static final Uri SALE_ITEM_ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);

    protected BigDecimal getSaleItemAmount(String orderGuid, Context context) {
        BigDecimal saleItemAmount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(SALE_ITEM_ORDER_URI)
                .where(ShopSchema2.SaleOrderItemsView2.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(context);
        BigDecimal itemQty = BigDecimal.ZERO;
        HashMap<String, BigDecimal> list = new HashMap<>();
        if (c.moveToFirst()) {
            do {
                String unitLabel = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.ItemTable.UNITS_LABEL));
                itemQty = unitLabel != null && unitLabel.equalsIgnoreCase("LB") ? BigDecimal.ONE : _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.SaleItemTable.QUANTITY)));
                String itemGuid = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.SaleItemTable.SALE_ITEM_GUID));
                list.put(itemGuid, itemQty);
//                if(!unitLabel.equalsIgnoreCase("LB"))
//                    saleItemAmount = saleItemAmount.add(itemQty);
//                else
//                    saleItemAmount = saleItemAmount.add(BigDecimal.ONE);
            } while (c.moveToNext());
            c.close();
        }
        Iterator iter = list.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            BigDecimal val = (BigDecimal) entry.getValue();
            saleItemAmount = saleItemAmount.add(val);
        }
        return saleItemAmount;
    }

}
