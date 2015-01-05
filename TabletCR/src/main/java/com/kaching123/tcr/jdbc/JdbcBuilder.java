package com.kaching123.tcr.jdbc;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.service.DecimalQueryParam;
import com.kaching123.tcr.service.QueryParam;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gdubina on 08/11/13.
 */
public abstract class JdbcBuilder {


    public static final String FIELD_UPDATE_TIME = "UPDATE_TIME";
    public static final String FIELD_SHOP_ID = "SHOP_ID";
    public static final String FIELD_IS_DELETED = "IS_DELETED";

    private static final String NULL = "NULL";

    protected final String methodPrefix;
    protected final String table;
    protected final String action;
    protected final ArrayList<Pair> columns = new ArrayList<Pair>();
    protected final ArrayList<Pair> where = new ArrayList<Pair>();
    //protected ArrayList<String> where = new ArrayList<String>();

    private JdbcBuilder(String table, String action, String methodPrefix) {
        this.table = table;
        this.action = action;
        this.methodPrefix = methodPrefix;
    }

    /*public JdbcBuilder addColumn(String column, String value) {
        columns.add(new Pair(column, Types.VARCHAR, value));
        return this
    }*/

    public JdbcBuilder add(String column, String value) {
        columns.add(new Pair(column, Types.VARCHAR, value));
        return this;
        //return addColumn(column, value == null ? NULL : "'" + value + "'");
    }

    public JdbcBuilder add(String column, BigDecimal value) {
        columns.add(new Pair(column, Types.DECIMAL, value));
        return this;
        //return addColumn(column, _jdbcDecimal(value));
    }

    public JdbcBuilder add(String column, BigDecimal value, int decimalScale) {
        columns.add(new Pair(column, new DecimalQueryParam(value, decimalScale)));
        return this;
    }

    public JdbcBuilder add(String column, boolean value) {
        columns.add(new Pair(column, Types.BOOLEAN, value));
        return this;
//        return addColumn(column, _jdbcBool(value));
    }

    public JdbcBuilder add(String column, Date date) {
        columns.add(new Pair(column, Types.TIMESTAMP, date));
        return this;
    }

    public JdbcBuilder add(String column, Enum value) {
        columns.add(new Pair(column, Types.VARCHAR, value == null ? null : value.name()));
        return this;
        //return addColumn(column, _jdbcEnum(value));
    }

    public JdbcBuilder add(String column, int value) {
        columns.add(new Pair(column, Types.INTEGER, value));
        return this;
//        return addColumn(column, String.valueOf(value));
    }

    public JdbcBuilder add(String column, long value) {
        columns.add(new Pair(column, Types.BIGINT, value));
        return this;
        //return addColumn(column, String.valueOf(value));
    }

    public JdbcBuilder where(String column, String value) {
        where.add(new Pair(column, Types.VARCHAR, value));
        return this;
        /*this.where.add(column + " = '" + value + "'");
        return this;*/
    }

    public JdbcBuilder where(String column, long value) {
        where.add(new Pair(column, Types.BIGINT, value));
        return this;
        /*this.where.add(column + " = " + value);
        return this;*/
    }

    protected String getWhereString() {
        if (where.isEmpty())
            return null;
        return Joiner.on(" AND ").join(
                FluentIterable.from(where).transform(new WhereTransformation())
        );
    }

    protected String buildCmd(){
        JSONObject json = new JSONObject();
        try {
            json.put("table", table);
            json.put("action", action);
            json.put("args", getArgsJson());
            return json.toString();
        } catch (JSONException e) {
            Logger.e("Can't create cmd", e);
            return null;
        }
    }

    protected JSONObject getArgsJson() throws JSONException {
        JSONObject args = new JSONObject();
        for (Pair c : columns) {
            args.put(c.column, c.value.isNull() ? JSONObject.NULL : c.value.toJson());
        }
        return args;
    }

    protected JSONObject getWhereJson() throws JSONException {
        JSONObject args = new JSONObject();
        for (Pair c : where) {
            args.put(c.column, c.value.isNull() ? JSONObject.NULL : c.value.toJson());
        }
        return args;
    }

    public SingleSqlCommand build(String method){
        return new SingleSqlCommand(
                methodPrefix + method,
                buildCmd()
        );
    }

    private static class Pair {
        final String column;
        final QueryParam value;

        private Pair(String column, int type, Object value) {
            this.column = column;
            this.value = new QueryParam(type, value);
        }

        private Pair(String column, QueryParam value) {
            this.column = column;
            this.value = value;
        }

    }

    public static JdbcBuilder _insert(String table, IAppCommandContext appCommandContext) {
        return new InsertBuilder(table)
                .add(FIELD_SHOP_ID, appCommandContext.getShopId());
    }

    public static JdbcBuilder _insert(long shopId, String table) {
        return new InsertBuilder(table)
                .add(FIELD_SHOP_ID, shopId);
    }

    public static InsertOrUpdateBuilder _insertOrUpdate(String table, IAppCommandContext appCommandContext) {
        InsertOrUpdateBuilder builder = new InsertOrUpdateBuilder(table);
        builder.add(FIELD_SHOP_ID, appCommandContext.getShopId());
        return builder;
    }

    public static JdbcBuilder _update(String table, IAppCommandContext appCommandContext) {
        return new UpdateBuilder(table);
    }

    /*public static JdbcBuilder _delete(String table){
        return new DeleteBuilder(table);
    }*/

    /*private static final class DeleteBuilder extends JdbcBuilder {

        protected DeleteBuilder(String table) {
            super(table, "delete", JdbcFactory.METHOD_DELETE);
        }

        @Override
        protected JSONObject getArgsJson() throws JSONException {
            return super.getWhereJson();
        }
    }*/

    private static final class UpdateBuilder extends JdbcBuilder {

        protected UpdateBuilder(String table) {
            super(table, "update", JdbcFactory.METHOD_UPDATE);
        }

        @Override
        protected JSONObject getArgsJson() throws JSONException {
            JSONObject json = new JSONObject();

            json.put("update", super.getArgsJson());
            json.put("where", getWhereJson());
            return json;
        }
    }

    private static final class InsertBuilder extends JdbcBuilder {

        protected InsertBuilder(String table) {
            super(table, "insert", JdbcFactory.METHOD_ADD);
        }
    }

    public static final class InsertOrUpdateBuilder extends JdbcBuilder {

        /*private final InsertBuilder insert;
        private final JdbcBuilder update;*/

        private InsertOrUpdateBuilder(String table) {
            super(table, "replace", "replace");
            //insert = (InsertBuilder)_insert(shopId, table);
            //update = new UpdateBuilder(shopId, table);
        }

        /*@Override
        protected JSONObject getArgsJson() throws JSONException {
            JSONObject insertCmd = insert.getArgsJson();
            JSONObject updateCmd = update.getArgsJson();

            JSONObject args = new JSONObject();
            args.put("insert", insertCmd);
            args.put("update", updateCmd);
            return args;
        }

        public JdbcBuilder insert() {
            return insert;
        }

        public JdbcBuilder update() {
            return update;
        }*/
    }

    private class WhereTransformation implements Function<Pair, String> {

        @Override
        public String apply(Pair pair) {
            return pair.column + " = ?";
        }
    }

    private class ColumnNameTransformation implements Function<Pair, String> {

        @Override
        public String apply(Pair pair) {
            return pair.column;
        }
    }

    private class ValuePlaceTransformation implements Function<Pair, String> {

        @Override
        public String apply(Pair pair) {
            return "?";
        }
    }

    private class ValueTransformation implements Function<Pair, QueryParam> {

        @Override
        public QueryParam apply(Pair pair) {
            return pair.value;
        }
    }
}
