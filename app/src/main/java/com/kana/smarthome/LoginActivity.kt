package com.kana.smarthome

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Récupération des vues avec findViewById
        val loginButton = findViewById<Button>(R.id.btnConnect)


        // Définir le listener pour le bouton de connexion
        loginButton.setOnClickListener {
            login()
        }

    }


    private fun login() {
        val nameEditText = findViewById<EditText>(R.id.txtname)
        val passwordEditText = findViewById<EditText>(R.id.txtPassword)

        val login = nameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (login.isNotBlank() && password.isNotBlank()) {
            // Créer une instance de LoginData
            val loginData = LoginData(login, password)

            // Appeler l'API pour se connecter
            Api().post<LoginData, Map<String, String>>(
                "https://polyhome.lesmoulinsdudev.com/api/users/auth",
                loginData,
                ::loginSuccess
            )
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }

    // Cette méthode sera appelée après une connexion réussie.
    // Elle doit gérer le code de réponse et le token renvoyé par le serveur.
    private fun loginSuccess(responseCode: Int, tokenData: Map<String, String>?) {
        Log.d("login", "dans la page : ${tokenData.toString()}")


        if (responseCode == 200 && tokenData != null) {
            val token = tokenData["token"]

            storeAuthToken(this, token)
            loadHouse(token)

            // Démarrer l'activité HomeActivity
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Terminer l'activité de connexion

        } else {
            runOnUiThread {
                Toast.makeText(this, "Erreur de connexion. Code: $responseCode", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun loadHouse(token: String?) {
                    Api().get<List<HouseData>>(
                        "https://polyhome.lesmoulinsdudev.com/api/houses",
                        ::handleHouseResponse,
                        token
                    )

         Log.d("HomeFragment", "Token introuvable pour charger les maisons.")
    }
    private fun handleHouseResponse(responseCode: Int, loadedHouses: List<HouseData>?) {
        if (responseCode == 200 && loadedHouses != null) {

                val houseId = loadedHouses.firstOrNull { it.owner }?.houseId
                houseId?.let {
                    saveHouseId(this,it)
                    saveStatusOwner(this,true)
                }
            saveUserName(this)
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des maisons.")
        }
    }


    private fun storeAuthToken(context: Context, token: String?) {
        if (!token.isNullOrEmpty()) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("TOKEN", token)
            editor.apply() // Enregistrer les modifications
            Log.d("HomeFragment", "Token enregistré avec succès")
        } else {
            Log.e("HomeFragment", "Token vide. Enregistrement ignoré.")
        }
    }

    private fun saveHouseId(context: Context, houseId: Int?) {
        if (houseId != null) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("houseId", houseId)
            editor.apply() // Enregistrer les modifications
            Log.d("HomeFragment", "HouseId proprietaire par défaut enregistré avec succès")
        } else {
            Log.e("HomeFragment", "HouseId vide. Enregistrement ignoré.")
        }
    }

    private fun saveStatusOwner(context: Context, isOwner: Boolean) {

            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isOwner", isOwner)
            editor.apply() // Enregistrer les modifications
            Log.d("HomeFragment", "Token enregistré avec succès")
    }
    private fun saveUserName(context: Context) {
        val userName = findViewById<EditText>(R.id.txtname).text.toString()
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("UserName", userName)
        editor.apply() // Enregistrer les modifications
        Log.d("HomeFragment", "Token enregistré avec succès")
    }

    public fun registerNewAccount(view: View) {
        val intent = Intent(this, RegisterActivity::class.java);
        startActivity(intent);
    }


}