package com.example.clncurrency.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clncurrency.Adapter.CoinAdapter
import com.example.clncurrency.R
import com.example.clncurrency.model.Coin
import io.realm.Realm

class CoinView : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var coinAdapter: CoinAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_view)

        realm = Realm.getDefaultInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.rv_coin_list)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val coins = getAllCoins()

        coinAdapter = CoinAdapter(this, realm, coins)
        recyclerView.adapter = coinAdapter
    }

    override fun onResume() {
        super.onResume()

        // Actualizar el RecyclerView al volver a la actividad
        updateRecyclerView()
    }

    // Método para actualizar el RecyclerView
    private fun updateRecyclerView() {
        val coins = getAllCoins()
        coinAdapter.updateData(coins)
    }

    private fun getAllCoins(): List<Coin> {
        val coins = realm.where(Coin::class.java).findAll()
        val coinList = realm.copyFromRealm(coins)
        return coinList
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}

