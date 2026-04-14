package com.example.tejview.ui.sleep

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tejview.R
import com.example.tejview.databinding.FragmentSleepBinding
import java.text.SimpleDateFormat
import java.util.*

class SleepFragment : Fragment() {

    private var _binding: FragmentSleepBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SleepViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SleepViewModel::class.java]

        setupObservers()
        setupClickListeners()

        viewModel.seedSleepData()
    }

    private fun setupObservers() {
        viewModel.lastSleep.observe(viewLifecycleOwner) { sleep ->
            sleep?.let {
                val hours = it.totalMinutes / 60
                val mins = it.totalMinutes % 60
                binding.tvSleepHours.text = "${hours}h ${mins}m"
                binding.tvSleepScore.text = it.quality.toString()

                // Schedule times
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.tvBedtime.text = timeFormat.format(Date(it.bedtime))
                binding.tvWakeTime.text = timeFormat.format(Date(it.wakeTime))

                // Phase percentages
                val total = it.totalMinutes.toFloat()
                if (total > 0) {
                    binding.tvDeepPercent.text = "${(it.deepSleepMinutes / total * 100).toInt()}%"
                    binding.tvLightPercent.text = "${(it.lightSleepMinutes / total * 100).toInt()}%"
                    binding.tvRemPercent.text = "${(it.remSleepMinutes / total * 100).toInt()}%"
                    binding.tvAwakePercent.text = "${(it.awakeMinutes / total * 100).toInt()}%"
                }
            } ?: run {
                binding.tvSleepHours.text = "—"
                binding.tvSleepScore.text = "—"
            }
        }

        viewModel.weeklyAverage.observe(viewLifecycleOwner) { avg ->
            avg?.let {
                val hours = it.toInt() / 60
                val mins = it.toInt() % 60
                binding.tvWeeklyAverage.text = "Weekly avg: ${hours}h ${mins}m"
            }
        }

        viewModel.isSleepTracking.observe(viewLifecycleOwner) { tracking ->
            binding.btnStartSleepTracking.text = if (tracking)
                getString(R.string.sleep_stop_tracking) else getString(R.string.sleep_start_tracking)
        }
    }

    private fun setupClickListeners() {
        binding.btnStartSleepTracking.setOnClickListener {
            haptic()
            if (viewModel.isSleepTracking.value == true) {
                viewModel.stopSleepTracking()
                Toast.makeText(context, "😴 Sleep tracked!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.startSleepTracking()
                Toast.makeText(context, "🌙 Sleep tracking started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun haptic() {
        val vibrator = context?.getSystemService<Vibrator>() ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
