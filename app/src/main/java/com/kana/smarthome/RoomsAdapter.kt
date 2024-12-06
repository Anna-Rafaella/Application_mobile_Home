package com.kana.smarthome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kana.smarthome.databinding.ItemRoomBinding

class RoomsAdapter(private val rooms: List<Room>, private val onClick: (Room) -> Unit) :
    RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.textViewRoomName.text = room.name
            binding.root.setOnClickListener { onClick(room) }
        }
    }
}
