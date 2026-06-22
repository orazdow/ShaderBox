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

package io.oceanos.shaderbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import io.oceanos.shaderbox.database.Shader;
import io.oceanos.shaderbox.storage.ShaderBackupDocument;
import io.oceanos.shaderbox.storage.ShaderRepository;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class ExportActivity extends Activity {
    private static final int REQUEST_BACKUP_ALL = 1;
    private ShaderRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        repository = new ShaderRepository(this);
        findViewById(R.id.action_backup_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBackupDocument();
            }
        });
    }

    private void createBackupDocument() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(ShaderBackupDocument.MIME_TYPE);
        intent.putExtra(Intent.EXTRA_TITLE, ShaderBackupDocument.DEFAULT_FILENAME);
        startActivityForResult(intent, REQUEST_BACKUP_ALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_BACKUP_ALL || resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        try {
            List<Shader> shaders = repository.findAll();
            ShaderBackupDocument.write(getContentResolver(), uri, shaders);
            Toast.makeText(this, getString(R.string.backup_all_success, shaders.size()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.backup_all_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.backup_all_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
