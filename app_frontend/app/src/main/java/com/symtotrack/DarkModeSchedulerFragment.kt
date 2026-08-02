package com.symtotrack

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class DarkModeSchedulerFragment : Fragment() {
    private lateinit var tvStart: TextView
    private lateinit var tvEnd: TextView
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dark_mode_scheduler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvStart = view.findViewById(R.id.tv_start_hour)
        tvEnd = view.findViewById(R.id.tv_end_hour)
        btnSave = view.findViewById(R.id.btn_save_schedule)

        val start = AppConfig.getInt(requireContext(), AppConfig.KEY_DARK_START, -1)
        val end = AppConfig.getInt(requireContext(), AppConfig.KEY_DARK_END, -1)
        if (start >= 0) tvStart.text = "Start: $start:00"
        if (end >= 0) tvEnd.text = "End: $end:00"

        tvStart.setOnClickListener { pickHour { hour ->
            AppConfig.setInt(requireContext(), AppConfig.KEY_DARK_START, hour)
            tvStart.text = "Start: $hour:00"
        } }
        tvEnd.setOnClickListener { pickHour { hour ->
            AppConfig.setInt(requireContext(), AppConfig.KEY_DARK_END, hour)
            tvEnd.text = "End: $hour:00"
        } }
        btnSave.setOnClickListener { /* persisted automatically */ }
    }

    private fun pickHour(onHourPicked: (Int) -> Unit) {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val dialog = TimePickerDialog(requireContext(), { _, hourOfDay, _ ->
            onHourPicked(hourOfDay)
        }, currentHour, 0, true)
        dialog.setTitle("Select Hour")
        dialog.show()
    }
}
