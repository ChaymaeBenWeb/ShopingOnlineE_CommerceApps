package com.example.shopingonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopingonline.Admin.AdminCategoryActivity;
import com.example.shopingonline.Model.Users;
import com.example.shopingonline.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    //déclaration des attributs
    private EditText InputPhoneNumber, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;
    //parte i'm an admin
    private TextView AdminLink, NotAdminLink, ForgetPasswordLink;
    private String parentDbName = "Users";//la classe users va contenir toutes les informations sur les clients et l'admin(comerçant)
    private CheckBox chkBoxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        LoginButton = (Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputPhoneNumber= (EditText) findViewById(R.id.login_phone_number_input);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
        chkBoxRememberMe =(CheckBox) findViewById(R.id.remember_me_chkb);
        //initialisation de la librairie
        Paper.init(this);




        //écouteur sur le bouton
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });


        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","login");
                startActivity(intent);
            }
        });

        //for i'm admin écouteur

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changer le texte de login button s'il s'agit vraiment d'un commerçant
                LoginButton.setText("Login Admin");
                //invisibler adminLink
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";

            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });
    }
    //c'est une méthode qui va contenir les conditions de remplissage de formulaire
    private void LoginUser() {
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if(TextUtils.isEmpty(phone)){
            //on va créé un toast qui va demander à l'utilisateur de créer son num s'il ne la pas saisi sur le formulaire
            Toast.makeText(this,"Please write your phone number!",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password)){
            //on va créé un toast qui va demander à l'utilisateur de créer son mdp s'il ne la pas saisi sur le formulaire
            Toast.makeText(this,"Please write your password!",Toast.LENGTH_SHORT).show();

        }
        else{
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, we are checking the credentials.");
            //mm si l'utilisateur touche n'importe où sur le screen ne va pas changer le processus
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


           AllowAccessToAccount(phone, password);
        }
    }

    private void AllowAccessToAccount(String phone, String password) {

        if(chkBoxRememberMe.isChecked()){
            //si remember me est chéqué enregistrer c'est deux valeurs
            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }
        //créer une reference de ma base de donnée
        final DatabaseReference RootRef;
        //la réference de la route
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            //
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(parentDbName).child(phone) != null) {


                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);


                    assert usersData != null;
                    if (usersData.getPhone() != null && usersData.getPhone().equals(phone)) {
                        if (usersData.getPassword() != null && usersData.getPassword().equals(password)) {

                            if (parentDbName.equals("Admins")) {

                                Toast.makeText(LoginActivity.this, "Welcome Admin you are logged in successfully  ", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if (parentDbName.equals("Users")) {

                                Toast.makeText(LoginActivity.this, "logged in successfully ... ", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser = usersData;
                                startActivity(intent);
                            }}

                        else {
                            Toast.makeText(LoginActivity.this, "Password not correct", Toast.LENGTH_SHORT).show();

                        }
                    }
                }


                else

                {
                    Toast.makeText(LoginActivity.this, "Account with this " + phone + "number do not exists", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }}