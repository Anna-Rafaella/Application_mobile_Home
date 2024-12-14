package com.kana.smarthome

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import java.util.*
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.Manifest
import android.app.AlertDialog
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts



class HomeFragment : Fragment(), HomeFragmentAdapter.OnHouseSelectedListener {

    private var houseId: Int? = null // Identifiant de la maison sélectionnée
    private var token: String? = null // Le token sera récupéré dans onCreateView
    private var isOwner: Boolean? = null
    private var userName: String? = null
    private val devices = ArrayList<DeviceData>() // Liste des appareils chargés
    private val house = ArrayList<HouseData>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var houseAdapter: HomeFragmentAdapter

    private var users: ArrayList<UsersData> = ArrayList()
    private lateinit var usersAdapter: HomeFragmentUsersAdapter

    private val usersAccess: ArrayList<UsersAccessData> = ArrayList()
    private lateinit var usersAccessAdapter: HomeFragmentUsersWithAccessAdapter


    private lateinit var txtUserName: TextView
    private lateinit var txtCity: TextView
    private lateinit var txtWeather: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>


    companion object {
        private const val TAG = "HomeFragment"
        private const val WEATHER_API_KEY = "a9d4e10d9a830bda95a30ad73d90474b" // Votre clé OpenWeatherMap

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)
        houseId = sharedPreferences.getInt("houseId",0)
        isOwner = sharedPreferences.getBoolean("isOwner",false)
        userName =sharedPreferences.getString("UserName",null)

        // Initialisation du client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialisation de locationPermissionLauncher
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchLocationAndWeather()
            } else {
                Log.e("HomeFragment", "Permission de localisation refusée.")
            }
        }
        if (token.isNullOrEmpty()) { Log.e("HomeFragment", "Aucun token trouvé dans SharedPreferences.") }
        if (houseId == 0) { Log.e("HomeFragment", "Aucun identifiant de maison trouvé dans SharedPreferences.") }
        if (userName.isNullOrEmpty()) { Log.e("HomeFragment", "Aucun nom d'utilisateur trouvé dans SharedPreferences.") }


        setupAdapters(rootView)
        initSwitches(rootView)

        loadHouse()

        //recuperer et afficher le nom de l'utlisateur
        txtUserName = rootView.findViewById(R.id.txtUserName)

        txtUserName.text = " Bienvenue $userName!! "

        //Gestion de la météo
        txtCity = rootView.findViewById(R.id.txtCity)
        txtWeather = rootView.findViewById(R.id.txtWeather)


        // Vérification et demande de permission de localisation
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocationAndWeather()
        }






        rootView.findViewById<Button>(R.id.btnUsers)?.setOnClickListener {
            sendUsersChoice(rootView)
        }

        // Ajouter un écouteur sur l'icône d'aide
        rootView.findViewById<ImageView>(R.id.ivHelp).setOnClickListener {
            showInstructionDialog()
        }

        return rootView
    }


    /**
     * Initialisation du launcher de permission.
     */
    private fun setupPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                fetchLocationAndWeather()
            } else {
                Toast.makeText(requireContext(), "Permission de localisation refusée.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    /**
     * Récupère la localisation et la météo.
     */
    private fun fetchLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permission manquante.", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                lifecycleScope.launch {
                    val cityName = fetchCityName(location)
                    txtCity.text = "$cityName"
                    fetchWeather(location.latitude, location.longitude)
                }
            } else {
                Toast.makeText(requireContext(), "Localisation introuvable.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erreur lors de la localisation.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Utilise le Geocoder pour récupérer le nom de la ville.
     */
    private suspend fun fetchCityName(location: Location): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.getOrNull(0)?.locality ?: "Ville inconnue"
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la récupération de la ville : ${e.message}")
                "Ville inconnue"
            }
        }
    }

    /**
     * Récupère les informations météo depuis OpenWeatherMap.
     */
    private fun fetchWeather(lat: Double, lon: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=fr&appid=$WEATHER_API_KEY"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)

                val weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                val temperature = jsonObject.getJSONObject("main").getDouble("temp")

                Log.d(TAG,"$weatherDescription et $temperature")
                withContext(Dispatchers.Main) {
                    txtWeather.text = "$weatherDescription, $temperature°C"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur météo : ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erreur météo.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    /**
     * Actualise la page lors de la sélection d'une maison
     */
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

    /**
     * Initialiser les adapters.
     */
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

    /**
     * Initialise l'ecoute sur les switchs.
     */
    private fun initSwitches(rootView: View) {
        rootView.findViewById<SwitchCompat>(R.id.switchAmpoule)?.apply {
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



    /**
     * Récupère la liste des maisons selon le token recuperé dans le sharePreference.
     */
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

    /**
     * Réponse du serveur sur  de la liste des maisons.
     */
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



    /**
     * Récupère la liste des devices de la maison associée au token.
     */
    private fun loadDevices() {
        houseId?.let {
            Api().get<DeviceResponse>(
                "https://polyhome.lesmoulinsdudev.com/api/houses/$it/devices",
                ::handleDevicesResponse,
                token.orEmpty()
            )
        }
    }

    /**
     * Réponse du serveur sur la liste des devices de la maison associée.
     */
    private fun handleDevicesResponse(responseCode: Int, responseBody: DeviceResponse?) {
        if (responseCode == 200 && responseBody != null) {
            devices.clear()
            devices.addAll(responseBody.devices)
        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des appareils.")
        }
    }


    /**
     * Récupère la liste des utilisateurs du serveur pour un choix ulterieur.
     */
    private fun loadUsers() {
        Api().get("https://polyhome.lesmoulinsdudev.com/api/users", ::loadUsersSuccess)
    }

    /**
     * Réponse du serveur sur la liste des utilisateurs.
     */
    private fun loadUsersSuccess(responseCode: Int, loadedUsers: List<UsersData>?) {
        if (responseCode == 200 && loadedUsers != null) {
            users.clear()
            users.addAll(loadedUsers)
            updateUsersList()

        } else {
            Log.e("HomeFragment", "Erreur lors du chargement des utilisateurs.")
        }
    }


    /**
     * Récupère la liste des utilisateurs ayant l'accès a la maison du proprietaire.
     */
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

    /**
     * Réponse du serveur sur la liste des utilisateurs aytant l'accès.
     */
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



    /**
     * Envoie au serveur du choix de l'utilisateur selectionné pour lui donner accès a nla maison de l'utilisateur.
     */
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

    /**
     * Réponse du serveur apres l'envoie de l'utilisateur a qui on veut donner un accès.
     */
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


    /**
     * Envoie la commande au appareils en effectuant un parcours des devices  selon le type actionné.
     */
    private fun sendGlobalCommandToAllDevices(deviceType: String, command: String) {
        devices.filter { it.id.startsWith(deviceType) }.forEach { device ->
            sendCommandToDevice(device.id, command)
        }
    }

    /**
     * Methode qui execute l'envoie de la commande a un appareil selon son type.
     */
    private fun sendCommandToDevice(deviceId: String, command: String) {
        Api().post(
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command",
            ResponseCommandData(command),
            { response -> Log.d("HomeFragment", "Commande envoyée avec succès à $deviceId : $response") },
            token.orEmpty()
        )
    }



    /**
     * Sauvergarde le HouseID de la maison dans le sharePreferences.
     */
    private fun saveHouseId(houseId: Int) {
        sharedPreferences.edit().putInt("MyHouseId", houseId).apply()
    }

    /**
     * Met a jour la liste des maisons recuperé du serveur.
     */
    private fun updateHouseList() {
        requireActivity().runOnUiThread{
            houseAdapter.notifyDataSetChanged()
            // Recalculer la hauteur après la mise à jour
            view?.findViewById<ListView>(R.id.list_house)?.let {
                setListViewHeightBasedOnChildren(it)
            }
        }

    }


    /**
     * Met a jour la liste des utilisateurs recuperé du serveur.
     */
    private fun updateUsersList() {
        requireActivity().runOnUiThread{
            usersAdapter.notifyDataSetChanged()
        }

    }


    /**
     * Met a jour la liste des utilisateurs ayant l'accès  recuperé du serveur.
     */
    private fun updateUsersWithAccess() {
        requireActivity().runOnUiThread{
            usersAccessAdapter.notifyDataSetChanged()

            // Recalculer la hauteur après la mise à jour
            view?.findViewById<ListView>(R.id.users_list_access)?.let {
                setListViewHeightBasedOnChildren(it)
            }
        }

    }


    /**
     * Gere la hauteur des listview pour s'adapter au contenu.
     */
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


    /**
     * Affiche les détails de la page en cours
     */
    private fun showInstructionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Instructions")
        builder.setMessage(
            "Bienvenue sur l'accueil de votre application domotique. Vous pouvez entre autre :\n\n" +
                    "- Agir sur l'ensemble des appareils de la maison en fonction des types au niveau de la rubrique Appareils fréquemment utilisés.\n" +
                    "- Sélectionner la maison sur laquelle agir. Par défaut, le choix est celui ou vous etes le propriétaire.\n" +
                    "- Si vous etes propriétaire de la maison sélectionnée , vous pouvez gérer les accès en accordant et en retirant ces droits aux differents utilisateurs. \n\n" +
                    "A vous de jouer !! "

        )
        builder.setPositiveButton("Compris") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }


}


