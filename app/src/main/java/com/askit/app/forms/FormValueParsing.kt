package com.askit.app.forms

import java.math.BigDecimal

/** Restores an enum saved by name (e.g. in a SavedStateHandle); null when missing or no longer valid. */
internal inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }

/** A form amount the user typed: trimmed, parsed, and only accepted when greater than zero. */
internal fun String.toPositiveAmount(): BigDecimal? = trim()
    .toBigDecimalOrNull()
    ?.takeIf { it > BigDecimal.ZERO }

internal fun String.isPositiveAmount(): Boolean = toPositiveAmount() != null
