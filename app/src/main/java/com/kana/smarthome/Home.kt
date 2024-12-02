package com.kana.smarthome

import android.os.Bundle
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
                R.id.devices -> replaceFragment(DevicesFragment())
                R.id.routines -> replaceFragment(RoutinesFragment())
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



}


