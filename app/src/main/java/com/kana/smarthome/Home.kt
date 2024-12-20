package com.kana.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kana.smarthome.databinding.ActivityHomeBinding


class Home : AppCompatActivity() {

    // Déclaration de la variable binding
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Initialiser le View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)

        // Définir la vue de l'activité
        setContentView(binding.root)




        // Charger le fragment par défaut
        replaceFragment(HomeFragment())

        // Gérer les sélections dans la barre de navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.rooms -> replaceFragment(RoomsFragment())
                R.id.devices -> replaceFragment(DevicesFragmentHouse())
                R.id.routines -> replaceFragment(RoutinesFragment())
                R.id.logout -> { logoutUser() }
            }
            true
        }


    }

    // Méthode pour remplacer les fragments
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
        fragmentTransaction.commit()
    }


    private fun logoutUser() {
        Toast.makeText(this, "Déconnexion en cours...", Toast.LENGTH_SHORT).show()
        // Suppression des données de session
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirection vers la page de login
        val intent = Intent(this, LoginActivity::class.java)

        // Effacons l'historique des activités
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish() // Fermer l'activité actuelle
    }


}


