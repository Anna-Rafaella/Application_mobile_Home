package com.kana.smarthome

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.acos

class HomeFragmentAdapter(
    private val context: Context,
    private val dataSource: List<HouseData>,
    private val listener: OnHouseSelectedListener // Ajoute le listener
) : BaseAdapter() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // Variable pour stocker l'ID de la maison sélectionnée, initialisé avec la valeur sauvegardée
    private var selectedHouseId: Int = sharedPreferences.getInt("houseId", -1)

    // Interface pour notifier le fragment
    interface OnHouseSelectedListener {
        fun onHouseSelected(houseId: Int)
    }

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_house, parent, false)

        val house = getItem(position) as HouseData

        Log.d("Adapter", "Affichage de la maison : ${house.houseId}, Propriétaire: ${house.owner}")

        // Initialiser les vues depuis le XML
        val radioButton = rowView.findViewById<RadioButton>(R.id.radioBtnSelect)
        val houseIdTextView = rowView.findViewById<TextView>(R.id.txtHouseId)
        val ownerStatusTextView = rowView.findViewById<TextView>(R.id.txtStatusOwner)




        // Afficher les informations de la maison
        houseIdTextView.text = "Maison : ${house.houseId}"
        ownerStatusTextView.text = if (house.owner) "Propriétaire" else "Locataire"

        // Vérifie si la maison est sélectionnée par défaut ou manuellement
        radioButton.isChecked = (selectedHouseId == house.houseId)


        // Gérer la sélection du RadioButton
        radioButton.setOnClickListener {

            // Si un autre bouton est sélectionné, mettre à jour l'ID de la maison sélectionnée
            if (selectedHouseId != house.houseId) {
                selectedHouseId = house.houseId // Mettre à jour l'ID sélectionné
                if (house.owner) saveStatusOwner(true) else saveStatusOwner(false)
                Log.d("Adapter", "Maison sélectionnée : ${house.houseId}")
                saveHouseId(selectedHouseId) // Sauvegarder l'ID de la maison sélectionnée
                notifyDataSetChanged() // Mettre à jour l'affichage pour refléter la sélection unique
                listener.onHouseSelected(selectedHouseId) // Notifier le fragment
            }
        }



        return rowView
    }

    // Sauvegarder l'ID de la maison dans SharedPreferences
    private fun saveHouseId(houseId: Int) {
        sharedPreferences.edit()
            .putInt("houseId", houseId)
            .apply()
        Log.d("HomeFragmentAdapter", "House ID sauvegardé : $houseId")
    }

    private fun saveStatusOwner(isOwner: Boolean) {
        sharedPreferences.edit()
            .putBoolean("isOwner", isOwner)
            .apply()
        Log.d("HomeFragmentAdapter", "status  enregistré avec succès")
    }

}
