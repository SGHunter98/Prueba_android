package com.example.clncurrency.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.clncurrency.R
import com.example.clncurrency.model.Coin
import com.example.clncurrency.views.MonedaDetalle
import io.realm.Realm

interface OnCoinDeleteListener {
    fun onCoinDeleted()
}

class CoinAdapter (val activity: Activity, private val realm: Realm, private val coins: List<Coin>,private val onCoinDeleteListener: OnCoinDeleteListener? = null) : RecyclerView.Adapter<CoinAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinAdapter.ViewHolder, position: Int) {
        val coin = coins[position]
        holder.bind(coin)
    }

    override fun getItemCount(): Int {
        return coins.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cvCoin: CardView = itemView.findViewById(R.id.cv_coin)
        private val etCountry: TextView = itemView.findViewById(R.id.et_country)
        private val etCoinName: TextView = itemView.findViewById(R.id.et_name_country)
        private val etCoinSymbol: TextView = itemView.findViewById(R.id.et_symbol_country)
        private val delete_coin: TextView = itemView.findViewById(R.id.tv_delete_coin)

        fun bind(coin: Coin) {
            etCountry.text = coin.country
            etCoinName.text = coin.coinName
            etCoinSymbol.text = coin.coinSymbol

            cvCoin.setOnClickListener {
                val intent = Intent(activity, MonedaDetalle::class.java)

                // Pasar los datos como extras en el Intent
                intent.putExtra("pais", coin.country)
                intent.putExtra("nombreMoneda", coin.coinName)
                intent.putExtra("simbolo", coin.coinSymbol)

                // Iniciar la otra actividad
                activity.startActivity(intent)
                val toastMessage = "País: ${coin.country}, Nombre: ${coin.coinName}, Símbolo: ${coin.coinSymbol}"
                showToast(toastMessage)
            }
            delete_coin.setOnClickListener {
                showDeleteConfirmationDialog(coin)
            }
        }
    }
    private fun showDeleteConfirmationDialog(coin: Coin) {
        AlertDialog.Builder(activity)
            .setTitle("Eliminar Moneda")
            .setMessage("¿Estás seguro de que deseas eliminar esta moneda?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCoin(coin)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCoin(coin: Coin) {
        realm.executeTransaction { bgRealm ->
            val coinToDelete = bgRealm.where(Coin::class.java).equalTo("id", coin.id).findFirst()
            coinToDelete?.deleteFromRealm()
            val position = coins.indexOf(coin)
            if (position != -1) {
                notifyItemRemoved(position)
            }
            onCoinDeleteListener?.onCoinDeleted()
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

}
