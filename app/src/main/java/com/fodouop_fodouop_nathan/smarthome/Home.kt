package com.fodouop_fodouop_nathan.smarthome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fodouop_fodouop_nathan.smarthome.databinding.ActivityMainBinding

class Home : AppCompatActivity() {

    // Déclaration de la variable binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser le View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)

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
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

//    import android.content.Context
//    import android.content.SharedPreferences
//
//    fun storeAuthToken(context: Context, token: String) {
//        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("auth_token", token)
//        editor.apply() // Enregistrer les modifications
//    }

}


