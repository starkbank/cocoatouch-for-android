
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

6.1 Add to Gradle the last version:

```sh
compile 'com.starkbank:cocoatouch:0.0.1'
```

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

MIT
