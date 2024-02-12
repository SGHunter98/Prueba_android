package com.example.clncurrency.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Coin(@PrimaryKey
                var id: Int = 0,
                var country: String = "",
                var coinName: String = "",
                var coinSymbol: String = "",
                var tasaCambio: Double = 0.0): RealmObject()


