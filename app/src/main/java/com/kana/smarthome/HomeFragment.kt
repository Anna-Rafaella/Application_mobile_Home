package com.fodouop_fodouop_nathan.smarthome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private val houseId = 10 // Identifiant de la maison
    private val token =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE3MzIzODk0NTB9.GeiTuo8sbJbs8gCTLPxu6eBH7w5hvwsSYrNmL8EMDN4"
    private var devices = ArrayList<DeviceData>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialisation des SwitchCompat
        val ampouleSwitch = rootView.findViewById<SwitchCompat>(R.id.home_switch_ampoule)
        val fenetreSwitch = rootView.findViewById<SwitchCompat>(R.id.switchFenetre)
        val garageSwitch = rootView.findViewById<SwitchCompat>(R.id.switchGarage)

        if (ampouleSwitch != null) {
            Log.d("HomeFragment", "Ampoule Switch found: $ampouleSwitch")
        } else {
            Log.e("HomeFragment", "Ampoule Switch not found!")
        }
        ampouleSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("HomeFragment", "Ampoule Switch changed: ${isChecked}")
            if (isChecked) {
                sendGlobalCommandToAllDevices("Light", "TURN ON")
            } else {
                sendGlobalCommandToAllDevices("Light", "TURN OFF")
            }
        }


        fenetreSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("HomeFragment", "Fenetre Switch changed: $isChecked")
            if (isChecked) {
                sendGlobalCommandToAllDevices("Shutter", "OPEN")
            } else {
                sendGlobalCommandToAllDevices("Shutter", "CLOSE")
            }
        }

        garageSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("HomeFragment", "Garage Switch changed: $isChecked")
            if (isChecked) {
                sendGlobalCommandToAllDevices("GarageDoor", "OPEN")
            } else {
                sendGlobalCommandToAllDevices("GarageDoor", "CLOSE")
            }
        }

        // Charger les appareils
        loadDevices()

        return rootView
    }


    // Charger les appareils
    private fun loadDevices() {
        if (token.isNotEmpty()) {
            Api().get<DeviceResponse>(
                "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices",
                { responseCode, responseBody ->
                    handleDevicesResponse(responseCode, responseBody)
                },
                token
            )
        } else {
            Log.e("HomeFragment", "Token d'authentification introuvable.")
        }
    }

    // Gérer la réponse des appareils
    private fun handleDevicesResponse(responseCode: Int, responseBody: DeviceResponse?) {
        Log.d("HomeFragment", "Response code: $responseCode and body: $responseBody")
        if (responseCode == 200 && responseBody != null) {
            // Effacer les appareils existants
            devices.clear()

            // Ajouter les appareils à la liste générale
            devices.addAll(responseBody.devices)

            Log.d("HomeFragment", "Devices chargés : ${responseBody.devices.size}")
        } else {
            Log.e(
                "HomeFragment",
                "Erreur lors du chargement des appareils: code $responseCode ou données nulles"
            )
        }
    }

    // Méthode pour envoyer une commande globale à tous les appareils d'un type spécifique
    private fun sendGlobalCommandToAllDevices(deviceType: String, command: String) {
        Log.d(
            "HomeFragment",
            "Envoi de la commande '$command' pour le type d'appareil : $deviceType"
        )

        // Filtrer les appareils en fonction du type
        val filteredDevices = devices.filter { it.id.startsWith(deviceType) }

        // Envoyer la commande à chaque appareil filtré
        filteredDevices.forEach { device ->
            sendCommandToDevice(device.id, command)
        }
    }

    // Méthode pour envoyer une commande à un appareil spécifique
    private fun sendCommandToDevice(deviceId: String, command: String) {
        val url =
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command"

        // Préparer le corps de la requête
        val requestBody = ResponseCommandData(command)

        Log.d("HomeFragment", "Envoi de la commande '$command' à l'appareil $deviceId via $url")

        Api().post(
            url,
            requestBody,
            { response ->
                Log.d(
                    "HomeFragment",
                    "Commande envoyée avec succès à l'appareil $deviceId : $response"
                )
            },
            token
        )
    }
}


/*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}*/