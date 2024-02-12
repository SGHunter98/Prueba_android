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

        val recyclerView = findViewById<RecyclerView>(R.id.rv_coin_list)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val coins = getAllCoins()
        val realm = Realm.getDefaultInstance()

        val coinAdapter = CoinAdapter(this, realm, coins)
        recyclerView.adapter = coinAdapter
    }

    private fun getAllCoins(): List<Coin> {
        val realm = Realm.getDefaultInstance()
        val coins = realm.where(Coin::class.java).findAll()
        val coinList = realm.copyFromRealm(coins)
        realm.close()

        return coinList
    }
}