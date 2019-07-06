package com.example.chatapplication.services;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.dashBoard;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;


public class googleSignInService {
    Boolean flag;
    Context mContext;
    FirebaseAuth mAuth;
    Activity mActivity;
    public GoogleSignInClient mGoogleSignInClient;
    userService us;

    public googleSignInService(Context context, String str, Activity activity) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mActivity = activity;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(str)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        us = new userService(context, activity);
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(mContext, "failed : " + e, Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            us.registerToDBService(user, 0);
                            Intent intent = new Intent(mActivity, dashBoard.class);
                            intent.putExtra("id", user.getUid());
                            // create progress dialog to delay start
                            final ProgressDialog progress = new ProgressDialog(mContext);
                            progress.setTitle("Connecting");
                            progress.setMessage("Please wait while we connect to devices...");
                            progress.show();

                            Runnable progressRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    progress.cancel();
                                }
                            };

                            Handler pdCanceller = new Handler();
                            pdCanceller.postDelayed(progressRunnable, 5000);
                            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        mActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mActivity).toBundle());
                                    } else {
                                        mActivity.startActivity(intent);
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(mContext, "failed2", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

}
