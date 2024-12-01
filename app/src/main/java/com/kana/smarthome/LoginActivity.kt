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
import com.fodouop_fodouop_nathan.smarthome.Api


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


    // Cette méthode sera appelée après une connexion réussie.
    // Elle doit gérer le code de réponse et le token renvoyé par le serveur.
    private fun loginSuccess(responseCode: Int, tokenData:Map<String,String>?) {
        Log.d("login","dans la page : ${tokenData.toString()}")


        if (responseCode == 200 && tokenData != null) {
            val token = tokenData["token"]

            storeAuthToken(this,token)

            // Démarrer l'activité HomeActivity
            val intent = Intent(this, Home::class.java)
            //intent.putExtra("TOKEN", token) // Passer le token à l'activité suivante
            startActivity(intent)
            finish() // Terminer l'activité de connexion

        } else {
            runOnUiThread {
                Toast.makeText(this, "Erreur de connexion. Code: $responseCode", Toast.LENGTH_SHORT)
                    .show()
            }
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
            Api().post<LoginData, Map<String,String>>("https://polyhome.lesmoulinsdudev.com/api/users/auth",loginData,::loginSuccess)
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }

    public fun registerNewAccount(view: View)
    {
        val intent = Intent(this, RegisterActivity::class.java);
        startActivity(intent);
    }

}