package com.fodouop_fodouop_nathan.smarthome;


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class RoomsAdapter(private val context: Context, private val dataSource: ArrayList<RoomData>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Réutiliser la vue existante ou en créer une nouvelle si nécessaire
        val rowView = convertView ?: inflater.inflate(R.layout.item_room, parent, false)

        // Obtenir l'objet Room correspondant à la position
        val room = getItem(position) as RoomData


        // Récupérer la TextView de la vue
        val roomNameTextView = rowView.findViewById<TextView>(R.id.Roomid)
        val roomstatusRole = rowView.findViewById<TextView>(R.id.RoomStatus)

        // Remplir la TextView avec le nom de la pièce
        roomNameTextView.text = room.houseId.toString()
        roomstatusRole.text = room.owner.toString()

        return rowView
    }
}

