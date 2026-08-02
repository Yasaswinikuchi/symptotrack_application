package com.symtotrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

data class Hospital(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val distance: Double,
    val rating: String,
    val phone: String,
    val specialties: String,
    val fee: String,
    val waitTime: Int
)

class HospitalAdapter(private val hospitals: List<Hospital>, private val onBookClicked: (Hospital) -> Unit) :
    RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_hospital_name)
        val tvAddress: TextView = view.findViewById(R.id.tv_hospital_address)
        val tvSpecialties: TextView = view.findViewById(R.id.tv_hospital_specialties)
        val tvRating: TextView = view.findViewById(R.id.tv_hospital_rating)
        val tvWait: TextView = view.findViewById(R.id.tv_wait_time)
        val tvDist: TextView = view.findViewById(R.id.tv_distance)
        val tvPhone: TextView = view.findViewById(R.id.tv_hospital_phone)
        val btnBook: Button = view.findViewById(R.id.btn_book_appointment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.tvName.text = hospital.name
        holder.tvAddress.text = hospital.address.ifEmpty { "Medical District" }
        holder.tvSpecialties.text = "Specialties: ${hospital.specialties.ifEmpty { "General Medicine, Emergency" }}"
        holder.tvRating.text = "${hospital.rating} ★"
        holder.tvWait.text = "Wait: ${hospital.waitTime} min"
        val df = DecimalFormat("#.#")
        holder.tvDist.text = "${df.format(hospital.distance)} km away"
        holder.tvPhone.text = "📞 ${hospital.phone.ifEmpty { "+1 800-555-0199" }}"

        val feeLabel = if (hospital.fee.startsWith("₹") || hospital.fee.lowercase().contains("contact") || hospital.fee.lowercase().contains("fee")) {
            hospital.fee
        } else {
            "₹${hospital.fee}"
        }

        holder.btnBook.text = "Book Appointment • $feeLabel"
        
        holder.btnBook.setOnClickListener {
            onBookClicked(hospital)
        }
    }

    override fun getItemCount() = hospitals.size
}
