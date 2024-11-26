package com.kapande_deng.mymobile_home_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération du bouton par son ID et configuration de son écouteur de clic
        val btnstart = findViewById<Button>(R.id.start_button)
        btnstart.setOnClickListener{
            // Création d'une intention pour démarrer la `LoginActivity`
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent) // Lance `SecondActivity`
        }

    }
}

