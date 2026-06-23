/*
 *    Copyright 2015 Rui Gil
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.oceanos.shaderbox.opengl;

import android.content.Context;
import android.content.SharedPreferences;

public class ShaderTouchState {
    private static final String PREFS_NAME = "shader_touch_state";
    private static final String KEY_X = "touch_x_";
    private static final String KEY_Y = "touch_y_";

    private ShaderTouchState() {}

    public static void save(Context context, long shaderId, float x, float y) {
        if (shaderId <= 0) return;
        preferences(context).edit()
                .putFloat(KEY_X + shaderId, x)
                .putFloat(KEY_Y + shaderId, y)
                .apply();
    }

    public static boolean applyTo(Context context, long shaderId, ShaderRenderer renderer) {
        if (shaderId <= 0) return false;
        SharedPreferences preferences = preferences(context);
        String xKey = KEY_X + shaderId;
        String yKey = KEY_Y + shaderId;
        if (!preferences.contains(xKey) || !preferences.contains(yKey)) return false;

        renderer.setTouch(preferences.getFloat(xKey, 0f), preferences.getFloat(yKey, 0f));
        return true;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
