package com.bigbang.myplacecompass.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bigbang.myplacecompass.R
import com.bigbang.myplacecompass.model.data.Result
import com.bigbang.myplacecompass.ui.PlaceImagesFragment
import com.bigbang.myplacecompass.ui.PlaceImagesFragment.Companion.PLACE_KEY
import com.bigbang.myplacecompass.ui.adapter.PlaceAdapter
import com.bigbang.myplacecompass.util.PlaceLocationListener
import com.bigbang.myplacecompass.viewmodel.CompassVMFactory
import com.bigbang.myplacecompass.viewmodel.CompassViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback, PopupMenu.OnMenuItemClickListener,
    PlaceLocationListener.LocationDelegate, PlaceAdapter.PlaceClickListener {

    private lateinit var mMap: GoogleMap

    private val placeAdapter: PlaceAdapter = PlaceAdapter(mutableListOf(), this)

    private val placeImageFragment: PlaceImagesFragment = PlaceImagesFragment()

    private val placeLocationListener: PlaceLocationListener = PlaceLocationListener(this)
    private val compassViewModel: CompassViewModel by viewModels<CompassViewModel>(
        factoryProducer = { CompassVMFactory() })

    lateinit var placeObserver: Observer<List<Result>>

    private fun displayResults(resultList: List<Result>?) {
        Log.d("TAG_X", "${resultList?.size}")
        resultList?.let { results ->
            placeAdapter.placeList = resultList
            placeAdapter.notifyDataSetChanged()

            drawOnMap(results)
        }
    }

    private fun drawOnMap(results: List<Result>) {

        results.forEach { placeItem ->

            val latLng = LatLng(placeItem.geometry.location.lat, placeItem.geometry.location.lng)
            mMap.addMarker(
                MarkerOptions().position(latLng).title(placeItem.name).icon(
                    BitmapDescriptorFactory.defaultMarker(150f)
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        recyclerView.adapter = placeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrollToPosition((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                }
            }
        })
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        map_menu_imageview.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.place_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        my_location_imageview.setOnClickListener {
            setLocation(placeLocationListener.locationLatLng)
        }

        placeObserver = Observer<List<Result>> { resultList ->
            displayResults(resultList)
        }
        compassViewModel.placesMutableData.observe(this, placeObserver)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            5f,
            placeLocationListener
        )
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

    }

    fun scrollToPosition(position: Int) {
        placeAdapter.placeList[position].let {
            val latLng = LatLng(it.geometry.location.lat, it.geometry.location.lng)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val placeType = when (item.itemId) {
            R.id.zoo_item -> "zoo"
            R.id.hospital_item -> "hospital"
            R.id.laundry_item -> "laundry"
            R.id.school_item -> "school"
            R.id.park_item -> "park"
            R.id.police_item -> "police"
            R.id.cafe_item -> "cafe"
            R.id.gym_item -> "gym"
            else -> ""
        }
        compassViewModel.getGetNearbyPlaces(placeLocationListener.locationString, 10000, placeType)
        return true
    }


    override fun setLocation(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.addMarker(
            MarkerOptions().position(currentLocation).title("This is you!").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.me_icon)
            )
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

    }

    override fun selectPlace(place: Result) {
        supportFragmentManager.beginTransaction()
            .add(R.id.place_frame, placeImageFragment.also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putSerializable(PLACE_KEY, place)
                }
            })
            .addToBackStack(placeImageFragment.tag)
            .commit()
        supportFragmentManager.executePendingTransactions()
    }

}
