package com.example.clncurrency.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ExchangeRate(
    @PrimaryKey
    var id: Int = 0,
    var fromCoin: Coin? = null,
    var toCoin: Coin? = null,
    var rate: Double = 0.0
) : RealmObject()
