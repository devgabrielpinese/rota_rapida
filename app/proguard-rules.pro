#############################
# PROGUARD — ROTA RÁPIDA
# Foco: Apache POI (poi + poi-ooxml-lite), OOXML schemas,
# evitar referências a AWT / OSGi / Log4j / bnd no Android.
#############################

# --- Manter APIs usadas em runtime (POI + schemas + xmlbeans) ---
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.etsi.uri.** { *; }
-keep class org.w3c.dom.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# Atributos úteis p/ reflexão/POI
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, Exceptions, SourceFile, LineNumberTable

# --- Silenciar dependências que não existem no Android ---
-dontwarn java.awt.**
-dontwarn java.awt.color.**
-dontwarn java.awt.geom.**
-dontwarn java.awt.image.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.namespace.**

-dontwarn org.osgi.framework.**
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.apache.commons.logging.**

# Algumas libs utilitárias usadas por POI
-dontwarn com.zaxxer.sparsebits.**
-dontwarn org.dom4j.**
-dontwarn org.antlr.**

# --- MapLibre / MLKit / CameraX (evita ruído no shrink) ---
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**

-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- GSON/Retrofit (modelos e adapters) ---
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# --- Enum e data classes (geralmente seguro manter membros) ---
-keepclassmembers enum * { *; }
