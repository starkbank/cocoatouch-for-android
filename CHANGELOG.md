# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html):
MAJOR for incompatible API changes, MINOR for backwards-compatible additions and
PATCH for backwards-compatible fixes.

## [Unreleased]

### Added
First release on Maven Central; every interface the library ships:

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
- Library resources prefixed with `cocoatouch_`, including `@id/cocoatouch_tab_container` for tab layouts

### Changed
Compared with 0.0.1, the only public predecessor:
- A `UITextField` keeps a single `TextWatcher`, and a programmatic `setText` does not notify it.
- `popToRootViewController` removes every pushed controller; 0.0.1 popped back to the root's class name.
- Paused navigation transitions are drained once, after the activity re-attaches in `onStart`, and no longer in `onRestart`.
- `viewDidDisappear` is forwarded to child controllers as `viewDidDisappear`; 0.0.1 forwarded it as `viewWillDisappear`.
- `NSMutableArray.removeObject` removes every occurrence, and navigation pops are positional.
- Back with no handler finishes the Activity that dispatched it, and back handlers are validated at bind and must receive a `UIButton`.
- `initApplication` is `protected`.
- The package `compability` is now `compatibility`.
- `UITableView` needs a `RecyclerView` in the layout; a `ListView` breaks.
- `UIApplication.activity`/`storyboard` are readable behind `@OptIn(InternalCocoaTouchApi::class)`; 0.0.1 kept them internal.
- `@IBAction` is validated at bind: exactly one sender parameter, a concrete sender type with a public no-argument constructor, and an existing view id, each failure an `IllegalStateException` naming the controller, method and id; 0.0.1 bound without checks.
- An exception thrown by an `@IBAction` handler propagates instead of being printed, so a throwing handler now crashes the app visibly.
- `@IBOutlet` and `@IBAction` bindings declared in base classes are inherited, and the `@IBAction` sender is the widget that was clicked; 0.0.1 read only the leaf class and passed a fresh `UIButton`.
- `NotificationCenter.addObserver` returns a `NotificationObserver` and `removeObserver(observer)` removes exactly that one; the no-argument `removeObserver()` is deprecated and removes every registered observer, where 0.0.1 kept a single slot, so it unregistered only the last observer and leaked the others.

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

### Differences from the in-house framework this release was extracted from
This release is extracted from the framework copy the Stark apps ship; teams moving from that copy will notice:
- `UIApplication` attaches in `onCreate`, re-attaches in `onStart` and releases on destroy.
- `UIApplication.activity`/`storyboard`, `UIImagePickerController.shared` and `UIDocumentPickerViewController.shared` are filled by the library and read-only for apps, which must delete their own assignments.
- `UIActivity.prepareLaunch()` runs before `super.onCreate`.
- Navigation requested after the activity is destroyed is ignored.
- Default styles come from `UIAppearance`, and the section-header font also sets the text size; the previous copy set only the typeface.
- `UISearchBarDelegate` replaces the in-house search callback.
- `@IBAction` is validated at bind: exactly one sender parameter, a concrete sender type with a public no-argument constructor, and an existing view id, each failure an `IllegalStateException` naming the controller, method and id.
- An exception thrown by an `@IBAction` handler propagates instead of being printed, so a throwing handler now crashes the app visibly.
- `@IBOutlet` and `@IBAction` bindings declared in base classes are inherited, and the `@IBAction` sender is the widget that was clicked.
- A missing outlet throws `IllegalStateException` naming the field ("not found in <Controller>"), and an outlet bound before the controller has a view throws one with "bound before <Controller> has a view".
- A same-id `@IBAction` declared in a base class and in a subclass binds the subclass handler.
- A sender parameter type that is not a `UIResponder` must receive a `UIView`.
- A push requested while the activity is stopped is queued before the attached-activity guards, so it survives a finishing activity in front.
- `DispatchGroup.notify` delivers asynchronously on the requested queue, fires at once on an empty or already finished group, and runs each block once.
- `DispatchGroup.leave()` without `enter()` throws `IllegalStateException`.
- A `DispatchGroup.notify` registered before the first `enter` fires at once and does not wait for the next balance, as in GCD.
- The preference file and the `FileProvider` authority are derived from `packageName`.
- Back on the root reaches the root controller.
- Not included in the library: `UIDatePicker`, `DocumentScannerController`, `UIImageView.load`, `UserDefaults.set<T>`/`object` (encode the value and call `set(string, key)`; the persisted format is unchanged), `UISearchBar.searchController`, `installSplashScreen` (now `prepareLaunch()`), and the cursor and handle tint of `UISearchBar` below API 29.
