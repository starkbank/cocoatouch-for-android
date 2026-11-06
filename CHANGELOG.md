# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html):
MAJOR for incompatible API changes, MINOR for backwards-compatible additions and
PATCH for backwards-compatible fixes.

## [Unreleased]

### Added
- `UIApplication`
- `UIViewController`
- `UINavigationController`
- `UITabBarController`
- `UIStoryboard`
- `UIView`
- `UILabel`
- `UIButton`
- `UISwitch`
- `UISegmentedControl`
- `UITextField`
- `UISearchBar` and `UISearchBarDelegate`
- `UITableView`
- `UICollectionView`
- `UIRefreshControl`
- `UIImageView`
- `UIImagePickerController`
- `UIDocumentPickerViewController`
- `UIAppearance`
- `UITraitCollection`
- `WKWebView`
- `NSArray`, `NSMutableArray` and `NSIndexPath`
- `NotificationCenter`
- `UserDefaults`
- `DispatchGroup`
- `@InternalCocoaTouchApi`
- `UIDefaultAppearance`, new in relation to the Bank copy
- `UIUserInterfaceStyle`, new in relation to the Bank copy
- `UIApplication.appearance`, new in relation to the Bank copy
- `UIActivity.prepareLaunch()`, new in relation to the Bank copy
- `tabContainerId()`, new in relation to the Bank copy
- `UITabBarController.popInTab()`, new in relation to the Bank copy
- `UINavigationController.viewControllers`, new in relation to the Bank copy

### Changed
Relative to the Bank copy:
- Attach and detach of `UIApplication` (Task 5).
- `UIApplication.activity`/`storyboard`, `UIImagePickerController.shared` and `UIDocumentPickerViewController.shared` are filled by the library on attach and are read-only for apps, which delete their own assignments (in the Bank, `Bapp src/main/java/com/starkbank/android/MainStoryboard.kt:106-107,112-113`) (Tasks 5 and 16).
- `prepareLaunch()` runs before `super.onCreate` (Task 5).
- Navigation after destroy is ignored (Task 5).
- Defaults come from `UIAppearance` (Task 7).
- One watcher and a silent `setText` (Task 8).
- `popToRootViewController` (Task 9).
- The paused queue (Task 10).
- `viewDidDisappear` (Task 11).
- Inherited bindings, the `@IBAction` sender is the clicked widget, and `IllegalStateException` with the field name for a missing outlet (Task 13).
- `removeObject` removes all occurrences (Task 14; in the Bank the only `NSMutableArray.removeObject` is `Bapp src/main/java/com/starkbank/android/components/BaseModalView/BottomNavigationController.kt:62`, a case for the migration plan; the other 7 `.removeObject(` calls in the app are `UserDefaults.removeObject(key)` and do not change).
- `DispatchGroup.notify` delivers asynchronously on the requested queue, `DispatchGroup.notify` fires on an empty or already completed group, each block runs once and is discarded (it does not fire again on the next cycle), and `leave()` without `enter()` throws `IllegalStateException` (Task 15). Consequence for migrating apps: a `notify` registered before the first `enter` fires immediately and does not wait for the next balance, as in GCD. In the Bank three screens do this and must register the `notify` after the presenter calls, as `CorporatePurchaseSeriesViewController.kt:104` already does: `CardHolderDetailViewController.kt:108`, `SpendingCounterViewController.kt:64` and `PurchaseDetailViewController.kt:108` (review of 2026-11-06; a case for the migration plan).
- Back on the root reaches the root controller (Task 3).
- `initApplication` is protected.
- Resources are prefixed with `cocoatouch_` and `@id/cocoatouch_tab_container`.
- The section header font coming from `UIAppearance` also applies `textSize` (B only swapped the typeface; the Bank passes `UIFont(R.font.pretendard, 14f)` to keep the default `TextView` size).
- An outlet bound before the controller has a view throws `IllegalStateException` "bound before <Controller> has a view", distinct from "not found in <Controller>" (Task 13).
- `@IBAction` is validated at bind (exactly one sender parameter; a concrete sender type with a public no-argument constructor; an existing view id), each failure with an `IllegalStateException` naming the controller, method and id, and the exception thrown by the app's handler propagates instead of being printed. Handlers that throw today in the Bank will now crash the app visibly (final round F2).
- `@IBAction` back handlers are validated at bind and must receive a `UIButton`.
- A sender parameter type that is not a `UIResponder` must receive a `UIView`.
- A same-id `@IBAction` declared in a base class and in a subclass binds the subclass handler.
- A push for a stopped activity is queued before the attached-activity guards, so it survives a finishing activity in front.
- Back without a handler finishes the Activity that dispatched it (`finish()` on `this`, Task 5).
- The paused queue is drained in `onStart`, after the reattach, and no longer in `onRestart` (final round F1).
- For those who used P's `0.0.1`: package `compability` is now `compatibility`, `UITableView` requires a `RecyclerView` in the layout (a `ListView` breaks) and `UIApplication.activity`/`storyboard` require the opt-in.

### Removed
- `UIDatePicker`
- `DocumentScannerController`
- `UIImageView.load`
- `UserDefaults.set<T>`/`object` (the app encodes and calls `set(string, key)`; the persisted format does not change)
- `UISearchBar.searchController`
- `installSplashScreen` (now `prepareLaunch()`)
- Cursor tint and handles of `UISearchBar` below API 29 (Task 6)

### Known issues
- CT-06 (`ct06PopCallsViewDidLoadAgainOnRoot`)
- CT-02 (`ct02SetTitleForDisabledOverwritesNormal`)
- CT-03 (`ct03PresentedControllerJoinsThePushStack`)
- CT-04 (`ct04ShouldChangeCannotVeto`)
- CT-07 (`ct07WillDisplaySkipsRowsBeforeTheLastVisible`)
- CT-12 (`ct12TagIsAStringNotAnInt`)
- CT-16 (`ct16RecreateKeepsOnlyTheInitialController`)
- CT-17 (`ct17ProgressViewAndScrollViewDeclareNoMembers`)
- The default `UIViewController` title is `javaClass.simpleName` (`B uikit/UIViewController.kt:13`), which the consumer's R8 obfuscates when the app does not set `title`.
- `presentViewController` of a controller created without a storyboard (outside `instantiateViewControllerWithIdentifier`) throws an NPE in `presentModalViewController` (`viewController.storyboard!!`; seen in Task 12, 2026-11-06).
- `UIApplication` reattaches only on `onStart`: if the attached `UIActivity` is destroyed while another one is already STARTED (translucent on top, multi-window), the survivor has no `UIApplication.activity` until it restarts, and each reattach registers extra launchers on it (Task 5; 1.1.0 brings per-Activity pickers).
- `popToRootViewController` also empties the pushes made inside a tab of a root `UITabBarController`, because `popBackStack(null, INCLUSIVE)` clears the whole back stack of the Activity (the Bank does not use it: the AnimatedTabBarController uses `childFragmentManager` without a back stack; final review F7, 2026-11-06).
- After a push queued while the app is in the background, the fragments already on screen receive `viewDidAppear` without `viewWillAppear` on return (the drain runs in `onStart` after `super.onStart`, and the queue no longer turns `isNotificationsEnabled` on when queueing; same family as CT-06; final review F15, 2026-11-06).
