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
import io.oceanos.shaderbox.database.Shader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShaderDocument {
    public static final String MIME_TYPE_TEXT = "text/plain";

    private final String title;
    private final String text;

    public ShaderDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public static ShaderDocument fromShader(Shader shader) {
        return new ShaderDocument(shader.getName(), shader.getText());
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public static String readText(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IOException("Unable to open shader document");

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

    public void writeTo(ContentResolver resolver, Uri uri) throws IOException {
        OutputStream out = resolver.openOutputStream(uri);
        if (out == null) throw new IOException("Unable to write shader document");

        try {
            out.write(text.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }
}
