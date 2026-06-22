package com.example.travelcompanion.ui.trip

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun formatDate(millis: Long): String = isoDateFormat.format(Date(millis))
