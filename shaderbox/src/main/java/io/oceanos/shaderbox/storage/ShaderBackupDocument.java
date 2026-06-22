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

package io.oceanos.shaderbox.storage;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Base64;
import io.oceanos.shaderbox.database.Shader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ShaderBackupDocument {
    public static final String MIME_TYPE = "application/json";
    public static final String DEFAULT_FILENAME = "shaderbox-backup.shaderbox.json";
    private static final String FORMAT = "io.oceanos.shaderbox.backup";
    private static final int VERSION = 1;

    public static void write(ContentResolver resolver, Uri uri, List<Shader> shaders)
            throws IOException, JSONException {
        OutputStream out = resolver.openOutputStream(uri);
        if (out == null) throw new IOException("Unable to write backup document");

        try {
            out.write(toJson(shaders).toString(2).getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private static JSONObject toJson(List<Shader> shaders) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("format", FORMAT);
        root.put("version", VERSION);
        root.put("exportedAt", currentUtcTime());

        JSONArray items = new JSONArray();
        for (Shader shader : shaders) {
            items.put(toJson(shader));
        }
        root.put("shaders", items);
        return root;
    }

    private static JSONObject toJson(Shader shader) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("name", shader.getName());
        item.put("shader", shader.getText());
        item.put("vrmode", shader.getVrMode());
        item.put("previewmode", shader.getPreviewMode());
        item.put("resolution", shader.getResolution());
        item.put("created", shader.getCreated());
        item.put("modified", shader.getModified());
        item.put("thumb", encodeThumb(shader.getThumb()));
        return item;
    }

    private static String encodeThumb(byte[] thumb) {
        if (thumb == null || thumb.length == 0) return "";
        return Base64.encodeToString(thumb, Base64.NO_WRAP);
    }

    private static String currentUtcTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
