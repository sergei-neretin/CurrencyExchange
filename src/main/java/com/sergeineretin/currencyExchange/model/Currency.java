package com.sergeineretin.currencyExchange.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Currency {
    private Long id;
    private String code;
    private String fullName;
    private String sign;
}
