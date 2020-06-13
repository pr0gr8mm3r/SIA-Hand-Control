/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sia.wazynski.simon.handcontrol

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.tfe_pn_activity_camera.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.interfaces.BluetoothCallback
import me.aflak.bluetooth.interfaces.DeviceCallback
import me.aflak.bluetooth.interfaces.DiscoveryCallback

class MainActivity : AppCompatActivity(),
    PosenetFragment.PosenetDataCallback,
    BtDeviceDialog.BtDeviceDialogCallback,
    SettingsCallback {

    private var active: Boolean = true

    private val devices = mutableListOf<BluetoothDevice>()

    private var protocol = ArduinoProtocol.DEFAULT

    companion object {
        const val TAG = "CameraActivity"
    }

    private val bluetooth = Bluetooth(this)

    private var btDialog: BtDeviceDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        bluetooth.setBluetoothCallback(bluetoothCallback)
        bluetooth.setDiscoveryCallback(discoveryCallback)
        bluetooth.setDeviceCallback(deviceCallback)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.tfe_pn_activity_camera)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                PosenetFragment()
            )
            .commit()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        fabStop.setOnTouchListener { _, _ -> stopTracking(); true }
        fabResume.setOnClickListener { resumeTracking() }

        btnBt.setOnClickListener { openBtDeviceDialog() }
        btnSettings.setOnClickListener { openSettingsDialog() }
    }

    override fun onStart() {
        super.onStart()
        bluetooth.onStart()
        if (bluetooth.isEnabled) {
            scan()
        } else {
            bluetooth.enable()
            scan()
        }
    }

    override fun onStop() {
        super.onStop()
        bluetooth.stopScanning()
        bluetooth.onStop()
    }

    private fun stopTracking() {
        fabStop.visibility = View.GONE
        fabResume.visibility = View.VISIBLE

        active = false
    }

    private fun resumeTracking() {
        fabStop.visibility = View.VISIBLE
        fabResume.visibility = View.GONE

        active = true
    }

    private fun scan() {
        if (bluetooth.bluetoothAdapter != null) bluetooth.startScanning()
    }

    private fun toast(resId: Int) {
        runOnUiThread { Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show() }
    }

    private fun toast(string: String) {
        runOnUiThread { Toast.makeText(this, string, Toast.LENGTH_LONG).show() }
    }

    private fun openSettingsDialog() {
        SettingsDialog().show(
            supportFragmentManager,
            TAG
        )
    }

    private fun openBtDeviceDialog() {
        if (btDialog == null) btDialog =
            BtDeviceDialog()

        if (btDialog?.isVisible != true) btDialog?.show(
            supportFragmentManager,
            TAG
        )
    }

    private val bluetoothCallback: BluetoothCallback = object : BluetoothCallback {
        override fun onBluetoothTurningOn() = toast(R.string.bt_turning_on)
        override fun onBluetoothTurningOff() {}
        override fun onBluetoothOff() = toast(R.string.error_bt_not_available)
        override fun onUserDeniedActivation() = toast(R.string.error_bt_required)
        override fun onBluetoothOn() {
            scan()
            toast(R.string.bt_on)
        }
    }

    private val discoveryCallback: DiscoveryCallback = object : DiscoveryCallback {
        override fun onDevicePaired(device: BluetoothDevice?) {
            toast(R.string.bt_paired)
            if (device != null) connect(device)
        }

        override fun onDiscoveryStarted() {
            toast(R.string.bt_discovery_started)
            println("Discovery started")
        }

        override fun onDeviceUnpaired(device: BluetoothDevice?) {
        }

        override fun onError(errorCode: Int) {
            println("Error scannning, code $errorCode")
            toast(R.string.error_bt_device_scan_error)
        }

        override fun onDiscoveryFinished() {
            println("Discovery finished")
        }

        override fun onDeviceFound(device: BluetoothDevice?) {
            if (!bluetooth.isConnected) {
                openBtDeviceDialog()
            }
            if (device != null) {
                devices.add(device)
                btDialog?.setDevices(devices)
            }
        }

    }

    private val deviceCallback = object : DeviceCallback {
        override fun onDeviceDisconnected(device: BluetoothDevice?, message: String?) {

        }

        override fun onDeviceConnected(device: BluetoothDevice?) {
            toast(R.string.bt_device_connected)
        }

        override fun onConnectError(device: BluetoothDevice?, message: String?) {
            toast(getString(R.string.error_bt_device_connect_error) + ": " + message)
        }

        override fun onMessage(message: ByteArray?) {
            println(message)
        }

        override fun onError(errorCode: Int) {
            toast(R.string.error_bt_undefined)
        }

    }

    override fun onDeviceToConnect(device: BluetoothDevice) {
        println("Connect to ${device.name}")
        if (!bluetooth.pairedDevices.contains(device)) bluetooth.pair(device)
        else connect(device)
    }

    fun connect(device: BluetoothDevice) {
        bluetooth.connectToDevice(device)
    }

    override fun dataAvailable(x: Float, y: Float) {
        println("x: $x y: $y")

        val leftToMiddleFraction = (x * 2).coerceIn(0.0f, 1.0f)
        val middleToRightFraction = ((1.0f - x) * 2).coerceIn(0.0f, 1.0f)
        val motorLeft = (y * middleToRightFraction * 512).toInt()
        val motorRight = (y * leftToMiddleFraction * 512).toInt()
        val message: String? = when (protocol) {
            ArduinoProtocol.DEFAULT -> "#,888,$motorLeft,$motorRight,999"
            ArduinoProtocol.CUSTOM -> {
                val stringLeft = motorLeft.toString().padStart(3, '0')
                val stringRight = motorLeft.toString().padStart(3, '0')
                "$stringLeft,$stringRight;"
            }
        }

        println("btmessage: $message")
        if (bluetooth.isConnected && message != null) bluetooth.send(message)
    }

    override fun onProtocolSet(protocol: ArduinoProtocol) {
        this.protocol = protocol
    }

}