/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2023 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cairo.pool;

import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.TableToken;
import io.questdb.cairo.sql.CompiledQuery;
import io.questdb.cairo.sql.QueryBuilder;
import io.questdb.cairo.sql.SqlCompiler;
import io.questdb.griffin.*;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public class SqlCompilerPool extends AbstractMultiTenantPool<SqlCompilerPool.R> {
    private static final TableToken SQL_COMPILER_TOKEN = new TableToken("sqlCompiler", "sqlCompiler", 0, false);
    private final DatabaseSnapshotAgent databaseSnapshotAgent;
    private final CairoEngine engine;
    private final FunctionFactoryCache functionFactoryCache;

    public SqlCompilerPool(CairoEngine engine, DatabaseSnapshotAgent databaseSnapshotAgent) {
        super(engine.getConfiguration());
        this.engine = engine;
        this.functionFactoryCache = new FunctionFactoryCache(engine.getConfiguration(), ServiceLoader.load(FunctionFactory.class, FunctionFactory.class.getClassLoader()));
        this.databaseSnapshotAgent = databaseSnapshotAgent;
    }

    @Override
    protected byte getListenerSrc() {
        return PoolListener.SRC_SQL_COMPILER;
    }

    @Override
    protected R newTenant(TableToken tableName, Entry<R> entry, int index) {
        return new R(entry, index);
    }

    public class R implements PoolTenant, SqlCompiler {
        private final int index;
        private Entry<R> entry;
        private boolean open;
        private AbstractMultiTenantPool<R> pool;

        public R(Entry<R> entry, int index) {
            super(engine, functionFactoryCache, databaseSnapshotAgent);
            this.entry = entry;
            this.index = index;
            this.open = true;
        }

        @Override
        public void close() {
            if (open) {
                open = false;
                final AbstractMultiTenantPool<R> pool = this.pool;
                if (pool != null && entry != null) {
                    if (pool.returnToPool(this)) {
                        return;
                    }
                }
                super.close();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Entry<R> getEntry() {
            return entry;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public TableToken getTableToken() {
            return SQL_COMPILER_TOKEN;
        }

        public void goodbye() {
            entry = null;
            pool = null;
        }

        @Override
        public void refresh() {
        }

        @Override
        public void updateTableToken(TableToken tableToken) {
            // noop
        }

        @Override
        public CompiledQuery compile(@NotNull CharSequence query, @NotNull SqlExecutionContext executionContext) throws SqlException {
            return null;
        }

        @Override
        public QueryBuilder query() {
            return null;
        }
    }
}
