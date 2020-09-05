package com.goga133.fintech2020.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goga133.fintech2020.models.Card
import com.goga133.fintech2020.models.Event
import com.goga133.fintech2020.models.RequestCard
import com.goga133.fintech2020.models.Section
import com.goga133.fintech2020.network.NetworkApi
import com.goga133.fintech2020.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()

    private val _randomCards = MutableLiveData<Event<Card>>()
    private val _latestCards = MutableLiveData<Event<RequestCard>>()
    private val _hotCards = MutableLiveData<Event<RequestCard>>()
    private val _topCards = MutableLiveData<Event<RequestCard>>()

    val randomCardsLiveData: LiveData<Event<Card>> = _randomCards
    val latestCardsLiveData: LiveData<Event<RequestCard>> = _latestCards
    val hotCardsLiveData: LiveData<Event<RequestCard>> = _hotCards
    val topCardsLiveData: LiveData<Event<RequestCard>> = _topCards

    fun getCards(section: Section, pageId: Int = 1) {
        request(
            when (section) {
                Section.RANDOM -> throw IllegalArgumentException("Random недоступен для получения карточек.")
                Section.LATEST -> _latestCards
                Section.HOT -> _hotCards
                Section.TOP -> _topCards
            }
        ) {
            // Converts an enumeration element to a string with lowercase letters.
            // Example: LATEST el. => "latest"
             api.getCards(section.name.toLowerCase(Locale.getDefault()), pageId)
        }
    }

    fun getRandomCard(){
        request(_randomCards){
            api.getRandomCard()
        }
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    private var api: NetworkApi = NetworkService.retrofitService()

    // Загрузка данных
    private fun <T> request(
        liveData: MutableLiveData<Event<T>>,
        request: suspend () -> Call<T>
    ) {

        liveData.postValue(Event.loading())

        this.viewModelScope.launch(Dispatchers.IO) {
            try {
                request.invoke().enqueue(object : Callback<T> {
                    override fun onFailure(call: Call<T>, t: Throwable) {
                        t.printStackTrace()
                        liveData.postValue(Event.error(t))
                    }

                    override fun onResponse(
                        call: Call<T>,
                        response: Response<T>
                    ) {
                        if (response.isSuccessful) {
                            liveData.postValue(Event.success(response.body()))
                        }
                        else {
                            liveData.postValue(Event.error(Throwable(response.errorBody()?.string())))
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                liveData.postValue(Event.error(null))
            }
        }
    }
}