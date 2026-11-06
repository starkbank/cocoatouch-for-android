package com.starkbank.cocoatouch.uikit

import com.starkbank.cocoatouch.foundation.NSIndexPath

interface UICollectionViewDelegate {
    fun collectionViewDidSelectItemAt(collectionView: UICollectionView, indexPath: NSIndexPath) {}
    fun collectionViewLayoutSizeForItem(collectionView: UICollectionView, sizeForItemAt: NSIndexPath) : CGSize {
        return CGSize(0,0)
    }
}