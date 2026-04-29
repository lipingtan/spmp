package com.spmp.common.init.runtime;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 可替换目标数据源的委托实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public class SwitchableDataSource extends AbstractDataSource {

    private volatile DataSource delegate;

    public SwitchableDataSource(DataSource initialDelegate) {
        this.delegate = initialDelegate;
    }

    public synchronized void replace(DataSource newDelegate) {
        DataSource oldDelegate = this.delegate;
        this.delegate = newDelegate;
        closeQuietly(oldDelegate);
    }

    public boolean hasDelegate() {
        return delegate != null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return currentDelegate().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return currentDelegate().getConnection(username, password);
    }

    private DataSource currentDelegate() {
        DataSource current = this.delegate;
        if (current == null) {
            throw new IllegalStateException("business datasource is not ready");
        }
        return current;
    }

    private void closeQuietly(DataSource dataSource) {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception ignored) {
                // ignore close exception
            }
        }
    }
}
