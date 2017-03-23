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

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.WindowManagerPolicyControl;

public class ExpandedDesktopTile extends TileService {

    private boolean mIsGloballyExpanded;

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refresh();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();

        mIsGloballyExpanded = isGloballyExpanded(getContentResolver());
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL),
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
        if (mIsGloballyExpanded) {
            userConfigurableSettings();
        } else {
            enableForAll();
        }
    }

    private void writeValue(String value) {
        Settings.Global.putString(getContentResolver(),
             Settings.Global.POLICY_CONTROL, value);
    }

    private void enableForAll() {
        mIsGloballyExpanded = true;
        writeValue("immersive.full=*");
    }

    private void userConfigurableSettings() {
        mIsGloballyExpanded = false;
        writeValue("");
        WindowManagerPolicyControl.reloadStyleFromSetting(this,
                Settings.Global.POLICY_CONTROL_STYLE);
    }

    private boolean isGloballyExpanded(ContentResolver cr) {
        final String value = Settings.Global.getString(cr, Settings.Global.POLICY_CONTROL);
        if ("immersive.full=*".equals(value)) {
            return true;
        }
        return false;
    }

    private int getExpandedDesktopStyle() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.POLICY_CONTROL_STYLE,
                WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_FULL);
    }

    private void refresh() {
        int state = getExpandedDesktopStyle();
        if (mIsGloballyExpanded) {
            switch (state) {
                case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_STATUS:
                    getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_expanded_desktop_hidestatusbar));
                    break;
                case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_NAVIGATION:
                    getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_expanded_desktop_hidenavbar));
                    break;
                case WindowManagerPolicyControl.ImmersiveDefaultStyles.IMMERSIVE_FULL:
                    getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_expanded_desktop));
                    break;
            }
        } else {
           getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_expanded_desktop_off));
        }
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }
}
