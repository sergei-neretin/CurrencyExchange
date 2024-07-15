package com.sergeineretin.currencyExchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExchangeRateDto {
    Integer id;
    CurrencyDto baseCurrency;
    CurrencyDto targetCurrency;
    BigDecimal rate;
}
