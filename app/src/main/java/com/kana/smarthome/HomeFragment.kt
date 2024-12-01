package com.kana.smarthome

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fodouop_fodouop_nathan.smarthome.Api
import com.fodouop_fodouop_nathan.smarthome.DeviceData
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var houseId: Int? = null // Identifiant de la maison sélectionnée
    private var token: String? = null // Le token sera récupéré dans onCreateView
    private val devices = ArrayList<DeviceData>() // Liste des appareils chargés
    private val house = ArrayList<HouseData>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var houseAdapter: HomeFragmentAdapter

    private var users: ArrayList<UsersData> = ArrayList()
    private lateinit var usersAdapter: HomeFragmentUsersAdapter

    private val usersAccess: ArrayList<UsersAccessData> = ArrayList()
    private lateinit var usersAccessAdapter: HomeFragmentUsersWithAccessAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)
        if (token.isNullOrEmpty()) {
            Log.e("HomeFragment", "Aucun token trouvé dans SharedPreferences.")
        }

        setupAdapters(rootView)
        initSwitches(rootView)

        loadHouse()
        loadUsers()
        loadUsersWithAccess()

        rootView.findViewById<Button>(R.id.btnUsers)?.setOnClickListener {
            sendUsersChoice(rootView)
        }

        return rootView
    }

    private fun setupAdapters(rootView: View) {
        houseAdapter = HomeFragmentAdapter(requireContext(), house)
        rootView.findViewById<ListView>(R.id.list_house)?.adapter = houseAdapter

        usersAccessAdapter = HomeFragmentUsersWithAccessAdapter(requireContext(), usersAccess)
        rootView.findViewById<ListView>(R.id.users_list_access)?.adapter = usersAccessAdapter

        usersAdapter = HomeFragmentUsersAdapter(requireContext(), users)
      rootView.findViewById<Spinner>(R.id.userchoice)?.adapter = usersAdapter
   }

    private fun sendUsersChoice(rootView: View) {
        val spinUsers = rootView.findViewById<Spinner>(R.id.userchoice)
        val selectedUser = spinUsers?.selectedItem as? UsersData

        if (selectedUser == null || selectedUser.login.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Aucun utilisateur valide sélectionné !", Toast.LENGTH_SHORT).show()
            return
        }

        val choiceData = UsersLoginData(selectedUser.login)
        Toast.makeText(requireContext(), "Vérification en cours...", Toast.LENGTH_SHORT).show()

        Api().post(
            path = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            data = choiceData,
            onSuccess = ::userChoiceSuccess,
            securityToken = token.orEmpty()
        )
    }

    private fun userChoiceSuccess(responseCode: Int) {
        requireActivity().runOnUiThread {
            if (responseCode == 200) {
                Toast.makeText(requireContext(), "Accès accordé !!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Erreur lors de l'accord d'accès", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUsers() {
        Api().get("https://polyhome.lesmoulinsdudev.com/api/users", ::loadUsersSuccess)
    }

    private fun loadUsersSuccess(responseCode: Int, loadedUsers: List<UsersData>?) {
        if (responseCode == 200 && loadedUsers != null) {
            users.clear()
            users.addAll(loadedUsers)
            updateUsersList()
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des utilisateurs.")
        }
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
            updateHouseList()

            if (houseId == null) {
                houseId = loadedHouses.firstOrNull { it.owner }?.houseId
                houseId?.let { saveHouseId(it) }
            }

            loadDevices()
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des maisons.")
        }
    }

    private fun loadUsersWithAccess() {
        token?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    Api().get<List<UsersAccessData>>(
                        "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
                        ::handleUsersWithAccessResponse,
                        it
                    )
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Erreur lors du chargement des utilisateurs avec accès : ${e.message}")
                }
            }
        }
    }

    private fun handleUsersWithAccessResponse(responseCode: Int, loadedUsers: List<UsersAccessData>?) {
        if (responseCode == 200 && loadedUsers != null) {
            usersAccess.clear()
            usersAccess.addAll(loadedUsers)
            updateUsersWithAccess()
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des utilisateurs avec accès.")
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

    private fun initSwitches(rootView: View) {
        rootView.findViewById<SwitchCompat>(R.id.home_switch_ampoule)?.apply {
            setOnCheckedChangeListener { _, isChecked ->
                sendGlobalCommandToAllDevices("Light", if (isChecked) "TURN ON" else "TURN OFF")
            }
        }

        rootView.findViewById<SwitchCompat>(R.id.switchFenetre)?.apply {
            setOnCheckedChangeListener { _, isChecked ->
                sendGlobalCommandToAllDevices("Shutter", if (isChecked) "OPEN" else "CLOSE")
            }
        }

        rootView.findViewById<SwitchCompat>(R.id.switchGarage)?.apply {
            setOnCheckedChangeListener { _, isChecked ->
                sendGlobalCommandToAllDevices("GarageDoor", if (isChecked) "OPEN" else "CLOSE")
            }
        }
    }

    private fun sendGlobalCommandToAllDevices(deviceType: String, command: String) {
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

    private fun saveHouseId(houseId: Int) {
        sharedPreferences.edit().putInt("MyHouseId", houseId).apply()
    }

    private fun updateHouseList() {
        houseAdapter.notifyDataSetChanged()
    }

    private fun updateUsersList() {
        usersAdapter.notifyDataSetChanged()
    }

    private fun updateUsersWithAccess() {
        usersAccessAdapter.notifyDataSetChanged()
    }
}
