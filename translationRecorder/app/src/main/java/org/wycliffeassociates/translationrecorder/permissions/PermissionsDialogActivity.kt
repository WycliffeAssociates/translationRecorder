package org.wycliffeassociates.translationrecorder.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.wycliffeassociates.translationrecorder.R
import org.wycliffeassociates.translationrecorder.login.Screen
import android.provider.Settings

private const val PERMISSIONS_PAGE = 50

class PermissionsDialogActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onResume() {
        super.onResume()
        Screen.lockOrientation(this)
        createPermissionDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PERMISSIONS_PAGE) {
            finish()
        }
    }

    private fun createPermissionDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.permissions_denied_title)
                .setMessage(R.string.permissions_denied_message)
                .setPositiveButton(R.string.open_permissions)
                {
                    _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    this@PermissionsDialogActivity.startActivityForResult(intent, PERMISSIONS_PAGE)
                }
                .setCancelable(false)
                .setView(R.layout.dialog_permissions_guide)
                .create()
                .show()

    }
}