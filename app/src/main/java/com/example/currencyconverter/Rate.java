package com.example.currencyconverter;

import android.icu.util.Currency;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Rate {
    private Currency currency;
    private double exchangeRate;

    public Currency getCurrency() {
        return currency;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public Rate(String ISO, double exchangeRate) {
        this.currency = Currency.getInstance(ISO);
        this.exchangeRate = exchangeRate;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%s - %s", currency.getDisplayName(), currency.getSymbol());
    }

    // write methods when needed
}

