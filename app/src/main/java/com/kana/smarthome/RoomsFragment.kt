package com.kana.smarthome

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kana.smarthome.databinding.FragmentRoomsBinding


class RoomsFragment : Fragment(R.layout.fragment_rooms) {

    private lateinit var binding: FragmentRoomsBinding

    private val rooms: List<Room> = listOf(
        Room("Entrée", listOf(
            DeviceRoom("Shutter 1.5", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Light 1.3", listOf("TURN ON", "TURN OFF"), null, null, 1)
        )),
        Room("Salon principal", listOf(
            DeviceRoom("Light 1.2", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 1.3", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 1.4", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        )),
        Room("Cuisine ouverte", listOf(
            DeviceRoom("Shutter 1.6", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 1.7", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 1.8", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Light 1.4", listOf("TURN ON", "TURN OFF"), null, null, 1)
        )),
        Room("Salle à manger", listOf(
            DeviceRoom("Light 1.1", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 1.1", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 1.2", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 1.11", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        )),
        Room("Garage", listOf(
            DeviceRoom("Light 1.5", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 1.9", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("GarageDoor 1.1", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        )),
        Room("Buanderie / Cellier", listOf(
            DeviceRoom("Shutter 1.10", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Light 1.6", listOf("TURN ON", "TURN OFF"), null, null, 1)
        )),

        Room("Chambre parentale avec salle de bain privée", listOf(
            DeviceRoom("Shutter 2.2", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 2.3", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Light 2.2", listOf("TURN ON", "TURN OFF"), null, null, 1)
        )),
        Room("Deuxième chambre d’enfant ou chambre d’amis", listOf(
            DeviceRoom("Shutter 2.10", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 2.1", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Light 2.1", listOf("TURN ON", "TURN OFF"), null, null, 1)
        )),
        Room("Troisième chambre d’enfant ou chambre d’amis", listOf(
            DeviceRoom("Light 2.4", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 2.7", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 2.8", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        )),
        Room("Salle de bain familiale", listOf(
            DeviceRoom("Light 2.5", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 2.9", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        )),
        Room("Bureau / Pièce de travail", listOf(
            DeviceRoom("Light 2.3", listOf("TURN ON", "TURN OFF"), null, null, 1),
            DeviceRoom("Shutter 2.4", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 2.5", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null),
            DeviceRoom("Shutter 2.6", listOf("OPEN", "CLOSE", "STOP"), 1, 0, null)
        ))
    )



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRoomsBinding.bind(view)


        val adapter = RoomsAdapter(rooms) { room ->
            val bundle = Bundle().apply {
                putParcelable("room", room)
            }

            // Appel de la méthode replaceFragment de l'activité parent
            (requireActivity() as? Home)?.replaceFragment(DeviceFragmentRoom().apply {
                arguments = bundle
            })
        }

        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewRooms.adapter = adapter

        // Ajouter un écouteur sur l'icône d'aide
        binding.ivHelp.setOnClickListener {
            showInstructionDialog()
        }

    }
    private fun showInstructionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Instructions")
        builder.setMessage(
            "Bienvenue sur la page des Pieces de la maison.\n\n" +
                    "En cliquant sur une pièce, vous trouverez l'ensemble des appareils contenus dans cette pièce que vous pourrez controlez.\n" +
                    "Allez-y, lancez-vous \n\n"+
                    "A vous de jouer !! "
        )
        builder.setPositiveButton("Compris") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

}

