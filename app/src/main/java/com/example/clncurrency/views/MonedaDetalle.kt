package com.example.clncurrency.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.clncurrency.Adapter.CoinAdapter
import com.example.clncurrency.R
import com.example.clncurrency.model.Coin
import com.example.clncurrency.model.ExchangeRate
import io.realm.Realm

class MonedaDetalle : AppCompatActivity() {

    private lateinit var etCountry: EditText
    private lateinit var etCoinName: EditText
    private lateinit var etCoinSymbol: EditText
    private lateinit var btnEditar: TextView
    private lateinit var btnGuardar: TextView
    private lateinit var btnAgregarTasaCambio: TextView
    private lateinit var tvTasaCambio: TextView
    private var isEditing = false
    private lateinit var realm: Realm
    private var coinId: String? = null
    private var coinAdapter: CoinAdapter? = null

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        realm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_moneda_detalle)

        etCountry = findViewById(R.id.et_country_d)
        etCoinName = findViewById(R.id.et_name_country_d)
        etCoinSymbol = findViewById(R.id.et_symbol_country_d)
        btnEditar = findViewById(R.id.tvCoinName_edit)
        btnGuardar = findViewById(R.id.tvCoinName_save)
        btnAgregarTasaCambio = findViewById(R.id.tvCoin_exchange_currency)
        tvTasaCambio = findViewById(R.id.tvExchangeRate)

        val pais = intent.getStringExtra("pais")
        val nombreMoneda = intent.getStringExtra("nombreMoneda")
        val simbolo = intent.getStringExtra("simbolo")
        coinId = intent.getStringExtra("coinId")


        etCountry.setText(pais)
        etCoinName.setText(nombreMoneda)
        etCoinSymbol.setText(simbolo)

        btnEditar.setOnClickListener {
            isEditing = !isEditing
            enableEditing(isEditing)
        }

        btnGuardar.setOnClickListener {
            if (isEditing) {
                saveChangesToDatabase()
                showToast("Cambios guardados exitosamente")
                enableEditing(false)
            }
        }

        btnAgregarTasaCambio.setOnClickListener {
            showAddExchangeRateDialog()
        }
    }

    private fun enableEditing(enable: Boolean) {
        etCountry.isEnabled = enable
        etCoinName.isEnabled = enable
        etCoinSymbol.isEnabled = enable

        if (enable) {
            btnGuardar.visibility = View.VISIBLE
            btnEditar.visibility = View.GONE
        } else {
            btnGuardar.visibility = View.GONE
            btnEditar.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun saveChangesToDatabase() {
        val newCountry = etCountry.text.toString()
        val newCoinName = etCoinName.text.toString()
        val newCoinSymbol = etCoinSymbol.text.toString()

        realm.executeTransaction { bgRealm ->
            val coin = bgRealm.where(Coin::class.java).equalTo("id", coinId).findFirst()
            coin?.apply {
                country = newCountry
                coinName = newCoinName
                coinSymbol = newCoinSymbol
            }
        }

        updateRecyclerView()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAddExchangeRateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Tasa de Cambio")

        val view = layoutInflater.inflate(R.layout.dialog_add_exchange_rate, null)
        builder.setView(view)

        val spinnerCurrency = view.findViewById<Spinner>(R.id.spinnerCurrency)
        val etRate = view.findViewById<EditText>(R.id.etRate)

        val allCoins = getAllCoinsFromDatabase().filter { it.id != coinId?.toInt() }

        val spinnerAdapter = ArrayAdapter<Coin>(this, android.R.layout.simple_spinner_item, allCoins)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = spinnerAdapter

        builder.setPositiveButton("Agregar") { dialog, _ ->
            val selectedCoin = spinnerCurrency.selectedItem as? Coin
            val exchangeRate = etRate.text.toString().toDoubleOrNull()

            if (selectedCoin != null && exchangeRate != null) {
                saveExchangeRateToDatabase(selectedCoin, exchangeRate)
                showToast("Tasa de cambio agregada exitosamente")
            } else {
                val errorMessage = when {
                    spinnerCurrency.selectedItem == null -> "Seleccione una moneda."
                    exchangeRate == null -> "Ingrese una tasa de cambio válida."
                    else -> "Ocurrió un error inesperado. Verifique su selección."
                }
                showToast(errorMessage)
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveExchangeRateToDatabase(toCoin: Coin, rate: Double) {
        val fromCoin = realm.where(Coin::class.java).equalTo("id", coinId).findFirst()

        if (fromCoin != null && toCoin != null) {

            realm.executeTransaction { bgRealm ->
                val exchangeRate = bgRealm.createObject(ExchangeRate::class.java)
                exchangeRate.fromCoin = fromCoin
                exchangeRate.toCoin = toCoin
                exchangeRate.rate = rate
            }
        }
    }

    private fun updateRecyclerView() {
        coinAdapter?.notifyDataSetChanged()
    }

    private fun getAllCoinsFromDatabase(): List<Coin> {
        return realm.where(Coin::class.java).findAll()
    }


}

