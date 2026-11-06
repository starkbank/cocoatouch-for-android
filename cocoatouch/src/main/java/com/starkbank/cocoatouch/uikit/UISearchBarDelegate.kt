package com.starkbank.cocoatouch.uikit

import com.starkbank.cocoatouch.annotation.Optional

interface UISearchBarDelegate {
    @Optional fun searchBarShouldBeginEditing(searchBar: UISearchBar): Boolean { return true }
}
