package com.example.hotelapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.hotelapp.databinding.ActivityHotelDetailBinding

class HotelDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotelDetailBinding
    private val resultImages: MutableList<ImageHotel> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)





        val hotelId = intent.getStringExtra("hotelId")
        val hotelName = intent.getStringExtra("hotelName")
        val hotelRegion = intent.getStringExtra("hotelRegion")
        val hotelPrice = intent.getStringExtra("hotelPrice")
        val hotelNumDay = intent.getIntExtra("hotelnumday",1)
        val hotelRev = intent.getStringExtra("hotelrev")
        val hotelTotalRev= intent.getStringExtra("hotelTotalReview")
        println("HotelID" + hotelId)



        binding.hotelNameText.text = hotelName
        binding.priceInfoText.text = hotelPrice
        binding.regionNameTextView.text = hotelRegion

        binding.priceInfoDays.text = "Precio por ${hotelNumDay} ${if (hotelNumDay > 1) "días" else "día"} impuestos incluidos"

        if (hotelTotalRev == 0.toString()) {
            binding.reviewScoreTextView.text = ""
            binding.totalReviewsText.text = "(Sin calificaciones)"
        } else {
            binding.reviewScoreTextView.text = "${hotelRev} / 10"
            binding.totalReviewsText.text= "(total ${hotelTotalRev})"
        }

        binding.bookButton.setOnClickListener {
            showConfirmationDialog()
        }






        // Add a back button to the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewPager: ViewPager = binding.imageViewPager
        val tabLayout: TabLayout = binding.imageTabLayout

        // Create an adapter for the ViewPager
        // Create an adapter for the ViewPager
        val adapter = ImagePagerAdapter(supportFragmentManager, resultImages)


        // Set the adapter on the ViewPager
        viewPager.adapter = adapter

        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager)

        val imageProgressBar: ProgressBar = binding.imageProgressBar
        imageProgressBar.visibility = View.VISIBLE //



        val testRapidAPI= testRapidAPI()
        if (hotelId!= null) {
            GlobalScope.launch {


                println(hotelId)
                val imageUrlList: List<ImageHotel> = testRapidAPI.fetchHotelImages(hotelId)

                resultImages.addAll(imageUrlList)
                println(imageUrlList.size)

                withContext(Dispatchers.Main){
                    adapter.updateImages(resultImages) // assuming your adapter has a method to update images
                    adapter.notifyDataSetChanged()
                    imageProgressBar.visibility = View.GONE

                }
            }

        }


    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estas seguro que quieres reservar este hotel?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            bookHotel()
//            Toast.makeText(this, "Reservación exitosa", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun bookHotel() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return
            }

            val database = FirebaseDatabase.getInstance()

            val hotelId = intent.getStringExtra("hotelId")
            val hotelName = intent.getStringExtra("hotelName")
            val hotelRegion = intent.getStringExtra("hotelRegion")
            val hotelPrice = intent.getStringExtra("hotelPrice")
            val hotelNumDay = intent.getIntExtra("hotelnumday", 1)
            val hotelRev = intent.getStringExtra("hotelrev") ?: "0"
            val hotelTotalRev = intent.getStringExtra("hotelTotalReview") ?: "0"
            val imageurl = intent.getStringExtra("imageurl")

            // Validar datos requeridos
            if (hotelId == null || hotelName == null || hotelRegion == null) {
                Toast.makeText(this, "Error: Datos del hotel incompletos", Toast.LENGTH_SHORT).show()
                return
            }

            // Si imageurl es nulo, usar una URL por defecto
            val finalImageUrl = imageurl ?: "https://via.placeholder.com/150"

            // Create a new Hotel object
            val hotel = Hotel(
                hotelId,
                hotelName,
                hotelRegion,
                finalImageUrl,
                hotelPrice ?: "0",
                false,
                true,
                hotelRev,
                hotelTotalRev,
                hotelNumDay
            )

            // Referencia a las reservaciones del usuario
            val reservationsRef = database.getReference("reservations/$userId")
            
            // Guardar con listener para detectar éxito o fracaso
            reservationsRef.child(hotelId).setValue(hotel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reservación guardada exitosamente", Toast.LENGTH_SHORT).show()
                    println("Reservación guardada: $hotelId para usuario $userId")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("Error al guardar reservación: ${e.message}")
                }
                
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}