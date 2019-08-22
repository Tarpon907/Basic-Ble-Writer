package com.swankdave.Tasker.BLEWriter;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.twofortyfouram.spackle.AppBuildInfo;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class EditActivity extends AbstractAppCompatPluginActivity {
    //private final static int REQUEST_ENABLE_BT = 1;
    Bundle CurrentBundle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                0);
    }


    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBnd, @NonNull final String previousBlurb) {
        BLEBundleManager previousBundle = new BLEBundleManager(previousBnd);
        ((EditText) findViewById(R.id.BLE_Device_Address)).setText(previousBundle.getDeviceAddress());
        ((EditText) findViewById(R.id.BLE_Service_Guid)).setText(previousBundle.getServiceGuid());
        ((EditText) findViewById(R.id.BLE_Characteristic_Guid)).setText(previousBundle.getCharacteristicGuid());
        ((EditText) findViewById(R.id.BLE_Value)).setText(previousBundle.getValue());
    }



    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        return true;
    }

    @Nullable
    @Override
    public Bundle getResultBundle() {
        final Bundle result = new Bundle();
        BLEBundleManager bundleInterface = new BLEBundleManager(result);

        bundleInterface.setDeviceAddress(((EditText) findViewById(R.id.BLE_Device_Address)).getText().toString());
        bundleInterface.setServiceGuid(((EditText) findViewById(R.id.BLE_Service_Guid)).getText().toString());
        bundleInterface.setCharacteristicGuid(((EditText) findViewById(R.id.BLE_Characteristic_Guid)).getText().toString());
        bundleInterface.setValue(((EditText) findViewById(R.id.BLE_Value)).getText().toString());
        result.putInt("com.swankdave.Tasker.BLEWriter.extra.INT_VERSION_CODE", AppBuildInfo.getVersionCode(getApplicationContext()));
        CurrentBundle = result;

        return result;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        BLEBundleManager BLEBundleManager = new BLEBundleManager(bundle);
        if (BLEBundleManager.getValue().isEmpty()) {
            return "Blank";
        }
        return BLEBundleManager.getValue();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
        }
        else if (R.id.menu_discard_changes == item.getItemId()) {
            // Signal to AbstractAppCompatPluginActivity that the user canceled.
            mIsCancelled = true;
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
