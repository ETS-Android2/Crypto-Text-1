package com.example.cryptotext;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES extends AppCompatActivity {

    EditText aesInput;
    Button enc_btn, clear_btn;
    ImageButton main_enc, main_dec, main_enc_orange, main_dec_orange, send_btn, cpy_btn, logout_btn;
    ConstraintLayout downC, UPc;
    TextView output, input_TV, output_TV;
    String password = "qwerty";

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_e_s);

        aesInput = findViewById(R.id.aes_inputTextarea);
        output = findViewById(R.id.aes_output_decrypt);
        main_enc = findViewById(R.id.aes_encrypt);
        main_dec = findViewById(R.id.aes_decrypt);
        main_enc_orange = findViewById(R.id.aes_enc_orange);
        main_dec_orange = findViewById(R.id.aes_decrypt_orange);
        enc_btn = findViewById(R.id.aes_encrypt_btn);
        clear_btn = findViewById(R.id.aes_clear);
        logout_btn = findViewById(R.id.aes_logout);
        cpy_btn = findViewById(R.id.aes_copy);
        send_btn = findViewById(R.id.aes_send);
        downC = findViewById(R.id.aes_constraintLayout);
        input_TV = findViewById(R.id.aes_inputTV);
        output_TV = findViewById(R.id.aes_outputTV);


        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();
                Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(i);
                finish();
            }
        });

        main_enc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_enc.setVisibility(View.INVISIBLE);
                main_enc_orange.setVisibility(View.VISIBLE);
                main_dec.setVisibility(View.VISIBLE);
                main_dec_orange.setVisibility(View.INVISIBLE);
                send_btn.setVisibility(View.INVISIBLE);
                cpy_btn.setVisibility(View.INVISIBLE);
                enc_btn.setText("ENCRYPT");
                input_TV.setText("Message");
                output_TV.setText("Encrypted Message");
                aesInput.setText("");
                output.setText("");
                downC.setVisibility(View.VISIBLE);

                enc_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = aesInput.getText().toString().trim();
                        if(input.length() == 0){
                            Toast.makeText(AES.this, "Enter a valid message", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            try {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                try {
                                    String encryptedText = aes_Encrypt(input, password);
                                    output.setText(encryptedText);
                                    send_btn.setVisibility(View.VISIBLE);
                                    cpy_btn.setVisibility(View.VISIBLE);
                                    send_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent sendIntent = new Intent();
                                            sendIntent.setAction(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT,encryptedText);
                                            sendIntent.setType("text/plain");

                                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                                            startActivity(shareIntent);

                                        }
                                    });
                                    cpy_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("public", encryptedText);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(AES.this, "Encrypted Text Copied", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                clear_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        aesInput.setText("");
                        output.setText("");
                        send_btn.setVisibility(View.INVISIBLE);
                        cpy_btn.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });

        main_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_dec.setVisibility(View.INVISIBLE);
                main_enc_orange.setVisibility(View.INVISIBLE);
                main_enc.setVisibility(View.VISIBLE);
                input_TV.setText("Encrypted Message");
                output_TV.setText("Message");
                main_dec_orange.setVisibility((View.VISIBLE));
                send_btn.setVisibility(View.INVISIBLE);
                cpy_btn.setVisibility(View.INVISIBLE);
                downC.setVisibility(View.VISIBLE);
                aesInput.setText("");
                output.setText("");
                enc_btn.setText("DECRYPT");
                enc_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        String input = aesInput.getText().toString();
                        String clearText = null;
                        try {
                            clearText = aes_Decrypt(input, password);
                            if(clearText.length() == 0){
                                Toast.makeText(AES.this, "Incorrect Cipher Text", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                output.setText(clearText);
                            }
                        } catch (Exception e) {
                            Toast.makeText(AES.this, "Incorrect Cipher Text", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private String aes_Encrypt(String message,String password_text) throws Exception {
        SecretKeySpec key=generate_AES_Key(password_text);
        Cipher cip = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cip.init(Cipher.ENCRYPT_MODE,key);
        byte[] encryptedByteValue = cip.doFinal(message.getBytes("utf-8"));

        String encryptedValue = Base64.encodeToString(encryptedByteValue,Base64.DEFAULT);

        return encryptedValue;


    }
    private  String aes_Decrypt(String message,String password_text) throws Exception {
        SecretKeySpec key = generate_AES_Key(password_text);
        Cipher cip = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cip.init(Cipher.DECRYPT_MODE,key);
        byte[] decryptedByteValue = Base64.decode(message,Base64.DEFAULT);
        byte[] decryptedVale = cip.doFinal(decryptedByteValue);

        return new String(decryptedVale,"utf-8");
    }

    private SecretKeySpec generate_AES_Key(String password_text) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password_text.getBytes("utf-8");

        digest.update(bytes,0,bytes.length);

        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

}

