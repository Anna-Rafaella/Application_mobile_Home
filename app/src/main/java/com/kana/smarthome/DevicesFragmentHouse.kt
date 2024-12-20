package com.kana.smarthome

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment


class DevicesFragmentHouse : Fragment() {

    private var devices = ArrayList<DeviceData>()
    private lateinit var devicesAdapter: DevicesAdapter
    private var token: String? = null
    private var houseId: Int? = null
    private lateinit var listViewDevices: ListView
    private  lateinit var textErreurDeChargement: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_device_house, container, false)

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
        listViewDevices = rootView.findViewById(R.id.devices_list)
        devicesAdapter = DevicesAdapter(requireContext(), devices)
        listViewDevices.adapter = devicesAdapter

        textErreurDeChargement = rootView.findViewById(R.id.textErreurDeChargement)

        loadDevices()

        // Ajouter un écouteur sur l'icône d'aide
        rootView.findViewById<ImageView>(R.id.ivHelp).setOnClickListener {
            showInstructionDialog()
        }

        return rootView
    }


    private fun showInstructionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Instructions")
        builder.setMessage(
            "Bienvenue sur la page Contenant l'ensemble des appareils de la maison.Vous pourriez effectuer differents actions selon le type d'appareils :\n\n" +
                    "- Pour les Ampoules identifiées par light : Vous pourrez les ALLUMER et les ETEINDRE\n" +
                    "- Pour les fenetres identifiées par shutter : Vous pourrez les OUVRIR, les FERMER et/ou STOPPER l'evolution d'une action\n" +
                    "- Pour la porte de garage identifiées par garage door : Vous pourrez l' OUVRIR, la FERMER et/ou STOPPER l'evolution d'une action.\n\n" +
                    "Si rien ne se charge sur la page, veuillez actualiser la maquette de la maison dans le navigateur  et recharger cette page.\n\n"+
                    "A vous de jouer !! "
        )
        builder.setPositiveButton("Compris") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
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
        }
        else if(responseCode == 500) {
            // Gérer l'affichage d'erreur dans le thread principal
            activity?.runOnUiThread {
                listViewDevices.visibility = View.GONE
                textErreurDeChargement.visibility = View.VISIBLE
                textErreurDeChargement.text = "OUVREZ LA MAQUETTE DE LA MAISON DANS UN NAVIGATEUR"
            }
             }
        else{
            Log.e(
                "DevicesFragment",
                "Erreur lors du chargement des appareils: code $responseCode ou données nulles"
            )
        }
    }



    private fun updateDevices() {
        activity?.runOnUiThread {
            devicesAdapter.notifyDataSetChanged()
        }
    }
}
