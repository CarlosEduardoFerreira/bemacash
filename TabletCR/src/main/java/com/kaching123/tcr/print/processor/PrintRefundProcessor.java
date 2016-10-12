package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.net.Uri;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery.PrintHandler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel.AddonInfo;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.fragment.UiHelper.integralIntegerFormat;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintRefundProcessor extends BasePrintProcessor<ITextPrinter> {

    private static final Uri SALE_ITEMS_URI = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    private static final Uri PAYMENT_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);
    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private boolean reprint;

    private List<RefundSaleItemInfo> refundItems;
    private List<String> transactionsGuids;
    private BigDecimal total = BigDecimal.ZERO;

    private String childOrderGuid;

    public PrintRefundProcessor setChildOrderGuid(String childOrderGuid) {
        this.childOrderGuid = childOrderGuid;
        return this;
    }

    public PrintRefundProcessor(String orderGuid, List<RefundSaleItemInfo> items, List<String> transactionsGuids, boolean reprint, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.refundItems = items;
        this.transactionsGuids = transactionsGuids;
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

    @Override
    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        printerWrapper.subTitle(context.getString(R.string.print_order_refund_subtitle));
        printerWrapper.drawLine();

        final boolean printTips = this.refundItems.isEmpty();
        if (printTips) {
            final BigDecimal tipsAmount = OrderTotalPriceCursorQuery.loadTips(context, orderGuid);
            printerWrapper.orderFooter(context.getString(R.string.printer_refund_tips), negative(tipsAmount), false);
            printerWrapper.drawLine();
            printerWrapper.orderFooter(context.getString(R.string.printer_refund_total), negative(tipsAmount));
        } else {
            final HashMap<String, BigDecimal> saleItemsQty = new HashMap<String, BigDecimal>();
            for (RefundSaleItemInfo i : refundItems) {
                saleItemsQty.put(i.saleItemGuid, i.qty);
            }
            OrderTotalPriceCursorQuery.loadSync(context, orderGuid, childOrderGuid, new PrintHandler() {

                @Override
                public void handleItem(String saleItemGuid, String description, String unitLabel,
                                       PriceType priceType, BigDecimal qty, BigDecimal itemSubtotal,
                                       BigDecimal itemDiscount, BigDecimal itemTax,
                                       BigDecimal singleItemPrice, List<Unit> units,
                                       ArrayList<AddonInfo> addons,
                                       BigDecimal transactionFee, BigDecimal itemFullPrice,
                                       String note, TaxGroupModel model1, TaxGroupModel model2, BigDecimal loyaltyPoints) {
                    if (!saleItemsQty.containsKey(saleItemGuid))
                        return;

                    BigDecimal refundQty = saleItemsQty.get(saleItemGuid);
                    BigDecimal itemTotal = getSubTotal(refundQty, singleItemPrice);
                    List<String> unitAsStrings = new ArrayList<String>(units.size());
                    if (childOrderGuid != null) {
                        for (Unit unit : units) {
                            if (unit.childOrderId != null && unit.childOrderId.equals(childOrderGuid)) {
                                unitAsStrings.add(unit.serialCode);
                            }
                        }
                    }
                    if (app.getShopPref().printDetailReceipt().get())
                        printerWrapper.add(description, refundQty, negative(itemTotal), singleItemPrice, unitLabel, priceType == PriceType.UNIT_PRICE, unitAsStrings);
                    else
                        printerWrapper.add(description, refundQty, negative(itemTotal), singleItemPrice, unitAsStrings);
                    total = total.add(itemTotal);
                    if (loyaltyPoints != null) {
                        orderInfo.earnedLoyaltyPoints = orderInfo.earnedLoyaltyPoints.add(getSubTotal(refundQty, loyaltyPoints.divide(qty)));
                    }
                }

                @Override
                public void handleTotal(BigDecimal totalSubtotal, Map<TaxGroupModel, BigDecimal> subtotals, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal totalLoyaltyPoints, BigDecimal tipsAmount, BigDecimal transactionFee, Map<TaxGroupModel, BigDecimal> taxes) {
                    printerWrapper.drawLine();
                    printerWrapper.orderFooter(context.getString(R.string.printer_refund_total), negative(total));
                }
            });
        }

        List<PaymentTransactionModel> payments = null;
        if (transactionsGuids != null && !transactionsGuids.isEmpty()) {
            payments = ReadPaymentTransactionsFunction.load(context, transactionsGuids);
            for (PaymentTransactionModel p : payments) {
                updateHasCreditCardPayment(p.gateway.isCreditCard());
                printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName, p.amount);
            }
        }
    }

    @Override
    protected void printLoyalty(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        if (orderInfo.customerLoyaltyPoints != null) {
            printerWrapper.header("Total Bonus Points Available", integralIntegerFormat(orderInfo.customerLoyaltyPoints));

            if (orderInfo.earnedLoyaltyPoints != null && orderInfo.earnedLoyaltyPoints.compareTo(BigDecimal.ZERO) != 0) {
                printerWrapper.header("Bonus Points on this Return", integralIntegerFormat(orderInfo.earnedLoyaltyPoints.negate()));
            }
        }
    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        super.printFooter(context, app, printerWrapper);
    }
}
