package com.webianks.bluechat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.support.v7.app.AlertDialog
import android.widget.*


class MainActivity : AppCompatActivity(), DevicesRecyclerViewAdapter.ItemClickListener {

    private val REQUEST_ENABLE_BT = 123
    private val TAG = javaClass.simpleName
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private val mDeviceList = arrayListOf<DeviceData>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter
    private var mBtAdapter: BluetoothAdapter? = null
    private val PERMISSION_REQUEST_LOCATION = 123
    private val PERMISSION_REQUEST_LOCATION_KEY = "PERMISSION_REQUEST_LOCATION"
    private var alreadyAskedForPermission = false
    private lateinit var headerLabel: TextView
    private lateinit var headerLabelContainer: LinearLayout

    private var mChatService: BluetoothChatService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.title = getString(R.string.nothing)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)

        val typeFace = Typeface.createFromAsset(assets, "fonts/product_sans.ttf")
        toolbarTitle.typeface = typeFace

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        headerLabel = findViewById(R.id.headerLabel)
        headerLabelContainer = findViewById(R.id.headerLabelContainer)

        headerLabelContainer.visibility = View.INVISIBLE

        if (savedInstanceState != null)
            alreadyAskedForPermission = savedInstanceState.getBoolean(PERMISSION_REQUEST_LOCATION_KEY, false)

        val llm = LinearLayoutManager(this)
        recyclerView.layoutManager = llm

        findViewById<Button>(R.id.search_devices).setOnClickListener {
            findDevices()
        }

        devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mDeviceList)
        recyclerView.adapter = devicesAdapter
        devicesAdapter.setItemClickListener(this)

        // Register for broadcasts when a device is discovered.
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = BluetoothChatService(this, mHandler)

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
                Log.d(TAG, "deviceName: $deviceName :: deviceHardwareAddress: $deviceHardwareAddress")
            }
        }
    }

    private fun checkPermissions() {

        if (alreadyAskedForPermission) {
            // don't check again because the dialog is still open
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.need_loc_access))
                builder.setMessage(getString(R.string.please_grant_loc_access))
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // the dialog will be opened so we have to save that
                    alreadyAskedForPermission = true
                    requestPermissions(arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ), PERMISSION_REQUEST_LOCATION)
                }
                builder.show()

            } else {
                startDiscovery()
            }
        } else {
            startDiscovery()
            alreadyAskedForPermission = true
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

        checkPermissions()

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    private fun startDiscovery() {

        headerLabelContainer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        headerLabel.text = getString(R.string.searching)
        mDeviceList.clear()

        // If we're already discovering, stop it
        if (mBtAdapter?.isDiscovering!!)
            mBtAdapter!!.cancelDiscovery()

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                val deviceData = DeviceData(deviceName, deviceHardwareAddress)
                mDeviceList.add(deviceData)

                devicesAdapter.notifyDataSetChanged()
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                progressBar.visibility = View.INVISIBLE
                headerLabel.text = getString(R.string.found)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressBar.visibility = View.INVISIBLE

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            //Bluetooth is now connected.

        }
        //label.setText("Bluetooth is now enabled.")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PERMISSION_REQUEST_LOCATION_KEY, alreadyAskedForPermission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {

            PERMISSION_REQUEST_LOCATION -> {
                // the request returned a result so the dialog is closed
                alreadyAskedForPermission = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "Coarse and fine location permissions granted")
                    startDiscovery()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.fun_limted))
                        builder.setMessage(getString(R.string.since_perm_not_granted))
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.show()
                    }
                }
            }
        }
    }

    override fun itemClicked(deviceData: DeviceData) {
        connectDevice(deviceData)
    }

    private fun connectDevice(deviceData: DeviceData) {

        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter?.cancelDiscovery()
        val deviceAddress = deviceData.deviceHardwareAddress

        val device = mBtAdapter?.getRemoteDevice(deviceAddress)
        // Attempt to connect to the device
        mChatService?.connect(device, true)

    }

    override fun onResume() {
        super.onResume()
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService?.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService?.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {

                    Log.d(TAG, "State changed ")

                    when (msg.arg1) {
                        BluetoothChatService.STATE_CONNECTED -> {
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName))
                            //mConversationArrayAdapter.clear()
                        }

                        BluetoothChatService.STATE_CONNECTING -> //setStatus(R.string.title_connecting)
                        {
                        }
                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                        } //setStatus(R.string.title_not_connected)

                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                    //mConversationArrayAdapter.add("Me:  " + writeMessage)
                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    //mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    //if (null != activity) {
                    // Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show()
                    // }
                }
                Constants.MESSAGE_TOAST -> {
                }//if (null != activity) {
            //Toast.makeText(activity, msg.data.getString(Constants.TOAST),
            // Toast.LENGTH_SHORT).show()
            }
        }
    }
}
