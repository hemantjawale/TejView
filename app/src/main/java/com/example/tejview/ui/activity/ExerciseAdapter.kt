package com.example.tejview.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tejview.data.model.Exercise
import com.example.tejview.databinding.ItemExerciseHistoryBinding

class ExerciseAdapter : ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExerciseViewHolder(private val binding: ItemExerciseHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            binding.tvExerciseEmoji.text = getEmoji(exercise.type)
            binding.tvExerciseName.text = formatName(exercise.type)

            val details = buildString {
                append("${exercise.durationMinutes} min")
                exercise.distanceKm?.let { append(" · ${String.format("%.1f", it)} km") }
                append(" · ${exercise.caloriesBurned} cal")
            }
            binding.tvExerciseDetails.text = details

            val elapsed = System.currentTimeMillis() - exercise.timestamp
            binding.tvExerciseTime.text = formatTimeAgo(elapsed)
        }

        private fun getEmoji(type: String): String {
            return when (type) {
                "running" -> "🏃"
                "cycling" -> "🚴"
                "yoga" -> "🧘"
                "gym" -> "🏋️"
                "swimming" -> "🏊"
                "walking" -> "🚶"
                else -> "💪"
            }
        }

        private fun formatName(type: String): String {
            return type.replaceFirstChar { it.uppercase() }
        }

        private fun formatTimeAgo(millis: Long): String {
            val minutes = millis / 60000
            return when {
                minutes < 60 -> "${minutes}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                else -> "${minutes / 1440}d ago"
            }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise) = oldItem == newItem
    }
}
