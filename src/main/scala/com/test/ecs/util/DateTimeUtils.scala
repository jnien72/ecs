package com.test.ecs.util

import java.text.SimpleDateFormat
import java.util.Date

object DateTimeUtils {
  val SECOND_IN_MILLIS: Long = 1000
  val MINUTE_IN_MILLIS: Long = SECOND_IN_MILLIS * 60
  val HOUR_IN_MILLIS: Long = MINUTE_IN_MILLIS * 60
  val DAY_IN_MILLIS: Long = HOUR_IN_MILLIS * 24

  def getDateTimeExp(millis: Long, pattern: String): String = {
    val sdf: SimpleDateFormat = new SimpleDateFormat(pattern)
    sdf.format(new Date(millis))
  }

  def getMillisFromDateTimeExp(dateTimeExpression: String, pattern: String): Long = {
    val sdf: SimpleDateFormat = new SimpleDateFormat(pattern)
    sdf.parse(dateTimeExpression).getTime
  }

  def getTimeDiffExp(start: Long, end: Long): String = {
    val elapsedMillis = if (end > 0) (end - start) else System.currentTimeMillis() - start
    if (elapsedMillis < 1000) {
      return elapsedMillis + " ms"
    }
    val elapsedSeconds = elapsedMillis / 1000;
    if (elapsedSeconds < 60) {
      return elapsedSeconds + " sec(s)";
    }
    val elapsedMinutes = elapsedSeconds / 60;
    if (elapsedMinutes < 60) {
      return elapsedMinutes + " min(s) " + (elapsedSeconds % 60) + " sec(s)";
    }
    val elapsedHours = elapsedMinutes / 60
    if (elapsedHours < 24) {
      return elapsedHours + " hr(s) " + (elapsedMinutes % 60) + " min(s)";
    }

    val elapsedDays = elapsedHours / 24
    return elapsedDays + " day(s) " + (elapsedHours % 24) + " hr(s)";

  }
}
