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

class UIImagePickerController internal constructor(activity: UIActivity) {

    constructor() : this(UIApplication.activity())

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

    private var owner: UIActivity? = activity
    private var selectImageFromGalleryResult: ActivityResultLauncher<String>?
    private var takePictureFromCameraResult: ActivityResultLauncher<Uri>?
    private var permissionRequestResult: ActivityResultLauncher<String>?
    internal var latestTmpUri: Uri? = null
    internal var latestTmpFile: File? = null
    private var released = false

    var delegate: UIImagePickerControllerDelegate? = null
    var sourceType: SourceType = SourceType.photoLibrary

    init {
        selectImageFromGalleryResult =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    encodeAndSendImage(uri)
                }
            }

        takePictureFromCameraResult =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
                onCaptureResult(isSuccess)
            }

        permissionRequestResult =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    getPicture()
                }
            }
    }

    fun present() {
        check(!released) {
            "UIImagePickerController.shared belongs to a destroyed activity; read UIImagePickerController.shared again"
        }
        when (sourceType) {
            SourceType.photoLibrary -> getImage()
            SourceType.camera -> getPicture()
        }
    }

    internal fun onCaptureResult(isSuccess: Boolean) {
        if (!isSuccess) {
            discardCapture(latestTmpFile)
            latestTmpFile = null
            latestTmpUri = null
            return
        }
        latestTmpUri?.let { uri ->
            encodeAndSendImage(uri)
        }
    }

    internal fun release() {
        selectImageFromGalleryResult?.unregister()
        takePictureFromCameraResult?.unregister()
        permissionRequestResult?.unregister()
        selectImageFromGalleryResult = null
        takePictureFromCameraResult = null
        permissionRequestResult = null
        owner = null
        delegate = null
        released = true
    }

    private fun getImage() {
        selectImageFromGalleryResult?.launch("image/*")
    }

    private fun getPicture() {
        val activity = owner ?: return
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            permissionRequestResult?.launch(Manifest.permission.CAMERA)
            return
        }
        val context = UIApplication.applicationContext!!
        val file = captureFile(context)
        val uri = tmpFileUri(context, file)
        latestTmpFile = file
        latestTmpUri = uri
        takePictureFromCameraResult?.launch(uri)
    }

    private fun encodeAndSendImage(uri: Uri) {
        delegate?.imagePickerControllerDidFinish(
            this,
            mapOf(
                InfoKey.originalImage to UIImage(uri = uri)
            )
        )
    }

}

internal fun fileProviderAuthority(context: Context): String {
    return "${context.packageName}.provider"
}

// Captures are often identity documents and deleteOnExit never runs on Android, and the picker that wrote the
// previous file may be gone with its activity, so the cache directory is swept by prefix to keep one capture per app
internal fun captureFile(context: Context): File {
    context.cacheDir.listFiles { file -> file.name.startsWith("tmp_image_file") }?.forEach { it.delete() }
    return File.createTempFile("tmp_image_file", ".jpeg", context.cacheDir)
}

internal fun discardCapture(file: File?) {
    file?.delete()
}

internal fun tmpFileUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        fileProviderAuthority(context),
        file
    )
}
