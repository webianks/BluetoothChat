package com.webianks.bluechat

/**
 * Created by ramankit on 20/7/17.
 */

data class DeviceData(val deviceName: String?,val deviceHardwareAddress: String){

    override fun equals(other: Any?): Boolean {
        val deviceData = other as DeviceData
        return deviceHardwareAddress == deviceData.deviceHardwareAddress
    }

    override fun hashCode(): Int {
        return deviceHardwareAddress.hashCode()
    }

}