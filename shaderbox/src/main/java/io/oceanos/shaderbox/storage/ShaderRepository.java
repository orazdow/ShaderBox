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

import android.content.Context;
import android.database.Cursor;
import io.oceanos.shaderbox.database.Shader;
import io.oceanos.shaderbox.database.ShaderDatabase;

import java.util.ArrayList;
import java.util.List;

public class ShaderRepository {
    private final Context context;

    public ShaderRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<Shader> findAll() {
        ShaderDatabase database = new ShaderDatabase(context);
        Cursor cursor = database.findAll();
        try {
            List<Shader> shaders = new ArrayList<Shader>();
            while (cursor.moveToNext()) {
                shaders.add(Shader.getValues(cursor));
            }
            return shaders;
        } finally {
            cursor.close();
            database.close();
        }
    }

    public Shader findById(long id) {
        ShaderDatabase database = new ShaderDatabase(context);
        Cursor cursor = database.findById(id);
        try {
            if (cursor.moveToFirst()) return Shader.getValues(cursor);
            return null;
        } finally {
            cursor.close();
            database.close();
        }
    }

    public Shader createDefaultShader() {
        ShaderDatabase database = new ShaderDatabase(context);
        try {
            return findById(database.newShader());
        } finally {
            database.close();
        }
    }

    public Shader createShaderFromText(String text) {
        Shader shader = createDefaultShader();
        if (shader == null) return null;
        shader.setText(text != null ? text : "");
        update(shader);
        return shader;
    }

    public long insert(Shader shader) {
        ShaderDatabase database = new ShaderDatabase(context);
        try {
            return database.insert(shader.getContentValues());
        } finally {
            database.close();
        }
    }

    public void update(Shader shader) {
        ShaderDatabase database = new ShaderDatabase(context);
        try {
            database.update(shader.getContentValues());
        } finally {
            database.close();
        }
    }

    public void delete(long id) {
        ShaderDatabase database = new ShaderDatabase(context);
        try {
            database.delete(id);
        } finally {
            database.close();
        }
    }
}
