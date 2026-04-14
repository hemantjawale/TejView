package com.example.tejview.ui.activity

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tejview.databinding.FragmentActivityBinding

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ActivityViewModel
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ActivityViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.seedExerciseData()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter()
        binding.rvExerciseHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = exerciseAdapter
        }
    }

    private fun setupObservers() {
        viewModel.recentExercises.observe(viewLifecycleOwner) { exercises ->
            exerciseAdapter.submitList(exercises)
        }

        viewModel.isWorkoutActive.observe(viewLifecycleOwner) { active ->
            binding.btnStartWorkout.visibility = if (active) View.GONE else View.VISIBLE
            binding.btnStopWorkout.visibility = if (active) View.VISIBLE else View.GONE
        }

        viewModel.workoutType.observe(viewLifecycleOwner) { type ->
            binding.tvSelectedWorkout.text = type.uppercase()
        }

        viewModel.workoutTimer.observe(viewLifecycleOwner) { time ->
            binding.tvWorkoutTimer.text = time
        }

        viewModel.dailyCalories.observe(viewLifecycleOwner) { cal ->
            binding.tvWorkoutCalories.text = (cal ?: 0).toString()
        }

        viewModel.dailySteps.observe(viewLifecycleOwner) { steps ->
            val dist = (steps ?: 0f) * 0.000762f
            binding.tvWorkoutDistance.text = String.format("%.1f", dist)
        }
    }

    private fun setupClickListeners() {
        val workoutButtons = mapOf(
            binding.btnWorkoutRunning to "running",
            binding.btnWorkoutCycling to "cycling",
            binding.btnWorkoutYoga to "yoga",
            binding.btnWorkoutGym to "gym",
            binding.btnWorkoutSwimming to "swimming",
            binding.btnWorkoutWalking to "walking"
        )

        workoutButtons.forEach { (button, type) ->
            button.setOnClickListener {
                haptic()
                viewModel.startWorkout(type)
                Toast.makeText(context, "🏃 $type started!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartWorkout.setOnClickListener {
            haptic()
            viewModel.startWorkout("running")
            Toast.makeText(context, "🏃 Workout started!", Toast.LENGTH_SHORT).show()
        }

        binding.btnStopWorkout.setOnClickListener {
            haptic()
            viewModel.stopWorkout()
            Toast.makeText(context, "✅ Workout saved!", Toast.LENGTH_SHORT).show()
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
