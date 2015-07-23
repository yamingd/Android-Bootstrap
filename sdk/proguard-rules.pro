# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in v.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-ignorewarnings
-dontoptimize
-dontobfuscate
-dontskipnonpubliclibraryclasses

-ignorewarnings

-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable,*Annotation*

-printmapping map.txt
-printseeds seed.txt

-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }

-keep class com.donnfelker.android.**
-keepclassmembers class com.donnfelker.android.** { public <init>(...); }
-keepclassmembers class com.donnfelker.android.** {
    *** set*(***);
    *** get*();
}
-keep class com.madgag.android.blockingprompt.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View { public <init>(android.content.Context); public <init>(android.content.Context, android.util.AttributeSet); public <init>(android.content.Context, android.util.AttributeSet, int); public void set*(...); }

-keepclassmembers class * extends android.app.Activity { public void *(android.view.View); }
-keepclassmembers class android.support.v4.app.Fragment { *** getActivity(); public *** onCreate(); public *** onCreateOptionsMenu(...); }

-keep public class * extends junit.framework.TestCase

-keepclassmembers class * { @com.google.inject.Provides *; @android.test.suitebuilder.annotation.* *; void test*(...); }

-keep class com.google.inject.Binder
-keep class com.google.inject.Key
-keep class com.google.inject.Provider
-keep class com.google.inject.TypeLiteral

-keepclassmembers class * { @com.google.inject.Inject <init>(...); }
-keepclassmembers class com.google.inject.assistedinject.FactoryProvider2 { *; }
-keepclassmembers class com.google.** {
    private void finalizeReferent();
    protected void finalizeReferent();
    public void finalizeReferent();
    void finalizeReferent();

    private *** startFinalizer(java.lang.Class,java.lang.Object);
    protected *** startFinalizer(java.lang.Class,java.lang.Object);
    public *** startFinalizer(java.lang.Class,java.lang.Object);
    *** startFinalizer(java.lang.Class,java.lang.Object);
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

## butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

## realm.io
-keep @io.realm.annotations.RealmModule class *
-dontwarn javax.**
-dontwarn io.realm.**

## otto
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}