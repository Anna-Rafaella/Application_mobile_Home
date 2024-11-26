package com.fodouop_fodouop_nathan.smarthome

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.kana.smarthome.R

class DevicesFragment : Fragment() {

    private var devices = ArrayList<DeviceData>()
    private lateinit var devicesAdapter: DevicesAdapter

    // Sections des appareils
    private val rezDeChausseeDevices = ArrayList<DeviceData>()
    private val niveauUnDevices = ArrayList<DeviceData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_devices, container, false)

        val listViewDevices = rootView.findViewById<ListView>(R.id.devices_list)
        devicesAdapter = DevicesAdapter(requireContext(), devices)
        listViewDevices.adapter = devicesAdapter

        loadDevices()

        return rootView
    }

    private fun getAuthToken(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun loadDevices() {
        val houseId = 10
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE3MzIzODk0NTB9.GeiTuo8sbJbs8gCTLPxu6eBH7w5hvwsSYrNmL8EMDN4"


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
            categorizeDevices(responseBody.devices)
            // Ajouter les appareils à la liste générale
            devices.addAll(rezDeChausseeDevices)
            devices.addAll(niveauUnDevices)

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
