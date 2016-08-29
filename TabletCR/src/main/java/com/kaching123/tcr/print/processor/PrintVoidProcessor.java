package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintVoidProcessor extends BasePrintProcessor<PosOrderTextPrinter> {

    private static final Uri SALE_ITEMS_URI = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private List<String> transactionsGuids;
    private boolean reprint;

    public PrintVoidProcessor(String orderGuid, List<String> transactionsGuids, boolean reprint, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.transactionsGuids = transactionsGuids;
        this.reprint = reprint;
    }

    @Override
    public void printHeader(Context context, TcrApplication app, PosOrderTextPrinter printerWrapper) {
        if (reprint) {
            printerWrapper.subTitle(context.getString(R.string.print_order_copy_header));
            printerWrapper.emptyLine();
        }

        super.printHeader(context, app, printerWrapper);
    }



    @Override
    protected void printBody(final Context context, final TcrApplication app, final PosOrderTextPrinter printerWrapper) {
        printerWrapper.subTitle(context.getString(R.string.print_order_void_subtitle));
        printerWrapper.drawLine();

        List<SaleOrderItemViewModel> items = _wrap(ProviderAction
                .query(SALE_ITEMS_URI)
                .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .orderBy(SaleItemTable.SEQUENCE)
                .perform(context),
                new SaleOrderItemViewModelWrapFunction(context));

        OrderTotalPriceCalculator.calculate(items, null, new Handler() {

            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty,
                                   BigDecimal itemPriceWithAddons, BigDecimal ignore1,
                                   BigDecimal ignore2, BigDecimal itemEbtTotal,
                                   BigDecimal itemFinalPrice,
                                   BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                BigDecimal itemTotal = CalculationUtil.getSubTotal(qty, itemFinalPrice);
                printerWrapper.add(description, qty, itemPriceWithAddons, CalculationUtil.negative(itemTotal), null);
            }

            @Override
            public void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue) {
                printerWrapper.drawLine();
                printerWrapper.orderFooter(context.getString(R.string.printer_refund_total), CalculationUtil.negative(totalOrderPrice));
            }

        });

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
    protected void printMidTid(PosOrderTextPrinter printer, String label, String value, boolean bold) {
        printer.addWithTab2(label, value, true, bold);
    }

    private OrderType getOrderType(Context context){
        Cursor c = ProviderAction.query(SALE_ORDER_URI)
                .projection(SaleOrderTable.ORDER_TYPE)
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);
        OrderType type = null;
        if (c.moveToFirst()){
            type = ContentValuesUtil._orderType(c, 0);
        }
        c.close();
        return type;
    }
}
