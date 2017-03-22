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

import android.content.Context;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class PowerMenuTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        sendEvent(1500, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            // Do nothing
        }
        /* We need to send action up on the power key now to release the wakelock */
        sendEvent(0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_POWER);
    }

    private void refresh() {
        getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_power));
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }

    private void sendEvent(int time, int flag, int action) {
        KeyEvent keyEvent = new KeyEvent(time, 0,
                flag, action, 0);
        InputManager.getInstance().injectInputEvent(keyEvent,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }
}
