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

            // Quét tất cả ứng dụng
            val allPackages = pm.getInstalledPackages(0)

            for (packageInfo in allPackages) {
                val launchIntent = pm.getLaunchIntentForPackage(packageInfo.packageName)
                val appInfo = packageInfo.applicationInfo

                if (launchIntent != null && appInfo != null && packageInfo.packageName != this@SelectAppActivity.packageName) {
                    val name = appInfo.loadLabel(pm).toString()
                    val icon = appInfo.loadIcon(pm)
                    val isSelected = savedPackages.contains(packageInfo.packageName)
                    fullAppList.add(AppInfo(name, packageInfo.packageName, icon, isSelected))
                }
            }
            fullAppList.sortWith(compareBy({ !it.isSelected }, { it.name }))

            withContext(Dispatchers.Main) {
                adapter = AppSelectionAdapter(fullAppList) { }
                recyclerView.adapter = adapter

                // Tìm kiếm
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        filterList(newText)
                        return true
                    }
                })

                // --- XỬ LÝ LƯU (Logic Mới) ---
                btnSave.setOnClickListener {
                    val previouslySelected =
                        AppPreferences.getSelectedPackages(this@SelectAppActivity)
                    val currentSelectedApps = fullAppList.filter { it.isSelected }
                    val currentSelectedPkgs = currentSelectedApps.map { it.packageName }.toSet()

                    val unselectedPkgs = previouslySelected - currentSelectedPkgs

                    AppPreferences.saveSelectedPackages(this@SelectAppActivity, currentSelectedPkgs)

                    val db = AppDatabase.Companion.getDatabase(this@SelectAppActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        // 1. Xóa sạch tin nhắn của App bị bỏ chọn (Dùng hàm mới deleteMessagesByPackage)
                        for (pkg in unselectedPkgs) {
                            db.messageDao().deleteMessagesByPackage(pkg)
                        }

                        // 2. Tạo tin chào mừng cho App MỚI được chọn
                        val newApps = currentSelectedPkgs - previouslySelected
                        for (app in currentSelectedApps) {
                            // Chỉ tạo nếu đây là app mới chọn thêm (để tránh spam)
                            if (newApps.contains(app.packageName)) {
                                val avatarBytes = drawableToBytes(app.icon)
                                val welcomeMsg = Message(
                                    sender = app.name,
                                    content = "Đã thêm vào danh sách theo dõi ✅",
                                    time = System.currentTimeMillis(),
                                    avatar = avatarBytes,
                                    packageName = app.packageName // <-- QUAN TRỌNG: Lưu đúng packageName
                                )
                                db.messageDao().insert(welcomeMsg)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SelectAppActivity,
                                "Đã cập nhật!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun filterList(query: String?) {
        if (query != null) {
            val searchText = query.lowercase(Locale.ROOT)
            val filtered = fullAppList.filter {
                it.name.lowercase(Locale.ROOT).contains(searchText)
            }
            adapter.updateList(filtered)
        }
    }

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