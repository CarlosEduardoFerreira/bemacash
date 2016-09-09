package com.kaching123.tcr.store;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.kaching123.tcr.store.ShopStore.SqlCommandTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by pkabakov on 28.07.2014.
 */
public class ShopSchemaEx {

    private static final String CREATE_STATEMENT_PREFIX = "SQL_CREATE";
    private static final String CREATE_TABLE_STATEMENT_PREFIX = "create table";
    private static final String CREATE_INDEX_STATEMENT_PREFIX = "create index";
    private static final String CREATE_VIEW_STATEMENT_PREFIX = "CREATE VIEW";
    private static final String FOREIGN_KEY_STATEMENT_FORMAT = "FOREIGN KEY(%1$s) REFERENCES %2$s(%3$s)";
    private static final String FOREIGN_KEY_STATEMENT_CASCADE_DELETE = " ON DELETE CASCADE";
    private static final String DROP_TABLE_STATEMENT_FORMAT = "drop table if exists %s";
    private static final String DROP_VIEW_STATEMENT_FORMAT = "drop view if exists %s";
    private static final String DROP_TRIGGER_STATEMENT_FORMAT = "drop trigger if exists %s";
    private static final String SELECT_TABLE_NAMES_STATEMENT = "SELECT name FROM sqlite_master WHERE type='table'";
    private static final String SELECT_VIEW_NAMES_STATEMENT = "SELECT name FROM sqlite_master WHERE type='view'";
    private static final String SELECT_TRIGGER_NAMES_STATEMENT = "SELECT name FROM sqlite_master WHERE type='trigger'";
    private static final String AUTOINCREMENT_STATEMENT = "AUTOINCREMENT";

    private static final String ANDROID_METADATA_TABLE_NAME = "android_metadata";
    private static final String SQLITE_SYSTEM_TABLES_PREFIX = "sqlite_";

    private static final HashMap<String, Table> CREATE_FOREIGN_KEY_STATEMENTS = new HashMap<String, Table>();

    public static void applyForeignKeys(String childTable, ForeignKey... foreignKeys) {
        if (TextUtils.isEmpty(childTable))
            throw new IllegalArgumentException("childTable cannot be empty or null!");
        if (foreignKeys == null || foreignKeys.length == 0)
            throw new IllegalArgumentException("foreignKeys cannot be empty or null!");
        if (CREATE_FOREIGN_KEY_STATEMENTS.containsKey(childTable))
            throw new IllegalStateException("foreign keys already applied to the table " + childTable + "!");

        CREATE_FOREIGN_KEY_STATEMENTS.put(childTable, new Table(childTable, foreignKeys));
    }

    private static final HashMap<String, List<String>> TMP_FIELDS = new HashMap<String, List<String>>();

    public static void applyTmpFields(String tableName, String... fields){
        if (TextUtils.isEmpty(tableName))
            throw new IllegalArgumentException("applyTmpFields: table cannot be empty or null!");
        if (fields == null || fields.length == 0)
            throw new IllegalArgumentException("applyTmpFields: fields cannot be empty or null!");
        if (TMP_FIELDS.containsKey(tableName))
            throw new IllegalStateException("temp fields already applied to the table " + tableName + "!");

        TMP_FIELDS.put(tableName, Arrays.asList(fields));
    }

    private static final ArrayList<Trigger> TRIGGERS = new ArrayList<Trigger>();

    public static void applyTriggers(Trigger... triggers) {
        if (triggers == null || triggers.length == 0)
            throw new IllegalArgumentException("triggers cannot be empty or null!");

        TRIGGERS.addAll(Arrays.asList(triggers));
    }

    public static void onCreate(final SQLiteDatabase db) {
        onCreate(db, false, false);
    }

    public static void onCreate(final SQLiteDatabase db, boolean keepSyncData) {
        onCreate(db, keepSyncData, false);
    }

    public static void onCreate(final SQLiteDatabase db, boolean keepSyncData, boolean isSyncDatabase) {
        ShopStore.init();

        ArrayList<Table> createTables = new ArrayList<Table>();
        List<String> createIndexStatements = new ArrayList<String>();
        List<String> createViewStatements = new ArrayList<String>();

        Field[] declaredFields = ShopSchema.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!isCreateStatementField(field)) {
                continue;
            }

            String sqlCreateStatement = getCreateStatement(field);
            if (sqlCreateStatement.startsWith(CREATE_TABLE_STATEMENT_PREFIX)) {
                String tableName = getTableName(sqlCreateStatement);
                if (keepSyncData && SqlCommandTable.TABLE_NAME.equals(tableName))
                    continue;

                Table table = !isSyncDatabase ? CREATE_FOREIGN_KEY_STATEMENTS.get(tableName) : null;
                if (table == null) {
                    table = new Table(tableName);
                }
                table.createStatement = sqlCreateStatement;
                createTables.add(table);
            } else if (sqlCreateStatement.startsWith(CREATE_INDEX_STATEMENT_PREFIX)) {
                createIndexStatements.add(sqlCreateStatement);
            } else if (sqlCreateStatement.startsWith(CREATE_VIEW_STATEMENT_PREFIX)) {
                createViewStatements.add(sqlCreateStatement);
            }
        }

        if (!isSyncDatabase)
            sortCreateTables(createTables);

        for (Table table: createTables) {
            db.execSQL(getCreateTableStatement(table, isSyncDatabase));
        }

        for (String sqlCreateStatement : createIndexStatements) {
            db.execSQL(sqlCreateStatement);
        }

        if (isSyncDatabase)
            return;

        for (Trigger trigger: TRIGGERS) {
            db.execSQL(getCreateTriggerStatement(trigger));
        }

        for (String sqlCreateStatement : createViewStatements) {
            db.execSQL(sqlCreateStatement);
        }
    }

    public static void onDrop(final SQLiteDatabase db) {
        onDrop(db, false);
    }

    public static void onDrop(final SQLiteDatabase db, boolean keepSyncData) {
        ShopStore.init();

        ArrayList<Table> dropTables = new ArrayList<Table>();
        List<String> dropViewStatements = new ArrayList<String>();
        List<String> dropTriggerStatements = new ArrayList<String>();

        Cursor c = db.rawQuery(SELECT_TABLE_NAMES_STATEMENT, null);
        while (c.moveToNext()) {
            String tableName = c.getString(0);
            if (keepSyncData && SqlCommandTable.TABLE_NAME.equals(tableName))
                continue;
            if (isSystemTable(tableName))
                continue;

            Table table = CREATE_FOREIGN_KEY_STATEMENTS.get(tableName);
            if (table == null) {
                table = new Table(tableName);
            }
            dropTables.add(table);
        }
        c.close();

        c = db.rawQuery(SELECT_VIEW_NAMES_STATEMENT, null);
        while (c.moveToNext()) {
            String viewName = c.getString(0);
            dropViewStatements.add(getDropViewStatement(viewName));
        }
        c.close();

        c = db.rawQuery(SELECT_TRIGGER_NAMES_STATEMENT, null);
        while (c.moveToNext()) {
            String triggerName = c.getString(0);
            dropTriggerStatements.add(getDropTriggerStatement(triggerName));
        }
        c.close();


        for (String sqlDropTriggerStatement : dropTriggerStatements) {
            db.execSQL(sqlDropTriggerStatement);
        }

        if (!db.getPath().contains("sync_"))
            sortCreateTables(dropTables);

        int count = dropTables.size();
        for (int i = count - 1; i >= 0; i--) {
            Table table = dropTables.get(i);
            db.execSQL(getDropTableStatement(table));
        }

        for (String sqlDropViewStatement : dropViewStatements) {
            db.execSQL(sqlDropViewStatement);
        }
    }

    public static List<String> getViewDropStatements(final SQLiteDatabase db) {
        List<String> dropViewStatements = new ArrayList<String>();
        Cursor c = db.rawQuery(SELECT_VIEW_NAMES_STATEMENT, null);
        while (c.moveToNext()) {
            String viewName = c.getString(0);
            dropViewStatements.add(getDropViewStatement(viewName));
        }
        c.close();
        return dropViewStatements;
    }

    public static List<String> getViewCreateStatement(final SQLiteDatabase db) {
        List<String> createViewStatements = new ArrayList<>();

        Field[] declaredFields = ShopSchema.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!isCreateStatementField(field)) {
                continue;
            }

            String sqlCreateStatement = getCreateStatement(field);
            if (sqlCreateStatement.startsWith(CREATE_VIEW_STATEMENT_PREFIX)) {
                createViewStatements.add(sqlCreateStatement);
            }
        }
        return createViewStatements;
    }

    private static boolean isSystemTable(String tableName) {
        return tableName.startsWith(SQLITE_SYSTEM_TABLES_PREFIX) || tableName.equals(ANDROID_METADATA_TABLE_NAME);
    }

    private static boolean isCreateStatementField(Field field) {
        return field.getName().startsWith(CREATE_STATEMENT_PREFIX);
    }

    private static String getCreateStatement(Field createStatementField) {
        String createStatement = null;
        try {
            createStatement = (String) createStatementField.get(null);
        } catch (IllegalAccessException ignore) {}
        if (TextUtils.isEmpty(createStatement))
            throw new IllegalArgumentException("Cannot get sql create statement from: " + createStatementField.getName());
        return createStatement;
    }

    private static String getDropTableStatement(Table table) {
        return String.format(Locale.US, DROP_TABLE_STATEMENT_FORMAT, table.tableName);
    }

    private static String getDropViewStatement(String viewName) {
        return String.format(Locale.US, DROP_VIEW_STATEMENT_FORMAT, viewName);
    }

    private static String getDropTriggerStatement(String triggerName) {
        return String.format(Locale.US, DROP_TRIGGER_STATEMENT_FORMAT, triggerName);
    }

    private static String getTableName(String createTableStatement) {
        return createTableStatement.substring(CREATE_TABLE_STATEMENT_PREFIX.length() + 1, createTableStatement.indexOf('('));
    }

    private static String getCreateTriggerStatement(Trigger trigger) {
        return "CREATE TRIGGER IF NOT EXISTS " + trigger.name + " " + trigger.time + " " + trigger.action + " ON " + trigger.tableName
                + " FOR EACH ROW "
                + (TextUtils.isEmpty(trigger.when) ? "" : " WHEN " + trigger.when)
                + " BEGIN "
                + trigger.body
                + ";END";
    }

    private static String getCreateTableStatement(Table table, boolean isSyncDatabase) {
        if (isSyncDatabase) {
            String statement = cutOffAutoincrementField(table);
            return cutOffTmpFields(table.tableName, statement);
        }

        if (table.foreignKeys == null) {
            return table.createStatement;
        }

        StringBuilder foreignKeyStatementsBuilder = new StringBuilder();
        for (ForeignKey foreignKey: table.foreignKeys) {
            if (foreignKeyStatementsBuilder.length() > 0)
                foreignKeyStatementsBuilder.append(',');
            foreignKeyStatementsBuilder.append(generateForeignKeyStatement(foreignKey));
        }
        return appendCreateForeignKeyStatement(table.createStatement, foreignKeyStatementsBuilder.toString());
    }

    private static String cutOffAutoincrementField(Table table) {
        int firstIndex = table.createStatement.indexOf(AUTOINCREMENT_STATEMENT);
        if (firstIndex == -1)
            return table.createStatement;

        int index = firstIndex + (AUTOINCREMENT_STATEMENT.length() - 1);
        boolean hasEndSemi = false;
        do {
            char ch = table.createStatement.charAt(++index);
            if (ch == ',') {
                hasEndSemi = true;
                --index;
                break;
            }
            if (ch == ')') {
                --index;
                break;
            }
        } while (index < table.createStatement.length());
        int lastIndex = index;

        index = firstIndex;
        boolean hasStartSemi = false;
        do {
            char ch = table.createStatement.charAt(--index);
            if (ch == '(') {
                ++index;
                break;
            }
            if (ch == ',') {
                hasStartSemi = true;
                ++index;
                break;
            }
        } while (index < table.createStatement.length());
        firstIndex = index;

        if (hasStartSemi && hasEndSemi) {
            firstIndex--;
        } else if (hasStartSemi) {
            firstIndex--;
        } else if (hasEndSemi) {
            lastIndex++;
        }

        return table.createStatement.substring(0, firstIndex) + table.createStatement.substring(lastIndex + 1, table.createStatement.length());
    }

    private static String cutOffTmpFields(String tableName, String createStatement){
        if (!TMP_FIELDS.containsKey(tableName))
            return createStatement;

        List<String> tmpFields = TMP_FIELDS.get(tableName);
        if (tmpFields.isEmpty())
            return createStatement;

        int openBracketIndex = createStatement.indexOf('(');

        String sub = createStatement.substring(openBracketIndex + 1, createStatement.length() - 1);

        for (String field : tmpFields){
            int index = sub.indexOf(field);
            if (index == -1)
                continue;

            int startIndex = 0;
            boolean first = true;
            while (index > 0){
                char ch = sub.charAt(--index);
                if (ch == ','){
                    startIndex = index;
                    first = false;
                    break;
                }
            }

            if (first){
                sub = sub.substring(sub.indexOf(',') + 1);
                continue;
            }

            boolean last = false;
            int endIndex = sub.indexOf(',', startIndex + 1);
            if (endIndex == -1){
                last = true;
            }

            if (last){
                sub = sub.substring(0, startIndex);
                continue;
            }

            sub = sub.substring(0, startIndex) + sub.substring(endIndex);
        }

        return createStatement.substring(0, openBracketIndex + 1) + sub + ")";
    }

    private static String generateForeignKeyStatement(ForeignKey foreignKey) {
        String foreignKeyStatement = String.format(Locale.US, FOREIGN_KEY_STATEMENT_FORMAT, foreignKey.childKey, foreignKey.parentTable, foreignKey.parentKey);
        if (foreignKey.enableCascadeDelete)
            foreignKeyStatement = foreignKeyStatement + FOREIGN_KEY_STATEMENT_CASCADE_DELETE;
        return foreignKeyStatement;
    }

    private static String appendCreateForeignKeyStatement(String createTableStatement, String createForeignKeyStatement) {
        return createTableStatement.substring(0, createTableStatement.length() - 1) + ',' + createForeignKeyStatement + ')';
    }

    private static void sortCreateTables(ArrayList<Table> createTableStatements) {
        boolean changed;
        HashSet<String> leftTables = new HashSet<String>(createTableStatements.size() - 1);
        do {
            changed = false;
            leftTables.clear();
            for (int i = 0; i < createTableStatements.size() - 1; i++) {
                Table leftTable = createTableStatements.get(i);

                boolean moveDown = false;
                if (leftTable.foreignKeys != null) {
                    for (ForeignKey foreignKey : leftTable.foreignKeys) {
                        if (foreignKey.parentTable.equals(leftTable.tableName))
                            continue;
                        if (!leftTables.contains(foreignKey.parentTable)) {
                            moveDown = true;
                            break;
                        }
                    }
                }

                if (!moveDown) {
                    leftTables.add(leftTable.tableName);
                    continue;
                }

                Table rightTable = createTableStatements.set(i + 1, leftTable);
                createTableStatements.set(i, rightTable);
                leftTables.add(rightTable.tableName);
                changed = true;
            }

        } while (changed);
    }


    private static class Table {
        public final String tableName;
        public final ArrayList<ForeignKey> foreignKeys;
        public String createStatement;

        public Table(String tableName) {
            this.tableName = tableName;
            foreignKeys = null;
        }

        public Table(String tableName, ForeignKey[] foreignKeys) {
            this.tableName = tableName;
            this.foreignKeys = new ArrayList<ForeignKey>();
            this.foreignKeys.addAll(Arrays.asList(foreignKeys));
        }

    }

    public static class ForeignKey {
        public final String childKey;
        public final String parentTable;
        public final String parentKey;
        public final boolean enableCascadeDelete;

        public static ForeignKey foreignKey(String childKey, String parentTable, String parentKey) {
            return new ForeignKey(childKey, parentTable, parentKey, false);
        }

        public static ForeignKey foreignKey(String childKey, String parentTable, String parentKey, boolean enableCascadeDelete) {
            return new ForeignKey(childKey, parentTable, parentKey, enableCascadeDelete);
        }

        private ForeignKey(String childKey, String parentTable, String parentKey, boolean enableCascadeDelete) {
            this.childKey = childKey;
            this.parentTable = parentTable;
            this.parentKey = parentKey;
            this.enableCascadeDelete = enableCascadeDelete;
        }
    }

    public static class Trigger {

        public final String name;
        public final Time time;
        public final Action action;
        public final String tableName;
        public final String when;
        public final String body;

        private Trigger(String name, Time time, Action action, String tableName, String when, String body) {
            this.name = name;
            this.time = time;
            this.action = action;
            this.tableName = tableName;
            this.when = when;
            this.body = body;
        }

        public static Trigger trigger(String name, Time time, Action action, String tableName, String when, String body) {
            return new Trigger(name, time, action, tableName, when, body);
        }

        public enum Time {
            BEFORE("BEFORE"), AFTER("AFTER");

            private final String name;

            private Time(String s) {
                name = s;
            }

            @Override
            public String toString(){
                return name;
            }
        }

        public enum Action {
            DELETE("DELETE"), INSERT("INSERT");

            private final String name;

            private Action(String s) {
                name = s;
            }

            @Override
            public String toString(){
                return name;
            }
        }



    }

}
