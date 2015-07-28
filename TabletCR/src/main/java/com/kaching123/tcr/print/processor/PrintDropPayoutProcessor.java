package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.util.DateUtils;
import com.kaching123.tcr.util.PhoneUtil;

import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.print.processor.BasePrintProcessor.getCityStateZip;

/**
 * Created by vkompaniets on 25.07.2014.
 */
public class PrintDropPayoutProcessor {

    private static final Uri URI_MOVEMENT = ShopProvider.getContentWithLimitUri(CashDrawerMovementTable.URI_CONTENT, 1);
    private static final Uri URI_EMPLOYEE = ShopProvider.getContentWithLimitUri(EmployeeTable.URI_CONTENT, 1);

    private String guid;

    public PrintDropPayoutProcessor(String guid) {
        this.guid = guid;
    }

    public void print(Context context, TcrApplication app, IReportsPrinter printer) {
        printHeader(context, app, printer);
        printBody(context, app, printer);
        printFooter(app, printer);
    }

    private void printHeader(Context context, TcrApplication app, IReportsPrinter printer) {
        if (app.isTrainingMode()) {
            printer.footer(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (app.printLogo()) {
            printer.logo();
            printer.emptyLine();
        }

        final ShopInfo shopInfo = app.getShopInfo();

        printer.header(shopInfo.name);
        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printer.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            printer.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printer.footer(phone);
        }

        printer.emptyLine();
    }

    private void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
        Cursor movementCursor = ProviderAction.query(URI_MOVEMENT)
                .where(CashDrawerMovementTable.GUID + " = ?", guid)
                .perform(context);
        if (!movementCursor.moveToFirst()) {
            movementCursor.close();
            return;
        }

        Cursor employeeCursor = ProviderAction.query(URI_EMPLOYEE)
                .projection(EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME)
                .where(EmployeeTable.GUID + " = ?", movementCursor.getString(movementCursor.getColumnIndex(CashDrawerMovementTable.MANAGER_GUID)))
                .perform(context);
        if (!employeeCursor.moveToFirst()) {
            movementCursor.close();
            employeeCursor.close();
            return;
        }

        MovementType type = ContentValuesUtil._movementType(movementCursor, movementCursor.getColumnIndex(CashDrawerMovementTable.TYPE));
        long time = movementCursor.getLong(movementCursor.getColumnIndex(CashDrawerMovementTable.MOVEMENT_TIME));
        String employeeName = UiHelper.concatFullname(employeeCursor.getString(0), employeeCursor.getString(1));
        BigDecimal amount = ContentValuesUtil._decimal(movementCursor, movementCursor.getColumnIndex(CashDrawerMovementTable.AMOUNT)).negate();
        String comment = movementCursor.getString(movementCursor.getColumnIndex(CashDrawerMovementTable.COMMENT));
        movementCursor.close();
        employeeCursor.close();

        printer.footer(type == MovementType.DROP ? context.getString(R.string.safe_drop) : context.getString(R.string.payout));
        printer.add(DateUtils.dateAndTimeAttendanceFormat(new Date(time)));
        printer.add(employeeName);
        printer.drawLine();
        printer.add(context.getString(R.string.amount), amount);
        if (comment != null) {
            printer.addComments(context.getString(R.string.comment), comment);
        }
    }

    private void printFooter(TcrApplication app, IReportsPrinter printer) {

    }
}
