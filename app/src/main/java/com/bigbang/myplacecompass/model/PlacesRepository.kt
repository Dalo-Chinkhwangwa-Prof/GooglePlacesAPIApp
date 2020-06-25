package com.bigbang.myplacecompass.model

import com.bigbang.myplacecompass.model.data.PlacesResponse
import com.bigbang.myplacecompass.model.network.PlacesRetrofitInstance
import io.reactivex.Observable

object PlacesRepository {

    fun getPlacesNearby(userLocation: String, radius: Int, type: String) : Observable<PlacesResponse> {
        return PlacesRetrofitInstance.getPlaces(userLocation, radius, type)
    }

}