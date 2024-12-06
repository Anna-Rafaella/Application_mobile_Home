package com.kana.smarthome

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Récupération des vues avec findViewById
        usernameEditText = findViewById(R.id.txtRegisterName)
        passwordEditText = findViewById(R.id.txtRegisterPassword)

        val registerButton = findViewById<Button>(R.id.btnRegister)

        // Définir le listener pour le bouton d'enregistrement
        registerButton.setOnClickListener {
            register()
        }
    }
    private fun register() {
        // Récupérer les valeurs des champs de texte
        val name = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Vérifier que les champs ne sont pas vides
        if (name.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        } else {
            // Afficher un message de confirmation
            Toast.makeText(this, "Enregistrement en cours...", Toast.LENGTH_SHORT).show()
        }
        // Créer une instance de RegisterData avec les informations de l'utilisateur
        val registerData = RegisterData(name, password)

        // Utiliser Api().post pour envoyer les données au serveur
        Api().post<RegisterData, String>("https://polyhome.lesmoulinsdudev.com/api/users/register", registerData, ::registerSuccess)

    }

    // Gérer la réponse du serveur après l'enregistrement
    // NB: Toujours bien vérifier le type d'arguments atttendues par la méthode post
    private fun registerSuccess(responseCode: Int, response: String?) {
        when (responseCode) {
            200 -> { // Succès
                runOnUiThread {
                    Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                }
                finish() // Terminer l'activité et revenir à la page de connexion
            }
            409 -> { // Conflit : login déjà utilisé
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Le login est déjà utilisé par un autre compte. Veuillez en choisir un autre.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            500 -> { // Erreur serveur
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Une erreur s'est produite sur le serveur. Veuillez réessayer plus tard.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else -> { // Cas inattendu
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Erreur lors de la création du compte. Code: $responseCode, Message: $response",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Log pour suivi des problèmes
        Log.d("RegisterActivity", "Code de réponse: $responseCode, Réponse: $response")
    }



    private fun clearField() {
        // Effacer le texte des champs de texte
        usernameEditText.setText("")
        passwordEditText.setText("")
    }

    public fun goToLogin(view: View) {
        finish()
    }

}