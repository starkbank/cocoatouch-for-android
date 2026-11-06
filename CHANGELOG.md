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
- `UIDefaultAppearance`
- `UIUserInterfaceStyle`
- `UIApplication.appearance`
- `UIActivity.prepareLaunch()`
- `tabContainerId()`
- `UITabBarController.popInTab()`
- `UINavigationController.viewControllers`

### Changed
Compared with 0.0.1, the only public predecessor, the core now matches the framework the Stark apps ship, with these behaviour changes:
- `UIApplication` attaches to the live activity on start and releases it on destroy. `UIApplication.activity`/`storyboard`, `UIImagePickerController.shared` and `UIDocumentPickerViewController.shared` are filled by the library and read-only for apps, which must delete their own assignments.
- `UIActivity.prepareLaunch()` runs before `super.onCreate`.
- Navigation requested after the activity is destroyed is ignored.
- Default styles come from `UIAppearance`.
- A text field keeps a single watcher, and `setText` does not notify it.
- `popToRootViewController` now removes every pushed controller from the back stack.
- Paused navigation transitions are drained once, in `onStart` after the activity re-attaches, and no longer in `onRestart`.
- `viewDidDisappear` is forwarded to child view controllers.
- `@IBOutlet` and `@IBAction` bindings declared in base classes are inherited.
- The `@IBAction` sender is the widget that was clicked.
- A missing outlet throws `IllegalStateException` naming the field.
- An outlet bound before the controller has a view throws `IllegalStateException` with "bound before <Controller> has a view", distinct from "not found in <Controller>".
- `@IBAction` is validated at bind: exactly one sender parameter, a concrete sender type with a public no-argument constructor, and an existing view id. Each failure throws `IllegalStateException` naming the controller, method and id.
- An exception thrown by an app's `@IBAction` handler propagates instead of being printed, so a throwing handler now crashes the app visibly.
- `@IBAction` back handlers are validated at bind and must receive a `UIButton`.
- A sender parameter type that is not a `UIResponder` must receive a `UIView`.
- A same-id `@IBAction` declared in a base class and in a subclass binds the subclass handler.
- A push requested while the activity is stopped is queued before the attached-activity guards, so it survives a finishing activity in front.
- Back on the root reaches the root controller.
- Back without a handler finishes the Activity that dispatched it.
- `NSMutableArray.removeObject` removes every occurrence.
- `DispatchGroup.notify` delivers asynchronously on the requested queue, and fires on an empty or already completed group.
- Each `DispatchGroup.notify` block runs once and is discarded, so it does not fire again on the next cycle.
- `DispatchGroup.leave()` without `enter()` throws `IllegalStateException`.
- A `notify` registered before the first `enter` fires immediately and does not wait for the next balance, as in GCD.
- The section header font coming from `UIAppearance` also applies `textSize`; before, only the typeface changed.
- `initApplication` is protected.
- Resources are prefixed with `cocoatouch_`, and tab layouts use `@id/cocoatouch_tab_container`.
- The package `compability` is now `compatibility`.
- `UITableView` requires a `RecyclerView` in the layout; a `ListView` breaks.
- `UIApplication.activity` and `storyboard` require the `@InternalCocoaTouchApi` opt-in.

### Removed
- `UIDatePicker`
- `DocumentScannerController`
- `UIImageView.load`
- `UserDefaults.set<T>`/`object`: the app encodes the value and calls `set(string, key)`; the persisted format does not change
- `UISearchBar.searchController`
- `installSplashScreen`, replaced by `prepareLaunch()`
- Cursor tint and handles of `UISearchBar` below API 29

### Known issues
Each one is pinned by a test that fails when the behaviour changes.
- CT-06: popping back to the root calls `viewDidLoad` on it again (`ct06PopCallsViewDidLoadAgainOnRoot`).
- CT-02: `setTitle` for the Disabled state overwrites the Normal title (`ct02SetTitleForDisabledOverwritesNormal`).
- CT-03: a controller passed to `presentViewController` joins the push stack instead of a separate modal stack (`ct03PresentedControllerJoinsThePushStack`).
- CT-04: `textFieldShouldChange` returns `Unit`, so a delegate cannot veto an edit (`ct04ShouldChangeCannotVeto`).
- CT-07: `tableViewWillDisplayForRowAt` fires only for the last visible row, so earlier rows are skipped (`ct07WillDisplaySkipsRowsBeforeTheLastVisible`).
- CT-12: `UIView.tag` is a `String`, where UIKit has an `Int` (`ct12TagIsAStringNotAnInt`).
- CT-16: after the activity is recreated, the navigation controller holds only the initial controller (`ct16RecreateKeepsOnlyTheInitialController`).
- CT-17: `UIProgressView` and `UIScrollView` declare no members (`ct17ProgressViewAndScrollViewDeclareNoMembers`).
- The default `UIViewController` title is `javaClass.simpleName`, which the consumer's R8 obfuscates when the app does not set `title`.
- `presentViewController` of a controller created without a storyboard (outside `instantiateViewControllerWithIdentifier`) throws an NPE in `presentModalViewController`.
- `UIApplication` re-attaches only on `onStart`: if the attached `UIActivity` is destroyed while another one is already STARTED (translucent on top, multi-window), the survivor has no `UIApplication.activity` until it restarts, and each re-attach registers extra launchers on it.
- `popToRootViewController` also empties the pushes made inside a tab of a root `UITabBarController`, because `popBackStack(null, INCLUSIVE)` clears the whole back stack of the Activity.
- After a push queued while the app is in the background, the fragments already on screen receive `viewDidAppear` without `viewWillAppear` on return: the queue drains in `onStart` after `super.onStart` and no longer turns `isNotificationsEnabled` on when queueing.
