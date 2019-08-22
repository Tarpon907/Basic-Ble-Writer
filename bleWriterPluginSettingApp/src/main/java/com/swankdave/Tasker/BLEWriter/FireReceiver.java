package com.swankdave.Tasker.BLEWriter;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver;

public final class FireReceiver extends AbstractPluginSettingReceiver {

    @Override
    protected boolean isBundleValid(@NonNull final Bundle bundle) {
        return true;
    }

    @Override
    protected boolean isAsync() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void firePluginSetting(@NonNull final Context context, @NonNull final Bundle bundle) {
        BundleExecutor.Execute(context, bundle);
    }
}
