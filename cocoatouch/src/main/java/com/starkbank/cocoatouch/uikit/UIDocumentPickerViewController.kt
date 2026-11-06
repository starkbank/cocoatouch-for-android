package com.starkbank.cocoatouch.uikit

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


interface UIDocumentPickerDelegate {
    fun documentPicker(controller: UIDocumentPickerViewController, uri: Uri)
}

class UIDocumentPickerViewController {

    enum class ContentTypes {
        pdf, imageOrPdf, photo
    }

    companion object {
        lateinit var shared: UIDocumentPickerViewController
            internal set
    }

    var delegate: UIDocumentPickerDelegate? = null
    var contentTypes = ContentTypes.pdf

    private var selectPdfFromDocumentsResult: ActivityResultLauncher<String>
    private var selectPhotoResult: ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>
    private var selectImageOrPdfResult: ActivityResultLauncher<Array<String>>

    init {
        val activity = UIApplication.activity()
        selectPhotoResult = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { encodePdf(it) }
        }
        selectImageOrPdfResult = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { encodePdf(it) }
        }
        selectPdfFromDocumentsResult =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    encodePdf(it)
                }
            }
    }

    fun present() {
        getPdf()
    }

    private fun getPdf() {
        when(contentTypes){
            ContentTypes.photo -> selectPhotoResult.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            ContentTypes.pdf -> selectPdfFromDocumentsResult.launch("application/pdf")
            ContentTypes.imageOrPdf -> selectImageOrPdfResult.launch(arrayOf("image/*", "application/pdf"))
        }
    }

    private fun encodePdf(uri: Uri) {
        delegate?.documentPicker(this, uri)
    }
}