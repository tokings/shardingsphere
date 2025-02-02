/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.scaling.core.job.check.source;

import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Abstract data source checker.
 */
public abstract class AbstractDataSourceChecker implements DataSourceChecker {
    
    @Override
    public final void checkConnection(final Collection<? extends DataSource> dataSources) {
        try {
            for (DataSource each : dataSources) {
                each.getConnection().close();
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("Data sources can not connect.", ex);
        }
    }
    
    @Override
    public final void checkTargetTable(final Collection<? extends DataSource> dataSources, final Collection<String> tableNames) {
        try {
            for (DataSource each : dataSources) {
                checkEmpty(each, tableNames);
            }
        } catch (final SQLException ex) {
            throw new PrepareFailedException("Check target table failed.", ex);
        }
    }
    
    private void checkEmpty(final DataSource dataSource, final Collection<String> tableNames) throws SQLException {
        for (String each : tableNames) {
            String sql = getSQLBuilder().buildCheckEmptySQL(each);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    throw new PrepareFailedException(String.format("Target table `%s` is not empty, sql: %s.", each, sql));
                }
            }
        }
    }
    
    protected abstract ScalingSQLBuilder getSQLBuilder();
}
