/*
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.customtiles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.WindowManager;

public class ScreenshotTile extends TileService {

    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_SERVICE =
            "com.android.systemui.screenshot.TakeScreenshotService";

    private Context mContext;
    private final Handler mHandler = new Handler();

    @Override
    public void onStartListening() {
        super.onStartListening();

        mContext = getApplicationContext();
        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        showDialog(screenShotDialog());
    }

    private void refresh() {
        getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_screenshot));
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }

    /**
     * functions needed for taking screenhots.
     */
    final Object mScreenshotLock = new Object();
    ServiceConnection mScreenshotConnection = null;

    final Runnable mScreenshotTimeout = new Runnable() {
        @Override public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                    mContext.unbindService(mScreenshotConnection);
                    mScreenshotConnection = null;
                }
            }
        }
    };

    private void takeScreenshot(final boolean partial) {
        synchronized (mScreenshotLock) {
            if (mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName(SYSUI_PACKAGE, SYSUI_SCREENSHOT_SERVICE);
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        msg.what = partial ? WindowManager.TAKE_SCREENSHOT_SELECTED_REGION
                                : WindowManager.TAKE_SCREENSHOT_FULLSCREEN;
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHandler.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        mContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        mHandler.removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;

                        /* wait for the dialog box to close */
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            // Do nothing
                        }

                        /* take the screenshot */
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            // Do nothing
                        }
                    }
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {}
            };
            if (mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                mHandler.postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }

    private Dialog screenShotDialog() {
        return new AlertDialog.Builder(getBaseContext())
                .setTitle(R.string.screenshot_label)
                .setIcon(R.drawable.ic_screenshot_dark)
                .setItems(R.array.screenshot_mode_entries,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int item) {
                                switch (item) {
                                    case 0:
                                        takeScreenshot(false); // full screenshot
                                        break;
                                    case 1:
                                        takeScreenshot(true); // partial screenshot
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
    }
}
