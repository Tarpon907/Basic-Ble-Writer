package com.swankdave.Tasker.BLEWriter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BundleExecutor extends BluetoothGattCallback {

    private BLEBundleManager currentBLEBundleManager;
    private Queue<BLEBundleManager> todo = new ConcurrentLinkedQueue<>();
    private BluetoothGatt gattDevice;
    private static Semaphore activeConversationLock = new Semaphore(1);
    static BundleExecutor executor;

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private BundleExecutor(Context context, Bundle bundle){
        currentBLEBundleManager = new BLEBundleManager(bundle);
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(currentBLEBundleManager.getDeviceAddress());
        gattDevice =  device.connectGatt(context, true, this);
        executor = this;
    }

    private boolean isCurrentDeviceKnownToBtSubsystem(BluetoothManager manager){
        if (gattDevice == null)
            return false;
        if (manager == null)
            return false;
        for (BluetoothDevice device : manager.getConnectedDevices(BluetoothProfile.GATT))
            if (gattDevice.getDevice().getAddress().equals(device.getAddress()))
                return true;
        return false;
    }

    static void Execute(Context context, Bundle bundle){
        BLEBundleManager BLEBundleManager = new BLEBundleManager(bundle);
        try {
            if (executor==null) {
                activeConversationLock.acquire();
                executor = new BundleExecutor(context, bundle);
                return;
            }
            executor.todo.add(BLEBundleManager);


            if (!activeConversationLock.tryAcquire())
                //connection exists, bundle queued at #"+ executor.todo.size()
                return;
            try {
                //using cached device connection: executor.gattDevice
                if (executor.isCurrentDeviceKnownToBtSubsystem((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)))
                    executor.Play(executor.todo.remove(), executor.gattDevice);
            }catch (Exception ex)
            {
                //Current connection is assumed dead, rebuilding...
                executor = new BundleExecutor(context, bundle);
            }
        } catch (Exception ex)
        {
            Log.e("BLEED", "FireReceiver.BundleExecutor.Execute Died", ex);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        try {
            super.onConnectionStateChange(gatt, status, newState);

            if (executor==null)
                //no executor!, bailing
                return;

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
                if (gatt.getServices().size() == 0)
                    gatt.discoverServices();
                else
                    Play(currentBLEBundleManager, gatt);
            else if (newState == BluetoothGatt.STATE_DISCONNECTED || status == BluetoothGatt.GATT_FAILURE) {
                activeConversationLock.release();
                gatt.close();
                executor = null;
            }
        }catch (Exception ex){
            Log.d("BLEED", "FireReceiver.firePluginSetting.onConnectionStateChange is off script, dropping connection",ex);
            activeConversationLock.release();
            if (!(gattDevice == null))
                if (newState != BluetoothGatt.STATE_DISCONNECTED)
                    gattDevice.disconnect();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try{
            gattDevice = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS)
                Play(currentBLEBundleManager, gatt);
        }catch (Exception ex){
            Log.d("BLEED", "FireReceiver.firePluginSetting.onServicesDiscovered is off script, dropping connection", ex);
            activeConversationLock.release();
            if (!(gattDevice == null))
                gattDevice.disconnect();
        }
    }


    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        try {
            gattDevice = gatt;
            if (status != BluetoothGatt.GATT_SUCCESS){
                //write failed, retrying
                Play(currentBLEBundleManager, gattDevice);
                return;
            }
            //write successful
            if (!todo.isEmpty()) {
                Play(todo.remove(), gattDevice);
            } else {
                // nothing more to do, releasing activeConversationLock
                activeConversationLock.release();
            }
        }catch (Exception ex){
            Log.d("BLEED", "FireReceiver.firePluginSetting.onCharacteristicWrite is off script, dropping connection", ex);
            activeConversationLock.release();
            if (!(gattDevice == null))
                gattDevice.disconnect();

        }
    }

    private static BluetoothGattCharacteristic BuildCharacteristic(BLEBundleManager BLEBundleManager, BluetoothGatt gattService) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = BLEBundleManager.getBluetoothGattCharacteristic(gattService);
        bluetoothGattCharacteristic.setValue(hexStringToByteArray(BLEBundleManager.getValue()));
        bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        return bluetoothGattCharacteristic;
    }

    private void Play(BLEBundleManager BLEBundleManager, BluetoothGatt gattService) {
        try{
            currentBLEBundleManager = BLEBundleManager;
            int i = 0;
            while (!gattService.writeCharacteristic(BuildCharacteristic(BLEBundleManager, gattService)) & (i < 4)) {
                //Log.d("BLEED", "FireReceiver.firePluginSetting.play: write was not accepted");
                i = i + 1;
                Thread.sleep(250);
            }
            //if (i < 4)
            //    Log.d("BLEED", "FireReceiver.firePluginSetting.play: write was accepted");
        }catch (Exception ex){
            Log.d("BLEED", "FireReceiver.firePluginSetting.play is off script, dropping connection", ex);
            activeConversationLock.release();
            if (!(gattDevice == null))
                gattDevice.disconnect();
        }
    }
}
