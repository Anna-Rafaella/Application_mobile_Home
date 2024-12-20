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
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RoutinesFragment : Fragment() {

    private var houseId: Int? = null
    private var token: String? = null
    private val devices = ArrayList<DeviceData>()
    private val house = ArrayList<HouseData>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvSelectedOpeningTime: TextView
    private lateinit var tvSelectedClosingTime: TextView
    private var openingHour: Int = 8
    private var openingMinute: Int = 0
    private var closingHour: Int = 20
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

        // Initialisation des vues
        tvSelectedOpeningTime = rootView.findViewById(R.id.tvSelectedOpeningTime)
        tvSelectedClosingTime = rootView.findViewById(R.id.tvSelectedClosingTime)
        val btnSaveSchedule = rootView.findViewById<Button>(R.id.btnSaveSchedule)

        // Charger les données depuis SharedPreferences
        loadScheduleFromPreferences()
        loadSwitchStateFromPreferences()

        // Boutons pour choisir les heures
        rootView.findViewById<Button>(R.id.btnSelectOpeningTime).setOnClickListener {
            showTimePicker { hour, minute ->
                openingHour = hour
                openingMinute = minute
                tvSelectedOpeningTime.text = "Heure d'ouverture : ${formatTime(hour, minute)}"
                saveScheduleToPreferences(openingHour, openingMinute, closingHour, closingMinute)
            }
        }

        rootView.findViewById<Button>(R.id.btnSelectClosingTime).setOnClickListener {
            showTimePicker { hour, minute ->
                closingHour = hour
                closingMinute = minute
                tvSelectedClosingTime.text = "Heure de fermeture : ${formatTime(hour, minute)}"
                saveScheduleToPreferences(openingHour, openingMinute, closingHour, closingMinute)
            }
        }

        // Ajouter un écouteur sur l'icône d'aide
        rootView.findViewById<ImageView>(R.id.ivHelp).setOnClickListener {
            showInstructionDialog()
        }

        // Switches pour activer/désactiver les modes
        setupDayModeSwitch(rootView.findViewById(R.id.day_switch))
        setupNightModeSwitch(rootView.findViewById(R.id.night_switch))

        // Gestion des vérifications automatiques
        handler = Handler(Looper.getMainLooper())
        startModeCheck()

        btnSaveSchedule.setOnClickListener {
            val openingTime = tvSelectedOpeningTime.text.toString()
            val closingTime = tvSelectedClosingTime.text.toString()
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
                    "- Utilisez les boutons ci-dessous pour enregistrer vos préférences."
        )
        builder.setPositiveButton("Compris") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun setupDayModeSwitch(daySwitch: SwitchCompat) {
        daySwitch.isChecked = isDayModeActive
        daySwitch.setOnCheckedChangeListener { _, isChecked ->
            isDayModeActive = isChecked
            saveSwitchStateToPreferences(isDayModeActive, isNightModeActive)
            val message = if (isChecked) "Mode Jour activé" else "Mode Jour désactivé"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNightModeSwitch(nightSwitch: SwitchCompat) {
        nightSwitch.isChecked = isNightModeActive
        nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNightModeActive = isChecked
            saveSwitchStateToPreferences(isDayModeActive, isNightModeActive)
            val message = if (isChecked) "Mode Nuit activé" else "Mode Nuit désactivé"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startModeCheck() {
        handler.post(object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                if (isDayModeActive && currentHour == openingHour && currentMinute == openingMinute) {
                    activateDayMode()
                }

                if (isNightModeActive && currentHour == closingHour && currentMinute == closingMinute) {
                    activateNightMode()
                }

                handler.postDelayed(this, 60000) // Vérification toutes les 60 secondes
            }
        })
    }

    private fun activateDayMode() {
        sendGlobalCommandToAllDevices("Light", "TURN OFF")
        sendGlobalCommandToAllDevices("GarageDoor", "OPEN")
        sendGlobalCommandToAllDevices("Shutter", "OPEN")
        Toast.makeText(requireContext(), "Mode Jour exécuté automatiquement", Toast.LENGTH_SHORT).show()
    }

    private fun activateNightMode() {
        sendGlobalCommandToAllDevices("Light", "TURN ON")
        sendGlobalCommandToAllDevices("GarageDoor", "CLOSE")
        sendGlobalCommandToAllDevices("Shutter", "CLOSE")
        Toast.makeText(requireContext(), "Mode Nuit exécuté automatiquement", Toast.LENGTH_SHORT).show()
    }

    private fun saveScheduleToPreferences(openingHour: Int, openingMinute: Int, closingHour: Int, closingMinute: Int) {
        with(sharedPreferences.edit()) {
            putInt("OpeningHour", openingHour)
            putInt("OpeningMinute", openingMinute)
            putInt("ClosingHour", closingHour)
            putInt("ClosingMinute", closingMinute)
            apply()
        }
    }

    private fun loadScheduleFromPreferences() {
        openingHour = sharedPreferences.getInt("OpeningHour", 8)
        openingMinute = sharedPreferences.getInt("OpeningMinute", 0)
        closingHour = sharedPreferences.getInt("ClosingHour", 20)
        closingMinute = sharedPreferences.getInt("ClosingMinute", 0)
        tvSelectedOpeningTime.text = "Heure d'ouverture : ${formatTime(openingHour, openingMinute)}"
        tvSelectedClosingTime.text = "Heure de fermeture : ${formatTime(closingHour, closingMinute)}"
    }

    private fun saveSwitchStateToPreferences(dayMode: Boolean, nightMode: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("DayModeActive", dayMode)
            putBoolean("NightModeActive", nightMode)
            apply()
        }
    }

    private fun loadSwitchStateFromPreferences() {
        isDayModeActive = sharedPreferences.getBoolean("DayModeActive", false)
        isNightModeActive = sharedPreferences.getBoolean("NightModeActive", false)
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

    private fun saveSchedule(openingTime: String, closingTime: String) {
        Log.d("RoutinesFragment", "Horaires sauvegardés : Ouverture - $openingTime, Fermeture - $closingTime")
    }

    private fun sendGlobalCommandToAllDevices(deviceType: String, command: String) {
        devices.filter { it.id.startsWith(deviceType) }.forEach { device ->
            sendCommandToDevice(device.id, command)
        }
    }

    private fun sendCommandToDevice(deviceId: String, command: String) {
        // Simuler l'envoi de commande
        Log.d("RoutinesFragment", "Commande $command envoyée à l'appareil $deviceId.")
    }
}
