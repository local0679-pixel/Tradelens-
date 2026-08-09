package com.example.analytics

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object FinancialFormatter {
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val noDecimalsCurrencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val numberFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
    }

    fun formatCurrency(
        amount: Double,
        showPlusSign: Boolean = true,
        showDecimals: Boolean = true
    ): String {
        val absAmount = abs(amount)
        val formattedAbs = if (showDecimals) {
            currencyFormatter.format(absAmount)
        } else {
            noDecimalsCurrencyFormatter.format(absAmount)
        }

        return when {
            amount > 0.0001 -> if (showPlusSign) "+$formattedAbs" else formattedAbs
            amount < -0.0001 -> "-$formattedAbs"
            else -> formattedAbs
        }
    }

    fun formatCompactCurrency(amount: Double, showPlusSign: Boolean = true): String {
        val absAmount = abs(amount)
        val prefix = when {
            amount > 0.0001 -> if (showPlusSign) "+" else ""
            amount < -0.0001 -> "-"
            else -> ""
        }

        val formatted = when {
            absAmount >= 1_000_000 -> "$${numberFormatter.format(absAmount / 1_000_000)}M"
            absAmount >= 1_000 -> "$${numberFormatter.format(absAmount / 1_000)}k"
            else -> currencyFormatter.format(absAmount)
        }

        return "$prefix$formatted"
    }

    fun formatPercent(percent: Double, showPlusSign: Boolean = true): String {
        val prefix = when {
            percent > 0.0001 -> if (showPlusSign) "+" else ""
            percent < -0.0001 -> ""
            else -> ""
        }
        return String.format(Locale.US, "%s%.1f%%", prefix, percent)
    }
}
