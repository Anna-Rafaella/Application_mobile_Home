package com.kana.smarthome


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.fodouop_fodouop_nathan.smarthome.Api


class RoutinesFragment : Fragment() {

    private var routines = ArrayList<RoutineData>()
    private lateinit var routinesAdapter: RoutinesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_routines, container, false)

        val listViewRoutines = rootView.findViewById<ListView>(R.id.listViewRoutines)
        routinesAdapter = RoutinesAdapter(requireContext(), routines)
        listViewRoutines.adapter = routinesAdapter

        val buttonUpdate = rootView.findViewById<Button>(R.id.btnUpdateRoutines)
        buttonUpdate.setOnClickListener {
            loadRoutines()
        }

        loadRoutines()
        return rootView
    }

    private fun loadRoutinesSuccess(responseCode: Int,loadRoutines: List<RoutineData>?)
    {
        if(responseCode == 200 && loadRoutines != null){
            routines.clear() // on vide l'attribut recipes
            routines.addAll(loadRoutines) // on ajoute dans recipes les elements de loadedRecipes
            updateRoutines()
        }
    }
    private fun loadRoutines() {
        Api().get("https://api.smarthome.com/routines",::loadRoutinesSuccess)

    }

    private fun updateRoutines() {
        activity?.runOnUiThread {
            routinesAdapter.notifyDataSetChanged()
        }
//        // Méthode pour gérer le clic sur "Ajouter une routine"
//        fun onAddRoutineClicked(view: View) {
//            // Exemple : Naviguer vers une nouvelle activité ou afficher un dialogue
//            val intent = Intent(activity, AddRoutineActivity::class.java)
//            startActivity(intent)
//        }
    }

}
