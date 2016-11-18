package com.gallapetti.anbarasan.gallapetti;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Gallapetti extends FragmentActivity {

    CallbackManager callbackManager;
    LoginButton login;
    ProfilePictureView profilePictureView;
    ShareDialog shareDialog;
    Button share,details;
    Dialog details_dialog;
    TextView details_txt;
    private String facebook_id,f_name, m_name, l_name, gender, profile_image, full_name, email_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_gallapetti);
        callbackManager = CallbackManager.Factory.create();
        //AppEventsLogger.activateApp(this);
        login=(LoginButton)findViewById(R.id.login_button);
        profilePictureView = (ProfilePictureView)findViewById(R.id.picture);
        shareDialog = new ShareDialog(this);
        share= (Button)findViewById(R.id.share);
        details=(Button)findViewById(R.id.details);
        login.setReadPermissions("public_profile email");
        share.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        details_dialog = new Dialog(this);
        details_dialog.setContentView(R.layout.dialog_details);
        details_dialog.setTitle("Details");
        details_txt=(TextView)details_dialog.findViewById(R.id.details);

        getKeyHash();

        details.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                details_dialog.show();
            }
        });

        if (AccessToken.getCurrentAccessToken()!=null){

            RequestData();
            share.setVisibility(View.VISIBLE);
            details.setVisibility(View.VISIBLE);

        }

        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(AccessToken.getCurrentAccessToken() != null)
                {
                    share.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    profilePictureView.setProfileId(null);
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                ShareLinkContent content= new ShareLinkContent.Builder().build();
                shareDialog.show(content);

            }

        });

        login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                facebook_id=f_name= m_name= l_name= gender= profile_image= full_name= email_id="";

                if(AccessToken.getCurrentAccessToken() != null)
                {
                    RequestData();
                    Profile profile= Profile.getCurrentProfile();
                    if(profile!=null)
                    {
                        facebook_id=profile.getId();
                        Log.e("facebook_id",facebook_id);
                        f_name=profile.getFirstName();
                        Log.e("f_name", f_name);
                        m_name=profile.getMiddleName();
                        Log.e("m_name", m_name);
                        l_name=profile.getLastName();
                        Log.e("l_name", l_name);
                        full_name=profile.getName();
                        Log.e("full_name", full_name);
                        profile_image=profile.getProfilePictureUri(400, 400).toString();
                        Log.e("profile_image", profile_image);
                    }
                    share.setVisibility(View.VISIBLE);
                    details.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }


    private void getKeyHash()
    {

        PackageInfo info;
        try
        {
          info=getPackageManager().getPackageInfo("com.gallapetti.anbarasan.gallapetti.Gallapetti", PackageManager.GET_SIGNATURES);
          for (Signature signature : info.signatures)
          {
              MessageDigest md;
              md=MessageDigest.getInstance("SHA");
              md.update(signature.toByteArray());
              String something= new String(Base64.encode(md.digest(),0));
              Log.e("hash key",something);
          }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("Name not found",e.toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e("No such an algorithm",e.toString());
        }
        catch (Exception e)
        {
            Log.e("Exception",e.toString());
        }
    }



    public void RequestData()
    {
        GraphRequest request= GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback(){

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                JSONObject json=response.getJSONObject();
                try
                {
                    if(json!=null)
                    {
                        String text="<b>Name :</b> "+json.getString("name")+"<br><br><b>Email :</b> "+json.getString("email")+"<br><br><b>Profile link :</b> "+json.getString("link");
                        details_txt.setText(Html.fromHtml(text));
                        profilePictureView.setProfileId(json.getString("id"));
                    }
                }
                catch (JSONException e)
                {
                    Log.e("JSON Exception",e.toString());
                }

            }
        });

        Bundle parameters= new Bundle();
        parameters.putString("fields","id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


}
