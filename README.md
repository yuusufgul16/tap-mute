# TapMute

Belirli uygulamaların bildirimlerini tek tıkla sessize alan Android uygulaması.

## Özellikler
- 🔇 Uygulama bazlı bildirim engelleme
- 📞 Telefon aramaları her zaman sesli kalır
- 🔍 Uygulama arama/filtreleme
- 📱 Home screen widget ile tek tıkla toggle
- 🌙 Karanlık tema

## Nasıl Çalışır
1. Uygulamayı aç
2. Bildirim erişim iznini ver
3. Sessize almak istediğin uygulamaları seç
4. Ana toggle'ı aç → Seçili uygulamalardan bildirim gelmez
5. Widget ile home screen'den tek tıkla aç/kapa

## Build
```
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Gereksinimler
- Android 8.0+ (API 26)
- Bildirim Erişim İzni
