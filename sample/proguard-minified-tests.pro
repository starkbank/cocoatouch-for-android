# This build type exists to prove cocoatouch's consumer rules under R8. R8 cannot see the test APK, so it strips harness classes the runner and Espresso need (kotlin.LazyKt, Guava's ListenableFuture); keeping everything outside com.starkbank leaves exactly our own code shrunk and obfuscated
-keep class !com.starkbank.** { *; }
-dontwarn javax.lang.model.element.Modifier
