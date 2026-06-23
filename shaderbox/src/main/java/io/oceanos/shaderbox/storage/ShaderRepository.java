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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderRepository {
    private static final Pattern COPY_PREFIX_PATTERN = Pattern.compile("^(Copy of\\s+)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_SUFFIX_PATTERN = Pattern.compile("^(.*)\\s+\\((\\d+)\\)$");

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

    public String nextCopyName(String name) {
        String baseName = copyBaseName(name);
        Set<String> existingNames = new HashSet<String>();
        for (Shader shader : findAll()) {
            existingNames.add(shader.getName());
        }

        int suffix = 1;
        String copyName;
        do {
            copyName = baseName + " (" + suffix + ")";
            suffix++;
        } while (existingNames.contains(copyName));
        return copyName;
    }

    public int countDuplicateNames(List<Shader> shaders) {
        ShaderDatabase database = new ShaderDatabase(context);
        Cursor cursor = database.findAll();
        try {
            Set<String> existingNames = new HashSet<String>();
            while (cursor.moveToNext()) {
                existingNames.add(cursor.getString(cursor.getColumnIndex(ShaderDatabase.COLUMN_NAME)));
            }

            int duplicates = 0;
            for (Shader shader : shaders) {
                if (existingNames.contains(shader.getName())) duplicates++;
            }
            return duplicates;
        } finally {
            cursor.close();
            database.close();
        }
    }

    public ImportResult importBackup(List<Shader> shaders, boolean replaceDuplicates) {
        ShaderDatabase database = new ShaderDatabase(context);
        int replaced = 0;
        int imported = 0;
        int skipped = 0;
        try {
            Set<String> existingNames = readExistingNames(database);
            for (Shader shader : shaders) {
                boolean duplicate = existingNames.contains(shader.getName());
                if (duplicate && !replaceDuplicates) {
                    skipped++;
                    continue;
                }

                if (duplicate) replaced += database.deleteByName(shader.getName());
                database.insertBackup(shader.getBackupContentValues());
                existingNames.add(shader.getName());
                imported++;
            }
            return new ImportResult(imported, replaced, skipped);
        } finally {
            database.close();
        }
    }

    private Set<String> readExistingNames(ShaderDatabase database) {
        Cursor cursor = database.findAll();
        try {
            Set<String> names = new HashSet<String>();
            while (cursor.moveToNext()) {
                names.add(cursor.getString(cursor.getColumnIndex(ShaderDatabase.COLUMN_NAME)));
            }
            return names;
        } finally {
            cursor.close();
        }
    }

    private String copyBaseName(String name) {
        String baseName = name != null ? name.trim() : "";
        baseName = COPY_PREFIX_PATTERN.matcher(baseName).replaceFirst("").trim();

        Matcher matcher = NUMBER_SUFFIX_PATTERN.matcher(baseName);
        if (matcher.matches()) baseName = matcher.group(1).trim();

        if (baseName.length() == 0) return "Shader";
        return baseName;
    }

    public static class ImportResult {
        private final int imported;
        private final int replaced;
        private final int skipped;

        public ImportResult(int imported, int replaced, int skipped) {
            this.imported = imported;
            this.replaced = replaced;
            this.skipped = skipped;
        }

        public int getImported() {
            return imported;
        }

        public int getReplaced() {
            return replaced;
        }

        public int getSkipped() {
            return skipped;
        }
    }
}
