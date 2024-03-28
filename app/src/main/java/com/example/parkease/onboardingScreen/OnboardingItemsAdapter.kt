package com.example.parkease.onboardingScreen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parkease.R

class OnboardingItemsAdapter(private val onbordingItem: List<OnbordingItem>):
    RecyclerView.Adapter<OnboardingItemsAdapter.OnboardingItemViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnboardingItemsAdapter.OnboardingItemViewHolder {
        return OnboardingItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.onboarding_item_container,parent,false
            )
        )
    }

    override fun onBindViewHolder(
        holder: OnboardingItemsAdapter.OnboardingItemViewHolder,
        position: Int
    ) {
        holder.bind(onbordingItem[position])
    }

    override fun getItemCount(): Int {
        return onbordingItem.size
    }

    inner class OnboardingItemViewHolder(view: View): RecyclerView.ViewHolder(view){

        private val imageOnbordingItem = view.findViewById<ImageView>(R.id.imageOnboarding)
        private val textTile = view.findViewById<TextView>(R.id.textTitle)
        private val textDescription = view.findViewById<TextView>(R.id.textDescription)

        fun bind(onbordingItem: OnbordingItem){
            imageOnbordingItem.setImageResource(onbordingItem.onbordingImage)
            textTile.text = onbordingItem.title
            textDescription.text = onbordingItem.description
        }

    }
}