package com.fodouop_fodouop_nathan.smarthome

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class DevicesAdapter(private val context: Context, private val dataSource: ArrayList<DeviceData>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val rowView = convertView ?: inflater.inflate(R.layout.item_device, parent, false)
        val device = getItem(position) as DeviceData

        val deviceName = rowView.findViewById<TextView>(R.id.device_name)
        val actionButton1 = rowView.findViewById<Button>(R.id.action_button_1)
        val actionButton2 = rowView.findViewById<Button>(R.id.action_button_2)
        val actionButton3 = rowView.findViewById<Button>(R.id.action_button_3)


        // Initialisation du nom de l'appareil
        deviceName.text = device.id

        // Configuration des boutons en fonction du type de périphérique
        when {
            device.id.startsWith("Light") || device.id.startsWith("Shutter") || device.id.startsWith("GarageDoor") -> {
                actionButton1.text = "Ouvrir"
                actionButton2.text = "Stop"
                actionButton3.text = "Fermer"
                actionButton3.visibility = View.VISIBLE
                actionButton1.setOnClickListener { sendCommand(device.id, "OPEN") }
                actionButton2.setOnClickListener { sendCommand(device.id, "STOP") }
                actionButton3.setOnClickListener { sendCommand(device.id, "CLOSE") }
            }
            else -> {
                actionButton1.visibility = View.GONE
                actionButton2.visibility = View.GONE
                actionButton3.visibility = View.GONE
            }
        }

        return rowView
    }



    private fun sendCommand(deviceId: String?, command: String) {
        if (deviceId.isNullOrEmpty()) {
            Log.e("DevicesFragment", "ID de l'appareil non valide.")
            return
        }
        val houseId = 10 // Identifiant de la maison
        val token =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE3MzIzODk0NTB9.GeiTuo8sbJbs8gCTLPxu6eBH7w5hvwsSYrNmL8EMDN4"

        // Préparer l'URL
        val url =
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command"

        // Préparer le corps de la requête
        val requestBody = ResponseCommandData(command)

        Log.d("DevicesFragment", "Envoi de la commande '$command' à l'appareil $deviceId via $url")

        // Vérifier si le token est disponible
        if (token.isNotEmpty()) {
            Api().post(
                url,
                requestBody, // Passer le corps de la requête
                ::responseCommand, // Callback pour gérer la réponse
                token
            )
        } else {
            Log.e("DevicesFragment", "Token d'authentification introuvable.")
        }
    }

    private fun responseCommand(responseCode: Int, responseBody: String?) {
        Log.d("DevicesFragment", "Code réponse : $responseCode, Body : $responseBody")
        when (responseCode) {
            200 -> {
                Log.d("DevicesFragment", "Commande exécutée avec succès : $responseBody")
            }

            403 -> {
                Log.e(
                    "DevicesFragment",
                    "Accès interdit : Token invalide ou permissions insuffisantes."
                )
            }

            500 -> {
                Log.e("DevicesFragment", "Erreur serveur : Problème au niveau de l'API.")
            }

            else -> {
                Log.e(
                    "DevicesFragment",
                    "Erreur inconnue : Code $responseCode, Body : $responseBody"
                )
            }
        }
    }
}


