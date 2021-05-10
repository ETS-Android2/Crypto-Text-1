package com.example.cryptotext;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

public class RSA extends AppCompatActivity {

    EditText rsaInput, userInput;
    Button enc_btn, clear_btn;
    ImageButton main_enc, main_dec, go_btn, main_enc_orange, main_dec_orange, go_orange, send_btn, cpy_btn, new_btn, logout_btn;
    ConstraintLayout downC, UPc;
    TextView output, input_TV, output_TV;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    GoogleSignInAccount signInAccount;

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_s_a);

        KeyPair keyP = null;
        try {
            keyP = getKeypair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PublicKey pubKey = keyP.getPublic();
        byte[] publicKeyBytes = pubKey.getEncoded();
        final String publicKeyBytesBase64 = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));

        PrivateKey privateKey = keyP.getPrivate();
        byte[] privateKeyBytes = privateKey.getEncoded();
        final String privateKeyBytesBase64 = new String(Base64.encode(privateKeyBytes, Base64.DEFAULT));

        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        HashMap<String, Object> map = new HashMap<>();
        String encodedmail = EncodeString(signInAccount.getEmail());
        String encodeKey = publicKeyBytesBase64.replaceAll("(\\r|\\n)", "");
        map.put("email", encodedmail);
        map.put("public", encodeKey);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference();
        reference.child("users").child(encodedmail).setValue(map);

        rsaInput = findViewById(R.id.rsa_inputTextarea);
        main_enc = findViewById(R.id.enc);
        main_dec = findViewById(R.id.decrypt);
        main_enc_orange = findViewById(R.id.enc_orange);
        main_dec_orange = findViewById(R.id.decrypt_orange);
        go_orange = findViewById(R.id.checkUser_orange);
        enc_btn = findViewById(R.id.rsa_encrypt_btn);
        new_btn = findViewById(R.id.rsa_new);
        go_btn = findViewById(R.id.checkUser);
        clear_btn = findViewById(R.id.rsa_clear);
        logout_btn = findViewById(R.id.rsa_logout);
        cpy_btn = findViewById(R.id.rsa_copy);
        send_btn = findViewById(R.id.rsa_send);
        output = findViewById(R.id.output_decrypt);
        userInput = findViewById(R.id.email);
        downC = findViewById(R.id.constraintLayout);
        UPc = findViewById(R.id.constraintLayout2);
        input_TV = findViewById(R.id.rsa_inputTV);
        output_TV = findViewById(R.id.rsa_outputTV);



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
                go_orange.setVisibility(View.INVISIBLE);
                go_btn.setVisibility(View.VISIBLE);
                main_dec.setVisibility(View.VISIBLE);
                main_dec_orange.setVisibility(View.INVISIBLE);
                userInput.setText("");
                UPc.setVisibility(View.VISIBLE);
                send_btn.setVisibility(View.INVISIBLE);
                cpy_btn.setVisibility(View.INVISIBLE);
                new_btn.setVisibility(View.INVISIBLE);
                downC.setVisibility(View.INVISIBLE);
                go_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userEntered = EncodeString(userInput.getText().toString().trim());

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

                        Query checkUser = reference.orderByChild("email").equalTo(userEntered);
                        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()){
                                    userInput.setError(null);
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                    String publicKeyFromDB = snapshot.child(userEntered).child("public").getValue(String.class);
                                    enc_btn.setText("ENCRYPT");
                                    go_btn.setVisibility(View.INVISIBLE);
                                    go_orange.setVisibility(View.VISIBLE);
                                    rsaInput.setText("");
                                    input_TV.setText("Message");
                                    output_TV.setText("Encrypted Message");
                                    output.setText("");
                                    downC.setVisibility(View.VISIBLE);
                                    enc_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String input = rsaInput.getText().toString().trim();
                                            if(input.length() == 0){
                                                Toast.makeText(RSA.this, "Enter a valid message", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                try {
                                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                                    String out = Rsa_enc(input, publicKeyFromDB);
                                                    output.setText(out);
                                                    send_btn.setVisibility(View.VISIBLE);
                                                    cpy_btn.setVisibility(View.VISIBLE);
                                                    new_btn.setVisibility(View.VISIBLE);
                                                    send_btn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent sendIntent = new Intent();
                                                            sendIntent.setAction(Intent.ACTION_SEND);
                                                            sendIntent.putExtra(Intent.EXTRA_TEXT,out);
                                                            sendIntent.setType("text/plain");

                                                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                                                            startActivity(shareIntent);

                                                        }
                                                    });
                                                    cpy_btn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                            ClipData clip = ClipData.newPlainText("public", out);
                                                            clipboard.setPrimaryClip(clip);
                                                            Toast.makeText(RSA.this, "Encrypted Text Copied", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    new_btn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            send_btn.setVisibility(View.INVISIBLE);
                                                            cpy_btn.setVisibility(View.INVISIBLE);
                                                            new_btn.setVisibility(View.INVISIBLE);
                                                            userInput.setText("");
                                                            rsaInput.setText("");
                                                            go_orange.setVisibility(View.INVISIBLE);
                                                            go_btn.setVisibility(View.VISIBLE);
                                                            downC.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }
                                else{
                                    userInput.setError("No such User exists");
                                    userInput.requestFocus();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
                    }
                });

        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rsaInput.setText("");
                output.setText("");
                send_btn.setVisibility(View.INVISIBLE);
                cpy_btn.setVisibility(View.INVISIBLE);
                new_btn.setVisibility(View.INVISIBLE);
            }
        });

        main_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UPc.setVisibility(View.INVISIBLE);
                main_dec.setVisibility(View.INVISIBLE);
                main_enc_orange.setVisibility(View.INVISIBLE);
                main_enc.setVisibility(View.VISIBLE);
                input_TV.setText("Encrypted Message");
                output_TV.setText("Message");
                main_dec_orange.setVisibility((View.VISIBLE));
                send_btn.setVisibility(View.INVISIBLE);
                new_btn.setVisibility(View.INVISIBLE);
                cpy_btn.setVisibility(View.INVISIBLE);
                downC.setVisibility(View.VISIBLE);
                rsaInput.setText("");
                output.setText("");
                enc_btn.setText("DECRYPT");
                enc_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        String input = rsaInput.getText().toString();
                        String clearText = Rsa_dec(input, privateKeyBytesBase64);
                        if(clearText.length() == 0){
                            Toast.makeText(RSA.this, "Incorrect Cipher Text", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            output.setText(clearText);
                        }
                    }
                });


            }
        });
        }
    public static KeyPair getKeypair(){
            KeyPairGenerator kpg = null;
            try {
                kpg = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }

    private static String Rsa_enc(String message, String publicKey) throws Exception {
            KeyFactory keyF = null;
            try {
                keyF = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey.trim().getBytes(), Base64.DEFAULT));
            Key key = null;
            try {
                key = keyF.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            // get an RSA cipher object and print the provider
        final Cipher cip = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
        // encrypt the plain text using the public key
        cip.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cip.doFinal(message.getBytes("UTF-8"));
        String encryptedBase64 = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));

        return encryptedBase64.replaceAll("(\\r|\\n)", "");

    }

    private static String Rsa_dec(String message, String PrivateKey)
    {
        String decString = "";
        try {
            KeyFactory keyF = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(PrivateKey.trim().getBytes(), Base64.DEFAULT));
            Key key = keyF.generatePrivate(keySpec);

            // get an RSA cipher object and print the provider
            final Cipher cip = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            // encrypt the plain text using the public key
            cip.init(Cipher.DECRYPT_MODE, key);

            byte[] encBytes = Base64.decode(message, Base64.DEFAULT);
            byte[] decBytes = cip.doFinal(encBytes);
            decString = new String(decBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decString;

    }
    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }

    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }
}


