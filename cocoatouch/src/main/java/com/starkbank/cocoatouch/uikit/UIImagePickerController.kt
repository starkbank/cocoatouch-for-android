package com.starkbank.cocoatouch.uikit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File


interface UIImagePickerControllerDelegate {
    fun imagePickerControllerDidFinish(
        imagePickerController: UIImagePickerController,
        info: Map<UIImagePickerController.InfoKey, UIImage>
    )
}

class UIImagePickerController {

    enum class InfoKey {
        originalImage
    }

    enum class SourceType {
        photoLibrary,
        camera
    }

    companion object {
        lateinit var shared: UIImagePickerController
            internal set
    }

    private var selectImageFromGalleryResult: ActivityResultLauncher<String>
    private var takePictureFromCameraResult: ActivityResultLauncher<Uri>
    private var permissionRequestResult: ActivityResultLauncher<String>
    private var latestTmpUri: Uri? = null

    var delegate: UIImagePickerControllerDelegate? = null
    var sourceType: SourceType = SourceType.photoLibrary

    init {
        val activity = UIApplication.activity()
        selectImageFromGalleryResult =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    encodeAndSendImage(uri)
                }
            }

        takePictureFromCameraResult =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
                if (isSuccess) {
                    latestTmpUri?.let { uri ->
                        encodeAndSendImage(uri)
                    }
                }
            }

        permissionRequestResult =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    getPicture()
                }
            }
    }

    fun present() {
        when (sourceType) {
            SourceType.photoLibrary -> getImage()
            SourceType.camera -> getPicture()
        }
    }

    private fun getImage() {
        selectImageFromGalleryResult.launch("image/*")
    }

    private fun getPicture() {
        val activity = UIApplication.activity()
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            permissionRequestResult.launch(Manifest.permission.CAMERA)
            return
        }
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takePictureFromCameraResult.launch(uri)
        }
    }

    private fun encodeAndSendImage(uri: Uri) {
        delegate?.imagePickerControllerDidFinish(
            this,
            mapOf(
                InfoKey.originalImage to UIImage(uri = uri)
            )
        )
    }

    private fun getTmpFileUri(): Uri {
        return tmpFileUri(UIApplication.applicationContext!!)
    }

}

internal fun fileProviderAuthority(context: Context): String {
    return "${context.packageName}.provider"
}

internal fun tmpFileUri(context: Context): Uri {
    val tmpFile = File.createTempFile("tmp_image_file", ".jpeg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        fileProviderAuthority(context),
        tmpFile
    )
}