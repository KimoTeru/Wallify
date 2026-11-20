# Project rules for R8 of the release build. Compose, Koin (compiler plugin, no reflection), Ktor, Coil, Room,
# kotlinx.serialization, Decompose, DataStore and Paging bring their own consumer rules, so they are not repeated
# here: a broad `-keep` of a library stops R8 from optimizing it. Add a rule only for what R8 reports
# (`build/outputs/mapping/release/missing_rules.txt`) or what fails on a release build.

# Readable stack traces in crash reports: the mapping file restores the names, these keep the line numbers.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# A release build logs nothing: every call of `log` (`core:common`) is removed together with its message.
-assumenosideeffects class org.kimoteru.wallify.core.common.log.LogKt {
    *** log(...);
}