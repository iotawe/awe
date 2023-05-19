package io.questdb.cairo.sql;

import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.Mutable;
import io.questdb.std.str.StringSink;

public class QueryBuilder implements Mutable {
    private final SqlCompiler sqlCompiler;
    private final StringSink sink = new StringSink();

    public QueryBuilder(SqlCompiler sqlCompiler) {
        this.sqlCompiler = sqlCompiler;
    }

    public QueryBuilder $(CharSequence value) {
        sink.put(value);
        return this;
    }

    public QueryBuilder $(int value) {
        sink.put(value);
        return this;
    }

    @Override
    public void clear() {
        sink.clear();
    }

    public CompiledQuery compile(SqlExecutionContext executionContext) throws SqlException {
        return sqlCompiler.compile(sink, executionContext);
    }
}
