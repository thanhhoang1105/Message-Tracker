package com.example.myapplication.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.detail.DetailActivity
import com.example.myapplication.ui.main.HeaderAdapter
import com.example.myapplication.ui.main.MessageAdapter
import com.example.myapplication.R
import com.example.myapplication.ui.selectapp.SelectAppActivity
import com.example.myapplication.data.AppPreferences
import com.example.myapplication.data.local.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private var loadDataJob: Job? = null // Coroutine job to manage data loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        ignoreBatteryOptimization()

        // Button to Add App for tracking
        findViewById<LinearLayout>(R.id.btnUiAddApp).setOnClickListener {
            startActivity(Intent(this, SelectAppActivity::class.java))
        }

        // Initialize Message List RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MessageAdapter(onClick = { senderName ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SENDER_NAME", senderName)
            startActivity(intent)
        })
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Reload Header when returning to this screen
        setupHeaderList()
    }

    private fun setupHeaderList() {
        val recyclerHeader = findViewById<RecyclerView>(R.id.recyclerHeaderApps)
        recyclerHeader.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val selectedPackages = AppPreferences.getSelectedPackages(this).toList()

        // DEFAULT LOGIC: Select the first app in the list if it exists
        val defaultApp = selectedPackages.firstOrNull()

        // Load messages for the first app by default
        loadMessages(defaultApp ?: "ALL")

        val headerAdapter = HeaderAdapter(
            packageNames = selectedPackages,
            packageManager = packageManager,
            initialSelection = defaultApp
        ) { selectedPkg ->
            loadMessages(selectedPkg)
        }
        recyclerHeader.adapter = headerAdapter
    }

    /**
     * Loads messages filtered by package name.
     * @param filterPackage The package name of the app to filter messages from.
     */
    private fun loadMessages(filterPackage: String) {
        // Cancel the previous job to avoid overlapping data loads
        loadDataJob?.cancel()
        val db = AppDatabase.Companion.getDatabase(this)

        loadDataJob = lifecycleScope.launch {
            if (filterPackage == "ALL") {
                // Fetch all sender messages if no specific app is selected
                db.messageDao().getListSenders().collect { list ->
                    adapter.submitList(list)
                }
            } else {
                // Fetch messages for a specific app package
                db.messageDao().getListSendersByApp(filterPackage).collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }

    private fun checkPermission() {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun ignoreBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
