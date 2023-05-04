package com.denizk0461.studip.dialog

import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Parcelable
import android.widget.TimePicker
import com.denizk0461.studip.data.getParcelableCompat

/**
 * Dialogue used for letting the user pick a timestamp.
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var listener: OnTimeSetListener
    private var isEventStart: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Retrieve listener for when the user finishes setting a time
        listener = arguments.getParcelableCompat("interface")

        // Retrieve whether the timestamp to be edited is the start of the event
        isEventStart = arguments?.getBoolean("isEventStart") == true

        // Retrieve timestamp to edit
        val timestampSplit = (arguments?.getString("timestamp") ?: "").split(":")

        return TimePickerDialog(
            activity,
            this,
            timestampSplit[0].toInt(),
            timestampSplit[1].toInt(),
            true,
        )
    }

    /**
     * Override the implementation of TimePickerDialog#OnTimeSetListener.
     */
    override fun onTimeSet(picker: TimePicker?, hours: Int, minutes: Int) {
        // Use custom implementation of OnTimeSetListener
        listener.onTimeSet(hours, minutes, isEventStart)
    }

    /**
     * Custom implementation of TimePickerDialog#OnTimeSetListener that includes a boolean to find
     * out whether the start or the end timestamp has been edited.
     */
    interface OnTimeSetListener : Parcelable {

        /**
         * Timestamp has been edited by the user.
         *
         * @param hours         hour of the timestamp
         * @param minutes       minute of the timestamp
         * @param isEventStart  whether the timestamp to be edited is the start of the event
         */
        fun onTimeSet(hours: Int, minutes: Int, isEventStart: Boolean)
    }
}