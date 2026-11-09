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
- `NotificationObserver`
- `UIApplication.initialize(context)`
- `CocoaTouchInitializer`, an androidx.startup `Initializer`
- `UIUserInterfaceStyle`
- `UIApplication.appearance`
- `UIActivity.prepareLaunch()`
- `tabContainerId()`
- `UITabBarController.popInTab()`
- `UINavigationController.viewControllers`
- Library resources prefixed with `cocoatouch_`, including `@id/cocoatouch_tab_container` for tab layouts

### Changed
- `UIImagePickerController` keeps only the latest camera capture in `cacheDir`: a new capture deletes every earlier `tmp_image_file*.jpeg` there, even one written by a previous activity instance, and a cancelled one is deleted; the files no longer rely on `deleteOnExit`, which never runs on Android.
- The delegates of `UISearchBar`, `UITabBar` and `UITabBarController` are held strongly, so an inline delegate object stays alive; the `UIButton` tint and `UISearchBar.barTintColor` read `UIApplication.appearance` when used, not at construction.
- `NotificationCenter` delivers to its observers directly on the main thread and no longer uses `LocalBroadcastManager`, which is not a dependency anymore; a receiver the app registered with `LocalBroadcastManager` for a notification name no longer fires.
Compared with 0.0.1, the only public predecessor:
- A `UITextField` keeps a single `TextWatcher`, and a programmatic `setText` does not notify it.
- After `resignFirstResponder` a `UITextField` stays reachable for TalkBack's double-tap and the keyboard's Next action; 0.0.1 left it unfocusable in touch mode until it was tapped again.
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
- `@IBOutlet` and `@IBAction` bindings declared in base classes are inherited, and the `@IBAction` sender is the widget that was clicked; 0.0.1 read only the leaf class and passed a fresh `UIButton`. A superclass outlet whose name a subclass outlet reuses (a Kotlin override, or a private base field next to a subclass field) is bound only when its id exists in the layout and skipped otherwise; an outlet with its own name must exist.
- `NotificationCenter.addObserver` returns a `NotificationObserver` and `removeObserver(observer)` removes exactly that one; `removeObserver()` without a token no longer compiles and is hidden from Java (0.0.1 kept a single slot, so it unregistered only the last observer and leaked the others).
- `UISwitch.onTintColor = null` clears the tint (0.0.1 crashed).
- `UserDefaults.set(null as Boolean?, key)` removes the key (0.0.1 crashed).
- A back handler on the root controller is responsible for finishing the activity: `popViewController` at the root is a no-op, so a root that inherits a pop-only handler cannot be left with Back.
- `UIApplication` re-attaches on `onResume` and when an activity becomes the top resumed activity (API 29+ multi-window), so an activity that was only paused under a translucent one, or whose split-screen neighbour closed, is attached again.
- Pops, dismisses, tab selection and in-tab presents requested while the activity's state is saved, including right after `super.onStop()`, are queued in order with the pushes and applied when it restarts; `removeFromSuperview` is queued the same way only when the controller has a navigation controller. They run on the activity that owns the navigation controller.
- `applicationWillEnterForeground` is called on every `onResume`, including the first launch and returns from permission dialogs and pickers; 0.0.1 never called it.
- `UIFragment`, `UIKeyInput`, `BackPressedParser`, `IBActionParser.parse`/`IBOutletParser.parse`, `Transition`, `NoneTransition`, `UIViewHolder` and `UIApplication.isNotificationsEnabled` are behind `@InternalCocoaTouchApi` (ERROR level), so using them needs an opt-in.
- `WKWebView` runs its user script once per finished page and only on the origin (scheme, host and port) passed to `load`; `file:` pages are told apart by path, so a path Chromium percent-encodes (spaces, non-ASCII) must be passed already encoded or the script does not run; a page whose URL has no `//` after the scheme (`data:`, `about:`, `file:/x`) never receives the script; 0.0.1 ran it on every subresource of any page. Navigation is not restricted, the script just never runs on another origin. It runs inside a guard block, so its top-level `let`/`const`/`class` are local to it, while a `function` or `var` stays global unless the script starts with `"use strict"` (assign to `window` to expose values), and a `"use strict"` directive on the first line is honoured.
### Known issues
Each one is pinned by a test that fails when the behaviour changes.
- CT-06: popping back to the root calls `viewDidLoad` on it again (`ct06PopCallsViewDidLoadAgainOnRoot`).
- CT-02: `setTitle` for the Disabled state overwrites the Normal title (`ct02SetTitleForDisabledOverwritesNormal`).
- CT-03: a controller passed to `presentViewController` joins the push stack instead of a separate modal stack (`ct03PresentedControllerJoinsThePushStack`).
- CT-04: `textFieldShouldChange` returns `Unit`, so a delegate cannot veto an edit (`ct04ShouldChangeCannotVeto`).
- CT-07: `tableViewWillDisplayForRowAt` fires only for the last visible row, so earlier rows are skipped (`ct07WillDisplaySkipsRowsBeforeTheLastVisible`).
- CT-12: `UIView.tag` is a `String`, where UIKit has an `Int` (`ct12TagIsAStringNotAnInt`).
- CT-16: after the activity is recreated, the navigation controller holds only the initial controller and the `UIFragment`s restored by the system are removed, along with the activity's back stack, which the library owns for pushes, so nothing piles up across recreations; a fragment of the app's own is left alone (`ct16RecreateKeepsOnlyTheInitialController`).
- CT-17: `UIProgressView` and `UIScrollView` declare no members (`ct17ProgressViewAndScrollViewDeclareNoMembers`).
- The default `UIViewController` title is `javaClass.simpleName`, which the consumer's R8 obfuscates when the app does not set `title`.
- `presentViewController` of a controller created without a storyboard (outside `instantiateViewControllerWithIdentifier`) throws an NPE in `commitModal`: at the call while the activity is resumed, and at the restart when the call was queued while its state was saved (`presentingAStoryboardLessControllerWhilePausedThrowsOnRestart`).
- A non-storyboard `UIActivity` keeps the `UIStoryboard` below it alive until the next storyboard attaches; its tables still create cells through that storyboard (`plainActivityKeepsTheFinishedStoryboardBelowIt`).
- `popToRootViewController` also empties the pushes made inside a tab of a root `UITabBarController`, because `popBackStack(null, INCLUSIVE)` clears the whole back stack of the Activity.
- After a push queued while the app is in the background, the fragments already on screen receive `viewDidAppear` without `viewWillAppear` on return: the queue drains in `onStart` after `super.onStart`; queueing does not turn `isNotificationsEnabled` on, the transaction turns it on when it runs.

### Differences from the in-house framework this release was extracted from
This release is extracted from the framework copy the Stark apps ship; teams moving from that copy will notice:
- The application context is set before `Application.onCreate` by `CocoaTouchInitializer`, run by androidx.startup's `InitializationProvider` (`UIApplication.initialize(context)` does the same by hand), so `UserDefaults` and `UIPasteboard` work with no activity; the previous copy needed a launched activity first. An app that removed that provider, or that runs code in another process (`android:process`), calls `UIApplication.initialize(this)` in `Application.onCreate`; to opt out of the automatic step, add `<meta-data android:name="com.starkbank.cocoatouch.CocoaTouchInitializer" tools:node="remove" />` inside the provider element.
- `UIFragment`, `UIKeyInput`, `BackPressedParser`, `IBActionParser.parse`/`IBOutletParser.parse`, `Transition`, `NoneTransition`, `UIViewHolder` and `UIApplication.isNotificationsEnabled` are behind `@InternalCocoaTouchApi` (ERROR level), so using them needs an opt-in.
- `UIApplication` attaches in `onCreate`, re-attaches in `onStart`, in `onResume` and when the activity becomes the top resumed one (API 29+ multi-window), and releases on destroy.
- `UIImagePickerController.shared` and `UIDocumentPickerViewController.shared` are created once per activity and released when it is destroyed; a picker kept from a destroyed activity throws on `present()`.
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
