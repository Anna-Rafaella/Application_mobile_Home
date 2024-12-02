package com.kana.smarthome

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class HomeFragmentUsersWithAccessAdapter(
    private val context: Context,
    private val dataSource: List<UsersAccessData>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var houseId: Int? = null
    private var token: String? = null

    init {
        // Récupérer le token et l'identifiant de la maison depuis SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        houseId = sharedPreferences.getInt("MyHouseId", 0)
        token = sharedPreferences.getString("TOKEN", null)
    }


    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_user_list, parent, false)

        val user = getItem(position) as UsersAccessData

        Log.d("Adapter", "Affichage des utilisateurs ayant l'accès : ${user.userLogin}")

        // Initialiser les vues depuis le XML
        val usersaccessTextView = rowView.findViewById<TextView>(R.id.UsersWithAccess)

        // Afficher le login de l'utilisateur
        usersaccessTextView.text = user.userLogin

        // Connecter le bouton Supprimer l'accès
        val sendButtonRemoveAccess = rowView.findViewById<Button>(R.id.RemoveAccess)
        sendButtonRemoveAccess.setOnClickListener {
            sendRemoveUsersChoice(user.userLogin)
        }

        return rowView
    }

    // Méthode pour traiter la réponse de l'API et supprimer l'accès
    private fun removeUserChoiceSuccess(responseCode: Int) {
        val activity = context as? FragmentActivity
        activity?.runOnUiThread {
            if (responseCode == 200) {
                Toast.makeText(
                    context,
                    "Accès supprimé avec succès !",
                    Toast.LENGTH_SHORT
                ).show()
                activity.supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(context, "Erreur lors de la suppression de l'accès", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Méthode pour envoyer la requête de suppression d'accès utilisateur
    private fun sendRemoveUsersChoice(login: String?) {
        if (login.isNullOrEmpty()) {
            Toast.makeText(context, "Login invalide", Toast.LENGTH_SHORT).show()
            return
        }

        val choiceData = UsersLoginData(login)

        // Debug : afficher les données dans les logs
        Log.d("HomeFragment", "Utilisateur sélectionné : ${choiceData.userLogin}")

        // Affiche un Toast pour indiquer que la commande est en cours
        Toast.makeText(context, "Suppression en cours...", Toast.LENGTH_SHORT).show()

        // Appel de l'API avec gestion sécurisée du token
        Api().delete(
            path = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
             data = choiceData,
            onSuccess = ::removeUserChoiceSuccess,
            securityToken = token ?: ""
        )
    }
}
