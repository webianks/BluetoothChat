package com.webianks.bluechat

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.content.BroadcastReceiver
import android.graphics.Typeface
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.support.v7.app.AlertDialog



class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 123
    private val TAG = javaClass.simpleName
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private val mList = arrayListOf<String>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter
    private var  mBtAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.title = getString(R.string.nothing)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)

        val typeFace = Typeface.createFromAsset(assets,"fonts/product_sans.ttf")
        toolbarTitle.typeface = typeFace

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        val llm = LinearLayoutManager(this)
        recyclerView.layoutManager = llm

        findViewById<Button>(R.id.search_devices).setOnClickListener{
            findDevices()
        }

        devicesAdapter = DevicesRecyclerViewAdapter(context = this,mList = mList)
        recyclerView.adapter = devicesAdapter

        // Register for broadcasts when a device is discovered.
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBtAdapter == null)
            showAlertAndExit()

        if (!mBtAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Get a set of currently paired devices
        val pairedDevices = mBtAdapter?.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices?.size!! > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                Log.d(TAG,"deviceName: $deviceName :: deviceHardwareAddress: $deviceHardwareAddress")
            }
        }

    }

    private fun showAlertAndExit() {

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.not_compatible))
                .setMessage(getString(R.string.no_support))
                .setPositiveButton("Exit", { _, _ -> System.exit(0) })
                .show()
    }

    private fun findDevices() {

        progressBar.visibility = View.VISIBLE

        // If we're already discovering, stop it
        if (mBtAdapter?.isDiscovering!!) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()

       /* val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)*/
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val mReceiver = object: BroadcastReceiver() {

        override fun onReceive(context : Context,intent : Intent) {

            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                Log.d(TAG,"Found this device ==>>> $device with address ==>> $deviceHardwareAddress")

                mList.add(deviceName)

                devicesAdapter.notifyDataSetChanged()

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressBar.visibility = View.INVISIBLE

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            //Bluetooth is now connected.

        }
            //label.setText("Bluetooth is now enabled.")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}
