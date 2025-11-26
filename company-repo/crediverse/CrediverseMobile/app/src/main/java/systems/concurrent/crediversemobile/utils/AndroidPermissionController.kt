package systems.concurrent.crediversemobile.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.services.Dialog
import systems.concurrent.crediversemobile.services.DialogType

class AndroidPermissionController(
    private val activity: Activity,
    private val permissions: List<Permission>
) {

    companion object {
        private val grantedPermissions = mutableListOf<Permission>()

        fun isGranted(permission: String) =
            grantedPermissions.find { it.toString() == permission } != null
    }

    data class Permission(
        private val permission: String,
        private val required: Boolean,
        private val titleForPermissionRequestResource: Int,
        private val reasonForPermissionRequestResource: Int
    ) {
        override fun toString() = permission
        fun isRequired() = required
        fun getReasonTitleResource() = titleForPermissionRequestResource
        fun getReasonResource() = reasonForPermissionRequestResource
    }

    private val mostRecentRequestedPermissions = mutableListOf<String>()

    fun initialiseAfterActivity() {
        permissions.forEach { permission ->
            if (isGrantedByAndroid(permission)) {
                grantedPermissions.add(permission)
            }
        }
    }

    fun onRequestPermissionsResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var hasOutstandingRequiredPermissions = false
        permissions.forEachIndexed { index, _permission ->
            val permission = this.permissions.find { it.toString() == _permission } ?: return
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            } else {
                grantedPermissions.remove(permission)
                if (!shouldShowRequestPermissionRationale(
                        activity,
                        _permission
                    ) && permission.isRequired()
                ) {
                    // "Never ask again" was selected, take us to appSettings
                    hasOutstandingRequiredPermissions = true
                } else if (permission.isRequired()) {
                    // "Deny" was selected, show dialog ... and ask again
                    showPermissionsDialog()
                }
            }
        }

        if (hasOutstandingRequiredPermissions) {
            showPermissionsDialog(fromAppSettings = true)
        }
    }

    private fun showPermissionsDialog(fromAppSettings: Boolean = false) {

        var dialogMessage: CharSequence = ""

        mostRecentRequestedPermissions.forEach { permissionString ->
            if (grantedPermissions.toString() != permissionString) {
                // not granted
                val missingPermission = permissions.find { it.toString() == permissionString }
                if (missingPermission != null) {
                    dialogMessage = Formatter.combine(
                        dialogMessage,
                        Formatter.bold(activity.getString(missingPermission.getReasonTitleResource()) + ":\n"),
                        Formatter.normal(activity.getString(missingPermission.getReasonResource()) + "\n\n"),
                    )
                }
            }
        }

        val dialog = Dialog(
            activity, DialogType.CONFIRM,
            activity.getString(R.string.permissions_needed), dialogMessage
        )

        dialog.onConfirm {
            if (fromAppSettings) {
                // Only if "Never Ask Again" was selected --- but we can't proceed without these
                //  So we have to take them to the app permissions
                //  because we can't bring up the permission dialog anymore
                askRequiredPermissionsFromAppSettings()
            } else {
                // NOTE:
                //      We honour settings that we do not require.
                //      So this specific request will only happen for `required` permissions
                requestPermissions()
            }
        }
        dialog.show()
    }

    private fun askRequiredPermissionsFromAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    fun isMissingRequiredPermissions(): Boolean {
        return permissions.any { it.isRequired() && !grantedPermissions.contains(it) }
    }

    private fun isGrantedByAndroid(permission: Permission): Boolean {
        return ContextCompat.checkSelfPermission(
            activity, permission.toString()
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        mostRecentRequestedPermissions.removeAll { true }
        val permissionsToRequest = permissions.filter { !grantedPermissions.contains(it) }

        // Already have all permissions? --- don't ask anything
        if (permissionsToRequest.isEmpty()) return

        if (isMissingRequiredPermissions()) {
            permissionsToRequest.forEach { mostRecentRequestedPermissions.add(it.toString()) }
            // ask for EVERYTHING
            ActivityCompat.requestPermissions(
                activity, permissionsToRequest.map { it.toString() }.toTypedArray(), 1
            )
        } else {
            // The permissions we want are not required...
            //  Ask AT LEAST once for those we have not denied

            permissionsToRequest.forEach {
                val permissionString = it.toString()
                val neverDeniedThisPermission =
                    !shouldShowRequestPermissionRationale(activity, permissionString)
                if (neverDeniedThisPermission) mostRecentRequestedPermissions.add(permissionString)
            }

            if (mostRecentRequestedPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity, permissionsToRequest.map { it.toString() }.toTypedArray(), 1
                )
            }
        }
    }
}
