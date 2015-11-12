package com.kaching123.tcr.commands.print;

import android.content.Context;

import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.fragment.reports.InventoryValueFragment.Info;
import com.kaching123.tcr.function.SalesByCustomersWrapFunction;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SalesByCustomerModel;
import com.kaching123.tcr.print.printer.PosReportsPrinter;
import com.kaching123.tcr.print.processor.PrintReportsProcessor;
import com.kaching123.tcr.reports.ClockInOutReportQuery;
import com.kaching123.tcr.reports.ClockInOutReportQuery.EmployeeInfo;
import com.kaching123.tcr.reports.ClockInOutReportQuery.TimeInfo;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery.EmployeeTipsInfo;
import com.kaching123.tcr.reports.EmployeeTipsReportQuery.ShiftTipsInfo;
import com.kaching123.tcr.reports.InventoryStatusReportQuery;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.DepInfo;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.DepInfoWrapFunction;
import com.kaching123.tcr.reports.InventoryStatusReportQuery.ItemInfo;
import com.kaching123.tcr.reports.InventoryValueQuery;
import com.kaching123.tcr.reports.ItemManualMovementQuery;
import com.kaching123.tcr.reports.ItemManualMovementQuery.MovementInfo;
import com.kaching123.tcr.reports.PayrollReportQuery;
import com.kaching123.tcr.reports.PayrollReportQuery.EmployeePayrollInfo;
import com.kaching123.tcr.reports.ReorderReportQuery;
import com.kaching123.tcr.reports.ReorderReportQuery.ItemQtyInfo;
import com.kaching123.tcr.reports.SalesByCustomersReportQuery;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery.CategoryStat;
import com.kaching123.tcr.reports.SalesByDepartmentsReportQuery.DepartmentStatistics;
import com.kaching123.tcr.reports.SalesByDropsAndPayoutsReportQuery;
import com.kaching123.tcr.reports.SalesByItemsReportQuery;
import com.kaching123.tcr.reports.SalesByItemsReportQuery.ReportItemInfo;
import com.kaching123.tcr.reports.SalesByTenderTypeQuery;
import com.kaching123.tcr.reports.SalesByTenderTypeQuery.PaymentStat;
import com.kaching123.tcr.reports.SalesTop10QtyQuery;
import com.kaching123.tcr.reports.SalesTop10RevenuesQuery;
import com.kaching123.tcr.util.DateUtils;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;
import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by gdubina on 28.01.14.
 */
public final class SaleReportsProcessor {

    private SaleReportsProcessor() {
    }

    public static PrintReportsProcessor print(Context context, ReportType reportType, long startTime, long endTime, long resisterId, String employeeGuid, IAppCommandContext appCommandContext) {
        switch (reportType) {
            case SALES_BY_ITEMS:
                return printSaleByItems(context, startTime, endTime, resisterId, OrderType.SALE, appCommandContext);
            case PREPAID_SALES_BY_ITEMS:
                return printSaleByItems(context, startTime, endTime, resisterId, OrderType.PREPAID, appCommandContext);
            case SALES_BY_DEPS:
                return printSaleByDeps(context, startTime, endTime, resisterId, appCommandContext);
            case SALES_TOP_10_REVENUES:
                return printSalesTop10Revenues(context, startTime, endTime, resisterId, OrderType.SALE, appCommandContext);
            case PREPAID_SALES_TOP_10_REVENUES:
                return printSalesTop10Revenues(context, startTime, endTime, resisterId, OrderType.PREPAID, appCommandContext);
            case SALES_TOP_10_QTY:
                return printSalesTop10Qty(context, startTime, endTime, resisterId, appCommandContext);
            case SALES_BY_TENDER_TYPES:
                return printSaleByTenderTypes(context, startTime, endTime, resisterId, appCommandContext);
            case EMPLOYEE_ATTENDANCE:
                return printEmployeeAttendance(context, startTime, endTime, employeeGuid, appCommandContext);
            case INVENTORY_LOG:
                return printItemManualMovement(context, startTime, endTime, appCommandContext);
            case REORDER_INVENTORY:
                return printRestock(context, appCommandContext);
            case EMPLOYEE_PAYROLL:
                return printEmployeePayroll(context, startTime, endTime, employeeGuid, appCommandContext);
            case INVENTORY_VALUE:
                return printInventoryValue(context, appCommandContext);
            case RETURNED_ITEMS:
                return printReturnerItems(context, startTime, endTime, resisterId, appCommandContext);
            case SALES_BY_CUSTOMERS:
                return printSalesByCustomers(context, startTime, endTime, appCommandContext);
            case INVENTORY_STATUS:
                return printInventoryStatus(context, employeeGuid, appCommandContext);
            case INVENTORY_STATUS_POS:
                return printInventoryStatusPos(context, employeeGuid, appCommandContext);
            case EMPLOYEE_TIPS:
                return printEmployeeTips(context, startTime, endTime, employeeGuid, appCommandContext);
        }

        return null;
    }

    public static PrintReportsProcessor print(Context context, ReportType reportType, long startTime, long endTime, long resisterId, String employeeGuid, String name, IAppCommandContext appCommandContext, int type) {

        return printDropsAndPayouts(context, startTime, endTime, employeeGuid, name, appCommandContext, type);
    }

    private static PrintReportsProcessor printDropsAndPayouts(final Context context, long startTime, long endTime, String employeeGuid, final String name, IAppCommandContext appCommandContext, int type) {
        final Collection<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState> items = new SalesByDropsAndPayoutsReportQuery().getItems(employeeGuid, context, type, startTime, endTime);

        return new PrintReportsProcessor<SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState>(context.getString(R.string.report_type_drops_and_payouts), items, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                if (name == null)
                    printer.add(context.getString(R.string.register_label_all));
                else
                    printer.add(name);
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState item) {
                if (item == null)
                    return null;

                if (item.type == 0)
                    printer.add(context.getString(R.string.report_type_title_drops));
                else
                    printer.add(context.getString(R.string.report_type_title_payouts));
                printer.startBody();

                printer.add((PosReportsPrinter.superShortDateFormat.format(new Date(Long.parseLong(item.date)))).toString(), item.amount);
                if (item.comment != null)
                    printer.addComments(context.getString(R.string.comment), item.comment);
                printer.emptyLine();
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (SalesByDropsAndPayoutsReportQuery.DropsAndPayoutsState item : items) {
                    totalAmount = totalAmount.add(item.amount);
                }
                printer.startBody();
                printer.addWithTab(context.getString(R.string.reports_employee_tips_print_total), totalAmount);
                printer.endBody();
            }
        };
    }

    private static PrintReportsProcessor printEmployeeTips(final Context context, long startTime, long endTime, String employeeGuid, IAppCommandContext appCommandContext) {
        final Collection<ShiftTipsInfo> items = EmployeeTipsReportQuery.getItems(context, startTime, endTime, employeeGuid);
        return new PrintReportsProcessor<ShiftTipsInfo>(context.getString(R.string.report_type_tips_report), items, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ShiftTipsInfo item) {
                if (item.zeroTips())
                    return null;

                printer.addBold(item.toPeriodString(true));
                for (EmployeeTipsInfo i : item.employeeTipsInfos) {
                    if (i.zeroTips())
                        continue;

                    printer.add(i.fullName);
                    printer.startBody();
                    if (i.cashTips.compareTo(BigDecimal.ZERO) != 0) {
                        printer.addWithTab(context.getString(R.string.reports_employee_tips_print_cash), i.cashTips);
                    }

                    if (i.creditTips.compareTo(BigDecimal.ZERO) != 0) {
                        printer.addWithTab(context.getString(R.string.reports_employee_tips_print_credit), i.creditTips);
                    }
                    printer.endBody();

                }
                printer.emptyLine();
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                BigDecimal cash = BigDecimal.ZERO;
                BigDecimal credit = BigDecimal.ZERO;
                for (ShiftTipsInfo item : items) {
                    for (EmployeeTipsInfo i : item.employeeTipsInfos) {
                        cash = cash.add(i.cashTips);
                        credit = credit.add(i.creditTips);
                    }
                }
                printer.startBody();
                printer.addWithTab(context.getString(R.string.reports_employee_tips_print_total), cash.add(credit));
                printer.addWithTab(context.getString(R.string.reports_employee_tips_print_total_cash), cash);
                printer.addWithTab(context.getString(R.string.reports_employee_tips_print_total_credit), credit);
                printer.endBody();
            }
        };
    }

    private static PrintReportsProcessor printInventoryStatus(final Context context, String depGuid, IAppCommandContext appCommandContext) {
        final List<DepInfo> items = _wrap(
                InventoryStatusReportQuery.syncQuery(depGuid).perform(context),
                new DepInfoWrapFunction()
        );

        return new PrintReportsProcessor<DepInfo>(context.getString(R.string.report_type_inventory_status), items, null, null, appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                //print nothing
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printer.startBody();
                printer.subHeader7Columns(
                        context.getString(R.string.reports_inventory_status_header_title),
                        context.getString(R.string.reports_inventory_status_ean_upc_code),
                        context.getString(R.string.reports_inventory_status_ean_product_code),
                        context.getString(R.string.reports_inventory_status_header_on_hand),
                        context.getString(R.string.reports_inventory_status_header_unit_cost),
                        context.getString(R.string.reports_inventory_status_header_total_cost),
                        context.getString(R.string.reports_inventory_status_header_active)
                );
                printer.endBody();

                printer.emptyLine();

                for (DepInfo dep : items) {
                    printItem(printer, dep);
                }
                printer.drawLine();
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, DepInfo dep) {
                printer.addBold(dep.title);
                printer.startBody();
                for (ItemInfo item : dep.items) {
                    printItemItem(printer, item);
                }
                printer.endBody();
                printer.startBody();
                printer.addBold(context.getString(R.string.reports_inventory_status_total_cost), dep.totalCost);
                printer.endBody();
                printer.emptyLine();
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }

            private void printItemItem(IReportsPrinter printer, ItemInfo item) {
                printer.add(item.title, item.ean, item.productCode, item.onHand, item.unitCost, item.totalCost, item.active);
            }
        };

    }

    private static PrintReportsProcessor printInventoryStatusPos(final Context context, String depGuid, IAppCommandContext appCommandContext) {
        final List<DepInfo> items = _wrap(
                InventoryStatusReportQuery.syncQuery(depGuid).perform(context),
                new DepInfoWrapFunction()
        );

        return new PrintReportsProcessor<DepInfo>(context.getString(R.string.report_type_inventory_status), items, null, null, appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                //print nothing
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printer.startBody();
                printer.subHeader4Columns(
                        context.getString(R.string.reports_inventory_status_header_title),
                        context.getString(R.string.reports_inventory_status_header_on_hand),
                        context.getString(R.string.reports_inventory_status_header_unit_cost_short),
                        context.getString(R.string.reports_inventory_status_header_total_cost_short)
                );
                printer.endBody();

                printer.emptyLine();

                for (DepInfo dep : items) {
                    printItem(printer, dep);
                }
                printer.drawLine();
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, DepInfo dep) {
                printer.addBold(dep.title);
                printer.startBody();
                for (ItemInfo item : dep.items) {
                    if (item.active)
                        printItemItem(printer, item);
                }
                printer.endBody();
                printer.startBody();
                printer.addBold(context.getString(R.string.reports_inventory_status_total_cost), dep.totalCost);
                printer.endBody();
                printer.emptyLine();
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }

            private void printItemItem(IReportsPrinter printer, ItemInfo item) {
                printer.add4Columns(item.title, item.onHand, item.unitCost, item.totalCost);
            }
        };

    }

    private static PrintReportsProcessor printSalesByCustomers(final Context context, long startTime, long endTime, IAppCommandContext appCommandContext) {

        List<SalesByCustomerModel> items = _wrap(
                SalesByCustomersReportQuery
                        .syncQuery(startTime, endTime)
                        .perform(context),
                new SalesByCustomersWrapFunction()
        );

        return new PrintReportsProcessor<SalesByCustomerModel>(context.getString(R.string.report_type_by_customers), items, new Date(startTime), new Date(endTime), appCommandContext) {
            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_customer_header_name), context.getString(R.string.report_customer_header_total));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, SalesByCustomerModel item) {
                printer.add(item.customerName, item.totalAmount);
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }
        };
    }

    private static PrintReportsProcessor printReturnerItems(final Context context, long startTime, long endTime, long resisterId, IAppCommandContext appCommandContext) {
        final ArrayList<ReportItemInfo> items = new ArrayList<ReportItemInfo>(new SalesByItemsReportQuery(false).getItems(context, startTime, endTime, resisterId));
        BigDecimal totalValue = BigDecimal.ZERO;
        for (ReportItemInfo i : items) {
            totalValue = totalValue.add(i.revenue);
        }
        return new PrintReportsProcessor<ReportItemInfo>(context.getString(R.string.report_type_returned_items), items, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sale_by_items_subheader_item), context.getString(R.string.report_sale_by_items_subheader_qty), context.getString(R.string.report_sale_by_items_subheader_revenue));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ReportItemInfo item) {
                printer.add(item.description, item.qty, item.revenue);
                return item.revenue;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                printer.total(context.getString(R.string.report_sale_by_items_total), total);
            }
        };
    }

    private static PrintReportsProcessor printInventoryValue(final Context context, IAppCommandContext appCommandContext) {
        ArrayList<Info> info = InventoryValueQuery.getItems(context);
        return new PrintReportsProcessor<Info>(context.getString(R.string.report_type_inventory_value), info, null, null, appCommandContext) {
            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printTableHeader(printer);

                printer.emptyLine();

                printer.startBody();
                for (Info item : report) {
                    printItem(printer, item);

                }
                printer.endBody();
                printer.emptyLine();

                printer.drawLine();
            }

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sales_by_tender_types_header_type), context.getString(R.string.report_sales_by_tender_types_header_amount));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, Info item) {
                printer.add(context.getString(item.label), item.value);
                return item.value;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }
        };
    }

    private static PrintReportsProcessor printEmployeePayroll(final Context context, final long startTime, final long endTime, String employeeGuid, IAppCommandContext appCommandContext) {
        final Collection<EmployeePayrollInfo> items = PayrollReportQuery.getItems(context, startTime, endTime, employeeGuid);
        final boolean isCommissionEnabled = ((TcrApplication) context.getApplicationContext()).isCommissionsEnabled();
        return new PrintReportsProcessor<EmployeePayrollInfo>(context.getString(R.string.report_type_employee_payroll), items, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                //print nothing
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, EmployeePayrollInfo item) {
                printer.startBody();
                printer.addHourly(item.name, item.hRate);
                printer.addWithTab(context.getString(R.string.report_payroll_header_total_hours), DateUtils.formatMins(item.totalMins), false);
                if (isCommissionEnabled) {
                    printer.addWithTab(context.getString(R.string.report_payroll_header_commission), priceFormat(item.commission), false);
                }
                printer.addWithTab(context.getString(R.string.report_payroll_header_total_due), commaPriceFormat(isCommissionEnabled ? item.totalDue.add(item.commission) : item.totalDue), false);
                printer.endBody();
                return item.hRate;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.dateRange(context.getString(R.string.report_date_range_label), new Date(startTime), new Date(endTime));
                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printer.emptyLine();

                for (EmployeePayrollInfo info : items) {
                    printItem(printer, info);
                    printer.emptyLine();
                }

                printer.drawLine();
            }
        };

    }

    private static PrintReportsProcessor printRestock(final Context context, IAppCommandContext appCommandContext) {
        Collection<ItemQtyInfo> itemQtyInfos = ReorderReportQuery.getItems(context);
        return new PrintReportsProcessor<ItemQtyInfo>(context.getString(R.string.report_type_reorder_inventory), itemQtyInfos, null, null, appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader4Columns(
                        context.getString(R.string.report_reorder_inventory_header_item),
                        context.getString(R.string.report_reorder_inventory_header_qty),
                        context.getString(R.string.report_reorder_inventory_header_rec_qty_short),
                        context.getString(R.string.report_reorder_inventory_header_to_order_qty)
                );
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ItemQtyInfo item) {
                printer.add4Columns(
                        item.description,
                        item.qty,
                        item.recQty,
                        item.recQty.subtract(item.qty)
                );
                return null;
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printTableHeader(printer);
                printer.emptyLine();

                printer.startBody();
                for (ItemQtyInfo item : report) {
                    printItem(printer, item);
                }
                printer.endBody();

                printer.emptyLine();
                printer.drawLine();
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }
        };
    }

    private static PrintReportsProcessor printItemManualMovement(final Context context, long startTime, long endTime, IAppCommandContext appCommandContext) {
        ArrayList<MovementInfo> movementInfos = ItemManualMovementQuery.getItems(context, startTime, endTime);
        return new PrintReportsProcessor<MovementInfo>(context.getString(ReportType.INVENTORY_LOG.getLabelRes()), movementInfos, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader2(context.getString(R.string.report_item_manual_movement_header_date), context.getString(R.string.report_item_manual_movement_header_item), context.getString(R.string.report_item_manual_movement_header_qty_short));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, MovementInfo item) {
                printer.add(new Date(item.date), item.itemName, item.qty);
                return null;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                //print nothing
            }
        };

    }

    private static PrintReportsProcessor printEmployeeAttendance(final Context context, final long startTime, final long endTime, String employeeGuid, IAppCommandContext appCommandContext) {
        final Collection<EmployeeInfo> employeeInfos = ClockInOutReportQuery.getItems(context, startTime, endTime, employeeGuid);

        return new PrintReportsProcessor<EmployeeInfo>(context.getString(ReportType.EMPLOYEE_ATTENDANCE.getLabelRes()), employeeInfos, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                //nothing, no need table header
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, EmployeeInfo item) {
                printer.addBold(item.name);

                printer.startBody();
                for (TimeInfo time : item.times) {
                    printer.add(time.clockIn, time.clockOut, DateUtils.isSameDay(time.clockIn, time.clockOut));
                    if (time.clockOut != null) {
                        printer.addShiftHrs(context.getString(R.string.report_employee_attendance_header_shift), DateUtils.formatMins(time.getDiff()));
                    }
                }
                printer.endBody();

                return item.totalMins;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                printer.startBody();
                printer.addWithTab(context.getString(R.string.report_employee_attendance_list_total_hours), DateUtils.formatMins(total), true);
                printer.endBody();
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.dateRange(context.getString(R.string.report_date_range_label), new Date(startTime), new Date(endTime));
                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                for (EmployeeInfo info : employeeInfos) {
                    BigDecimal total = printItem(printer, info);
                    printTotal(printer, context.getString(R.string.report_employee_attendance_list_total_hours), total != null ? total : BigDecimal.ZERO);
                    printer.emptyLine();
                }

                printer.drawLine();

            }
        };

    }

    private static PrintReportsProcessor printSaleByItems(final Context context, long startTime, long endTime, long resisterId, OrderType orderType, IAppCommandContext appCommandContext) {
        Collection<ReportItemInfo> items = new SalesByItemsReportQuery().getItems(context, startTime, endTime, resisterId, orderType);

        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = getGroupedResult(new ArrayList<ReportItemInfo>(items), orderType);

        String reportName = context.getString(orderType == OrderType.SALE ? ReportType.SALES_BY_ITEMS.getLabelRes() : ReportType.PREPAID_SALES_BY_ITEMS.getLabelRes());
        return new PrintReportsProcessor<ReportItemInfo>(reportName, groupedResult, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                //print nothing
            }

            @Override
            protected void printBody(Context context, TcrApplication app, IReportsPrinter printer) {
                printer.subTitle(reportName);
                printer.drawLine();

                printer.time(context.getString(R.string.report_print_time_label), new Date());
                printer.drawLine();

                printer.startBody();
                printer.subHeader5Columns(context.getString(R.string.report_sale_by_items_subheader_item),
                        context.getString(R.string.report_sale_by_items_subheader_ean),
                        context.getString(R.string.report_sale_by_items_subheader_product_code),
                        context.getString(R.string.report_sale_by_items_subheader_qty),
                        context.getString(R.string.report_sale_by_items_subheader_revenue));

                printer.endBody();
                printer.emptyLine();
                printer.startBody();
                BigDecimal total = BigDecimal.ZERO;
                for (ReportItemInfo item : groupedResult) {
                    total = total.add(printItem(printer, item));
                }

                printer.endBody();
                printer.drawLine();

                printTotal(printer, context.getString(R.string.report_sale_by_items_total), total);
                printer.drawLine();
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ReportItemInfo item) {
                printer.add5Columns(item.description, item.ean, item.productCode, item.qty, item.revenue);
                return item.revenue;
            }
        };
    }

    private static PrintReportsProcessor printSalesTop10Revenues(final Context context, long startTime, long endTime, long resisterId, OrderType orderType, IAppCommandContext appCommandContext) {
        Collection<ReportItemInfo> items = new SalesTop10RevenuesQuery().getItems(context, startTime, endTime, resisterId, orderType);

        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = getGroupedResult(new ArrayList<ReportItemInfo>(items), orderType);
        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> sortedResult = getTop10SortedByRevenue(groupedResult);

        String reportName = context.getString(orderType == OrderType.SALE ? ReportType.SALES_TOP_10_REVENUES.getLabelRes() : ReportType.PREPAID_SALES_TOP_10_REVENUES.getLabelRes());
        return new PrintReportsProcessor<ReportItemInfo>(reportName, sortedResult, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sale_by_items_subheader_item), context.getString(R.string.report_sale_by_items_subheader_qty), context.getString(R.string.report_sale_by_items_subheader_revenue));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ReportItemInfo item) {
                printer.add(item.description, item.qty, item.revenue);
                return item.revenue;
            }
        };
    }

    public static ArrayList<SalesByItemsReportQuery.ReportItemInfo> getTop10SortedByRevenue(ArrayList<SalesByItemsReportQuery.ReportItemInfo> result) {
        final ArrayList<ReportItemInfo> items = getSortedByRevenue(result);
        return new ArrayList<ReportItemInfo>(items.subList(0, Math.min(10, items.size())));
    }

    public static ArrayList<SalesByItemsReportQuery.ReportItemInfo> getSortedByRevenue(ArrayList<SalesByItemsReportQuery.ReportItemInfo> result) {
        Collections.sort(result, new Comparator<ReportItemInfo>() {
            @Override
            public int compare(SalesByItemsReportQuery.ReportItemInfo l, SalesByItemsReportQuery.ReportItemInfo r) {
                return l.revenue == null ? -1 : r.revenue == null ? 1 : r.revenue.compareTo(l.revenue);
            }
        });
        return result;
    }

    public static ArrayList<SalesByItemsReportQuery.ReportItemInfo> getGroupedResult(final ArrayList<ReportItemInfo> result, OrderType orderType) {
        final HashMap<String, ReportItemInfo> groupedMap = new HashMap<String, SalesByItemsReportQuery.ReportItemInfo>(result.size());
        for (final SalesByItemsReportQuery.ReportItemInfo itemInfo : result) {
            String key = orderType == OrderType.SALE ? itemInfo.itemGuid : itemInfo.description;
            if (key == null)
                continue;
            if (groupedMap.containsKey(key)) {
                final SalesByItemsReportQuery.ReportItemInfo existingInfo = groupedMap.get(key);
                final SalesByItemsReportQuery.ReportItemInfo newInfo = new SalesByItemsReportQuery.ReportItemInfo(itemInfo.itemGuid, itemInfo.description,
                        itemInfo.ean, itemInfo.productCode,
                        existingInfo.qty.add(itemInfo.qty),
                        existingInfo.revenue.add(itemInfo.revenue));
                groupedMap.remove(key);
                groupedMap.put(key, newInfo);
            } else {
                groupedMap.put(key, itemInfo);
            }
        }
        return new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(groupedMap.values());
    }

    private static PrintReportsProcessor printSalesTop10Qty(final Context context, long startTime, long endTime, long resisterId, IAppCommandContext appCommandContext) {
        Collection<ReportItemInfo> items = new SalesTop10QtyQuery().getItems(context, startTime, endTime, resisterId);
        return new PrintReportsProcessor<ReportItemInfo>(context.getString(ReportType.SALES_TOP_10_QTY.getLabelRes()), items, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sale_by_items_subheader_item), context.getString(R.string.report_sale_by_items_subheader_qty), context.getString(R.string.report_sale_by_items_subheader_revenue));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, ReportItemInfo item) {
                printer.add(item.description, item.qty, item.revenue);
                return item.qty;
            }

            @Override
            protected void printTotal(IReportsPrinter printer, String totalLabel, BigDecimal total) {
                printer.total2(context.getString(R.string.report_sale_by_items_total_qty), total);
            }
        };
    }

    private static PrintReportsProcessor printSaleByDeps(final Context context, long startTime, long endTime, long resisterId, IAppCommandContext appCommandContext) {
        Collection<DepartmentStatistics> deps = new SalesByDepartmentsReportQuery().getItems(context, startTime, endTime, resisterId);
        return new PrintReportsProcessor<DepartmentStatistics>(context.getString(ReportType.SALES_BY_DEPS.getLabelRes()), deps, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sales_by_deps_header_title), context.getString(R.string.report_sale_by_items_subheader_revenue));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, DepartmentStatistics item) {
                printer.addBold(item.description, item.revenue);
                Collection<CategoryStat> cats = item.getSortedList();
                item.reset();
                for (CategoryStat c : cats) {
                    printer.addWithTab(c.description, c.revenue);
                }
                return item.revenue;
            }
        };
    }

    private static PrintReportsProcessor printSaleByTenderTypes(final Context context, long startTime, long endTime, long resisterId, IAppCommandContext appCommandContext) {
        PaymentStat stat = SalesByTenderTypeQuery.getItems(context, startTime, endTime, resisterId);
        ArrayList<TenderInfo> result = new ArrayList<TenderInfo>();
        result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_cash), stat.cash));
        result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_credit), stat.creditCard));
        result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_credit_receipt), stat.creditReceipt));
        result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_offline_credit), stat.offlineCredit));
        result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_check), stat.check));
        ShopInfo shopInfo = ((TcrApplication) context.getApplicationContext()).getShopInfo();
        if (shopInfo.acceptEbtCards) {
            result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_ebt_cash), stat.ebtCash));
            result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_ebt_foodstamp), stat.ebtFoodstamp));
        }
        if (shopInfo.acceptDebitCards) {
            result.add(new TenderInfo(context.getString(R.string.report_sales_by_tender_types_debit), stat.debit));
        }

        return new PrintReportsProcessor<TenderInfo>(context.getString(ReportType.SALES_BY_TENDER_TYPES.getLabelRes()), result, new Date(startTime), new Date(endTime), appCommandContext) {

            @Override
            protected void printTableHeader(IReportsPrinter printer) {
                printer.subHeader(context.getString(R.string.report_sales_by_tender_types_header_type), context.getString(R.string.report_sales_by_tender_types_header_amount));
            }

            @Override
            protected BigDecimal printItem(IReportsPrinter printer, TenderInfo item) {
                printer.add(item.title, item.amaount);
                return item.amaount;
            }
        };
    }

    public static class TenderInfo {
        private String title;
        private BigDecimal amaount;

        public TenderInfo(String title, BigDecimal amaount) {
            this.title = title;
            this.amaount = amaount;
        }
    }

    private static class HeaderRow {

        String name;

        private HeaderRow(String name) {
            this.name = name;
        }
    }

    private static class TotalRow {

        BigDecimal total;

        private TotalRow(BigDecimal total) {
            this.total = total;
        }
    }

}
