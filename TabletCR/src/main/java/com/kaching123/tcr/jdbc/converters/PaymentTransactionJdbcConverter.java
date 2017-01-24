package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 20/11/13.
 */
public class PaymentTransactionJdbcConverter extends JdbcConverter<PaymentTransactionModel> {

    private static final String TABLE_NAME = "PAYMENT_TRANSACTION";

    private static final String ID = "ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String PARENT_ID = "PARENT_ID";
    private static final String AMOUNT = "AMOUNT";
    private static final String TYPE = "TYPE";
    private static final String STATUS = "STATUS";
    private static final String GATEWAY = "GATEWAY";
    private static final String GT_PAYMENT_ID = "GT_PAYMENT_ID";
    private static final String GT_PREAUTH_PAYMENT_ID = "GT_PREAUTH_PAYMENT_ID";
    private static final String GT_CLOSED_PERAUTH_GUID = "GT_CLOSED_PERAUTH_GUID";
    private static final String DECLINE_REASON = "DECLINE_REASON";
    private static final String OPERATOR_ID = "OPERATOR_ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String CARD_NAME = "CARD_NAME";
    private static final String CHANGE_AMOUNT = "CHANGE_AMOUNT";
    private static final String IS_PREAUTH = "IS_PREAUTH";
    private static final String CASH_BACK = "CASH_BACK";
    private static final String EBT_BALANCE = "EBT_BALANCE";

    private static final String LAST_FOUR = "LAST_FOUR";
    private static final String ENTRY_METHOD = "ENTRY_METHOD";
    private static final String APPLICATION_IDENTIFIER = "APPLICATION_IDENTIFIER";
    private static final String APPLICATION_CRYPTOGRAM_TYPE = "APPLICATION_CRYPTOGRAM_TYPE";
    private static final String AUTHORIZATION_NUMBER = "AUTHORIZATION_NUMBER";
    private static final String SIGNATURE_BYTES = "SIGNATURE_BYTES";

    @Override
    public PaymentTransactionModel toValues(JdbcJSONObject rs) throws JSONException {
        return new PaymentTransactionModel(
                rs.getString(ID),
                rs.getString(PARENT_ID),
                rs.getString(ORDER_ID),
                rs.getBigDecimal(AMOUNT),
                _enum(PaymentType.class, rs.getString(TYPE), PaymentType.SALE),
                _enum(PaymentStatus.class, rs.getString(STATUS), PaymentStatus.FAILED),
                rs.getString(OPERATOR_ID),
                _enum(PaymentGateway.class, rs.getString(GATEWAY), null),
                rs.getString(GT_PAYMENT_ID),
                rs.getString(GT_PREAUTH_PAYMENT_ID),
                rs.getString(GT_CLOSED_PERAUTH_GUID),
                rs.getString(DECLINE_REASON),
                rs.getDate(CREATE_TIME),
                rs.getString(SHIFT_ID),
                rs.getString(CARD_NAME),
                rs.getBigDecimal(CHANGE_AMOUNT),
                rs.getBoolean(IS_PREAUTH),
                rs.getBigDecimal(CASH_BACK),
                rs.getBigDecimal(EBT_BALANCE)
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
    public String getParentGuidColumn() {
        return PARENT_ID;
    }

    @Override
    public SingleSqlCommand insertSQL(PaymentTransactionModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ORDER_ID, model.orderGuid)
                .add(PARENT_ID, model.parentTransactionGuid)
                .add(AMOUNT, model.amount)
                .add(TYPE, model.paymentType)
                .add(STATUS, model.status)
                .add(OPERATOR_ID, model.operatorId)
                .add(GATEWAY, model.gateway)
                .add(GT_PAYMENT_ID, model.paymentId)
                .add(GT_PREAUTH_PAYMENT_ID, model.preauthPaymentId)
                .add(GT_CLOSED_PERAUTH_GUID, model.closedPerauthGuid)
                .add(DECLINE_REASON, model.declineReason)
                .add(CREATE_TIME, model.createTime)
                .add(SHIFT_ID, model.shiftGuid)
                .add(CARD_NAME, model.cardName)
                .add(CHANGE_AMOUNT, model.changeAmount)
                .add(IS_PREAUTH, model.isPreauth)
                .add(CASH_BACK, model.cashBack)
                .add(EBT_BALANCE, model.balance)
                .add(LAST_FOUR, model.lastFour)
                .add(ENTRY_METHOD, model.entryMethod)
                .add(APPLICATION_IDENTIFIER, model.applicationIdentifier)
                .add(APPLICATION_CRYPTOGRAM_TYPE, model.applicationCryptogramType)
                .add(AUTHORIZATION_NUMBER, model.authorizationNumber)
                .add(SIGNATURE_BYTES, model.paxDigitalSignature)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(PaymentTransactionModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(STATUS, model.status)
                .add(AMOUNT, model.amount)
                .add(GT_PAYMENT_ID, model.paymentId)
                .add(GT_CLOSED_PERAUTH_GUID, model.closedPerauthGuid)
                .add(DECLINE_REASON, model.declineReason)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateAmountTypeStatus(PaymentTransactionModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(STATUS, model.status)
                .add(AMOUNT, model.amount)
                .add(TYPE, model.paymentType)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateStatus(PaymentTransactionModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(STATUS, model.status)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand insertDeleted(PaymentTransactionModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ORDER_ID, model.orderGuid)
                .add(PARENT_ID, model.parentTransactionGuid)
                .add(AMOUNT, model.amount)
                .add(TYPE, model.paymentType)
                .add(STATUS, model.status)
                .add(OPERATOR_ID, model.operatorId)
                .add(GATEWAY, model.gateway)
                .add(GT_PAYMENT_ID, model.paymentId)
                .add(GT_PREAUTH_PAYMENT_ID, model.preauthPaymentId)
                .add(GT_CLOSED_PERAUTH_GUID, model.closedPerauthGuid)
                .add(DECLINE_REASON, model.declineReason)
                .add(CREATE_TIME, model.createTime)
                .add(SHIFT_ID, model.shiftGuid)
                .add(CARD_NAME, model.cardName)
                .add(CHANGE_AMOUNT, model.changeAmount)
                .add(IS_PREAUTH, model.isPreauth)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .add(CASH_BACK, model.cashBack)
                .add(EBT_BALANCE, model.balance)
                .build(JdbcFactory.getApiMethod(model));
    }
}
