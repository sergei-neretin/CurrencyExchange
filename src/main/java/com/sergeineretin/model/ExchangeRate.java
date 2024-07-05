package com.sergeineretin.model;

import com.sergeineretin.dto.CurrencyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExchangeRate {
    Integer ID;
    CurrencyDto baseCurrency;
    CurrencyDto targetCurrency;
    BigDecimal rate;
}
