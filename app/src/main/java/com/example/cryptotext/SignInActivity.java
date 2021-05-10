 package com.example.cryptotext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

 public class SignInActivity extends AppCompatActivity {

     private static final String TAG = "Google Sign In :";
     FirebaseAuth mAuth;
     GoogleSignInClient mGoogleSignInClient;
     int RC_SIGN_IN = 123;

     private Boolean exit = false;
     @Override
     public void onBackPressed() {
         if (exit) {
             finish(); // finish activity
         } else {
             Toast.makeText(this, "Press Back again to Exit.",
                     Toast.LENGTH_SHORT).show();
             exit = true;
             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     exit = false;
                 }
             }, 3 * 1000);

         }
     }

     @Override
     protected void onStart() {
         super.onStart();

         FirebaseUser user = mAuth.getCurrentUser();
         if(user != null){
             Intent i = new Intent(getApplicationContext(), MainActivity.class);
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
             i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
             startActivity(i);
             finish();
         }
     }

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        createRequest();

    }

    private void createRequest(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.google_signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

     public void signIn() {
         Intent signInIntent = mGoogleSignInClient.getSignInIntent();
         startActivityForResult(signInIntent, RC_SIGN_IN);
     }

     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
         if (requestCode == RC_SIGN_IN) {
             Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
             try {
                 // Google Sign In was successful, authenticate with Firebase
                 GoogleSignInAccount account = task.getResult(ApiException.class);
                 Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                 firebaseAuthWithGoogle(account.getIdToken());
             } catch (ApiException e) {
                 // Google Sign In failed, update UI appropriately
                 Toast.makeText(this, "Unable To SignIn", Toast.LENGTH_SHORT).show();
             }
         }
     }

     private void firebaseAuthWithGoogle(String idToken) {
         AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
         mAuth.signInWithCredential(credential)
                 .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             // Sign in success, update UI with the signed-in user's information
                             FirebaseUser user = mAuth.getCurrentUser();
                             Intent i = new Intent(getApplicationContext(), MainActivity.class);
                             startActivity(i);
                             finish();

                         } else {
                             // If sign in fails, display a message to the user.
                             Toast.makeText(SignInActivity.this, "Sorry Auth failed", Toast.LENGTH_SHORT).show();

                         }
                     }
                 });
     }
 }