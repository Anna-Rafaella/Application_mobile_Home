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

class HomeFragmentAdapter(private val context: Context, private val dataSource: List<HouseData>) : BaseAdapter() {
    private  var sharedPreferences: SharedPreferences // Préférences partagées

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // Variable pour stocker l'ID de la maison sélectionnée
    private var selectedHouseId: Int = 0

    init {
        // Initialiser les SharedPreferences
        sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        // Supprimer le houseId sauvegardé à chaque démarrage
        clearSavedHouseId()
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

        // Initialiser le selectedHouseId après la récupération des maisons
        if (selectedHouseId == 0) {
            // Chercher la première maison où l'utilisateur est propriétaire
            val defaultHouse = dataSource.firstOrNull { it.owner }

            // Si une maison du propriétaire est trouvée, l'initialiser
            if (defaultHouse != null) {
                selectedHouseId = defaultHouse.houseId
                saveHouseId(selectedHouseId) // Sauvegarder l'ID de la maison par défaut
                Log.d("Adapter", "Maison initialisée : ${defaultHouse.houseId}")
            }
        }

        // Mettre à jour l'état du RadioButton en fonction de la sélection actuelle
        radioButton.isChecked = (selectedHouseId == house.houseId)

        // Gérer la sélection du RadioButton
        radioButton.setOnClickListener {
            selectedHouseId = house.houseId
            Log.d("Adapter", "Maison sélectionnée : ${house.houseId}")
            saveHouseId(selectedHouseId) // Sauvegarder l'ID de la maison sélectionnée
            notifyDataSetChanged() // Mettre à jour l'affichage pour refléter la sélection unique
        }

        return rowView
    }

    // Sauvegarder l'ID de la maison dans SharedPreferences
    private fun saveHouseId(houseId: Int) {
        sharedPreferences.edit()
            .putInt("MyHouseId", houseId)
            .apply()
        Log.d("HomeFragmentAdapter", "House ID sauvegardé : $houseId")
    }

    // Effacer le houseId sauvegardé dans SharedPreferences
    private fun clearSavedHouseId() {
        sharedPreferences.edit()
            .remove("MyHouseId") // Supprimer l'ID de la maison
            .apply()
        Log.d("HomeFragmentAdapter", "House ID supprimé")
    }
}
