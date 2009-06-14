  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.xapool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import javax.sql.XAConnection;

public class TransactedConnection implements Connection {
    
    private Connection wrappedConnection;
    private XAConnection xaConnection;
    
    public TransactedConnection(XAConnection xaConnection) throws SQLException {
        this.wrappedConnection = xaConnection.getConnection();
        this.xaConnection = xaConnection;
    }
    
    /**
     * Closes the pooled and XA connections.
     */
    public void closeForReal() {
        try {
            // wrappedConnection.close();
            xaConnection.close();
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Delegate operation.
     */
    public void clearWarnings() throws SQLException {
        wrappedConnection.clearWarnings();
    }

    /**
     * Don't close the physical connection.
     */
    public void close() throws SQLException {
    }

    /**
     * Delegate operation.
     */
    public void commit() throws SQLException {
        wrappedConnection.commit();
    }

    /**
     * Delegate operation.
     */
    public Statement createStatement() throws SQLException {
        return wrappedConnection.createStatement();
    }

    /**
     * Delegate operation.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Delegate operation.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * Delegate operation.
     */
    public boolean getAutoCommit() throws SQLException {
        return wrappedConnection.getAutoCommit();
    }

    /**
     * Delegate operation.
     */
    public String getCatalog() throws SQLException {
        return wrappedConnection.getCatalog();
    }

    /**
     * Delegate operation.
     */
    public int getHoldability() throws SQLException {
        return wrappedConnection.getHoldability();
    }

    /**
     * Delegate operation.
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return wrappedConnection.getMetaData();
    }

    /**
     * Delegate operation.
     */
    public int getTransactionIsolation() throws SQLException {
        return wrappedConnection.getTransactionIsolation();
    }

    /**
     * Delegate operation.
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return wrappedConnection.getTypeMap();
    }

    /**
     * Delegate operation.
     */
    public SQLWarning getWarnings() throws SQLException {
        return wrappedConnection.getWarnings();
    }

    /**
     * Delegate operation.
     */
    public boolean isClosed() throws SQLException {
        return wrappedConnection.isClosed();
    }

    /**
     * Delegate operation.
     */
    public boolean isReadOnly() throws SQLException {
        return wrappedConnection.isReadOnly();
    }

    /**
     * Delegate operation.
     */
    public String nativeSQL(String sql) throws SQLException {
        return wrappedConnection.nativeSQL(sql);
    }

    /**
     * Delegate operation.
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Delegate operation.
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Delegate operation.
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return wrappedConnection.prepareCall(sql);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return wrappedConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return wrappedConnection.prepareStatement(sql, columnIndexes);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return wrappedConnection.prepareStatement(sql, columnNames);
    }

    /**
     * Delegate operation.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return wrappedConnection.prepareStatement(sql);
    }

    /**
     * Delegate operation.
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        wrappedConnection.releaseSavepoint(savepoint);
    }

    /**
     * Delegate operation.
     */
    public void rollback() throws SQLException {
        wrappedConnection.rollback();
    }

    /**
     * Delegate operation.
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        wrappedConnection.rollback(savepoint);
    }

    /**
     * Delegate operation.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        wrappedConnection.setAutoCommit(autoCommit);
    }

    /**
     * Delegate operation.
     */
    public void setCatalog(String catalog) throws SQLException {
        wrappedConnection.setCatalog(catalog);
    }

    /**
     * Delegate operation.
     */
    public void setHoldability(int holdability) throws SQLException {
        wrappedConnection.setHoldability(holdability);
    }

    /**
     * Delegate operation.
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        wrappedConnection.setReadOnly(readOnly);
    }

    /**
     * Delegate operation.
     */
    public Savepoint setSavepoint() throws SQLException {
        return wrappedConnection.setSavepoint();
    }

    /**
     * Delegate operation.
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return wrappedConnection.setSavepoint(name);
    }

    /**
     * Delegate operation.
     */
    public void setTransactionIsolation(int level) throws SQLException {
        wrappedConnection.setTransactionIsolation(level);
    }

    /**
     * Delegate operation.
     */
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        wrappedConnection.setTypeMap(map);
    }

}
