package com.kana.smarthome

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class RoutinesFragment : Fragment() {

    private var houseId: Int? = null // Identifiant de la maison sélectionnée
    private var token: String? = null // Le token sera récupéré dans onCreateView
    private val devices = ArrayList<DeviceData>() // Liste des appareils chargés
    private val house = ArrayList<HouseData>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvSelectedOpeningTime: TextView
    private lateinit var tvSelectedClosingTime: TextView
    private var openingHour: Int = 0
    private var openingMinute: Int = 0
    private var closingHour: Int = 0
    private var closingMinute: Int = 0
    private var isDayModeActive = false
    private var isNightModeActive = false
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_routines, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)
        if (token.isNullOrEmpty()) {
            Log.e("HomeFragment", "Aucun token trouvé dans SharedPreferences.")
        }


        houseId = sharedPreferences.getInt("houseId",0)
        if (houseId == 0) {
            Log.e("HomeFragment", "Aucun identifiant de maison trouvé dans SharedPreferences.")
        }

        // Initialisation des vues
        tvSelectedOpeningTime = rootView.findViewById(R.id.tvSelectedOpeningTime)
        tvSelectedClosingTime = rootView.findViewById(R.id.tvSelectedClosingTime)
        val btnSaveSchedule = rootView.findViewById<Button>(R.id.btnSaveSchedule)

        // Boutons pour choisir les heures
        rootView.findViewById<Button>(R.id.btnSelectOpeningTime).setOnClickListener {
            showTimePicker { hour, minute ->
                openingHour = hour
                openingMinute = minute
                tvSelectedOpeningTime.text = "Heure d'ouverture : ${formatTime(hour, minute)}"
                Log.d("RoutinesFragment", "Heure d'ouverture définie : $openingHour:$openingMinute")
            }
        }

        // Ajouter un écouteur sur l'icône d'aide
        rootView.findViewById<ImageView>(R.id.ivHelp).setOnClickListener {
            showInstructionDialog()
        }

        rootView.findViewById<Button>(R.id.btnSelectClosingTime).setOnClickListener {
            showTimePicker { hour, minute ->
                closingHour = hour
                closingMinute = minute
                tvSelectedClosingTime.text = "Heure de fermeture : ${formatTime(hour, minute)}"
                Log.d("RoutinesFragment", "Heure de fermeture définie : $closingHour:$closingMinute")
            }
        }

        // Switches pour activer/désactiver les modes
        setupDayModeSwitch(rootView.findViewById(R.id.day_switch))
        setupNightModeSwitch(rootView.findViewById(R.id.night_switch))

        // Gestion des vérifications automatiques
        handler = Handler(Looper.getMainLooper())
        startModeCheck()
        // Gestion de l'enregistrement des

        loadHouse()

        btnSaveSchedule.setOnClickListener {
            val openingTime = tvSelectedOpeningTime.text.toString()
            val closingTime = tvSelectedClosingTime.text.toString()

            // Enregistrer ou passer les données à une autre fonction
            saveSchedule(openingTime, closingTime)
        }

        return rootView
    }

    private fun showInstructionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Instructions")
        builder.setMessage(
            "Configurez les horaires pour définir le mode jour et le mode nuit.\n\n" +
                    "- Le mode jour s'active automatiquement à l'heure d'ouverture.\n" +
                    "- Le mode nuit s'active automatiquement à l'heure de fermeture.\n" +
                    "- Utilisez les boutons ci-dessous pour enregistrer vos préférences.\n\n"+
                    "A vous de jouer !! "
        )
        builder.setPositiveButton("Compris") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }


    private fun setupDayModeSwitch(daySwitch: SwitchCompat) {
        daySwitch.setOnCheckedChangeListener { _, isChecked ->
            isDayModeActive = isChecked
            if (isChecked) {
                Toast.makeText(requireContext(), "Mode Jour activé", Toast.LENGTH_SHORT).show()
                Log.d("RoutinesFragment", "Mode Jour activé.")
            } else {
                Toast.makeText(requireContext(), "Mode Jour désactivé", Toast.LENGTH_SHORT).show()
                Log.d("RoutinesFragment", "Mode Jour désactivé.")
            }
        }
    }

    private fun setupNightModeSwitch(nightSwitch: SwitchCompat) {
        nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNightModeActive = isChecked
            if (isChecked) {
                Toast.makeText(requireContext(), "Mode Nuit activé", Toast.LENGTH_SHORT).show()
                Log.d("RoutinesFragment", "Mode Nuit activé.")
            } else {
                Toast.makeText(requireContext(), "Mode Nuit désactivé", Toast.LENGTH_SHORT).show()
                Log.d("RoutinesFragment", "Mode Nuit désactivé.")
            }
        }
    }

    private fun startModeCheck() {
        handler.post(object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                Log.d(
                    "RoutinesFragment",
                    "Heure actuelle : $currentHour:$currentMinute. Mode Jour : $isDayModeActive, Mode Nuit : $isNightModeActive"
                )

                if (isDayModeActive && currentHour == openingHour && currentMinute == openingMinute) {
                    Log.d("RoutinesFragment", "Correspondance avec l'heure d'ouverture détectée.")
                    activateDayMode()
                }

                if (isNightModeActive && currentHour == closingHour && currentMinute == closingMinute) {
                    Log.d("RoutinesFragment", "Correspondance avec l'heure de fermeture détectée.")
                    activateNightMode()
                }

                // Vérification toutes les 60 secondes
                handler.postDelayed(this, 10000)
            }
        })
    }

    private fun activateDayMode() {
        Log.d("RoutinesFragment", "Activation automatique du Mode Jour.")
        sendGlobalCommandToAllDevices("Light", "TURN OFF")
        sendGlobalCommandToAllDevices("GarageDoor", "OPEN")
        sendGlobalCommandToAllDevices("Shutter", "OPEN")
        Toast.makeText(requireContext(), "Mode Jour exécuté automatiquement", Toast.LENGTH_SHORT).show()
    }

    private fun activateNightMode() {
        Log.d("RoutinesFragment", "Activation automatique du Mode Nuit.")
        sendGlobalCommandToAllDevices("Light", "TURN ON")
        sendGlobalCommandToAllDevices("GarageDoor", "CLOSE")
        sendGlobalCommandToAllDevices("Shutter", "CLOSE")
        Toast.makeText(requireContext(), "Mode Nuit exécuté automatiquement", Toast.LENGTH_SHORT).show()
    }


    private fun loadHouse() {
        token?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    Api().get<List<HouseData>>(
                        "https://polyhome.lesmoulinsdudev.com/api/houses",
                        ::handleHouseResponse,
                        it
                    )
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Erreur lors du chargement des maisons : ${e.message}")
                }
            }
        } ?: Log.e("HomeFragment", "Token introuvable pour charger les maisons.")
    }
    private fun handleHouseResponse(responseCode: Int, loadedHouses: List<HouseData>?) {
        if (responseCode == 200 && loadedHouses != null) {
            house.clear()
            house.addAll(loadedHouses)

            loadDevices()
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des maisons.")
        }
    }



    private fun loadDevices() {
        houseId?.let {
            Api().get<DeviceResponse>(
                "https://polyhome.lesmoulinsdudev.com/api/houses/$it/devices",
                ::handleDevicesResponse,
                token.orEmpty()
            )
        }
    }

    private fun handleDevicesResponse(responseCode: Int, responseBody: DeviceResponse?) {
        if (responseCode == 200 && responseBody != null) {
            devices.clear()
            devices.addAll(responseBody.devices)
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des appareils.")
        }
    }

    private fun sendGlobalCommandToAllDevices(deviceType: String, command: String) {
        Log.d("RoutinesFragment", "Commande envoyée : $command pour $deviceType.")
        // Simuler l'envoi des commandes
        devices.filter { it.id.startsWith(deviceType) }.forEach { device ->
            sendCommandToDevice(device.id, command)
        }
    }
    private fun sendCommandToDevice(deviceId: String, command: String) {
        Api().post(
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command",
            ResponseCommandData(command),
            { response -> Log.d("HomeFragment", "Commande envoyée avec succès à $deviceId : $response") },
            token.orEmpty()
        )
    }

    private fun showTimePicker(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }
    // Simuler l'enregistrement des données
    private fun saveSchedule(openingTime: String, closingTime: String) {
        // Vous pouvez transmettre ces données à une autre activité ou fonction
        println("Horaires sauvegardés : Ouverture - $openingTime, Fermeture - $closingTime")
    }

}
