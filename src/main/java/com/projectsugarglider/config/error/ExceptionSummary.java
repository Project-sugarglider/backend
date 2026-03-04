package com.projectsugarglider.config.error;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

public final class ExceptionSummary {

    private ExceptionSummary() {}

    private static final Pattern KEY_COLUMNS = Pattern.compile("Key \\(([^)]+)\\)=");

    public static String summarize(Throwable t) {
        SqlSummary s = summarizeSql(t);

        // Sql exception 상황
        if (s != null) {

            return "DB[" + nvl(s.sqlState) + "]"
                    + " table=" + nvl(s.table)
                    + " constraint=" + nvl(s.constraint)
                    + (s.keyColumns != null ? " key=(" + s.keyColumns + ")" : "")
                    + " detail=" + nvl(s.detail);
        }

        // 일반적인 exception 상황
        return t.getClass().getSimpleName() + ": " + nvl(t.getMessage());
    }

    private static SqlSummary summarizeSql(Throwable t) {
        PSQLException p = findPSQLException(t);
        if (p == null) return null;

        ServerErrorMessage sem = p.getServerErrorMessage();
        String sqlState = p.getSQLState();

        String constraint = (sem != null) ? sem.getConstraint() : null;
        String table = (sem != null) ? sem.getTable() : null;
        String detail = (sem != null) ? sem.getDetail() : p.getMessage();

        String keyColumns = null;
        if (detail != null) {
            Matcher m = KEY_COLUMNS.matcher(detail);
            if (m.find()) keyColumns = m.group(1);
        }

        return new SqlSummary(sqlState, constraint, table, detail, keyColumns);
    }

    private static PSQLException findPSQLException(Throwable t) {
        Throwable cur = t;
        int depth = 0;
        while (cur != null && depth < 12) {
            if (cur instanceof PSQLException p) return p;
            cur = cur.getCause();
            depth++;
        }
        return null;
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static final class SqlSummary {
        private final String sqlState;
        private final String constraint;
        private final String table;
        private final String detail;
        private final String keyColumns;

        private SqlSummary(String sqlState, String constraint, String table, String detail, String keyColumns) {
            this.sqlState = sqlState;
            this.constraint = constraint;
            this.table = table;
            this.detail = detail;
            this.keyColumns = keyColumns;
        }
    }
}