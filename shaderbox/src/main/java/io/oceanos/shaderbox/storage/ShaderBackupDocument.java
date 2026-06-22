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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static List<Shader> read(ContentResolver resolver, Uri uri)
            throws IOException, JSONException {
        JSONObject root = new JSONObject(readText(resolver, uri));
        if (!FORMAT.equals(root.getString("format"))) {
            throw new JSONException("Unsupported backup format");
        }
        if (root.getInt("version") != VERSION) {
            throw new JSONException("Unsupported backup version");
        }

        JSONArray items = root.getJSONArray("shaders");
        List<Shader> shaders = new ArrayList<Shader>();
        for (int i = 0; i < items.length(); i++) {
            shaders.add(fromJson(items.getJSONObject(i)));
        }
        return shaders;
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

    private static Shader fromJson(JSONObject item) throws JSONException {
        Shader shader = new Shader();
        shader.setName(item.getString("name"));
        shader.setText(item.getString("shader"));
        shader.setVrMode(item.getInt("vrmode"));
        shader.setPreviewMode(item.getInt("previewmode"));
        shader.setResolution(item.getInt("resolution"));
        shader.setCreated(item.optString("created", null));
        shader.setModified(item.optString("modified", null));
        shader.setThumb(decodeThumb(item.optString("thumb", "")));
        return shader;
    }

    private static String encodeThumb(byte[] thumb) {
        if (thumb == null || thumb.length == 0) return "";
        return Base64.encodeToString(thumb, Base64.NO_WRAP);
    }

    private static byte[] decodeThumb(String thumb) {
        if (thumb == null || thumb.length() == 0) return new byte[0];
        return Base64.decode(thumb, Base64.DEFAULT);
    }

    private static String readText(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IOException("Unable to read backup document");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int r; (r = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, r);
            }
            return out.toString("UTF-8");
        } finally {
            in.close();
        }
    }

    private static String currentUtcTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
