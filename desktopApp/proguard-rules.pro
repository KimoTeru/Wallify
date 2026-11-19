# ProGuard rules of the Desktop release tasks (`packageRelease…`, `runRelease`). The Compose plugin adds the rules
# of the Compose runtime itself. Unlike R8 on Android, the rules libraries ship in `META-INF/proguard` are not
# guaranteed to be applied here, so the ones this app needs are copied from the jars of the versions in
# `gradle/libs.versions.toml`. Update them together with those versions.
# The IDE checks names against the compile classpath of this module and marks classes of transitive `implementation`
# dependencies (Ktor serialization, the protobuf of DataStore) as unresolved; ProGuard gets the runtime classpath,
# where they are.

# Classes found by ServiceLoader: ProGuard does not see them through `META-INF/services` and removes them.
# Main dispatcher of the coroutines (kotlinx-coroutines-swing), https://github.com/Kotlin/kotlinx.coroutines/issues/4025
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
# Ktor: the engine and the JSON provider of content negotiation.
-keep class io.ktor.client.engine.** implements io.ktor.client.HttpClientEngineContainer
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
# Coil: the Ktor network fetcher (coil-core-jvm rules).
-keep class coil3.util.DecoderServiceLoaderTarget { *; }
-keep class coil3.util.FetcherServiceLoaderTarget { *; }
-keep class coil3.util.ServiceLoaderComponentRegistry { *; }
-keep class * implements coil3.util.DecoderServiceLoaderTarget { *; }
-keep class * implements coil3.util.FetcherServiceLoaderTarget { *; }

# Fields updated through atomic field updaters (kotlinx-coroutines-core, ktor-utils).
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-keepclassmembers class io.ktor.** {
    volatile <fields>;
}
-keepclassmembers class kotlinx.coroutines.flow.ReadonlySharedFlow {
    kotlinx.coroutines.Job job;
}
-keepclassmembers class kotlinx.coroutines.flow.ReadonlyStateFlow {
    kotlinx.coroutines.Job job;
}

# kotlinx.serialization (kotlinx-serialization-core-jvm rules): serializers are found through the companion.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static ** Companion;
}
-if @kotlinx.serialization.internal.NamedCompanion class *
-keepclassmembers class * {
    static <1> *;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
# An optimization of ProGuard breaks this field, https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

# Room finds the generated database through its constructor; the bundled SQLite is called through JNI.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keepclasseswithmembers class androidx.sqlite.driver.bundled.** { native <methods>; }

# DataStore Preferences reads its protobuf messages by field.
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Optional classes that the libraries look up only when they are present.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
# OkHttp 5 ships no ProGuard rules; its `okhttp3.internal.graal` classes are used only by GraalVM native-image.
-dontwarn org.graalvm.nativeimage.**
-dontwarn com.oracle.svm.core.annotate.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn java.lang.instrument.**
-dontwarn sun.misc.Signal
-dontwarn sun.misc.SignalHandler
-dontnote kotlinx.serialization.**

# Readable stack traces in crash reports.
-keepattributes SourceFile,LineNumberTable