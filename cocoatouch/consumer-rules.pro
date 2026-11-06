-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep @interface com.starkbank.cocoatouch.annotation.IBOutlet
-keep @interface com.starkbank.cocoatouch.annotation.IBAction
-keepclassmembers class * {
    @com.starkbank.cocoatouch.annotation.IBOutlet <fields>;
    @com.starkbank.cocoatouch.annotation.IBAction <methods>;
}
-keepclassmembers class * extends com.starkbank.cocoatouch.uikit.UIResponder {
    public <init>();
}
