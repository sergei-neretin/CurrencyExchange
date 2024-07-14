package com.sergeineretin.dao;

public class Statements {
    private Statements() {}
    public static final String EXCHANGE_RATE_SELECT_BY_CODE = "SELECT \n" +
            "    er.ID, \n" +
            "    er.Rate, \n" +
            "    c.ID AS BaseID, \n" +
            "    c.FullName AS BaseFullName, \n" +
            "    c.Code AS BaseCode, \n" +
            "    c.Sign AS BaseSign, \n" +
            "    c2.ID AS TargetID, \n" +
            "    c2.FullName AS TargetFullName, \n" +
            "    c2.Code AS TargetCode, \n" +
            "    c2.Sign AS TargetSign\n" +
            "FROM \n" +
            "    ExchangeRates er\n" +
            "INNER JOIN \n" +
            "    Currencies c ON er.BaseCurrencyID = c.ID AND c.Code = ?\n" +
            "INNER JOIN \n" +
            "    Currencies c2 ON er.TargetCurrencyID = c2.ID AND c2.Code = ?;\n";
    public static final String EXCHANGE_RATE_CREATE = "INSERT OR ABORT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)\n" +
            "VALUES ((SELECT ID FROM Currencies WHERE Code = ?), \n" +
            "        (SELECT ID FROM Currencies WHERE Code = ?), \n" +
            "        ?);";

    public static final String EXCHANGE_RATE_UPDATE = "UPDATE ExchangeRates SET Rate = ? WHERE\n" +
            "BaseCurrencyId = (SELECT ID from Currencies c WHERE c.Code = ?)\n" +
            "AND TargetCurrencyId = (SELECT ID from Currencies c2 WHERE c2.Code = ?);";

    public static final String EXCHANGE_RATE_SELECT_ALL = "SELECT er.ID, er.Rate, c.ID AS BaseID, c.FullName AS BaseFullName, c.Code AS BaseCode, c.Sign AS BaseSign, c2.*,\n" +
            "c2.ID AS TargetID, c2.FullName AS TargetFullName, c2.Code AS TargetCode, c2.Sign AS TargetSign\n" +
            "from ExchangeRates er \n" +
            "INNER JOIN Currencies c ON er.BaseCurrencyId = c.ID \n" +
            "INNER JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID;";
}
