package com.kana.smarthome

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class HomeFragmentUsersAdapter(private val context: Context, private val dataSource: List<UsersData>) : BaseAdapter() {


    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_users, parent, false)

        val users = getItem(position) as UsersData

        Log.d("Adapter", "Affichage des users : ${users.login}")

        // Initialiser les vues depuis le XML
        val usersTextView = rowView.findViewById<TextView>(R.id.userTextView)

        // Afficher les informations de la maison
        usersTextView.text = users.login

        return rowView
    }


}
