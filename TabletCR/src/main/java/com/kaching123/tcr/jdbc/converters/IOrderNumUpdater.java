package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.service.SingleSqlCommand;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by Vladimir on 29.08.2016.
 */
public interface IOrderNumUpdater {
    SingleSqlCommand updateOrderNum(String id, int orderNum, IAppCommandContext appCommandContext);
}
