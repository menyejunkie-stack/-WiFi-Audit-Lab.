package com.example.wifiauditlab

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.*
import java.math.BigInteger
import java.security.SecureRandom

class MainActivity : Activity() {
    private lateinit var output: TextView
    private val requestCode = 71
    private val random = SecureRandom()
    private val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lower = "abcdefghijklmnopqrstuvwxyz"
    private val digits = "0123456789"
    private val symbols = "!@#$%^&*()-_=+[]{};:,.<>?/|\\~`"
    private lateinit var lengthBar: SeekBar
    private lateinit var lengthLabel: TextView
    private lateinit var generated: EditText
    private lateinit var passwordCheck: EditText
    private lateinit var demoGuess: EditText
    private val demoSecret = "LabWiFi-2026!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
        }
        root.addView(TextView(this).apply {
            text = "WiFi Audit Lab\nGenerator & pemeriksa password offline"
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = "Semua proses generator dan pemeriksaan berjalan di perangkat. Mode demo hanya memakai password contoh lokal; aplikasi tidak mencoba login atau menebak password Wi-Fi sungguhan."
            textSize = 13f
        })
        root.addView(section("1. Pilih karakter"))
        val upperBox = check(root, "Huruf besar A-Z", true)
        val lowerBox = check(root, "Huruf kecil a-z", true)
        val digitBox = check(root, "Angka 0-9", true)
        val symbolBox = check(root, "Simbol", false)
        lengthLabel = TextView(this).apply { text = "Panjang password: 16" }
        root.addView(lengthLabel)
        lengthBar = SeekBar(this).apply { max = 62; progress = 14 }
        root.addView(lengthBar)
        lengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lengthLabel.text = "Panjang password: ${progress + 2}"
                updateEstimate(upperBox.isChecked, lowerBox.isChecked, digitBox.isChecked, symbolBox.isChecked, progress + 2)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        val estimate = TextView(this).apply { textSize = 13f }
        root.addView(estimate)
        fun refreshEstimate() {
            val pool = (if (upperBox.isChecked) upper.length else 0) +
                (if (lowerBox.isChecked) lower.length else 0) +
                (if (digitBox.isChecked) digits.length else 0) +
                (if (symbolBox.isChecked) symbols.length else 0)
            val count = if (pool == 0) BigInteger.ZERO else BigInteger.valueOf(pool.toLong()).pow(lengthBar.progress + 2)
            estimate.text = "Ruang kemungkinan: ${pool} karakter × panjang ${lengthBar.progress + 2}\nJumlah kemungkinan: ${count.toString()}"
        }
        listOf(upperBox, lowerBox, digitBox, symbolBox).forEach { it.setOnCheckedChangeListener { _, _ -> refreshEstimate() } }
        lengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lengthLabel.text = "Panjang password: ${progress + 2}"
                refreshEstimate()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        refreshEstimate()
        root.addView(Button(this).apply {
            text = "Buat password acak"
            setOnClickListener {
                val pool = buildString {
                    if (upperBox.isChecked) append(upper)
                    if (lowerBox.isChecked) append(lower)
                    if (digitBox.isChecked) append(digits)
                    if (symbolBox.isChecked) append(symbols)
                }
                if (pool.isEmpty()) {
                    output.text = "Pilih minimal satu kelompok karakter."
                } else {
                    val size = lengthBar.progress + 2
                    val result = buildString { repeat(size) { append(pool[random.nextInt(pool.length)]) } }
                    generated.setText(result)
                    output.text = "Password acak berhasil dibuat. Simpan dengan aman."
                }
            }
        })
        generated = EditText(this).apply {
            hint = "Hasil password acak"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        root.addView(generated)
        root.addView(Button(this).apply {
            text = "Salin hasil"
            setOnClickListener {
                val value = generated.text.toString()
                if (value.isNotEmpty()) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Password", value))
                    output.text = "Password disalin ke clipboard."
                }
            }
        })
        root.addView(section("2. Periksa kekuatan password"))
        passwordCheck = EditText(this).apply {
            hint = "Masukkan password untuk diperiksa"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(passwordCheck)
        root.addView(Button(this).apply {
            text = "Periksa kekuatan"
            setOnClickListener { output.text = strength(passwordCheck.text.toString()) }
        })
        root.addView(section("3. Lab pencocokan offline (contoh buatan)"))
        root.addView(TextView(this).apply {
            text = "Target demo tetap: LabWiFi-2026!\nMasukkan tebakan untuk belajar cara kerja pencocokan lokal. Ini bukan pengujian jaringan Wi-Fi."
            textSize = 13f
        })
        demoGuess = EditText(this).apply {
            hint = "Tebakan untuk password demo"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(demoGuess)
        root.addView(Button(this).apply {
            text = "Cocokkan dengan target demo"
            setOnClickListener {
                output.text = if (demoGuess.text.toString() == demoSecret) {
                    "COCOK — tebakan sama dengan password contoh lokal."
                } else {
                    "BELUM COCOK — tebakan berbeda dari password contoh lokal."
                }
            }
        })
        root.addView(section("4. Informasi Wi-Fi (opsional)"))
        root.addView(Button(this).apply {
            text = "Pindai jaringan sekitar"
            setOnClickListener { requestWifiScan() }
        })
        val qr = EditText(this).apply { hint = "Tempel teks QR Wi-Fi (WIFI:T:WPA;S:...;P:...;;)" }
        root.addView(qr)
        root.addView(Button(this).apply {
            text = "Baca metadata QR Wi-Fi"
            setOnClickListener { output.text = parseWifi(qr.text.toString()) }
        })
        output = TextView(this).apply { textSize = 14f; setPadding(0, 16, 0, 16) }
        root.addView(ScrollView(this).apply { addView(output) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun check(root: LinearLayout, label: String, checked: Boolean): CheckBox {
        val box = CheckBox(this).apply { text = label; isChecked = checked }
        root.addView(box)
        return box
    }

    private fun section(title: String) = TextView(this).apply {
        text = "\n$title"
        textSize = 18f
        setPadding(0, 12, 0, 4)
    }

    private fun updateEstimate(a: Boolean, b: Boolean, c: Boolean, d: Boolean, length: Int) {}

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
        return "Perkiraan kekuatan: $label\nPanjang: ${value.length} karakter\nCatatan: penilaian ini heuristik sederhana, bukan jaminan password tidak bisa ditebak."
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
