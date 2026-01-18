package com.example.myapplication.ui.selectapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AppPreferences
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entities.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 * Activity to manage which applications are tracked for notifications.
 */
class SelectAppActivity : AppCompatActivity() {

    private lateinit var adapter: AppSelectionAdapter
    private var fullAppList: ArrayList<AppInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_app)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerApps)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val searchView = findViewById<SearchView>(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val pm = packageManager
            val savedPackages = AppPreferences.getSelectedPackages(this@SelectAppActivity)
            fullAppList.clear()

            // Scan all installed applications
            val allPackages = pm.getInstalledPackages(0)

            for (packageInfo in allPackages) {
                val launchIntent = pm.getLaunchIntentForPackage(packageInfo.packageName)
                val appInfo = packageInfo.applicationInfo

                // Only add apps that have a launch intent and are not this application itself
                if (launchIntent != null && appInfo != null && packageInfo.packageName != this@SelectAppActivity.packageName) {
                    val name = appInfo.loadLabel(pm).toString()
                    val icon = appInfo.loadIcon(pm)
                    val isSelected = savedPackages.contains(packageInfo.packageName)
                    fullAppList.add(AppInfo(name, packageInfo.packageName, icon, isSelected))
                }
            }
            // Sort list: Selected apps first, then alphabetically by name
            fullAppList.sortWith(compareBy({ !it.isSelected }, { it.name }))

            withContext(Dispatchers.Main) {
                adapter = AppSelectionAdapter(fullAppList) { }
                recyclerView.adapter = adapter

                // Set up search functionality
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        filterList(newText)
                        return true
                    }
                })

                // --- SAVE LOGIC ---
                btnSave.setOnClickListener {
                    val previouslySelected =
                        AppPreferences.getSelectedPackages(this@SelectAppActivity)
                    val currentSelectedApps = fullAppList.filter { it.isSelected }
                    val currentSelectedPkgs = currentSelectedApps.map { it.packageName }.toSet()

                    // Identify apps that were unselected by the user
                    val unselectedPkgs = previouslySelected - currentSelectedPkgs

                    AppPreferences.saveSelectedPackages(this@SelectAppActivity, currentSelectedPkgs)

                    val db = AppDatabase.Companion.getDatabase(this@SelectAppActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        // 1. Delete messages of apps that have been unselected
                        for (pkg in unselectedPkgs) {
                            db.messageDao().deleteMessagesByPackage(pkg)
                        }

                        // 2. Create a welcome message for NEWLY selected apps
                        val newApps = currentSelectedPkgs - previouslySelected
                        for (app in currentSelectedApps) {
                            // Only create for newly added apps to prevent spam
                            if (newApps.contains(app.packageName)) {
                                val avatarBytes = drawableToBytes(app.icon)
                                val welcomeMsg = Message(
                                    sender = app.name,
                                    content = "Added to tracking list ✅",
                                    time = System.currentTimeMillis(),
                                    avatar = avatarBytes,
                                    packageName = app.packageName // <-- IMPORTANT: Store the correct packageName
                                )
                                db.messageDao().insert(welcomeMsg)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SelectAppActivity,
                                "Updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    /**
     * Filters the app list based on search query.
     */
    private fun filterList(query: String?) {
        if (query != null) {
            val searchText = query.lowercase(Locale.ROOT)
            val filtered = fullAppList.filter {
                it.name.lowercase(Locale.ROOT).contains(searchText)
            }
            adapter.updateList(filtered)
        }
    }

    /**
     * Converts a Drawable to a ByteArray for storage.
     */
    private fun drawableToBytes(drawable: Drawable): ByteArray? {
        val bitmap = if (drawable is BitmapDrawable) drawable.bitmap else {
            val b = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            drawable.setBounds(0, 0, c.width, c.height)
            drawable.draw(c)
            b
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
