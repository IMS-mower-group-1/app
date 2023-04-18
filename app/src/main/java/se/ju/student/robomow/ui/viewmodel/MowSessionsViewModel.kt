package se.ju.student.robomow.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
}