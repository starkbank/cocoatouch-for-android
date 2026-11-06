package com.starkbank.cocoatouch.uikit


internal class ActivityPickers(
    val image: UIImagePickerController,
    val document: UIDocumentPickerViewController
) {

    fun release() {
        image.release()
        document.release()
    }
}
