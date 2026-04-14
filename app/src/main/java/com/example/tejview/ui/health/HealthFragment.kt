package com.example.tejview.ui.health

import android.app.AlertDialog
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tejview.R
import com.example.tejview.databinding.FragmentHealthBinding

class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HealthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HealthViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.heartRate.observe(viewLifecycleOwner) { metric ->
            binding.tvHR.text = metric?.value?.toInt()?.toString() ?: "72"
        }

        viewModel.dailyWater.observe(viewLifecycleOwner) { total ->
            val count = total?.toInt() ?: 0
            updateWaterDots(count)
        }

        viewModel.dailyProtein.observe(viewLifecycleOwner) { total ->
            val protein = total?.toInt() ?: 0
            binding.tvProtein.text = "${protein}g / 120g"
            binding.progressProteinHealth.progress = protein
        }
    }

    private fun updateWaterDots(count: Int) {
        val dots = listOf(
            binding.dot1, binding.dot2, binding.dot3, binding.dot4,
            binding.dot5, binding.dot6, binding.dot7, binding.dot8
        )
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < count) R.drawable.bg_water_circle
                else R.drawable.bg_water_dot_empty
            )
        }
    }

    private fun setupClickListeners() {
        binding.btnAddWater.setOnClickListener {
            haptic()
            viewModel.addWater()
            Toast.makeText(context, "💧 +1 glass", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddMedication.setOnClickListener {
            haptic()
            showMedicationDialog()
        }

        binding.btnReportIssue.setOnClickListener {
            haptic()
            showIssueDialog()
        }
    }

    private fun showMedicationDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val nameInput = EditText(context).apply {
            hint = "Medication name"
            setPadding(16, 16, 16, 16)
        }

        val dosageInput = EditText(context).apply {
            hint = "Dosage (e.g., 500mg)"
            setPadding(16, 16, 16, 16)
        }

        layout.addView(nameInput)
        layout.addView(dosageInput)

        AlertDialog.Builder(requireContext(), R.style.Theme_TejView)
            .setTitle("Log Medication")
            .setView(layout)
            .setPositiveButton("Log") { _, _ ->
                val name = nameInput.text.toString()
                val dosage = dosageInput.text.toString()
                if (name.isNotBlank()) {
                    viewModel.logMedication(name, dosage)
                    Toast.makeText(context, "💊 Medication logged", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showIssueDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val titleInput = EditText(context).apply {
            hint = "Issue title"
            setPadding(16, 16, 16, 16)
        }

        val descInput = EditText(context).apply {
            hint = "Describe the issue"
            inputType = android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            setPadding(16, 16, 16, 16)
        }

        layout.addView(titleInput)
        layout.addView(descInput)

        AlertDialog.Builder(requireContext(), R.style.Theme_TejView)
            .setTitle("Report Health Issue")
            .setView(layout)
            .setPositiveButton("Report") { _, _ ->
                val title = titleInput.text.toString()
                val desc = descInput.text.toString()
                if (title.isNotBlank()) {
                    viewModel.reportHealthIssue(title, desc, "medium")
                    Toast.makeText(context, "⚠️ Issue reported", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
