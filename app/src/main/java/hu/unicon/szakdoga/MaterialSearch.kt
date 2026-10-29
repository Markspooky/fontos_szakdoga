package hu.unicon.szakdoga

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.unicon.szakdoga.adapter.MaterialAdapter
import hu.unicon.szakdoga.api.ApiClient
import hu.unicon.szakdoga.model.Material
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialSearch : AppCompatActivity() {
    private var searchEditText: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var loader: ProgressBar? = null
    private var emptyStateTextView: TextView? = null

    private val materialAdapter = MaterialAdapter()
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_material_search)
        
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.materialsRecyclerView)
        loader = findViewById(R.id.loader)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = materialAdapter

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    fetchMaterialsFromApi(s.toString().trim())
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        })

        fetchMaterialsFromApi("")
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }

    private fun fetchMaterialsFromApi(query: String) {
        loader?.visibility = View.VISIBLE
        emptyStateTextView?.visibility = View.GONE

        val apiService = ApiClient.getService(this)
        val searchQuery = if (query.isBlank()) null else query

        apiService.getMaterials(searchQuery).enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                loader?.visibility = View.GONE
                if (response.isSuccessful) {
                    val materials = response.body() ?: emptyList()
                    materialAdapter.updateItems(materials)

                    if (materials.isEmpty()) {
                        emptyStateTextView?.visibility = View.VISIBLE
                        recyclerView?.visibility = View.GONE
                    } else {
                        emptyStateTextView?.visibility = View.GONE
                        recyclerView?.visibility = View.VISIBLE
                    }
                } else {
                    showErrorSnackbar("Hiba az adatok lekérésekor") {
                        fetchMaterialsFromApi(query)
                    }
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                loader?.visibility = View.GONE
                Log.e("MaterialSearch", "API hívási hiba", t)
                showErrorSnackbar("Hálózati hiba történt") {
                    fetchMaterialsFromApi(query)
                }
            }
        })
    }

    private fun showErrorSnackbar(message: String, onRetry: () -> Unit) {
        val mainView = findViewById<View>(R.id.main) ?: return
        Snackbar.make(mainView, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("ÚJRA") { onRetry() }
            .show()
    }
}
