package com.example.clncurrency.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.clncurrency.R
import com.example.clncurrency.model.Coin
import com.google.android.material.textfield.TextInputEditText
import io.realm.Realm
import io.realm.RealmConfiguration

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .schemaVersion(1)
            .allowWritesOnUiThread(true)
            .build()
        Realm.setDefaultConfiguration(config)
        realm = Realm.getDefaultInstance()

        val inputCountry = findViewById<TextInputEditText>(R.id.input_country)
        val inputCountryCoin = findViewById<TextInputEditText>(R.id.input_countryCoin)
        val inputCoinSymbol = findViewById<TextInputEditText>(R.id.input_CoinSymbol)
        val btnCreate = findViewById<Button>(R.id.btn_create)
        val btn_next = findViewById<Button>(R.id.btn_second_view)

        btnCreate.setOnClickListener {
            val country = inputCountry.text.toString()
            val coinName = inputCountryCoin.text.toString()
            val coinSymbol = inputCoinSymbol.text.toString()

            if (country.isNotBlank() && coinName.isNotBlank() && coinSymbol.isNotBlank()) {
                addDataToDatabase(country, coinName, coinSymbol)

                inputCountry.text = null
                inputCountryCoin.text = null
                inputCoinSymbol.text = null
            }
        }
        btn_next.setOnClickListener { next() }
    }

    private fun addDataToDatabase(cointCountry: String, cointName: String, cointSymbol: String) {
        val modal = Coin()
        val id = realm.where(Coin::class.java).max("id")
        val nextId: Long = id?.toLong() ?: 1

        modal.id = (nextId + 1).toInt()
        modal.country = cointCountry
        modal.coinName = cointName
        modal.coinSymbol = cointSymbol

        realm.executeTransaction { realm ->
            realm.copyToRealm(modal)
            Log.d("RealmInsert", "Registro insertado: $modal")
            showToast("Moneda cargada")
        }
    }
    private fun next(){
        val intent = Intent(this, CoinView::class.java)
        startActivity(intent)

    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}