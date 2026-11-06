package com.starkbank.cocoatouch.uikit

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


interface UIDocumentPickerDelegate {
    fun documentPicker(controller: UIDocumentPickerViewController, uri: Uri)
}

class UIDocumentPickerViewController internal constructor(activity: UIActivity) {

    constructor() : this(UIApplication.activity())

    enum class ContentTypes {
        pdf, imageOrPdf, photo
    }

    companion object {
        lateinit var shared: UIDocumentPickerViewController
            internal set
    }

    var delegate: UIDocumentPickerDelegate? = null
    var contentTypes = ContentTypes.pdf

    private var selectPdfFromDocumentsResult: ActivityResultLauncher<String>?
    private var selectPhotoResult: ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>?
    private var selectImageOrPdfResult: ActivityResultLauncher<Array<String>>?
    private var released = false

    init {
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
        check(!released) {
            "UIDocumentPickerViewController.shared belongs to a destroyed activity; " +
                "read UIDocumentPickerViewController.shared again"
        }
        getPdf()
    }

    internal fun release() {
        selectPdfFromDocumentsResult?.unregister()
        selectPhotoResult?.unregister()
        selectImageOrPdfResult?.unregister()
        selectPdfFromDocumentsResult = null
        selectPhotoResult = null
        selectImageOrPdfResult = null
        delegate = null
        released = true
    }

    private fun getPdf() {
        when(contentTypes){
            ContentTypes.photo -> selectPhotoResult?.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            ContentTypes.pdf -> selectPdfFromDocumentsResult?.launch("application/pdf")
            ContentTypes.imageOrPdf -> selectImageOrPdfResult?.launch(arrayOf("image/*", "application/pdf"))
        }
    }

    private fun encodePdf(uri: Uri) {
        delegate?.documentPicker(this, uri)
    }
}