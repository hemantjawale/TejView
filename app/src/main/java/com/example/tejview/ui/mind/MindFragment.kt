package com.example.tejview.ui.mind

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
import com.example.tejview.databinding.FragmentMindBinding

class MindFragment : Fragment() {

    private var _binding: FragmentMindBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MindViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MindViewModel::class.java]

        setupObservers()
        setupClickListeners()

        viewModel.seedScreenTimeData()
    }

    private fun setupObservers() {
        viewModel.selectedMood.observe(viewLifecycleOwner) { mood ->
            mood?.let {
                binding.tvMoodLabel.text = viewModel.getMoodLabel(it)
                updateStressFromMood(it)
            }
        }

        viewModel.todayMood.observe(viewLifecycleOwner) { entry ->
            entry?.let {
                binding.tvMoodLabel.text = viewModel.getMoodLabel(it.mood)
                binding.tvStressLabel.text = viewModel.getStressLabel(it.stressLevel)
                binding.tvStressValue.text = it.stressLevel.toString()
                binding.progressStress.progress = it.stressLevel
            }
        }

        viewModel.todayScreenTime.observe(viewLifecycleOwner) { time ->
            time?.let {
                binding.tvScreenTimeSocial.text = formatMinutes(it.socialMinutes)
                binding.tvScreenTimeProductive.text = formatMinutes(it.productiveMinutes)
                binding.tvScreenTimeOther.text = formatMinutes(it.otherMinutes)
            }
        }

        viewModel.meditationMinutes.observe(viewLifecycleOwner) { mins ->
            binding.tvMeditationTimer.text = if (mins > 0) "${mins}:00" else "10:00"
        }

        viewModel.meditationStreak.observe(viewLifecycleOwner) { streak ->
            binding.tvMeditationStreak.text = "🔥 $streak day streak"
        }
    }

    private fun setupClickListeners() {
        // Mood buttons — mapped by index: 1=sad, 2=low, 3=neutral, 4=good, 5=great
        binding.btnMood1.setOnClickListener {
            haptic()
            viewModel.setMood(1)
            highlightMoodButton(1)
        }
        binding.btnMood2.setOnClickListener {
            haptic()
            viewModel.setMood(2)
            highlightMoodButton(2)
        }
        binding.btnMood3.setOnClickListener {
            haptic()
            viewModel.setMood(3)
            highlightMoodButton(3)
        }
        binding.btnMood4.setOnClickListener {
            haptic()
            viewModel.setMood(4)
            highlightMoodButton(4)
        }
        binding.btnMood5.setOnClickListener {
            haptic()
            viewModel.setMood(5)
            highlightMoodButton(5)
        }

        // Meditation
        binding.btnStartMeditation.setOnClickListener {
            haptic()
            viewModel.addMeditationMinutes(15)
            Toast.makeText(context, "🧘 +15 min meditation", Toast.LENGTH_SHORT).show()
        }

        // Journal
        binding.btnSaveJournal.setOnClickListener {
            haptic()
            val text = binding.etJournal.text.toString()
            if (text.isNotBlank()) {
                viewModel.saveJournalEntry(text)
                binding.etJournal.text.clear()
                Toast.makeText(context, "📝 Journal saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun highlightMoodButton(level: Int) {
        val buttons = listOf(
            binding.btnMood1,
            binding.btnMood2,
            binding.btnMood3,
            binding.btnMood4,
            binding.btnMood5
        )

        buttons.forEachIndexed { index, btn ->
            btn.alpha = if (index + 1 == level) 1f else 0.4f
            if (index + 1 == level) {
                btn.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start()
            } else {
                btn.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }
    }

    private fun updateStressFromMood(mood: Int) {
        val stress = when (mood) {
            5 -> 18
            4 -> 28
            3 -> 45
            2 -> 65
            1 -> 82
            else -> 50
        }
        binding.tvStressLabel.text = viewModel.getStressLabel(stress)
        binding.tvStressValue.text = stress.toString()
        binding.progressStress.progress = stress
    }

    private fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
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
