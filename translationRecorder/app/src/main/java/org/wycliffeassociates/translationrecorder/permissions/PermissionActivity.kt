package org.wycliffeassociates.translationrecorder.permissions

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.concurrent.atomic.AtomicBoolean

abstract class PermissionActivity : AppCompatActivity() {

    protected val requestingPermission = AtomicBoolean(false)

    // Opens an activity to guide the user to enabling settings from the settings app
    // this is called when the user has checked "never ask again"
    fun presentPermissionDialog() {
        startActivity(Intent(this, PermissionsDialogActivity::class.java))
    }

    /**
     * This method should replace onResume for activities that subclass PermissionActivity.
     *
     * This is due to needing a callback in onResume before safely executing further code.
     */
    protected abstract fun onPermissionsAccepted()

    override fun onResume() {
        super.onResume()
        requestingPermission.set(true)
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(
                        object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                if (report.areAllPermissionsGranted()) {
                                    requestingPermission.set(false)
                                    onPermissionsAccepted()
                                } else {
                                    requestingPermission.set(false)
                                    presentPermissionDialog()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                    permissions: List<PermissionRequest>,
                                    token: PermissionToken)
                            {
                                token.continuePermissionRequest()
                            }
                        }
                ).check()
    }
}