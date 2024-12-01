package com.kana.smarthome


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class RoutinesAdapter(private val context: Context, private val dataSource: ArrayList<RoutineData>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Réutiliser la vue existante ou en créer une nouvelle si nécessaire
        val rowView = convertView ?: inflater.inflate(R.layout.item_routine, parent, false)

        // Obtenir l'objet Routine correspondant à la position
        val routine = getItem(position) as RoutineData

        // Récupérer la TextView de la vue
        val routineNameTextView = rowView.findViewById<TextView>(R.id.tvRoutineName)

        // Remplir la TextView avec le nom de la routine
        routineNameTextView.text = routine.name

        return rowView
    }
}
