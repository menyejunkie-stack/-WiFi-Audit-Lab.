# WiFi Audit Lab

Aplikasi edukasi untuk memeriksa jaringan Wi-Fi milik sendiri atau yang telah memberi izin. Aplikasi tidak membobol Wi-Fi, tidak menebak password, dan tidak mengambil password tersimpan dari Android.

## Build APK
Push ke branch main akan menjalankan workflow GitHub Actions. Buka tab Actions, pilih run terbaru, lalu unduh artifact WiFi-Audit-Lab-debug. Ekstrak ZIP artifact untuk mendapatkan app-debug.apk.

## Fitur
- Scan SSID sekitar (izin lokasi dan Wi-Fi diperlukan).
- Menampilkan sinyal, frekuensi, dan label keamanan.
- Pemeriksa kekuatan password lokal.
- Parser teks QR Wi-Fi tanpa menampilkan password.
