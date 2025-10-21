package com.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

/**
 * Currency Utilities
 * Para birimi işlemleri için yardımcı sınıf
 * 
 * Özellikler:
 * - Döviz kuru dönüşümleri
 * - Para birimi formatlaması
 * - Yuvarlama kuralları
 * - Çoklu para birimi desteği
 */
public class CurrencyUtils {

    // Supported currencies with their properties
    private static final Map<String, CurrencyInfo> SUPPORTED_CURRENCIES = new HashMap<>();
    
    // Exchange rates (mock data for testing)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();
    
    static {
        // Initialize supported currencies
        SUPPORTED_CURRENCIES.put("TL", new CurrencyInfo("TL", "Turkish Lira", "₺", 2, "tr_TR"));
        SUPPORTED_CURRENCIES.put("USD", new CurrencyInfo("USD", "US Dollar", "$", 2, "en_US"));
        SUPPORTED_CURRENCIES.put("EUR", new CurrencyInfo("EUR", "Euro", "€", 2, "de_DE"));
        SUPPORTED_CURRENCIES.put("GBP", new CurrencyInfo("GBP", "British Pound", "£", 2, "en_GB"));
        SUPPORTED_CURRENCIES.put("JPY", new CurrencyInfo("JPY", "Japanese Yen", "¥", 0, "ja_JP"));
        SUPPORTED_CURRENCIES.put("CHF", new CurrencyInfo("CHF", "Swiss Franc", "CHF", 2, "de_CH"));
        SUPPORTED_CURRENCIES.put("CAD", new CurrencyInfo("CAD", "Canadian Dollar", "C$", 2, "en_CA"));
        SUPPORTED_CURRENCIES.put("AUD", new CurrencyInfo("AUD", "Australian Dollar", "A$", 2, "en_AU"));
        SUPPORTED_CURRENCIES.put("SEK", new CurrencyInfo("SEK", "Swedish Krona", "kr", 2, "sv_SE"));
        SUPPORTED_CURRENCIES.put("NOK", new CurrencyInfo("NOK", "Norwegian Krone", "kr", 2, "nb_NO"));
        
        // Initialize exchange rates (base currency: TL)
        EXCHANGE_RATES.put("TL", new BigDecimal("1.00"));
        EXCHANGE_RATES.put("USD", new BigDecimal("30.50"));
        EXCHANGE_RATES.put("EUR", new BigDecimal("33.20"));
        EXCHANGE_RATES.put("GBP", new BigDecimal("38.75"));
        EXCHANGE_RATES.put("JPY", new BigDecimal("0.20"));
        EXCHANGE_RATES.put("CHF", new BigDecimal("34.10"));
        EXCHANGE_RATES.put("CAD", new BigDecimal("22.80"));
        EXCHANGE_RATES.put("AUD", new BigDecimal("20.15"));
        EXCHANGE_RATES.put("SEK", new BigDecimal("2.85"));
        EXCHANGE_RATES.put("NOK", new BigDecimal("2.75"));
    }

    /**
     * Currency Information Class
     */
    public static class CurrencyInfo {
        private final String code;
        private final String name;
        private final String symbol;
        private final int decimalPlaces;
        private final String locale;

        public CurrencyInfo(String code, String name, String symbol, int decimalPlaces, String locale) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
            this.decimalPlaces = decimalPlaces;
            this.locale = locale;
        }

        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public int getDecimalPlaces() { return decimalPlaces; }
        public String getLocale() { return locale; }
    }

    /**
     * Converts amount from one currency to another
     */
    public static BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        if (!EXCHANGE_RATES.containsKey(fromCurrency) || !EXCHANGE_RATES.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency: " + fromCurrency + " or " + toCurrency);
        }

        // Convert to base currency (TL) first, then to target currency
        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
        
        BigDecimal baseAmount = amount.multiply(fromRate);
        BigDecimal convertedAmount = baseAmount.divide(toRate, 4, RoundingMode.HALF_UP);
        
        return roundToCurrencyPrecision(convertedAmount, toCurrency);
    }

    /**
     * Converts amount from string with currency validation
     */
    public static BigDecimal convertCurrency(String amount, String fromCurrency, String toCurrency) {
        BigDecimal amountDecimal = new BigDecimal(amount);
        return convertCurrency(amountDecimal, fromCurrency, toCurrency);
    }

    /**
     * Rounds amount according to currency precision rules
     */
    public static BigDecimal roundToCurrencyPrecision(BigDecimal amount, String currency) {
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        if (currencyInfo == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        
        return amount.setScale(currencyInfo.getDecimalPlaces(), RoundingMode.HALF_UP);
    }

    /**
     * Formats amount according to currency and locale
     */
    public static String formatCurrency(BigDecimal amount, String currency) {
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        if (currencyInfo == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        Locale locale = Locale.forLanguageTag(currencyInfo.getLocale().replace("_", "-"));
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        
        // Set currency if supported by Java
        try {
            Currency curr = Currency.getInstance(currency.equals("TL") ? "TRY" : currency);
            formatter.setCurrency(curr);
        } catch (IllegalArgumentException e) {
            // Use custom formatting for unsupported currencies
            return currencyInfo.getSymbol() + " " + 
                   NumberFormat.getNumberInstance(locale).format(amount);
        }
        
        return formatter.format(amount);
    }

    /**
     * Formats amount as string with currency code
     */
    public static String formatCurrencyWithCode(BigDecimal amount, String currency) {
        BigDecimal roundedAmount = roundToCurrencyPrecision(amount, currency);
        return roundedAmount.toString() + " " + currency;
    }

    /**
     * Validates if currency is supported
     */
    public static boolean isCurrencySupported(String currency) {
        return SUPPORTED_CURRENCIES.containsKey(currency);
    }

    /**
     * Gets all supported currencies
     */
    public static Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES.keySet();
    }

    /**
     * Gets currency information
     */
    public static CurrencyInfo getCurrencyInfo(String currency) {
        return SUPPORTED_CURRENCIES.get(currency);
    }

    /**
     * Gets exchange rate for currency (relative to TL)
     */
    public static BigDecimal getExchangeRate(String currency) {
        return EXCHANGE_RATES.get(currency);
    }

    /**
     * Calculates exchange rate between two currencies
     */
    public static BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency pair: " + fromCurrency + "/" + toCurrency);
        }
        
        return fromRate.divide(toRate, 6, RoundingMode.HALF_UP);
    }

    /**
     * Validates amount format for specific currency
     */
    public static boolean isValidAmountFormat(String amount, String currency) {
        try {
            BigDecimal amountDecimal = new BigDecimal(amount);
            
            // Check if amount is positive
            if (amountDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            
            // Check decimal places
            CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
            if (currencyInfo != null) {
                int scale = amountDecimal.scale();
                return scale <= currencyInfo.getDecimalPlaces();
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Generates test amounts for different currencies
     */
    public static Map<String, String> generateTestAmounts() {
        Map<String, String> testAmounts = new HashMap<>();
        
        testAmounts.put("TL", "10000");      // 100.00 TL
        testAmounts.put("USD", "100");       // 100.00 USD
        testAmounts.put("EUR", "100");       // 100.00 EUR
        testAmounts.put("GBP", "100");       // 100.00 GBP
        testAmounts.put("JPY", "10000");     // 10000 JPY (no decimals)
        testAmounts.put("CHF", "100");       // 100.00 CHF
        testAmounts.put("CAD", "100");       // 100.00 CAD
        testAmounts.put("AUD", "100");       // 100.00 AUD
        testAmounts.put("SEK", "1000");      // 1000.00 SEK
        testAmounts.put("NOK", "1000");      // 1000.00 NOK
        
        return testAmounts;
    }

    /**
     * Generates boundary test amounts for currency
     */
    public static Map<String, String> generateBoundaryAmounts(String currency) {
        Map<String, String> boundaryAmounts = new HashMap<>();
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        
        if (currencyInfo != null) {
            // Minimum amount (1 unit in smallest denomination)
            if (currencyInfo.getDecimalPlaces() == 0) {
                boundaryAmounts.put("MIN", "1");
                boundaryAmounts.put("MAX", "999999999");
            } else {
                boundaryAmounts.put("MIN", "0.01");
                boundaryAmounts.put("MAX", "999999999.99");
            }
            
            // Edge cases
            boundaryAmounts.put("ZERO", "0");
            boundaryAmounts.put("NEGATIVE", "-100");
            boundaryAmounts.put("DECIMAL_PRECISION", "123.456789");
        }
        
        return boundaryAmounts;
    }

    /**
     * Simulates real-time exchange rate fluctuation
     */
    public static BigDecimal simulateExchangeRateFluctuation(String currency, double fluctuationPercent) {
        BigDecimal baseRate = EXCHANGE_RATES.get(currency);
        if (baseRate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        
        // Apply fluctuation
        double fluctuation = 1.0 + (fluctuationPercent / 100.0);
        BigDecimal fluctuatedRate = baseRate.multiply(new BigDecimal(fluctuation));
        
        return fluctuatedRate.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Validates currency conversion accuracy
     */
    public static boolean validateConversionAccuracy(BigDecimal originalAmount, String fromCurrency, 
                                                   BigDecimal convertedAmount, String toCurrency, 
                                                   double tolerancePercent) {
        BigDecimal expectedAmount = convertCurrency(originalAmount, fromCurrency, toCurrency);
        BigDecimal difference = convertedAmount.subtract(expectedAmount).abs();
        BigDecimal tolerance = expectedAmount.multiply(new BigDecimal(tolerancePercent / 100.0));
        
        return difference.compareTo(tolerance) <= 0;
    }

    /**
     * Gets currency symbol for display
     */
    public static String getCurrencySymbol(String currency) {
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        return currencyInfo != null ? currencyInfo.getSymbol() : currency;
    }

    /**
     * Gets currency name for display
     */
    public static String getCurrencyName(String currency) {
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        return currencyInfo != null ? currencyInfo.getName() : currency;
    }

    /**
     * Checks if currency supports decimals
     */
    public static boolean supportsDecimals(String currency) {
        CurrencyInfo currencyInfo = SUPPORTED_CURRENCIES.get(currency);
        return currencyInfo != null && currencyInfo.getDecimalPlaces() > 0;
    }

    /**
     * Generates multi-currency test scenario
     */
    public static List<Map<String, Object>> generateMultiCurrencyTestScenario() {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        
        for (String currency : getSupportedCurrencies()) {
            Map<String, Object> scenario = new HashMap<>();
            scenario.put("currency", currency);
            scenario.put("amount", generateTestAmounts().get(currency));
            scenario.put("symbol", getCurrencySymbol(currency));
            scenario.put("name", getCurrencyName(currency));
            scenario.put("decimals", getCurrencyInfo(currency).getDecimalPlaces());
            scenario.put("locale", getCurrencyInfo(currency).getLocale());
            scenarios.add(scenario);
        }
        
        return scenarios;
    }
}