package com.kana.smarthome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kana.smarthome.databinding.FragmentDevicesBinding

class DeviceFragmentRoom : Fragment(R.layout.fragment_devices) {

    private lateinit var binding: FragmentDevicesBinding
    private var room: Room? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDevicesBinding.bind(view)

        room = arguments?.getParcelable("room")

        room?.let {
            val adapter = DeviceAdapterRoom(it.devices,requireContext())

            binding.recyclerViewDevices.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewDevices.adapter = adapter

        }

        binding.backbtn.setOnClickListener {
            Log.d("deviceHome","bouton declenché")
            requireActivity().supportFragmentManager.popBackStack()
        }

    }



}