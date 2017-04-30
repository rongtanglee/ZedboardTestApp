package com.ron.zedboardtestapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class PreferenceActivity extends Activity {

    private EditText editTextSSID;
    private EditText editTextPassword;
    private EditText editTextAccountID;
    private EditText editTextPoolID;
    private EditText editTextPoolRegion;
    private EditText editTextUnauthRoleARN;
    private EditText editTextAuthRoleARN;
    private EditText editTextBucketName;
    private EditText editTextBucketRegion;

    private Button buttonSave;
    private Button buttonRestore;

    private ZedboardPreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        preference = new ZedboardPreference(this);
        initWidgets();
        setEventListener();
    }

    public void initWidgets() {
        editTextSSID = (EditText) findViewById(R.id.editTextSSID);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextAccountID = (EditText) findViewById(R.id.editTextAccountID);
        editTextPoolID = (EditText) findViewById(R.id.editTextUserPoolID);
        editTextUnauthRoleARN = (EditText) findViewById(R.id.editTextUnauthRoleARN);
        editTextAuthRoleARN = (EditText) findViewById(R.id.editTextAuthRoleARN);
        editTextPoolRegion = (EditText) findViewById(R.id.editTextPoolRegion);
        editTextBucketName = (EditText) findViewById(R.id.editTextBucketName);
        editTextBucketRegion = (EditText) findViewById(R.id.editTextBucketRegion);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonRestore = (Button) findViewById(R.id.buttonRestore);

        readPreference();
    }

    public void savePreference() {

        preference.setSSID(editTextSSID.getText().toString());
        preference.setPassword(editTextPassword.getText().toString());
        preference.setAccountID(editTextAccountID.getText().toString());
        preference.setPoolID(editTextPoolID.getText().toString());
        preference.setPoolRegion(editTextPoolRegion.getText().toString());
        preference.setUnauthRoleARN(editTextUnauthRoleARN.getText().toString());
        preference.setAuthRoleARN(editTextAuthRoleARN.getText().toString());
        preference.setBucketName(editTextBucketName.getText().toString());
        preference.setBucketRegion(editTextBucketRegion.getText().toString());
    }

    public void readPreference() {
        editTextSSID.setText(preference.getSSID());
        editTextPassword.setText(preference.getPassword());
        editTextAccountID.setText(preference.getAccountID());
        editTextPoolID.setText(preference.getPoolID());
        editTextPoolRegion.setText(preference.getPoolRegion());
        editTextUnauthRoleARN.setText(preference.getUnauthRoleARN());
        editTextAuthRoleARN.setText(preference.getAuthRoleARN());
        editTextBucketName.setText(preference.getBucketName());
        editTextBucketRegion.setText(preference.getBucketRegion());
    }

    public void setEventListener() {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreference();
                Toast.makeText(PreferenceActivity.this, "Save Preference", Toast.LENGTH_SHORT).show();
            }
        });

        buttonRestore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                readPreference();
                Toast.makeText(PreferenceActivity.this, "Restore Preference", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
