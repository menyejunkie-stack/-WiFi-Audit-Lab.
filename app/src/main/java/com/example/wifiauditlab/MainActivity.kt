package com.example.wifiauditlab

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {
    private lateinit var output: TextView
    private val requestCode = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = "WiFi Audit Lab\nAudit hanya jaringan milik sendiri atau yang diizinkan."
            textSize = 20f
        })
        root.addView(Button(this).apply {
            text = "Pindai jaringan sekitar"
            setOnClickListener { requestWifiScan() }
        })
        val password = EditText(this).apply {
            hint = "Password untuk diperiksa"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(password)
        root.addView(Button(this).apply {
            text = "Cek kekuatan password"
            setOnClickListener { output.text = strength(password.text.toString()) }
        })
        val qr = EditText(this).apply { hint = "Tempel teks QR Wi-Fi (WIFI:T:WPA;S:...;P:...;;)" }
        root.addView(qr)
        root.addView(Button(this).apply {
            text = "Baca teks QR Wi-Fi"
            setOnClickListener { output.text = parseWifi(qr.text.toString()) }
        })
        output = TextView(this).apply { textSize = 14f }
        root.addView(ScrollView(this).apply { addView(output) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun requestWifiScan() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        } else scanWifi()
    }

    @Suppress("DEPRECATION")
    private fun scanWifi() {
        try {
            val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            manager.startScan()
            val networks = manager.scanResults
            output.text = if (networks.isEmpty()) "Belum ada hasil. Aktifkan Wi-Fi dan izin lokasi, lalu coba lagi."
            else networks.take(30).joinToString("\n\n") {
                "SSID: ${it.SSID.ifBlank { "(tersembunyi)" }}\nSinyal: ${it.level} dBm\nFrekuensi: ${it.frequency} MHz\nKeamanan: ${it.capabilities}"
            }
        } catch (e: Exception) { output.text = "Pemindaian gagal: ${e.message}" }
    }

    private fun strength(value: String): String {
        if (value.isEmpty()) return "Masukkan password terlebih dahulu."
        var score = 0
        if (value.length >= 12) score++
        if (value.any(Char::isLowerCase) && value.any(Char::isUpperCase)) score++
        if (value.any(Char::isDigit)) score++
        if (value.any { !it.isLetterOrDigit() }) score++
        val label = when (score) { 0, 1 -> "Lemah"; 2 -> "Sedang"; 3 -> "Kuat"; else -> "Sangat kuat" }
        return "Perkiraan kekuatan: $label\nPanjang: ${value.length} karakter"
    }

    private fun parseWifi(raw: String): String {
        if (!raw.startsWith("WIFI:")) return "Format tidak dikenali. Contoh: WIFI:T:WPA;S:NamaWiFi;P:Password;;"
        val fields = raw.removePrefix("WIFI:").trimEnd(';').split(';').mapNotNull { part ->
            val i = part.indexOf(':')
            if (i > 0) part.substring(0, i) to part.substring(i + 1) else null
        }.toMap()
        return "SSID: ${fields["S"] ?: "(tidak ada)"}\nKeamanan: ${fields["T"] ?: "(tidak ada)"}\nPassword pada QR: ${if (fields["P"].isNullOrEmpty()) "Tidak ada" else "Ada (disembunyikan)"}"
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, grants: IntArray) {
        super.onRequestPermissionsResult(code, permissions, grants)
        if (code == requestCode && grants.firstOrNull() == PackageManager.PERMISSION_GRANTED) scanWifi()
        else output.text = "Izin lokasi diperlukan Android untuk pemindaian Wi-Fi."
    }
}
