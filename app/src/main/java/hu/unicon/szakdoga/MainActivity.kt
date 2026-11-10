package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.unicon.szakdoga.analytics.AnalyticsManager
import hu.unicon.szakdoga.api.ApiClient
import hu.unicon.szakdoga.api.UserSession
import hu.unicon.szakdoga.repository.DataSourceMode
import hu.unicon.szakdoga.repository.DataSourcePreferences
import hu.unicon.szakdoga.repository.RepositoryProvider

class MainActivity : AppCompatActivity() {
    private var configButton: Button? = null
    private var menuButton: ImageView? = null
    private var userBadgeTextView: TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect if not logged in
        if (!UserSession.isLoggedIn(this)) {
            redirectToLogin()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configButton = findViewById(R.id.load_config_button)
        menuButton = findViewById(R.id.menubutton)
        userBadgeTextView = findViewById(R.id.userBadgeTextView)

        val currentUser = UserSession.getUser(this)
        if (currentUser != null) {
            userBadgeTextView?.text = "Bejelentkezve: ${currentUser.name} (${currentUser.getRoleDisplayName()})"
        }

        // Role-based visibility: Config button is ONLY visible for Admin
        if (UserSession.isAdmin(this)) {
            configButton?.visibility = View.VISIBLE
            menuButton?.visibility = View.VISIBLE
            updateConfigButtonText()
        } else {
            configButton?.visibility = View.GONE
            menuButton?.visibility = View.GONE
        }

        val title = findViewById<View>(R.id.title)
        val belepes = findViewById<View>(R.id.belepes)
        val pdfek = findViewById<View>(R.id.pdfek)

        val anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up)
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up).apply { startOffset = 150 }
        val anim3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up).apply { startOffset = 300 }

        title.startAnimation(anim1)
        belepes.startAnimation(anim2)
        pdfek.startAnimation(anim3)
    }

    private fun updateConfigButtonText() {
        val currentMode = DataSourcePreferences.getMode(this)
        if (currentMode == DataSourceMode.ORACLE_REST) {
            val currentUrl = ApiClient.getBaseUrl(this)
            configButton?.text = "Adatforrás: Oracle REST ($currentUrl)"
        } else {
            configButton?.text = "Adatforrás: Helyi SQLite Adatbázis"
        }
    }

    fun logoutUser(view: View?) {
        UserSession.logout(this)
        Toast.makeText(this, "Kijelentkezve", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun menuPoint(view: View?) {
        if (UserSession.isAdmin(this)) {
            showServerUrlDialog()
        }
    }

    fun selectConfigFromJsonFile(view: View?) {
        if (UserSession.isAdmin(this)) {
            showServerUrlDialog()
        }
    }

    private fun showServerUrlDialog() {
        val currentMode = DataSourcePreferences.getMode(this)
        val currentUrl = ApiClient.getBaseUrl(this)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val radioGroup = RadioGroup(this)
        val rbOracle = RadioButton(this).apply {
            text = "Oracle DB (REST API)"
            id = View.generateViewId()
        }
        val rbSqlite = RadioButton(this).apply {
            text = "Helyi SQLite Adatbázis"
            id = View.generateViewId()
        }

        radioGroup.addView(rbOracle)
        radioGroup.addView(rbSqlite)

        if (currentMode == DataSourceMode.ORACLE_REST) {
            rbOracle.isChecked = true
        } else {
            rbSqlite.isChecked = true
        }

        val inputUrl = EditText(this).apply {
            setText(currentUrl)
            setSelection(text.length)
            hint = "Oracle REST API URL"
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == rbOracle.id) {
                inputUrl.visibility = View.VISIBLE
            } else {
                inputUrl.visibility = View.GONE
            }
        }

        inputUrl.visibility = if (currentMode == DataSourceMode.ORACLE_REST) View.VISIBLE else View.GONE

        container.addView(radioGroup)
        container.addView(inputUrl)

        val analyticsBtn = Button(this).apply {
            text = "📊 Telemetria & Mérések (Szakdolgozathoz)"
            setOnClickListener {
                showAnalyticsDialog()
            }
        }
        container.addView(analyticsBtn)

        AlertDialog.Builder(this)
            .setTitle("Adatforrás Beállítása")
            .setMessage("Válaszd ki az alkalmazás adatforrását:")
            .setView(container)
            .setPositiveButton("Mentés") { _, _ ->
                val selectedMode = if (rbOracle.isChecked) DataSourceMode.ORACLE_REST else DataSourceMode.LOCAL_SQLITE
                DataSourcePreferences.setMode(this, selectedMode)

                if (selectedMode == DataSourceMode.ORACLE_REST) {
                    val newUrl = inputUrl.text.toString().trim()
                    if (newUrl.isNotEmpty()) {
                        ApiClient.setBaseUrl(this, newUrl)
                    }
                }

                RepositoryProvider.reset()
                updateConfigButtonText()
                Toast.makeText(this, "Adatforrás átállítva: ${selectedMode.displayName}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Mégse", null)
            .show()
    }

    private fun showAnalyticsDialog() {
        val summary = AnalyticsManager.getSummary(this)

        val message = """
            📊 Összes Művelet: ${summary.totalOperations}
            ✅ Sikeres: ${summary.successCount}
            ❌ Hibás: ${summary.failureCount}
            📈 Sikerességi arány: ${String.format("%.1f", summary.successRatePercent)}%

            ⏱️ ÁTLAGOS VÁLASZIDŐK:
            • Alkatrész keresés (Oracle REST): ${String.format("%.1f", summary.avgMaterialOracleRestMs)} ms
            • Alkatrész keresés (SQLite): ${String.format("%.1f", summary.avgMaterialLocalSqliteMs)} ms

            • Dokumentum keresés (Oracle REST): ${String.format("%.1f", summary.avgDocumentOracleRestMs)} ms
            • Dokumentum keresés (SQLite): ${String.format("%.1f", summary.avgDocumentLocalSqliteMs)} ms
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Telemetria & Mérések (Szakdolgozathoz)")
            .setMessage(message)
            .setPositiveButton("CSV Exportálás") { _, _ ->
                val csvData = AnalyticsManager.generateCsvReport(this)
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Telemetry CSV", csvData)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "CSV adatok a vágólapra másolva! (Excelbe/Wordbe beilleszthető)", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Törlés") { _, _ ->
                AnalyticsManager.clearAll(this)
                Toast.makeText(this, "Mérési adatok törölve", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Bezárás", null)
            .show()
    }

    fun loggingIn(view: View?) {
        val intent = Intent(this, MaterialSearch::class.java)
        startActivity(intent)
    }

    fun pdfOpen(view: View?) {
        val intent = Intent(this, PDFOpen::class.java)
        startActivity(intent)
    }
}
