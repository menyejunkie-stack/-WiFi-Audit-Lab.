package com.example.wifiauditlab

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.*
import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : Activity() {
    private val random = SecureRandom()
    private val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lower = "abcdefghijklmnopqrstuvwxyz"
    private val digits = "0123456789"
    private val symbols = "!@#$%^&*()-_=+[]{};:,.<>?/|\\\\~`"
    private val demoTarget = "LabWiFi-2026!"
    private val cancelled = AtomicBoolean(false)
    private lateinit var status: TextView
    private lateinit var lengthBar: SeekBar
    private lateinit var lengthText: TextView
    private lateinit var output: EditText
    private lateinit var attemptsInput: EditText
    private lateinit var startButton: Button
    private lateinit var cancelButton: Button
    private lateinit var upperBox: CheckBox
    private lateinit var lowerBox: CheckBox
    private lateinit var digitBox: CheckBox
    private lateinit var symbolBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        page.addView(TextView(this).apply {
            text = "WiFi Audit Lab"
            textSize = 26f
        })
        page.addView(TextView(this).apply {
            text = "Laboratorium kombinasi password — mode offline"
            textSize = 15f
        })
        page.addView(TextView(this).apply {
            text = "\nSemua percobaan hanya membandingkan kandidat dengan password contoh yang tertanam di aplikasi. Tidak ada koneksi, percobaan login, atau pengiriman data ke jaringan Wi-Fi."
            textSize = 13f
        })
        page.addView(section("1. Atur karakter"))
        upperBox = checkbox(page, "Huruf besar A-Z", true)
        lowerBox = checkbox(page, "Huruf kecil a-z", true)
        digitBox = checkbox(page, "Angka 0-9", true)
        symbolBox = checkbox(page, "Simbol", true)
        lengthText = TextView(this).apply { text = "Panjang kandidat: 13" }
        page.addView(lengthText)
        lengthBar = SeekBar(this).apply { max = 30; progress = 11 }
        page.addView(lengthBar)
        val estimate = TextView(this).apply { textSize = 13f }
        page.addView(estimate)
        val updateEstimate = {
            val pool = selectedPool()
            val length = lengthBar.progress + 2
            val total = if (pool.isEmpty()) BigInteger.ZERO else BigInteger.valueOf(pool.length.toLong()).pow(length)
            lengthText.text = "Panjang kandidat: $length"
            estimate.text = "Ukuran alfabet: ${pool.length} karakter\nRuang kombinasi: ${total}"
        }
        listOf(upperBox, lowerBox, digitBox, symbolBox).forEach {
            it.setOnCheckedChangeListener { _, _ -> updateEstimate() }
        }
        lengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateEstimate()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        updateEstimate()

        page.addView(section("2. Generator password"))
        output = EditText(this).apply {
            hint = "Password acak akan muncul di sini"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        page.addView(output)
        page.addView(Button(this).apply {
            text = "Buat password acak"
            setOnClickListener {
                val pool = selectedPool()
                if (pool.isEmpty()) {
                    status.text = "Pilih minimal satu kelompok karakter."
                } else {
                    val length = lengthBar.progress + 2
                    output.setText(buildString { repeat(length) { append(pool[random.nextInt(pool.length)]) } })
                    status.text = "Password acak dibuat di perangkat."
                }
            }
        })
        page.addView(Button(this).apply {
            text = "Salin password"
            setOnClickListener {
                val value = output.text.toString()
                if (value.isNotEmpty()) {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Password", value))
                    status.text = "Password disalin."
                }
            }
        })

        page.addView(section("3. Pengujian kombinasi berulang (offline)"))
        page.addView(TextView(this).apply {
            text = "Target demo lokal: $demoTarget\nTarget ini hanya data latihan bawaan aplikasi dan tidak bisa diganti menjadi password Wi-Fi sungguhan."
            textSize = 13f
        })
        attemptsInput = EditText(this).apply {
            hint = "Batas percobaan (contoh 100000)"
            setText("100000")
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        page.addView(attemptsInput)
        startButton = Button(this).apply {
            text = "Mulai pengujian lokal"
            setOnClickListener { startOfflineTest() }
        }
        page.addView(startButton)
        cancelButton = Button(this).apply {
            text = "Hentikan"
            isEnabled = false
            setOnClickListener { cancelled.set(true); status.text = "Menghentikan pengujian..." }
        }
        page.addView(cancelButton)

        page.addView(section("4. Pemeriksa kekuatan password"))
        val strengthInput = EditText(this).apply {
            hint = "Masukkan password untuk dianalisis"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        page.addView(strengthInput)
        page.addView(Button(this).apply {
            text = "Analisis kekuatan"
            setOnClickListener { status.text = strength(strengthInput.text.toString()) }
        })

        status = TextView(this).apply {
            text = "Status: siap"
            textSize = 14f
            setPadding(0, 20, 0, 20)
        }
        page.addView(status)
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun section(label: String) = TextView(this).apply {
        text = "\n$label"
        textSize = 19f
        setPadding(0, 12, 0, 4)
    }

    private fun checkbox(parent: LinearLayout, label: String, checked: Boolean): CheckBox {
        val item = CheckBox(this).apply { text = label; isChecked = checked }
        parent.addView(item)
        return item
    }

    private fun selectedPool(): String = buildString {
        if (upperBox.isChecked) append(upper)
        if (lowerBox.isChecked) append(lower)
        if (digitBox.isChecked) append(digits)
        if (symbolBox.isChecked) append(symbols)
    }.toSet().joinToString("")

    private fun startOfflineTest() {
        val pool = selectedPool()
        if (pool.isEmpty()) {
            status.text = "Pilih minimal satu kelompok karakter."
            return
        }
        val maxAttempts = attemptsInput.text.toString().toLongOrNull()
        if (maxAttempts == null || maxAttempts !in 1..5_000_000) {
            status.text = "Masukkan batas percobaan antara 1 sampai 5.000.000."
            return
        }
        val length = lengthBar.progress + 2
        cancelled.set(false)
        startButton.isEnabled = false
        cancelButton.isEnabled = true
        status.text = "Pengujian lokal dimulai..."
        Thread {
            val indexes = IntArray(length)
            var tried = 0L
            var found = false
            while (tried < maxAttempts && !cancelled.get()) {
                val candidate = buildString(length) { indexes.forEach { append(pool[it]) } }
                tried++
                if (candidate == demoTarget) {
                    found = true
                    break
                }
                var position = indexes.lastIndex
                while (position >= 0) {
                    indexes[position]++
                    if (indexes[position] < pool.length) break
                    indexes[position] = 0
                    position--
                }
                if (position < 0) break
                if (tried % 10000L == 0L) {
                    val progress = tried
                    runOnUiThread { status.text = "Menguji kandidat lokal: $progress / $maxAttempts" }
                }
            }
            val result = when {
                found -> "COCOK! Target demo ditemukan setelah $tried percobaan."
                cancelled.get() -> "Pengujian dihentikan. Total kandidat diperiksa: $tried."
                else -> "Target demo belum ditemukan. Total kandidat diperiksa: $tried."
            }
            runOnUiThread {
                status.text = result
                startButton.isEnabled = true
                cancelButton.isEnabled = false
            }
        }.start()
    }

    private fun strength(value: String): String {
        if (value.isEmpty()) return "Masukkan password terlebih dahulu."
        var score = 0
        if (value.length >= 12) score++
        if (value.any(Char::isLowerCase) && value.any(Char::isUpperCase)) score++
        if (value.any(Char::isDigit)) score++
        if (value.any { !it.isLetterOrDigit() }) score++
        val level = when (score) { 0, 1 -> "Lemah"; 2 -> "Sedang"; 3 -> "Kuat"; else -> "Sangat kuat" }
        return "Perkiraan kekuatan: $level\nPanjang: ${value.length} karakter\nAnalisis ini heuristik sederhana, bukan jaminan keamanan."
    }
}
