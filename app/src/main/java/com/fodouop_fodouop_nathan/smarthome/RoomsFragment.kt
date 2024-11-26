package com.fodouop_fodouop_nathan.smarthome

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment


class RoomsFragment : Fragment() {

    private var rooms = ArrayList<RoomData>()
    private lateinit var roomsAdapter: RoomsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_rooms, container, false)

        val listViewRooms = rootView.findViewById<ListView>(R.id.listViewRooms)
        roomsAdapter = RoomsAdapter(
            requireContext(),
            rooms
        )
        listViewRooms.adapter = roomsAdapter

        loadRooms()
        return rootView
    }

    private fun getAuthToken(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null) // Retourne null si aucun token n'est trouvé
    }


    private fun loadRoomsSuccess(responseCode: Int,loadRooms: List<RoomData>?)
    {
        if (responseCode == 200 && loadRooms != null) {
            rooms.clear()
            rooms.addAll(loadRooms)
            updateRooms()
        }
    }
    private fun loadRooms() {
        //val token = getAuthToken(requireContext())
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE3MzIzODk0NTB9.GeiTuo8sbJbs8gCTLPxu6eBH7w5hvwsSYrNmL8EMDN4"

        Api().get("https://polyhome.lesmoulinsdudev.com/api/houses ",::loadRoomsSuccess,token)
    }

    private fun updateRooms() {
        activity?.runOnUiThread {
            roomsAdapter.notifyDataSetChanged()
        }
    }
}
