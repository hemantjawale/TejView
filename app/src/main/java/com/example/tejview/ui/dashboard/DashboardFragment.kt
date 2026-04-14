package com.example.tejview.ui.dashboard

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
import com.example.tejview.MainActivity
import com.example.tejview.R
import com.example.tejview.databinding.FragmentDashboardBinding
import com.example.tejview.utils.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupGreeting()
        setupThemeToggle()
        setupObservers()
        setupClickListeners()
        setupAnimations()

        // Seed demo data on first launch
        viewModel.seedDemoData()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> getString(R.string.dashboard_greeting_morning)
            in 12..16 -> getString(R.string.dashboard_greeting_afternoon)
            in 17..20 -> getString(R.string.dashboard_greeting_evening)
            else -> getString(R.string.dashboard_greeting_night)
        }
        binding.tvGreeting.text = greeting

        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())
    }

    private fun setupThemeToggle() {
        // Set initial icon based on current theme
        val isDark = ThemeManager.isDarkMode(requireContext())
        binding.tvThemeIcon.text = if (isDark) "☀️" else "🌙"

        binding.btnThemeToggle.setOnClickListener {
            provideHapticFeedback()

            // Animate the toggle button
            it.animate()
                .rotationBy(360f)
                .setDuration(400)
                .withEndAction {
                    // Toggle theme via MainActivity
                    (activity as? MainActivity)?.toggleTheme()
                }
                .start()
        }

        binding.btnSettings.setOnClickListener {
            provideHapticFeedback()
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        android.app.AlertDialog.Builder(requireContext(), R.style.Theme_TejView)
            .setTitle("App Settings")
            .setMessage("Manage your app settings and data here.")
            .setPositiveButton("Reset All Data") { _, _ ->
                confirmResetData()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun confirmResetData() {
        android.app.AlertDialog.Builder(requireContext(), R.style.Theme_TejView)
            .setTitle("Confirm Reset")
            .setMessage("Are you sure you want to delete all stored health readings and start from zero? This cannot be undone.")
            .setPositiveButton("Yes, Reset") { _, _ ->
                Thread {
                    com.example.tejview.data.database.HealthDatabase.getDatabase(requireContext()).clearAllTables()
                    activity?.runOnUiThread {
                        Toast.makeText(context, "All data reset successfully.", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.recreate()
                    }
                }.start()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        // Serenity Score
        viewModel.serenityScore.observe(viewLifecycleOwner) { score ->
            binding.tvSerenityScore.text = score.toString()
            binding.serenityRing.setProgress(score.toFloat())
        }

        viewModel.serenityStatus.observe(viewLifecycleOwner) { status ->
            binding.tvSerenityStatus.text = status
        }

        // Heart Rate
        viewModel.heartRate.observe(viewLifecycleOwner) { metric ->
            metric?.let {
                binding.tvHeartRate.text = it.value.toInt().toString()
                startHeartPulse()
            } ?: run {
                binding.tvHeartRate.text = "—"
            }
        }

        // SpO2
        viewModel.spo2.observe(viewLifecycleOwner) { metric ->
            binding.tvSpO2.text = metric?.value?.toInt()?.toString() ?: "—"
        }

        // Steps
        viewModel.dailySteps.observe(viewLifecycleOwner) { total ->
            val steps = total?.toInt() ?: 0
            binding.tvSteps.text = String.format("%,d", steps)
            binding.progressSteps.progress = steps
            binding.tvStepsGoal.text = "/ 10,000 goal"
        }

        // Calories
        viewModel.dailyCalories.observe(viewLifecycleOwner) { total ->
            val cal = total?.toInt() ?: 0
            binding.tvCalories.text = String.format("%,d", cal)
            binding.progressCalories.progress = cal
            binding.tvCaloriesGoal.text = "/ 2,500 goal"
        }

        // Active Minutes
        viewModel.dailyActiveMinutes.observe(viewLifecycleOwner) { minutes ->
            binding.tvActiveMinutes.text = (minutes ?: 0).toString()
        }

        // Water
        viewModel.dailyWater.observe(viewLifecycleOwner) { total ->
            binding.tvWaterCount.text = (total?.toInt() ?: 0).toString()
        }

        // Protein
        viewModel.dailyProtein.observe(viewLifecycleOwner) { total ->
            val protein = total?.toInt() ?: 0
            binding.tvProteinValue.text = "${protein}g"
            binding.progressProtein.progress = protein
            binding.tvProteinGoal.text = "/ 120g goal"
        }

        // Sleep
        viewModel.lastSleep.observe(viewLifecycleOwner) { sleep ->
            sleep?.let {
                val hours = it.totalMinutes / 60
                val mins = it.totalMinutes % 60
                binding.tvSleepDuration.text = "${hours}h ${mins}m"
                binding.tvSleepQuality.text = when {
                    it.quality >= 80 -> "Excellent Quality"
                    it.quality >= 60 -> "Good Quality"
                    it.quality >= 40 -> "Fair Quality"
                    else -> "Poor Quality"
                }
            } ?: run {
                binding.tvSleepDuration.text = "—"
                binding.tvSleepQuality.text = "No data"
            }
        }
    }

    private fun setupClickListeners() {
        // Quick Water Add
        binding.btnQuickWater.setOnClickListener {
            viewModel.addWater()
            provideHapticFeedback()
            Toast.makeText(context, "💧 Water logged!", Toast.LENGTH_SHORT).show()
        }

        binding.cardWater.setOnClickListener {
            viewModel.addWater()
            provideHapticFeedback()
            Toast.makeText(context, "💧 +1 glass", Toast.LENGTH_SHORT).show()
        }

        // Protein Add
        binding.btnAddProtein.setOnClickListener {
            viewModel.addProtein(20f)
            provideHapticFeedback()
            Toast.makeText(context, "🥩 +20g protein", Toast.LENGTH_SHORT).show()
        }

        // Quick Exercise
        binding.btnQuickExercise.setOnClickListener {
            provideHapticFeedback()
        }

        // Quick Mood
        binding.btnQuickMood.setOnClickListener {
            provideHapticFeedback()
        }

        // Heart rate card - haptic heartbeat
        binding.cardHeartRate.setOnClickListener {
            provideHeartbeatHaptic()
        }
    }

    private fun setupAnimations() {
        val cards = listOf(
            binding.cardSerenity,
            binding.cardHeartRate,
            binding.cardSpO2,
            binding.cardSteps,
            binding.cardCalories,
            binding.cardSleepSummary,
            binding.cardProtein
        )

        cards.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 80).toLong())
                .setDuration(400)
                .start()
        }
    }

    private fun startHeartPulse() {
        val pulseView = binding.heartPulseDot
        val pulse = android.animation.ObjectAnimator.ofFloat(pulseView, "alpha", 1f, 0.3f, 1f)
        pulse.duration = 1000
        pulse.repeatCount = android.animation.ObjectAnimator.INFINITE
        pulse.start()
    }

    private fun provideHapticFeedback() {
        val vibrator = context?.getSystemService<Vibrator>() ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun provideHeartbeatHaptic() {
        val vibrator = context?.getSystemService<Vibrator>() ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 100, 50)
            val amplitudes = intArrayOf(0, 150, 0, 200)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
