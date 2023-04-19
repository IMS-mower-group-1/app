package se.ju.student.robomow.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.ju.student.robomow.api.RoboMowApi
import se.ju.student.robomow.model.MowSession
import javax.inject.Inject
@HiltViewModel
class MowSessionsViewModel @Inject constructor(
    private val roboMowApi: RoboMowApi
) : ViewModel() {
    private val _mowSessions = MutableLiveData<List<MowSession>>()
    val mowSessions: LiveData<List<MowSession>> get() = _mowSessions


    fun getMowSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = roboMowApi.getMowSessions("aBPovOQznCxzNHE0Uo97")
            if (response.isSuccessful){
                Log.d("Response body:", response.body().toString())
                val sessions = response.body()
                if (response.body() is List<MowSession>){
                    _mowSessions.postValue(sessions)
                }
            } else {
                Log.e("API Request:", response.errorBody().toString())
            }
        }
    }
    init {
        getMowSessions()
    }
}