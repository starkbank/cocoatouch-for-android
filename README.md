
# CocoaTouch for Android (Kotlin version)

If you are here, we will assume:

  - You are an iOS Developer
  - You like CocoaTouch
  - You need to code Android but you don't want to learn it
  - You want to replicate your iOS code to Android
  - You prefer Kotlin over Java


### 1. Why you should use it?

In Android, Activity is equivalent to UIViewController and it normally looks like:

```sh
public class MainActivity extends AppCompatActivity 
{
    Button button; // Equivalent to iOS UIButton
    ListView tableview; //Equivalent to iOS UITableView
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Link XML to variables
        this.button = this.findViewById(R.id.button);
        this.listView = this.findViewById(R.id.tableview);
        
        //Set action to button
        this.button.setOnClickListener(new View.OnclickListener()
        {
            @Override
            public void onClick(View view) 
            {
               ...
            }
        }
    }
}
```

Not bad. But using this library, it becomes:

```sh
open class MainActivity: UIViewController() {
  
    @IBOutlet(R.id.button) lateinit var button: UIButton
    @IBOutlet(R.id.tableview) lateinit var tableview: UITableView
    
    @IBAction(R.id.button) fun click(sender: UIButton) {
        ...
    }
}
```

Nice right? But it becomes better. The thing we most hate on Android SDK, it's how you can't alloc 
the activity to be presented and just pass objects. You need to transfer data via bundle and the 
SDK will create an activity for you. This starts to be annoying when you need to pass a custom 
object and you have to implement Parcelable or Serializable. This adds lots of lines of code, 
because Android copy objects between activities. This is an example of how to transfer a 
custom objects from one Activity to another in Android:

CustomObject.java

```sh
public class CustomObject extends Object implements Parcelable
{
    ... 
    public String name;

    public CustomObject() 
    {
        name = "";
    }
    
    //
    // Parcelable Trash code
    //
    public CustomObject(Parcel in) 
    {
        name = in.readString();
    }
    @Override public int describeContents() 
    {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) 
    {
        ...
        dest.writeString(name);
    }
    public static final Parcelable.Creator<CustomObject> CREATOR = new Parcelable.Creator<CustomObject>()
    {
        public CustomObject createFromParcel(Parcel in)
        {
            return new CustomObject(in);
        }
        public CustomObject[] newArray(int size)
        {
            return new CustomObject[size];
        }
    }
}
```
SenderActivity.java

```sh
public void presentReceiverController()
{
    CustomObject customObject = new CustomObject(...);
    
    Intent intent = new Intent(this, ReceiverActivity.class);
    intent.putParcelableExtra("key_customObject", customObject);
    
    this.startActivity(intent);
}
```

ReceiverActivity.java

```sh
public class ReceiverActivity extends AppCompatActivity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        
        //Get custom Object
        Bundle bundle = getIntent().getExtras();
        CustomObject customObject = bundle.getParcelable("key_customObject");
    }
}
```

Boring, right? Now, forget about Pacelable, Serializable, Bundle and Intent. Just alloc and pass data:

CustomObject.kt

```sh
open class CustomObject: NSObject() {
    ...
    lateinit var name: String
}
```

SenderController.kt

```sh
fun presentReceiverController() {
  
    var customObject = CustomObject(...)
    customObject.name = "My name"
    
    var controller = this.storyboard.instantiateViewControllerWithIdentifier(R.layout.receiver) as ReceiverController
    controller.customObject = customObject
    
    this.navigationController.pushViewController(controller, true)
}
```

ReceiverController.kt

```sh
open class ReceiverController: UIViewController() {
  
    lateinit var customObject: CustomObject 
    
    override fun viewDidLoad() {
        super.viewDidLoad()
        // Just access object normally
        println(this.customObject.name)
    }
```


Sweet, right? Simple as you do in iOS.


### 2. How it works?

This framework is based on a Single Activity Application and each ViewController is represented 
by fragments using composition. 


### 3. How you receive onBackPressed Notifications

If you add the method bellow to your controller, you will receive on back notifications. It's the 
same of receive a button pressed action. 

```sh
@IBAction(DefaultActions.onBackPressed) fun back(sender: UIButton) {
    //TODO
}
```

If you don't add it, the back button will close you app.

### 4. How Can you get Context or the Activity?

```sh

var activity: Activity = UIApplication.sharedApplication().activity()

var context: Context = UIApplication.sharedApplication().context()

```

### 5. Limitations

We created some structures such as UIViewContorller, UIButton, UITableView, UILabel... that we 
needed to create the Stark Bank app. But there are a lot of to do yet. iOS have a bunch of other elements 
that needs to be coded. Maybe We are here looking for contributors, just maybe. 

We added an example app that use some structures, you should try it.


### 6. Installation and Configure Project

6.1 Add the dependency (Maven Central):

```sh
implementation 'com.starkbank:cocoatouch:<version>'
```

Requirements of the consuming app:

- `minSdk` 25.
- `compileSdk` 35 or higher (androidx.core 1.15.0 declares minCompileSdk 35).
- Kotlin 2.1 or higher (the artifact's kotlin-stdlib is 2.2.21, and a compiler reads metadata at most one minor version ahead).
- `android.nonFinalResIds=false` in `gradle.properties`, so `@IBOutlet(R.id.x)` accepts the ids.
- `UIImagePickerController` needs a FileProvider with the authority `${applicationId}.provider` in the manifest, with the `android.support.FILE_PROVIDER_PATHS` meta-data pointing to an xml that contains `<cache-path name="cocoatouch_images" path="."/>`, because the temporary file goes to `cacheDir`. The `.provider` authority is a common name and may collide with another library's provider.
- `<uses-permission android:name="android.permission.CAMERA"/>` when the `UIImagePickerController` camera is used.
- `UIApplication.context()`/`activity()` are valid between the `onCreate` and the `onDestroy` of the `UIStoryboard`; never call `UserDefaults.standard` from a `Service` or from `Application.onCreate`.
- Set `title` on the controllers when the app is minified (see Known issues in the CHANGELOG).
- `UIApplication.appearance` sets the theme; a change applies to views created afterwards, and presenting the screen again is up to the app.
- Use `@id/cocoatouch_tab_container` in tab layouts.
- Use `@OptIn(InternalCocoaTouchApi::class)` (`import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi`) to read `UIApplication.activity`/`storyboard` and the other members marked `@InternalCocoaTouchApi`.

6.2 - Create file: MainStoryboard.kt

```sh
class MainStoryboard: UIStoryboard() {

}
```

6.3 - Go to AndroidManifest and make it the fist launch Activity
```sh
        <activity android:name=".MainStoryboard">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity> 
```
6.4 - Create a XML in res/layout named mainstoryboard.xml. (Copy and paste this code)

```sh
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/container">
</FrameLayout>
```

6.5 - Create file: AppDelegate.kt

```sh
class AppDelegate: UIResponder(), UIApplicationDelegate {

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<String, String>?): Boolean {
        return true
    }

    override fun applicationWillResignActive(application: UIApplication) {

    }

    override fun applicationDidEnterBackground(application: UIApplication) {

    }

    override fun applicationWillEnterForeground(application: UIApplication) {

    }

    override fun applicationDidBecomeActive(application: UIApplication) {

    }

    override fun applicationWillTerminate(application: UIApplication) {

    }
}
```
6.6 - Override methods in MainStoryboard, declaring the initial UIViewController id and mapping 
layout ids with classes

```sh
class MainStoryboard: UIStoryboard() {

    override fun applicationDelegate(): UIApplicationDelegate {
        return AppDelegate()
    }

    override fun initialViewControllerId(): Int {
        return R.layout.mainviewcontroller
    }

    override fun storyboardId(): Int {
        return R.layout.mainstoryboard
    }

    override fun containerId(): Int {
        return R.id.container
    }

    override fun viewControllerForIdentifier(identifier: Int): UIViewController {
        return when (identifier) {
            R.layout.mainviewcontroller -> MainViewController()
            else -> throw Resources.NotFoundException()
        }
    }

    override fun viewCellForIdentifier(identifier: Int): UITableViewCell {
        return when (identifier) {
            R.layout.maintableviewcell -> MainTableViewCell()
            else -> throw Resources.NotFoundException()
        }
    }
}
```

The initialViewControllerId is the id of the first ViewController. Your app will start there. 
You can set any ViewController you want.

### Supported interfaces

| Interface | Apple equivalent | Supported | Deliberate differences |
|---|---|---|---|
| `UIActivity` | AppCompatActivity (Android) | Android-specific | `prepareLaunch()` runs before `super.onCreate` |
| `UIStoryboard` | `UIStoryboard` | yes | Is the launch Activity |
| `UIApplication` | `UIApplication` | yes | Filled by the library on attach; `activity`/`storyboard` are read-only for apps |
| `UIApplicationDelegate` | `UIApplicationDelegate` | yes | - |
| `UIResponder` | `UIResponder` | yes | - |
| `NSObject` | `NSObject` | yes | - |
| `UIViewController` | `UIViewController` | yes | Default title is `javaClass.simpleName` |
| `UINavigationController` | `UINavigationController` | yes | - |
| `UITabBarController` | `UITabBarController` | yes | Tab layouts use `@id/cocoatouch_tab_container` |
| `UIFragment` | Fragment (Android) | Android-specific | - |
| `UIView` | `UIView` | yes | - |
| `UIControl` | `UIControl` | yes | - |
| `UILabel` | `UILabel` | yes | - |
| `UIButton` | `UIButton` | yes | - |
| `UISwitch` | `UISwitch` | yes | - |
| `UISegmentedControl` | `UISegmentedControl` | yes | - |
| `UITextField` | `UITextField` | yes | `setText` is silent |
| `BackEditText` | none | Android-specific | - |
| `UITextFieldDelegate` | `UITextFieldDelegate` | yes | `shouldChange` cannot veto (CT-04) |
| `UIKeyInput` | `UIKeyInput` | yes | A class here, a protocol on Apple |
| `UISearchBar` | `UISearchBar` | yes | No `searchController`; cursor tint and handles only from API 29 |
| `UISearchBarDelegate` | `UISearchBarDelegate` | yes | - |
| `UIProgressView` | `UIProgressView` | type only, no progress API | - |
| `UIScrollView` | `UIScrollView` | superclass of UITableView/UICollectionView only | - |
| `UITableView` | `UITableView` | yes | Requires a `RecyclerView` in the layout |
| `UITableViewCell` | `UITableViewCell` | yes | - |
| `UITableViewDataSource` | `UITableViewDataSource` | yes | - |
| `UITableViewDelegate` | `UITableViewDelegate` | yes | `willDisplay` skips rows before the last visible (CT-07) |
| `UICollectionView` | `UICollectionView` | yes | - |
| `UICollectionViewCell` | `UICollectionViewCell` | yes | - |
| `UICollectionViewDataSource` | `UICollectionViewDataSource` | yes | - |
| `UICollectionViewDelegate` | `UICollectionViewDelegate` | yes | - |
| `UIViewHolder` | none | Android-specific | - |
| `UIRefreshControl` | `UIRefreshControl` | yes | - |
| `UIActivityIndicatorView` | `UIActivityIndicatorView` | yes | - |
| `UIImage` | `UIImage` | yes | - |
| `UIImageView` | `UIImageView` | yes | No `load` |
| `UIImagePickerController` | `UIImagePickerController` | yes | Needs the FileProvider and, for the camera, the CAMERA permission |
| `UIImagePickerControllerDelegate` | `UIImagePickerControllerDelegate` | yes | - |
| `UIDocumentPickerViewController` | `UIDocumentPickerViewController` | yes | - |
| `UIDocumentPickerDelegate` | `UIDocumentPickerDelegate` | yes | - |
| `UINavigationBar` | `UINavigationBar` | yes | - |
| `UITabBar` | `UITabBar` | yes | - |
| `UITabBarItem` | `UITabBarItem` | yes | - |
| `UITabBarDelegate` | `UITabBarDelegate` | yes | - |
| `UIColor` | `UIColor` | yes | - |
| `UIFont` | `UIFont` | yes | - |
| `UIEdgeInsetsMake` | `UIEdgeInsets` | yes | - |
| `UIScreen` | `UIScreen` | yes | - |
| `UIPasteboard` | `UIPasteboard` | yes | - |
| `UIControlState` | `UIControl.State` | yes | - |
| `UIAppearance` | none | Android-specific | Theme defaults, set through `UIApplication.appearance` |
| `UIDefaultAppearance` | none | Android-specific | Default `UIAppearance` |
| `UITraitCollection` | `UITraitCollection` | yes | `userInterfaceStyle` only |
| `UIUserInterfaceStyle` | `UIUserInterfaceStyle` | yes | - |
| `WKWebView` | `WKWebView` | yes | - |
| `WKWebViewConfiguration` | `WKWebViewConfiguration` | yes | - |
| `WKUserContentController` | `WKUserContentController` | yes | - |
| `WKUserScript` | `WKUserScript` | yes | - |
| `URLRequest` | `URLRequest` | url only, for WKWebView.load | - |
| `NSArray` | `NSArray` | yes | - |
| `NSMutableArray` | `NSMutableArray` | yes | `removeObject` removes all occurrences |
| `NSIndexPath` | `NSIndexPath` | yes | - |
| `NotificationCenter` | `NotificationCenter` | yes | - |
| `NSNotificationName` | `NSNotification.Name` | yes | - |
| `UserDefaults` | `UserDefaults` | yes | No `set<T>`/`object`; never call `standard` from a `Service` or `Application.onCreate` |
| `DispatchGroup` | `DispatchGroup` | yes | `leave()` without `enter()` throws |
| `DispatchQueue` | `DispatchQueue` | yes | - |
| `Transition` | none | Android-specific | - |
| `NoneTransition` | none | Android-specific | - |
| `CGSize` | `CGSize` | yes | - |
| `Frame` | `CGRect` | yes | - |
| `IBAction` | `@IBAction` | yes | Validated at bind |
| `IBOutlet` | `@IBOutlet` | yes | Takes an `R.id` |
| `Optional` | none | Android-specific | The library's `@Optional` annotation on functions and accessors, not Swift's `Optional` type |
| `IBActionParser` | none | Android-specific | Android-specific binding and back-dispatch machinery; not part of the UIKit surface |
| `IBOutletParser` | none | Android-specific | Android-specific binding and back-dispatch machinery; not part of the UIKit surface |
| `BackPressedParser` | none | Android-specific | Android-specific binding and back-dispatch machinery; not part of the UIKit surface |
| `DefaultActions` | none | Android-specific | Android-specific binding and back-dispatch machinery; not part of the UIKit surface |
| `WKUserScript.WKUserScriptInjectionTime` | `WKUserScriptInjectionTime` | yes | Nested enum here |
| `InternalCocoaTouchApi` | none | Android-specific | Opt-in marker for the Android access |

### 7. Add new UIViewControllers or UITableViewCells to your project:

7.1 - New UIViewController

- Create a new kotlin file and make your class inherit UIViewController
- Create a XML file in res/layout
- Go to MainStoryboard
    - Find the method viewControllerForIdentifier
    - Add the case R.laytout.YOUR_LAYOUT_NAME: return new YOUR_CLASS_NAME()
    
Example:

File: TutorialViewController.kt
```sh
open class TutorialViewController: UIViewController() {
  
}
```

File: tutorialviewcontroller.xml

Create it in the folder res->layout. You can edit your canvas and work with pure XML. Yeah you 
will need to learn how to handle pure XML for Android. (We miss you XCODE interfacebuilder...).

File: MainStoryboard.kt
```sh
class MainStoryboard: UIStoryboard() {
    ...
    override fun viewControllerForIdentifier(identifier: Int): UIViewController {
        return when (identifier) {
            ...
            R.layout.tutorialviewcontroller -> TutorialViewController()
            else -> throw Resources.NotFoundException()
        }
    }
}
```

You have your new UIViewController set =) 

7.2 - New UITableViewCell

- Create a new kotlin file and make your class inherit UITableViewCell
- Create a XML file in res/layout
- Go to MainStoryboard
    - Find the method viewCellForIdentifier
    - Add the case R.laytout.YOUR_LAYOUT_NAME: return new YOUR_CLASS_NAME()

Example:

File: TutorialTableViewCell.kt
```sh
open class TutorialTableViewCell: UITableViewCell() {
  
}
```

File: tutorialtableviewcell.xml

Create it in the folder res->layout. You can edit your canvas and work with pure XML. 
Yeah you will need to learn how to handle pure XML for Android. (We miss you XCODE interfacebuilder...).

File: MainStoryboard.kt
```sh
class MainStoryboard: UIStoryboard() {
    ...
    override fun viewCellForIdentifier(identifier: Int): UITableViewCell {
        return when (identifier) {
            ...
            R.layout.tutorialtableviewcell -> TutorialTableViewCell()
            else -> throw Resources.NotFoundException()
        }
    }
}
```

You have your new TutorialTableViewCell set =)

### 8. How we created it?

- Think in one example or structure in iOS you would like to create
- Code the example in iOS
- Stackoverflow how to do the same/similar in Android
- Code it in Android and make it works
- Create classes to simulate iOS but do the Android code under the table

### 9. Enjoy

You can contact us here or just send an email to: developers@starkbank.com

### 10. License

MIT, see [LICENSE](LICENSE).
