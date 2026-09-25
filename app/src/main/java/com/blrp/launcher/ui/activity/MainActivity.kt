package com.blrp.launcher.ui.activity

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.blrp.game.R
import com.blrp.game.core.Samp
import com.blrp.launcher.async.dto.response.FileInfo
import com.blrp.launcher.config.Config.DONATE_URL
import com.blrp.launcher.config.Config.FORUM_URL
import com.blrp.launcher.config.Config.TELEGRAM_URI
import com.blrp.launcher.domain.enums.DownloadType
import com.blrp.launcher.storage.NativeStorage
import com.blrp.launcher.utils.MainUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var userNameTV: TextView
    private lateinit var playButtonLayout: ConstraintLayout
    private lateinit var logBtn: ConstraintLayout
    private lateinit var donateBtn: ConstraintLayout

    private lateinit var tgButton: ImageView
    private lateinit var vkButton: ImageView
    private lateinit var dsButton: ImageView

    private lateinit var mainRoot: ConstraintLayout
    private lateinit var contentRoot: ConstraintLayout
    private lateinit var loadingOverlayRoot: ConstraintLayout
    private lateinit var loadingText: TextView

    companion object {
        private const val TEST_MODE_ON_VALUE = "1"
        private val SETTINGS_CHARSET = Charset.forName("windows-1251")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppBaseTheme)
        setContentView(R.layout.launcher_main)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        bindViews()
        setLoadingState(false)
        fillUserInfo()
        setupClicks()
    }

    private fun bindViews() {
        mainRoot = findViewById(R.id.mainRoot)
        contentRoot = findViewById(R.id.contentRoot)

        userNameTV = findViewById(R.id.name)
        playButtonLayout = findViewById(R.id.play_btn)

        tgButton = findViewById(R.id.tg_btn)
        vkButton = findViewById(R.id.vk_btn)
        dsButton = findViewById(R.id.web_btn)

        logBtn = findViewById(R.id.log_btn)
        donateBtn = findViewById(R.id.donate_btn)

        loadingOverlayRoot = findViewById(R.id.loadingOverlayRoot)
        loadingText = findViewById(R.id.loadingText)
    }

    private fun fillUserInfo() {
        val nickname = getNicknameFromSettings()
        if (!nickname.isNullOrBlank()) {
            userNameTV.text = nickname
            NativeStorage.addClientProperty("name", nickname, this)
        } else {
            val nativeNick = NativeStorage.getClientProperty("name", this)
            if (!nativeNick.isNullOrBlank()) userNameTV.text = nativeNick
        }
    }

    private fun setupClicks() {
        playButtonLayout.setOnClickListener {
            playClickAnimation(it) { onClickPlay() }
        }

        findViewById<TextView>(R.id.name)?.setOnClickListener {
            enableNicknameEditing()
        }

        tgButton.setOnClickListener { playClickAnimation(it) { openLink(TELEGRAM_URI) } }
        vkButton.setOnClickListener { playClickAnimation(it) { openLink(FORUM_URL) } }
        dsButton.setOnClickListener { playClickAnimation(it) { openLink(DONATE_URL) } }

        donateBtn.setOnClickListener { playClickAnimation(it) { openLink(DONATE_URL) } }
        logBtn.setOnClickListener { playClickAnimation(it) { sendLogFile() } }
    }
    private fun enableNicknameEditing() {
        userNameTV.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        userNameTV.isFocusable = true
        userNameTV.isFocusableInTouchMode = true
        userNameTV.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        userNameTV.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            val isEnter = event != null
                    && event.action == KeyEvent.ACTION_DOWN
                    && event.keyCode == KeyEvent.KEYCODE_ENTER
            if (isDone || isEnter) {
                saveNickname()
                true
            } else false
        }

        userNameTV.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(userNameTV, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun saveNickname() {
        val newNick = userNameTV.text.toString().trim()
        if (newNick.isEmpty()) {
            Toast.makeText(this, "Ник не может быть пустым", Toast.LENGTH_SHORT).show()
            fillUserInfo()
        } else {
            if (saveNicknameToSettings(newNick)) {
                NativeStorage.addClientProperty("name", newNick, this)
                Toast.makeText(this, "Никнейм сохранён", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось сохранить ник", Toast.LENGTH_SHORT).show()
                fillUserInfo()
            }
        }

        userNameTV.clearFocus()
        userNameTV.inputType = InputType.TYPE_NULL
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(userNameTV.windowToken, 0)
    }

    private fun getSettingsFile(): File? {
        val extDir = this.getExternalFilesDir(null)
        return if (extDir != null) File(extDir, "SAMP/settings.ini") else null
    }

    private fun getNicknameFromSettings(): String {
        try {
            val settingsFile = getSettingsFile()
            if (settingsFile == null || !settingsFile.exists()) {
                return ""
            }

            var inClientSection = false
            BufferedReader(InputStreamReader(FileInputStream(settingsFile), SETTINGS_CHARSET)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()

                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        inClientSection = "[client]".equals(trimmed, ignoreCase = true)
                        continue
                    }

                    if (inClientSection && trimmed.startsWith("name", ignoreCase = true)) {
                        val idx = trimmed.indexOf('=')
                        if (idx != -1 && idx + 1 < trimmed.length) {
                            return trimmed.substring(idx + 1).trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun saveNicknameToSettings(newNick: String): Boolean {
        try {
            val settingsFile = getSettingsFile()
            if (settingsFile == null || !settingsFile.exists()) {
                return true
            }

            val lines = ArrayList<String>()
            BufferedReader(InputStreamReader(FileInputStream(settingsFile), SETTINGS_CHARSET)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    lines.add(line!!)
                }
            }

            var inClientSection = false
            var nameUpdated = false
            for (i in lines.indices) {
                val original = lines[i]
                val trimmed = original.trim()

                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    inClientSection = "[client]".equals(trimmed, ignoreCase = true)
                    continue
                }

                if (inClientSection && trimmed.startsWith("name", ignoreCase = true)) {
                    lines[i] = "name = $newNick"
                    nameUpdated = true
                    break
                }
            }

            if (!nameUpdated) {
                var clientSectionFound = false
                for (i in lines.indices) {
                    val trimmed = lines[i].trim()
                    if ("[client]".equals(trimmed, ignoreCase = true)) {
                        clientSectionFound = true
                        lines.add(i + 1, "name = $newNick")
                        break
                    }
                }
                if (!clientSectionFound) {
                    return false
                }
            }

            writeLines(settingsFile, lines)
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun writeLines(file: File, lines: List<String>) {
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, false), SETTINGS_CHARSET)).use { writer ->
            for (i in lines.indices) {
                writer.write(lines[i])
                if (i < lines.size - 1) {
                    writer.newLine()
                }
            }
            writer.flush()
        }
    }

    private fun playClickAnimation(view: View, action: () -> Unit) {
        val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
        anim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(a: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(a: android.view.animation.Animation?) {}
            override fun onAnimationEnd(a: android.view.animation.Animation?) { action() }
        })
        view.startAnimation(anim)
    }

    private fun sendLogFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            val logFile = File(this@MainActivity.getExternalFilesDir(null), "logcat.txt")
            if (logFile.exists()) {
                val uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "$packageName.provider",
                    logFile
                )
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Отправить логи"))
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Файл logcat.txt не найден", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onNicknameChanged(newNick: String) {
        userNameTV.text = newNick
    }

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun onClickPlay() {
        if (!validateBeforeStart()) return

        if (!isCheckSkipping && !isCachePresent()) {
            MainUtils.type = DownloadType.RELOAD_GAME_FILES
            startActivity(Intent(this, LoaderActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return
        }

        loadingText.text = "Запуск игры..."
        setLoadingState(true)

        mainRoot.postDelayed({ startGameInternal() }, 300)
    }

    private fun validateBeforeStart(): Boolean {
        NativeStorage.addClientProperty("password", StringUtils.EMPTY, this)
        return true
    }

    private fun isCachePresent(): Boolean {
        val baseDir = this.getExternalFilesDir(null) ?: return false
        val gta3Img = File(baseDir, "texdb/gta3.img")
        return gta3Img.exists()
    }

    private fun startGameInternal() {
        File(this.getExternalFilesDir(null), "log.txt").delete()

        val intent = Intent(this, Samp::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            loadingOverlayRoot.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                contentRoot.setRenderEffect(
                    RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)
                )
            } else {
                contentRoot.alpha = 0.4f
            }
        } else {
            loadingOverlayRoot.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                contentRoot.setRenderEffect(null)
            } else {
                contentRoot.alpha = 1f
            }
        }
    }

    private val isCheckSkipping: Boolean
        get() = NativeStorage.getClientProperty("test", this) == TEST_MODE_ON_VALUE

    private fun doAfterCacheChecked(fileToReload: MutableList<FileInfo>) {
        if (fileToReload.isEmpty()) startGameInternal()
        else {
            MainUtils.FILES_TO_RELOAD = fileToReload
            MainUtils.type = DownloadType.RELOAD_GAME_FILES
            startActivity(Intent(this, LoaderActivity::class.java))
        }
    }
}
