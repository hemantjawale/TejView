package com.example.tejview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.tejview.databinding.ActivityMainBinding
import com.example.tejview.services.HealthAlertWorker
import com.example.tejview.services.StepCounterService
import com.example.tejview.utils.HealthDataEncryption
import com.example.tejview.utils.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startStepTracking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme BEFORE super.onCreate and setContentView
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup window colors based on theme
        val isDark = ThemeManager.isDarkMode(this)
        applyWindowColors(isDark)
        applyGlowBackgrounds(isDark)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Initialize encryption key
        try {
            HealthDataEncryption.initializeKey()
        } catch (e: Exception) {
            // Handle keystore initialization failure gracefully
        }

        // Request permissions and start services
        requestHealthPermissions()

        // Schedule health alert checks
        HealthAlertWorker.schedule(this)

        // Aurora glow breathing animation
        animateAuroraGlow()
    }

    /**
     * Called by fragments when theme is toggled.
     * Recreates the activity with a smooth crossfade.
     */
    fun toggleTheme() {
        ThemeManager.toggleTheme(this)
        // Recreate with crossfade transition
        window.setWindowAnimations(android.R.style.Animation_Toast)
        recreate()
    }

    private fun applyWindowColors(isDark: Boolean) {
        if (isDark) {
            window.statusBarColor = getColor(R.color.aurora_background)
            window.navigationBarColor = getColor(R.color.aurora_nav_background)
        } else {
            window.statusBarColor = getColor(R.color.light_background)
            window.navigationBarColor = getColor(R.color.light_nav_background)
        }
    }

    private fun applyGlowBackgrounds(isDark: Boolean) {
        if (isDark) {
            binding.glowLeft.setBackgroundResource(R.drawable.bg_aurora_glow_left)
            binding.glowRight.setBackgroundResource(R.drawable.bg_aurora_glow_right)
        } else {
            binding.glowLeft.setBackgroundResource(R.drawable.bg_aurora_glow_left_light)
            binding.glowRight.setBackgroundResource(R.drawable.bg_aurora_glow_right_light)
        }
    }

    private fun requestHealthPermissions() {
        val permissions = mutableListOf<String>()

        // Activity recognition for step counting
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // Body sensors
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BODY_SENSORS)
        }

        // Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Bluetooth (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }

        // Location for workout route mapping
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            startStepTracking()
        }
    }

    private fun startStepTracking() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun animateAuroraGlow() {
        // Subtle breathing animation for aurora glow effect
        binding.glowLeft.animate()
            .alpha(0.4f)
            .setDuration(4000)
            .withEndAction {
                binding.glowLeft.animate()
                    .alpha(0.7f)
                    .setDuration(4000)
                    .withEndAction { animateAuroraGlow() }
                    .start()
            }
            .start()

        binding.glowRight.animate()
            .alpha(0.6f)
            .setDuration(3500)
            .withEndAction {
                binding.glowRight.animate()
                    .alpha(0.3f)
                    .setDuration(3500)
                    .start()
            }
            .start()
    }
}