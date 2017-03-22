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

import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class BrightnessTile extends TileService {

    private static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
    private static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
    private static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refresh();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                false, mObserver);

        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onClick() {
        super.onClick();

        toggleState();
        refresh();
    }

    protected void toggleState() {
        int mode = getBrightnessState();
        switch (mode) {
            case SCREEN_BRIGHTNESS_MODE_MANUAL:
                Settings.System.putInt(getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                break;
            case SCREEN_BRIGHTNESS_MODE_AUTOMATIC:
                Settings.System.putInt(getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
                break;
        }
    }

    private int getBrightnessState() {
        return Settings.System.getIntForUser(getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
            UserHandle.USER_CURRENT);
    }

    private void refresh() {
        boolean autoBrightness =
            getBrightnessState() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        if (autoBrightness) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_brightness_auto_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_brightness_auto_off));
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        getQsTile().updateTile();
    }
}
