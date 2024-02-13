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
import android.widget.TableLayout
import android.widget.TableRow
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
    private var coinId: Int = 0
    private lateinit var spinnerAdapter: ArrayAdapter<Coin>
    private lateinit var tableLayout: TableLayout
    private val exchangeRatesList = mutableListOf<ExchangeRate>()
    private var lastGeneratedId: Int = 0
    private lateinit var allCoins: List<Coin>

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moneda_detalle)

        Realm.init(this)
        realm = Realm.getDefaultInstance()

        etCountry = findViewById(R.id.et_country_d)
        etCoinName = findViewById(R.id.et_name_country_d)
        etCoinSymbol = findViewById(R.id.et_symbol_country_d)
        btnEditar = findViewById(R.id.tvCoinName_edit)
        btnGuardar = findViewById(R.id.tvCoinName_save)
        btnAgregarTasaCambio = findViewById(R.id.tvCoin_exchange_currency)
        tvTasaCambio = findViewById(R.id.tvExchangeRate)
        tableLayout = findViewById(R.id.table_layout)
        allCoins = getAllCoinsFromDatabase().filter { it.id != coinId }


        val pais = intent.getStringExtra("pais")
        val nombreMoneda = intent.getStringExtra("nombreMoneda")
        val simbolo = intent.getStringExtra("simbolo")
        coinId = intent.getIntExtra("coinId", 0)
        Log.d("CoinIdSelected", "Datos actualizados: $coinId")

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

    // Funciones para editar y guardar las modificaciones
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

    private fun saveChangesToDatabase() {
        val newCountry = etCountry.text.toString()
        val newCoinName = etCoinName.text.toString()
        val newCoinSymbol = etCoinSymbol.text.toString()

        try {
            realm.executeTransaction { bgRealm ->
                val coin = bgRealm.where(Coin::class.java).equalTo("id", coinId).findFirst()
                if (coin != null) {
                    coin.apply {
                        country = newCountry
                        coinName = newCoinName
                        coinSymbol = newCoinSymbol
                    }
                } else {
                    Log.e("Realm", "Objeto Coin es nulo para el ID: $coinId")
                }
            }

            // Log para verificar que se realizó la transacción
            Log.d("Realm", "Cambios guardados en la base de datos" )
        } catch (e: Exception) {
            // Log para mostrar cualquier excepción
            Log.e("Realm", "Error al guardar cambios en la base de datos: ${e.message}")
        }
    }

    // Función de llamada a un spinner
    private fun showAddExchangeRateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Tasa de Cambio")

        val view = layoutInflater.inflate(R.layout.dialog_add_exchange_rate, null)
        builder.setView(view)

        val spinnerCurrency = view.findViewById<Spinner>(R.id.spinnerCurrency)
        val etRate = view.findViewById<EditText>(R.id.etRate)


        spinnerAdapter = ArrayAdapter<Coin>(this, android.R.layout.simple_spinner_item, getAllCoinsFromDatabase())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = spinnerAdapter

        builder.setPositiveButton("Agregar") { dialog, _ ->
            val selectedCoin = spinnerCurrency.selectedItem as? Coin
            val exchangeRateValue = etRate.text.toString().toDoubleOrNull()

            if (selectedCoin != null && exchangeRateValue != null) {
                // Guardar la tasa de cambio en la base de datos
                saveExchangeRateToDatabase(selectedCoin, exchangeRateValue)

                // Agregar la nueva tasa de cambio a la lista
                val newExchangeRate = ExchangeRate(
                    id = generateExchangeRateId(),
                    fromCoin = realm.where(Coin::class.java).equalTo("id", coinId).findFirst(),
                    toCoin = selectedCoin,
                    rate = exchangeRateValue
                )
                exchangeRatesList.add(newExchangeRate)

                // Actualizar el TableLayout
                updateTableLayout()
            } else {
                // Manejar errores y mostrar mensajes según sea necesario
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
            val nextId = generateExchangeRateId()

            // Verificar si el ID ya existe antes de crear el objeto
            val existingExchangeRate = realm.where(ExchangeRate::class.java)
                .equalTo("id", nextId)
                .findFirst()

            if (existingExchangeRate == null) {
                realm.executeTransaction { bgRealm ->
                    val exchangeRate = bgRealm.createObject(ExchangeRate::class.java, nextId)
                    exchangeRate.fromCoin = fromCoin
                    exchangeRate.toCoin = toCoin
                    exchangeRate.rate = rate
                }
            } else {
                // Manejar el caso de ID duplicado
                showToast("Ya existe una tasa de cambio con este ID")
            }
        }

        // Utiliza la lista filtrada allCoins que obtuviste inicialmente
        spinnerAdapter.clear()
        spinnerAdapter.addAll(allCoins.filter { it.id != coinId?.toInt() })
        spinnerAdapter.notifyDataSetChanged()
    }



    private fun getAllCoinsFromDatabase(): List<Coin> {
        return realm.where(Coin::class.java).findAll().filter { it.id != coinId }
    }

    // Table Layout
    private fun updateTableLayout() {
        // Limpia las filas existentes en el TableLayout
        tableLayout.removeAllViews()

        // Itera sobre las tasas de cambio y agrega filas al TableLayout
        for (exchangeRate in exchangeRatesList) {
            val newRow = TableRow(this)

            // Configura las celdas de la fila según tus necesidades
            val fromCoinCell = TextView(this)
            fromCoinCell.text = exchangeRate.fromCoin?.coinName
            newRow.addView(fromCoinCell)

            val toCoinCell = TextView(this)
            toCoinCell.text = exchangeRate.toCoin?.coinName
            newRow.addView(toCoinCell)

            val rateCell = TextView(this)
            rateCell.text = exchangeRate.rate.toString()
            newRow.addView(rateCell)

            // Agrega la fila al TableLayout
            tableLayout.addView(newRow)
        }
    }

    private fun generateExchangeRateId(): Int {
        lastGeneratedId++
        return lastGeneratedId
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}


