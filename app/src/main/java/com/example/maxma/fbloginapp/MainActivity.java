package com.example.maxma.fbloginapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    TextView textStatus,txtEmail,txtBirthday,txtFriendNum;
    LoginButton login_button;
    CallbackManager callbackManager;
    ProgressDialog mDialog;
    ImageView imgAvatar;

    private URL profilePicture;
    private String userId;
    private String TAG = "LoginActivity";
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    private String firstName,lastName, email,birthday,gender;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initilizeControl();


    }

    private void printKeyHash(){
        try {
            PackageInfo info =getPackageManager().getPackageInfo("com.example.maxma.fbloginapp", PackageManager.GET_SIGNATURES);
            for(android.content.pm.Signature signature:info.signatures){
                MessageDigest md= MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));


            }
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

    }


    private void initilizeControl(){
        callbackManager=CallbackManager.Factory.create();
        textStatus=(TextView)findViewById(R.id.textStatus);
        login_button=(LoginButton)findViewById(R.id.login_button);
        txtEmail=(TextView)findViewById(R.id.txtEmail);
        txtBirthday=(TextView)findViewById(R.id.txtBirthday);
        txtFriendNum=(TextView)findViewById(R.id.txtFriendNum);
        login_button.setReadPermissions(Arrays.asList("public_profile","email","user_birthday","user_friends"));
        imgAvatar=(ImageView)findViewById(R.id.avatar);



        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textStatus.setText("Login Success\n"+"\n");//+loginResult.getAccessToken().getToken()
                mDialog=new ProgressDialog(MainActivity.this);
                Log.e(TAG,loginResult.getAccessToken().getToken());

                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();
                GraphRequest request =GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        Log.e(TAG,object.toString());
                        Log.e(TAG,response.toString());






                        getData(object);
                    }
                });


                // request Graph API
                Bundle parameter = new Bundle();
                parameter.putString("field","id,email,birthday,friends");
                request.setParameters(parameter);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                textStatus.setText("Login Cancenlled");

            }
            @Override
            public void onError(FacebookException error){

            }

        });

       // if already login
        if(AccessToken.getCurrentAccessToken()!=null){
                // just set user id
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());


        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getData(JSONObject object) {
        try{
            URL profile_picture=new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");

            Picasso.get().load(profile_picture.toString()).into(imgAvatar);

            Log.e(TAG,object.toString());


            //txtEmail.setText(object.getString("email"));


            txtBirthday.setText(object.getString("birthday"));
            txtFriendNum.setText("Friends: "+object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
