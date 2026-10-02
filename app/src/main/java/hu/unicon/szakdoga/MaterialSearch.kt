package hu.unicon.szakdoga

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.unicon.szakdoga.api.ApiClient
import hu.unicon.szakdoga.model.Material
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialSearch : AppCompatActivity() {
    private var searchEditText: EditText? = null
    private var resultContainer: LinearLayout? = null
    private val materialsList = mutableListOf<Material>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_material_search)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val clearButton = findViewById<ImageView>(R.id.clearButton)
        searchEditText = findViewById(R.id.searchEditText)
        resultContainer = findViewById(R.id.resultContainer)

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable) {
                fetchMaterialsFromApi(s.toString().trim())
            }
        })

        clearButton.setOnClickListener {
            searchEditText?.setText("")
        }

        fetchMaterialsFromApi("")
    }

    private fun fetchMaterialsFromApi(query: String) {
        val apiService = ApiClient.getService(this)
        val searchQuery = if (query.isBlank()) null else query

        apiService.getMaterials(searchQuery).enqueue(object : Callback<List<Material>> {
            override fun onResponse(call: Call<List<Material>>, response: Response<List<Material>>) {
                if (response.isSuccessful) {
                    materialsList.clear()
                    response.body()?.let { materialsList.addAll(it) }
                    displayMaterials()
                } else {
                    Toast.makeText(this@MaterialSearch, "Hiba az adatok lekérésekor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                Log.e("MaterialSearch", "API hívási hiba", t)
                Toast.makeText(this@MaterialSearch, "Hálózati hiba: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun displayMaterials() {
        resultContainer?.removeAllViews()

        if (materialsList.isEmpty()) {
            val emptyTextView = TextView(this).apply {
                text = "Nincs találat"
                setTextColor(resources.getColor(R.color.white, null))
                textSize = 20f
                setPadding(30, 30, 30, 30)
            }
            resultContainer?.addView(emptyTextView)
            return
        }

        for (material in materialsList) {
            val displayText = buildString {
                append("Név: ${material.name}\n")
                if (!material.skuCode.isNullOrEmpty()) {
                    append("Cikkszám (SKU): ${material.skuCode}\n")
                }
                if (!material.categoryName.isNullOrEmpty()) {
                    append("Kategória: ${material.categoryName}\n")
                }
                if (!material.details.isNullOrEmpty()) {
                    append("Részletek: ${material.details}")
                }
            }

            val textView = TextView(this).apply {
                setTextColor(resources.getColor(R.color.white, null))
                text = displayText
                setPadding(30, 30, 30, 30)
                textSize = 20f
                setBackgroundResource(R.drawable.rounded_edittext)
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                elevation = 1f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 30)
                }
            }
            resultContainer?.addView(textView)
        }
    }
}
