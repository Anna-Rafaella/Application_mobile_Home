package com.kapande_deng.mymobile_home_application

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    private var token: String? = null  // Initialise `token` ici comme nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Récupérer le token de l'intent dans onCreate
        token = intent.getStringExtra("TOKEN")

        Log.d("HomeActivity", "Token récupéré : $token") // Log pour vérifier la valeur du token

        // Vérification si le token est nul
        if (token == null) {
            Toast.makeText(this, "Erreur : Token manquant", Toast.LENGTH_SHORT).show()
            finish()  // Termine l'activité si le token est manquant
            return
        }

        Log.d("HomeActivity", "Activity Started") // Log pour vérifier que l'activité est démarrée

    }



}