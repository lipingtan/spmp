package com.spmp.owner.config;

import com.spmp.common.util.EncryptUtils;
import com.spmp.common.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis AES 加解密 TypeHandler。
 * <p>
 * 写入数据库时自动加密，读取时自动解密。
 * 由于 TypeHandler 不是 Spring Bean，通过 {@link SpringContextHolder} 获取 {@link EncryptUtils}。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@MappedTypes(String.class)
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private EncryptUtils getEncryptUtils() {
        return SpringContextHolder.getBean(EncryptUtils.class);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, getEncryptUtils().encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? getEncryptUtils().decrypt(value) : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? getEncryptUtils().decrypt(value) : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? getEncryptUtils().decrypt(value) : null;
    }
}
