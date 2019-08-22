package com.swankdave.Tasker.BLEWriter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import java.util.UUID;

/*
 * This class manages storage and retrieval of values from the bundle, in a strongly referenced manner
 */

class BLEBundleManager {
    private Bundle bundle;

    BLEBundleManager(Bundle bundle){
        this.bundle = bundle;
    }

    String getDeviceAddress() { return bundle.getString("BLE_Address", ""); }
    void setDeviceAddress(String address){ bundle.putString("BLE_Address", address); }

    String getServiceGuid() { return bundle.getString("BLE_Service_Guid", ""); }
    void setServiceGuid(String address){ bundle.putString("BLE_Service_Guid", address); }

    String getCharacteristicGuid() { return bundle.getString("BLE_Characteristic_Guid", ""); }
    void setCharacteristicGuid(String address){ bundle.putString("BLE_Characteristic_Guid", address); }

    String getValue(){ return bundle.getString("BLE_Value", "");}
    void setValue(String value){ bundle.putString("BLE_Value", value);}

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothGatt gattService) {
        return gattService
                .getService(UUID.fromString(getServiceGuid()))
                .getCharacteristic(UUID.fromString(getCharacteristicGuid()));
    }

}
