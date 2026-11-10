package hu.unicon.szakdoga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import hu.unicon.szakdoga.api.UserSession
import hu.unicon.szakdoga.model.User
import hu.unicon.szakdoga.repository.RepositoryProvider

class LoginActivity : AppCompatActivity() {

    private var usernameEditText: EditText? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
        if (UserSession.isLoggedIn(this)) {
            navigateToMain()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usernameEditText = findViewById(R.id.usernameEditText)
        progressBar = findViewById(R.id.loginProgressBar)

        val btnQuickWorker = findViewById<MaterialButton>(R.id.btnQuickWorker)
        val btnQuickMuszi = findViewById<MaterialButton>(R.id.btnQuickMuszi)
        val btnQuickAdmin = findViewById<MaterialButton>(R.id.btnQuickAdmin)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnQuickWorker.setOnClickListener { performLogin("dolgozo") }
        btnQuickMuszi.setOnClickListener { performLogin("muszakvezeto") }
        btnQuickAdmin.setOnClickListener { performLogin("admin") }

        btnLogin.setOnClickListener {
            val username = usernameEditText?.text.toString().trim()
            if (username.isNotEmpty()) {
                performLogin(username)
            } else {
                Toast.makeText(this, "Add meg a felhasználónevet!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String) {
        progressBar?.visibility = View.VISIBLE
        val repository = RepositoryProvider.getRepository(this)

        repository.login(username) { result ->
            progressBar?.visibility = View.GONE
            result.onSuccess { user ->
                saveAndContinue(user)
            }.onFailure {
                val fallbackUser = createFallbackUser(username)
                saveAndContinue(fallbackUser)
            }
        }
    }

    private fun createFallbackUser(username: String): User {
        return when (username.lowercase()) {
            "admin" -> User(1L, "admin", "Rendszergazda", "ADMIN")
            "muszakvezeto", "muszi", "mueszi" -> User(2L, "muszakvezeto", "Kovács Péter Műszakvezető", "MUSZAKVEZETO")
            else -> User(3L, "dolgozo", "Szabó János Dolgozó", "DOLGOZO")
        }
    }

    private fun saveAndContinue(user: User) {
        UserSession.saveUser(this, user)
        Toast.makeText(this, "Üdvözlünk, ${user.name}! (${user.getRoleDisplayName()})", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
