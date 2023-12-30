package com.wecareapp.android.utilities

import android.net.ParseException
import android.text.TextUtils
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun displayTimeSlots(fromTime: String, toTime: String): MutableList<String> {
        val timeValue = "T00:00:4.896+05:30"
        val sdf = SimpleDateFormat("'T'hh:mm:ss.SSS")
        val timeList = mutableListOf<String>()
        if (TextUtils.isEmpty(fromTime) || TextUtils.isEmpty(toTime))
            return timeList
        try {
            val startCalendar = Calendar.getInstance()
            startCalendar.time = sdf.parse(timeValue)
            startCalendar.set(Calendar.HOUR, fromTime.split(":")[0].toInt() - 1)
            startCalendar.set(Calendar.MINUTE, fromTime.split(":")[1].toInt())
            startCalendar.set(Calendar.SECOND, 0)
            if (startCalendar[Calendar.MINUTE] < 30) {
                startCalendar[Calendar.MINUTE] = 30
            } else {
                startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                startCalendar.clear(Calendar.MINUTE)
            }
            val endCalendar = Calendar.getInstance()
            endCalendar.time = startCalendar.time

            // if you want dates for whole next day, uncomment next line
            //endCalendar.add(Calendar.DAY_OF_YEAR, 1);
            endCalendar.add(
                Calendar.HOUR_OF_DAY,
                toTime.split(":")[0].toInt() - startCalendar[Calendar.HOUR_OF_DAY]
            )
            endCalendar.set(Calendar.MINUTE, toTime.split(":")[1].toInt())
            endCalendar.clear(Calendar.SECOND)
            endCalendar.clear(Calendar.MILLISECOND)
            val slotTime = SimpleDateFormat("hh:mm a")
            while (endCalendar.after(startCalendar)) {
                startCalendar.add(Calendar.MINUTE, 30)
                val timeSlots: String = slotTime.format(startCalendar.time)
                timeList.add(timeSlots)
                Log.e("DATE", timeSlots)
            }
        } catch (e: ParseException) {
            // date in wrong format
        }
        return timeList
    }

    fun displayTimeSlot(fromTime: String?): String {
        val timeValue = "T00:00:4.896+05:30"
        val sdf = SimpleDateFormat("'T'hh:mm:ss.SSS")
        if (fromTime == null || TextUtils.isEmpty(fromTime))
            return ""
        try {
            val startCalendar = Calendar.getInstance()
            startCalendar.time = sdf.parse(timeValue)

            val d = fromTime.split(":")
            startCalendar.set(Calendar.HOUR, d[0].toInt() - 1)
            startCalendar.set(Calendar.MINUTE, d[1].toInt())
            startCalendar.set(Calendar.SECOND, 0)

            /*if (startCalendar[Calendar.MINUTE] < 30) {
                startCalendar[Calendar.MINUTE] = 30
            } else {
                startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                startCalendar.clear(Calendar.MINUTE)
            }*/
//            val endCalendar = Calendar.getInstance()
//            endCalendar.time = startCalendar.time

            // if you want dates for whole next day, uncomment next line
            //endCalendar.add(Calendar.DAY_OF_YEAR, 1);
//            endCalendar.add(
//                Calendar.HOUR_OF_DAY,
//                toTime.split(":")[0].toInt() - startCalendar[Calendar.HOUR_OF_DAY]
//            )
//            endCalendar.set(Calendar.MINUTE, toTime.split(":")[1].toInt())
//            endCalendar.clear(Calendar.SECOND)
//            endCalendar.clear(Calendar.MILLISECOND)
            val slotTime = SimpleDateFormat("hh:mm a")
//            while (endCalendar.after(startCalendar)) {
//                startCalendar.add(Calendar.MINUTE, 30)
            val timeSlots: String = slotTime.format(startCalendar.time)
//                timeList.add(timeSlots)
            Log.e("DATE", timeSlots)
            return timeSlots.replace(" ", "")
//            }
        } catch (e: ParseException) {
            // date in wrong format
            return ""
        } catch (e: Exception) {
            // date in wrong format
            return ""
        }
        return ""
    }

    fun displayTimeSlot2(fromTime: String?): String {
        val timeValue = "T00:00:4.896+05:30"
        val sdf = SimpleDateFormat("'T'hh:mm:ss.SSS")
        if (fromTime == null || TextUtils.isEmpty(fromTime))
            return ""
        try {
            val startCalendar = Calendar.getInstance()
            startCalendar.time = sdf.parse(timeValue)

            val d = fromTime.split(":")
            startCalendar.set(Calendar.HOUR, d[0].toInt())
            startCalendar.set(Calendar.MINUTE, d[1].toInt())
            startCalendar.set(Calendar.SECOND, 0)

            /*if (startCalendar[Calendar.MINUTE] < 30) {
                startCalendar[Calendar.MINUTE] = 30
            } else {
                startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                startCalendar.clear(Calendar.MINUTE)
            }*/
//            val endCalendar = Calendar.getInstance()
//            endCalendar.time = startCalendar.time

            // if you want dates for whole next day, uncomment next line
            //endCalendar.add(Calendar.DAY_OF_YEAR, 1);
//            endCalendar.add(
//                Calendar.HOUR_OF_DAY,
//                toTime.split(":")[0].toInt() - startCalendar[Calendar.HOUR_OF_DAY]
//            )
//            endCalendar.set(Calendar.MINUTE, toTime.split(":")[1].toInt())
//            endCalendar.clear(Calendar.SECOND)
//            endCalendar.clear(Calendar.MILLISECOND)
            val slotTime = SimpleDateFormat("hh:mm a")
//            while (endCalendar.after(startCalendar)) {
//                startCalendar.add(Calendar.MINUTE, 30)
            val timeSlots: String = slotTime.format(startCalendar.time)
//                timeList.add(timeSlots)
            Log.e("DATE", timeSlots)
            return timeSlots.replace(" ", "")
//            }
        } catch (e: ParseException) {
            // date in wrong format
            return ""
        } catch (e: Exception) {
            // date in wrong format
            return ""
        }
        return ""
    }
}