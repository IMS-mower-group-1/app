package se.ju.student.robomow.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

interface PermissionCallback {
    fun onPermissionMissing()
}
class PermissionBroadcastReceiver(private val permissionCallback: PermissionCallback) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        permissionCallback.onPermissionMissing()
    }
}