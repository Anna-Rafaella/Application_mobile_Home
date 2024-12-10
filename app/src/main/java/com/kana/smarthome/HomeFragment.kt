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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeFragmentAdapter.OnHouseSelectedListener {

    private var houseId: Int? = null // Identifiant de la maison sélectionnée
    private var token: String? = null // Le token sera récupéré dans onCreateView
    private var isOwner: Boolean? = null
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

        houseId = sharedPreferences.getInt("houseId",0)
        if (houseId == 0) {
            Log.e("HomeFragment", "Aucun identifiant de maison trouvé dans SharedPreferences.")
        }

        isOwner = sharedPreferences.getBoolean("isOwner",false)

        setupAdapters(rootView)
        initSwitches(rootView)

        loadHouse()


        rootView.findViewById<Button>(R.id.btnUsers)?.setOnClickListener {
            sendUsersChoice(rootView)
        }


        return rootView
    }


    companion object {
        private const val TAG = "HomeFragment"
    }



    override fun onHouseSelected(houseId: Int) {

        if (this.houseId != houseId) {
            requireActivity().runOnUiThread {
                val activity = requireActivity()
                val intent = activity.intent
                activity.finish()
                startActivity(intent)

            }
        }
    }



    private fun setupAdapters(rootView: View) {
        // Configurer les adaptateurs
        houseAdapter = HomeFragmentAdapter(requireContext(), house,this)
        val houseListView = rootView.findViewById<ListView>(R.id.list_house)
        houseListView?.adapter = houseAdapter
        setListViewHeightBasedOnChildren(houseListView) // Ajuster la hauteur dynamiquement

        usersAccessAdapter = HomeFragmentUsersWithAccessAdapter(requireContext(), usersAccess)
        val usersAccessListView = rootView.findViewById<ListView>(R.id.users_list_access)
        usersAccessListView?.adapter = usersAccessAdapter
        setListViewHeightBasedOnChildren(usersAccessListView) // Ajuster la hauteur dynamiquement

        usersAdapter = HomeFragmentUsersAdapter(requireContext(), users)
        rootView.findViewById<Spinner>(R.id.userchoice)?.adapter = usersAdapter

        val accesBlock = rootView.findViewById<ConstraintLayout>(R.id.Accessblock)

        // Sécuriser l'accès à la gestion d'access
        accesBlock?.let {
            // Vérifier si l'utilisateur est le propriétaire de la maison sélectionnée
            if (isOwner == true) {
                // Si la maison sélectionnée est celle de l'utilisateur, afficher le bloc
                it.visibility = View.VISIBLE
            } else {
                // Sinon, masquer le bloc
                it.visibility = View.GONE
            }
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

            loadDevices()
            loadUsers()
            loadUsersWithAccess()
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

            // Ajout uniquement des utilisateurs avec owner == 0 soit n'etant pas proprietaire et ayant uniquement été ajouté
            //val filteredUsers = loadedUsers.filter { it.owner.toInt() == 0 }
            val filteredUsers = loadedUsers.filter {
                try {
                    it.owner.toInt() == 0
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Erreur de conversion de l'attribut owner : ${e.message}")
                    false
                }
            }

            houseId?.let { saveHouseId(it) }
            usersAccess.addAll(filteredUsers)
            updateUsersWithAccess()
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des utilisateurs avec accès.")
        }
    }



    private fun sendUsersChoice(rootView: View) {
        val spinUsers = rootView.findViewById<Spinner>(R.id.userchoice)
        val selectedUser = spinUsers?.selectedItem as? UsersData

        if (selectedUser == null || selectedUser.login.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Utilisateur invalide sélectionné !", Toast.LENGTH_SHORT).show()
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
                loadHouse()
            } else {
                Toast.makeText(requireContext(), "Erreur lors de l'accord d'accès", Toast.LENGTH_SHORT).show()
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
        requireActivity().runOnUiThread{
            houseAdapter.notifyDataSetChanged()
            // Recalculer la hauteur après la mise à jour
            view?.findViewById<ListView>(R.id.list_house)?.let {
                setListViewHeightBasedOnChildren(it)
            }
        }

    }

    private fun updateUsersList() {
        requireActivity().runOnUiThread{
            usersAdapter.notifyDataSetChanged()
        }

    }

    private fun updateUsersWithAccess() {
        requireActivity().runOnUiThread{
            usersAccessAdapter.notifyDataSetChanged()

            // Recalculer la hauteur après la mise à jour
            view?.findViewById<ListView>(R.id.users_list_access)?.let {
                setListViewHeightBasedOnChildren(it)
            }
        }

    }


    //Gerer la hauteur des listview pour s'adapter au contenu
    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }

        // Ajouter la hauteur des diviseurs
        val dividerHeight = listView.dividerHeight * (listAdapter.count - 1)
        totalHeight += dividerHeight

        // Appliquer la hauteur calculée
        val params = listView.layoutParams
        params.height = totalHeight
        listView.layoutParams = params
        listView.requestLayout()
    }

}


