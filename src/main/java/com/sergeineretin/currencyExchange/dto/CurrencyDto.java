package com.sergeineretin.currencyExchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CurrencyDto {
    private Long id;
    private String code;
    private String name;
    private String sign;
}
