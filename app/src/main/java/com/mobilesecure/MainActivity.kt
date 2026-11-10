package com.mobilesecure

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var numberInput: EditText
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(24))
            setBackgroundColor(0xFFF5F7FB.toInt())
        }

        page.addView(TextView(this).apply {
            text = "MobileSecure"
            textSize = 28f
            setTextColor(0xFF14213D.toInt())
            gravity = Gravity.CENTER
        }, matchWrap())

        page.addView(TextView(this).apply {
            text = "Your personal mobile security dashboard"
            textSize = 14f
            setTextColor(0xFF526078.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(20))
        }, matchWrap())

        numberInput = EditText(this).apply {
            hint = "Enter a number to filter your local call log"
            textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        page.addView(numberInput, matchWrap())

        page.addView(makeButton("Grant device permissions") {
            requestPermissions(arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 100)
        }, matchWrap())

        page.addView(makeButton("View my call history") { showCallHistory() }, matchWrap())
        page.addView(makeButton("View SIM and network information") { showSimInfo() }, matchWrap())
        page.addView(makeButton("Find my phone location") { showMyLocation() }, matchWrap())
        page.addView(makeButton("View device information") { showDeviceInfo() }, matchWrap())

        output = TextView(this).apply {
            text = "Welcome. Grant permissions, then choose a feature above.\n\nPrivacy: information is read locally from this phone and is not uploaded."
            textSize = 14f
            setTextColor(0xFF172033.toInt())
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        page.addView(output, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })

        scroll.addView(page)
        setContentView(scroll)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }

    private fun allowed(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun showCallHistory() {
        if (!allowed(Manifest.permission.READ_CALL_LOG)) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 101)
            return
        }
        val entered = numberInput.text.toString().filter { it.isDigit() }
        val lines = mutableListOf<String>()
        try {
            val projection = arrayOf(
                CallLog.Calls.NUMBER, CallLog.Calls.DATE,
                CallLog.Calls.TYPE, CallLog.Calls.DURATION
            )
            val cursor: Cursor? = contentResolver.query(
                CallLog.Calls.CONTENT_URI, projection, null, null,
                "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                val ni = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val di = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val ti = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val du = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                while (it.moveToNext() && lines.size < 100) {
                    val number = it.getString(ni) ?: "Unknown"
                    val digits = number.filter(Char::isDigit)
                    if (entered.isNotBlank() &&
                        !digits.endsWith(entered.takeLast(7)) &&
                        !entered.endsWith(digits.takeLast(7))) continue
                    val kind = when (it.getInt(ti)) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        CallLog.Calls.REJECTED_TYPE -> "Rejected"
                        CallLog.Calls.BLOCKED_TYPE -> "Blocked"
                        else -> "Other"
                    }
                    lines.add("$kind • $number\n${DateFormat.getDateTimeInstance().format(Date(it.getLong(di)))} • ${it.getLong(du)} seconds")
                }
            }
            output.text = if (lines.isEmpty()) "No matching call records were found on this phone."
                else "MY DEVICE CALL HISTORY (up to 100 records)\n\n" + lines.joinToString("\n\n")
        } catch (e: SecurityException) {
            output.text = "Call-log permission was denied or restricted by Android."
        } catch (e: Exception) {
            output.text = "Could not read call history: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    private fun showSimInfo() {
        if (!allowed(Manifest.permission.READ_PHONE_STATE)) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 102)
            return
        }
        try {
            val tm = getSystemService(TelephonyManager::class.java)
            val state = when (tm.simState) {
                TelephonyManager.SIM_STATE_READY -> "Ready"
                TelephonyManager.SIM_STATE_ABSENT -> "No SIM detected"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
                else -> "Unavailable / unknown"
            }
            output.text = """
                SIM AND NETWORK INFORMATION

                SIM state: $state
                SIM operator: ${tm.simOperatorName.ifBlank { "Not available" }}
                Network operator: ${tm.networkOperatorName.ifBlank { "Not available" }}
                Network country: ${tm.networkCountryIso.ifBlank { "Not available" }}
                Phone type: ${phoneType(tm.phoneType)}

                Android may hide subscriber numbers and SIM identifiers from ordinary apps.
            """.trimIndent()
        } catch (e: Exception) {
            output.text = "Could not read SIM information: ${e.localizedMessage ?: "permission or device restriction"}"
        }
    }

    private fun phoneType(type: Int) = when (type) {
        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
        TelephonyManager.PHONE_TYPE_SIP -> "SIP"
        else -> "None / unknown"
    }

    private fun showMyLocation() {
        if (!allowed(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !allowed(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 103)
            return
        }
        try {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager
            var best: Location? = null
            for (provider in manager.getProviders(true)) {
                val candidate = try { manager.getLastKnownLocation(provider) }
                    catch (_: SecurityException) { null }
                if (candidate != null && (best == null || candidate.time > best!!.time)) best = candidate
            }
            output.text = if (best == null) {
                "No recent location is cached. Turn on Location, wait briefly, and try again. This version reads the last known location; it does not continuously track."
            } else {
                """
                    MY PHONE LOCATION

                    Latitude: ${best.latitude}
                    Longitude: ${best.longitude}
                    Accuracy: ${best.accuracy} metres
                    Provider: ${best.provider ?: "Unknown"}
                    Recorded: ${DateFormat.getDateTimeInstance().format(Date(best.time))}

                    Location remains on this device in this starter version.
                """.trimIndent()
            }
        } catch (e: Exception) {
            output.text = "Could not get location: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    private fun showDeviceInfo() {
        output.text = """
            DEVICE INFORMATION

            Manufacturer: ${android.os.Build.MANUFACTURER}
            Model: ${android.os.Build.MODEL}
            Android version: ${android.os.Build.VERSION.RELEASE}
            SDK level: ${android.os.Build.VERSION.SDK_INT}
            App version: 1.0
        """.trimIndent()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(
            this,
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED })
                "Permission updated. Select the feature again."
            else "Permission not granted; that feature will be unavailable.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
