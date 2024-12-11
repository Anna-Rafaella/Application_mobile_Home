package com.kana.smarthome

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment


class DevicesFragment : Fragment() {

    private var devices = ArrayList<DeviceData>()
    private lateinit var devicesAdapter: DevicesAdapter
    private var token: String? = null
    private var houseId: Int? = null

    // Sections des appareils
    private val rezDeChausseeDevices = ArrayList<DeviceData>()
    private val niveauUnDevices = ArrayList<DeviceData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_device, container, false)

        // Récupérer le token depuis SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)  // Récupérer le token, null si non trouvé

        if (token != null) {
            Log.d("MyFragment", "Token récupéré : $token")
        } else {
            Log.e("MyFragment", "Aucun token trouvé.")
        }

        houseId = sharedPreferences.getInt("houseId",0)
        if (houseId == 0) {
            Log.e("HomeFragment", "Aucun identifiant de maison trouvé dans SharedPreferences.")
        }
        val listViewDevices = rootView.findViewById<ListView>(R.id.devices_list)
        devicesAdapter = DevicesAdapter(requireContext(), devices)
        listViewDevices.adapter = devicesAdapter

        loadDevices()

        return rootView
    }



    private fun loadDevices() {



        if (token != null) {
            Api().get<DeviceResponse>(
                "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices",
                { responseCode, responseBody ->
                    handleDevicesResponse(responseCode, responseBody)
                },
                token
            )
        } else {
            Log.e("DevicesFragment", "Token d'authentification introuvable.")
        }
    }

    private fun handleDevicesResponse(responseCode: Int, responseBody: DeviceResponse?) {
        Log.d("DevicesFragment", "Response code: $responseCode et body:$responseBody")
        if (responseCode == 200 && responseBody != null) {
            devices.clear()
            // Séparer les appareils en fonction de leur ID
            //categorizeDevices(responseBody.devices)
            // Ajouter les appareils à la liste générale
            devices.addAll(responseBody.devices)

            updateDevices()
            Log.d("DevicesFragment", "Devices chargés : ${responseBody.devices}")
        } else {
            Log.e(
                "DevicesFragment",
                "Erreur lors du chargement des appareils: code $responseCode ou données nulles"
            )
        }
    }

    private fun categorizeDevices(devicesList: List<DeviceData>) {
        // Séparer les appareils en fonction de leur ID
        for (device in devicesList) {
            when {
                device.id.startsWith("Light 1") -> rezDeChausseeDevices.add(device)
                device.id.startsWith("Shutter 1") -> rezDeChausseeDevices.add(device)
                device.id.startsWith("GarageDoor 1") -> rezDeChausseeDevices.add(device)
                device.id.startsWith("Light 2") -> niveauUnDevices.add(device)
                device.id.startsWith("Shutter 2") -> niveauUnDevices.add(device)
                else -> Log.d("DevicesFragment", "Appareil non catégorisé : ${device.id}")
            }
        }
    }

    private fun updateDevices() {
        activity?.runOnUiThread {
            devicesAdapter.notifyDataSetChanged()
        }
    }
}
