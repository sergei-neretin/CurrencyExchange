package com.sergeineretin.services;

import com.sergeineretin.dto.CurrencyDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyService {
    public static CurrencyDto convertResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return CurrencyDto.builder()
                    .id(rs.getLong("Id"))
                    .code(rs.getString("Code"))
                    .fullName(rs.getString("FullName"))
                    .sign(rs.getString("Sign"))
                    .build();
        } else {
            return new CurrencyDto();
        }
    }
}
