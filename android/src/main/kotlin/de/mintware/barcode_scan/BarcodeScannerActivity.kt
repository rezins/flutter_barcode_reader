package de.mintware.barcode_scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class BarcodeScannerActivity : Activity(), ZXingScannerView.ResultHandler {

    lateinit var scannerView: me.dm7.barcodescanner.zxing.ZXingScannerView
    var autoFocus = true
    var zoom = ZOOM_2X

    companion object {
        val REQUEST_TAKE_PHOTO_CAMERA_PERMISSION = 100
        val TOGGLE_FLASH = 200
        val AUTO_FOCUS = 300
        val ZOOM = 400
        val ZOOM_1X = 1.0
        val ZOOM_2X = 2.0
        val ZOOM_4X = 4.0

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        scannerView = ZXingScannerView(this)
        scannerView.setAutoFocus(autoFocus)
        // this paramter will make your HUAWEI phone works great!
        scannerView.setAspectTolerance(0.5f)
        setContentView(scannerView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (zoom == ZOOM_4X) {
            val item = menu.add(0,
                    ZOOM, 0, "4x")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else  if (zoom == ZOOM_2X) {
            val item = menu.add(0,
                    ZOOM, 0, "2x")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else {
            val item = menu.add(0,
                    ZOOM, 0, "1x")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        if (scannerView.flash) {
            val item = menu.add(0,
                    TOGGLE_FLASH, 1, "Flash Off")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else {
            val item = menu.add(0,
                    TOGGLE_FLASH, 1, "Flash On")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        if (autoFocus) {
            val item = menu.add(0,
                    AUTO_FOCUS, 2, "Auto Focus Off")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else {
            val item = menu.add(0,
                    AUTO_FOCUS, 2, "Auto Focus On")
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == ZOOM) {
            zoom = if (zoom == ZOOM_1X) ZOOM_2X else if (zoom == ZOOM_2X) ZOOM_4X else ZOOM_1X
            scannerView.zoom = zoom
            this.invalidateOptionsMenu()
            return true
        }
        if (item.itemId == TOGGLE_FLASH) {
            scannerView.flash = !scannerView.flash
            this.invalidateOptionsMenu()
            return true
        }
        if (item.itemId == AUTO_FOCUS) {
            autoFocus = !autoFocus
            scannerView.setAutoFocus(autoFocus)
            this.invalidateOptionsMenu()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        // start camera immediately if permission is already given
        if (!requestCameraAccessIfNecessary()) {
            scannerView.zoom = zoom
            scannerView.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", result.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun finishWithError(errorCode: String) {
        val intent = Intent()
        intent.putExtra("ERROR_CODE", errorCode)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun requestCameraAccessIfNecessary(): Boolean {
        val array = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat
                .checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, array,
                    REQUEST_TAKE_PHOTO_CAMERA_PERMISSION)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO_CAMERA_PERMISSION -> {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    scannerView.zoom = zoom
                    scannerView.startCamera()
                } else {
                    finishWithError("PERMISSION_NOT_GRANTED")
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}

object PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].

     * @see Activity.onRequestPermissionsResult
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.size < 1) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
