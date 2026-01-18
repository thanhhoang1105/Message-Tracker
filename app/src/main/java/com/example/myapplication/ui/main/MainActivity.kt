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
    private var loadDataJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        ignoreBatteryOptimization()

        findViewById<LinearLayout>(R.id.btnUiAddApp).setOnClickListener {
            startActivity(Intent(this, SelectAppActivity::class.java))
        }

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
        // Chỉ cần gọi setupHeaderList, hàm này sẽ tự động load tin nhắn cho App đầu tiên
        setupHeaderList()
    }

    private fun setupHeaderList() {
        val recyclerHeader = findViewById<RecyclerView>(R.id.recyclerHeaderApps)
        recyclerHeader.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val selectedPackages = AppPreferences.getSelectedPackages(this).toList()

        // Chọn App đầu tiên làm mặc định nếu có
        val defaultApp = selectedPackages.firstOrNull()

        // Load tin nhắn: Nếu có app được chọn thì lọc theo app đó, nếu không có app nào thì hiện "ALL" (hoặc trống)
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

    private fun loadMessages(filterPackage: String) {
        loadDataJob?.cancel()
        val db = AppDatabase.Companion.getDatabase(this)

        loadDataJob = lifecycleScope.launch {
            if (filterPackage == "ALL") {
                db.messageDao().getListSenders().collect { list ->
                    adapter.submitList(list)
                }
            } else {
                // Chỉ lấy tin nhắn của đúng Package Name được truyền vào
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