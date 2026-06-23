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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
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

public class ImportActivity extends Activity {
    private static final int REQUEST_IMPORT_BACKUP = 1;
    private ShaderRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        repository = new ShaderRepository(this);
        findViewById(R.id.action_import_backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBackupDocument();
            }
        });
    }

    private void openBackupDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(ShaderBackupDocument.MIME_TYPE);
        startActivityForResult(intent, REQUEST_IMPORT_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT_BACKUP || resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        try {
            List<Shader> shaders = ShaderBackupDocument.read(getContentResolver(), uri);
            int duplicates = repository.countDuplicateShaders(shaders);
            if (duplicates > 0) {
                showDuplicateDialog(shaders, duplicates);
            } else {
                importBackup(shaders, true);
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.import_backup_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.import_backup_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void showDuplicateDialog(final List<Shader> shaders, int duplicates) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.import_duplicates_found, duplicates))
                .setPositiveButton(R.string.replace, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importBackup(shaders, true);
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importBackup(shaders, false);
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private void importBackup(List<Shader> shaders, boolean replaceDuplicates) {
        ShaderRepository.ImportResult result = repository.importBackup(shaders, replaceDuplicates);
        Toast.makeText(
                this,
                getString(
                        R.string.import_backup_success,
                        result.getImported(),
                        result.getReplaced(),
                        result.getSkipped()),
                Toast.LENGTH_LONG).show();
    }
}
