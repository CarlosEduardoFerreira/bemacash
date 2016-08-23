package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 06/02/14.
 */
public class BillPaymentDescriptionJdbcConverter extends JdbcConverter<BillPaymentDescriptionModel> {

    private static final String TABLE_NAME = "BILL_PAYMENTS_DESCRIPTION";

    private static final String ID = "ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String TYPE = "TYPE";
    private static final String IS_VOIDED = "IS_VOIDED";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String IS_FAILED = "IS_FAILED";
    private static final String ORDER_ID = "ORDER_ID";

    @Override
    public BillPaymentDescriptionModel toValues(JdbcJSONObject rs) throws JSONException {
        return new BillPaymentDescriptionModel(
                rs.getString(ID),
                rs.getString(DESCRIPTION),
                _enum(PrepaidType.class, rs.getString(TYPE), null),
                rs.getLong(TRANSACTION_ID),
                rs.getBoolean(IS_VOIDED),
                rs.getBoolean(IS_FAILED),
                rs.getString(ORDER_ID)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(BillPaymentDescriptionModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(DESCRIPTION, model.description)
                .add(TYPE, model.type.name())
                .add(TRANSACTION_ID,  model.description.equalsIgnoreCase("Gift Card") ? 0 : model.orderId)
                .add(IS_VOIDED,model.isVoided)
                .add(IS_FAILED, model.isFailed)
                .add(ORDER_ID, model.saleOrderId)
                .build(JdbcFactory.getApiMethod(CategoryModel.class));
    }

    @Override
    public SingleSqlCommand updateSQL(BillPaymentDescriptionModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(IS_VOIDED, model.isVoided)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateFailedStatusSQL(long transactionId, boolean isFailed, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(IS_FAILED, isFailed)
                .where(TRANSACTION_ID, transactionId)
                .build(JdbcFactory.getApiMethod(BillPaymentDescriptionModel.class));
    }

    public SingleSqlCommand updateOrderIdSQL(BillPaymentDescriptionModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ORDER_ID, model.saleOrderId)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
