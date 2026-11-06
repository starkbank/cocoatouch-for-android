package com.starkbank.cocoatouch.uikit

import com.starkbank.cocoatouch.foundation.NSIndexPath

interface UICollectionViewDataSource {
    fun collectionViewCellForItemAt(collectionView: UICollectionView, indexPath: NSIndexPath): UICollectionViewCell
    fun collectionViewNumberOfItemsInSection(collectionView: UICollectionView, section: Int): Int
    fun numberOfSections(collectionView: UICollectionView): Int { return 1 }

}