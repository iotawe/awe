package io.questdb.cairo.sql;

import io.questdb.griffin.BatchCallback;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.std.QuietCloseable;
import org.jetbrains.annotations.NotNull;

public interface SqlCompiler extends QuietCloseable {
    CompiledQuery compile(@NotNull CharSequence query, @NotNull SqlExecutionContext executionContext) throws SqlException;

    /**
     * Allows processing of batches of sql statements (sql scripts) separated by ';' .
     * Each query is processed in sequence and processing stops on first error and whole batch gets discarded.
     * Noteworthy difference between this and 'normal' query is that all empty queries get ignored, e.g.
     * <br>
     * select 1;<br>
     * ; ;/* comment \*\/;--comment\n; - these get ignored <br>
     * update a set b=c  ; <br>
     * <p>
     * Useful PG doc link :
     *
     * @param query            - block of queries to process
     * @param executionContext - SQL execution context
     * @param batchCallback    - callback to perform actions prior to or after batch part compilation, e.g. clear caches or execute command
     * @throws SqlException - in case of syntax error
     * @throws Exception    - propagated from the callback
     */
    void compileBatch(@NotNull CharSequence query, @NotNull SqlExecutionContext executionContext, BatchCallback batchCallback) throws Exception;

    QueryBuilder query();
}
