package com.kana.smarthome

import DeviceRoom
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fodouop_fodouop_nathan.smarthome.Api
import com.kana.smarthome.databinding.ItemDevicesBinding

class DeviceAdapterRoom(private val devices: List<DeviceRoom>,
                        private val context: Context,
                        private val onBackClick: () -> Unit // Ajoutez un callback pour le bouton Back
    ) : RecyclerView.Adapter<DeviceAdapterRoom.DeviceViewHolder>() {

    private var token: String? = null

    init {
        // Récupérer le token depuis SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        //val device = devices[position]
        holder.bind(devices[position], onBackClick)
    }

    override fun getItemCount(): Int = devices.size

    inner class DeviceViewHolder(private val binding: ItemDevicesBinding) :
        RecyclerView.ViewHolder(binding.root) {





        fun bind(device: DeviceRoom , onBackClick: () -> Unit) {
            // Gérer le clic sur le bouton Back
            binding.buttonBack.setOnClickListener {
                onBackClick()
            }
            binding.deviceName.text = device.id
            binding.actionButton1.text =device.power.toString()
            binding.actionButton2.text = device.opening.toString()
            binding.actionButton3.text = device.openingMode.toString()


            // Configuration des boutons en fonction du type de périphérique
            when {
                device.id.startsWith("Shutter") || device.id.startsWith("GarageDoor") -> {
                    binding.actionButton1.text = "Ouvrir"
                    binding.actionButton2.text = "Stop"
                    binding.actionButton3.text = "Fermer"
                    binding.actionButton3.visibility = View.VISIBLE
                    binding.actionButton1.setOnClickListener { sendCommand(device.id, "OPEN") }
                    binding.actionButton2.setOnClickListener { sendCommand(device.id, "STOP") }
                    binding.actionButton3.setOnClickListener { sendCommand(device.id, "CLOSE") }
                }
                device.id.startsWith("Light") -> {
                    binding.actionButton1.text = "Allumer"
                    binding.actionButton2.text = "Aucune action"
                    binding.actionButton3.text = "Eteindre"
                    binding.actionButton2.visibility = View.GONE
                    binding.actionButton1.setOnClickListener { sendCommand(device.id, "TURN ON") }
                    binding.actionButton2.setOnClickListener { sendCommand(device.id, " ") }
                    binding.actionButton3.setOnClickListener { sendCommand(device.id, "TURN OFF") }
                }
                else -> {
                    binding.actionButton1.visibility = View.GONE
                    binding.actionButton2.visibility = View.GONE
                    binding.actionButton3.visibility = View.GONE
                }
            }
        }



    }



    private fun sendCommand(deviceId: String?, command: String) {
        if (deviceId.isNullOrEmpty()) {
            Log.e("DeviceAdapterRoom", "ID de l'appareil non valide.")
            return
        }
        val houseId = 10 // Identifiant de la maison

        // Préparer l'URL
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command"

        // Préparer le corps de la requête
        val requestBody = ResponseCommandData(command)

        Log.d("DeviceAdapterRoom", "Envoi de la commande '$command' à l'appareil $deviceId via $url")

        // Vérifier si le token est disponible
        if (token.isNullOrEmpty()) {
            Log.e("DeviceAdapterRoom", "Token d'authentification introuvable.")
            return
        }

        Api().post(url, requestBody, ::responseCommand, token!!)
    }

    private fun responseCommand(responseCode: Int, responseBody: String?) {
        Log.d("DeviceAdapterRoom", "Code réponse : $responseCode, Body : $responseBody")
        when (responseCode) {
            200 -> {
                Log.d("DeviceAdapterRoom", "Commande exécutée avec succès : $responseBody")
            }
            403 -> {
                Log.e("DeviceAdapterRoom", "Accès interdit : Token invalide ou permissions insuffisantes.")
            }
            500 -> {
                Log.e("DeviceAdapterRoom", "Erreur serveur : Problème au niveau de l'API.")
            }
            else -> {
                Log.e("DeviceAdapterRoom", "Erreur inconnue : Code $responseCode, Body : $responseBody")
            }
        }
    }
}

