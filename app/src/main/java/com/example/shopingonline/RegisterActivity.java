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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText InputName, InputPhoneNumber, InputPassword;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CreateAccountButton = (Button) findViewById(R.id.register_btn);
        InputName = (EditText) findViewById(R.id.register_username_input);
        InputPassword = (EditText) findViewById(R.id.register_password_input);
        InputPhoneNumber= (EditText) findViewById(R.id.register_phone_number_input);

        loadingBar = new ProgressDialog(this);


        //créer un écouteur pour le bouton créer un compte
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //créer une méthode CreateAccount
                CreateAccount();

            }
        });

    }
    //cette méthode va nous permettre d'afficher le formulaire que le client va remplire pour avoir un compte
    private void CreateAccount(){
        String name = InputName.getText().toString();
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        //ensuite on va checke si chaqu'un de ces élément est créer ou non
        if(TextUtils.isEmpty(name)){
            //on va créé un toast qui va demander à l'utilisateur de créer son nom s'il ne la pas saisi sur le formulaire
            Toast.makeText(this,"Please write your name!",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(phone)){
            //on va créé un toast qui va demander à l'utilisateur de créer son num s'il ne la pas saisi sur le formulaire
            Toast.makeText(this,"Please write your phone number!",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password)){
            //on va créé un toast qui va demander à l'utilisateur de créer son mdp s'il ne la pas saisi sur le formulaire
            Toast.makeText(this,"Please write your password!",Toast.LENGTH_SHORT).show();

        }
        else{

            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait, we are checking the credentials.");
            //mm si l'utilisateur touche n'importe où sur le screen ne va pas changer le processus
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            //une fois ça est fait on va essayer de valider son numéro de tel(le num existe déjà dans la bdd ou non)
            ValidatephoneNumber(name, phone, password);
        }

    }

    private void ValidatephoneNumber(String name, String phone, String password) {

        //créer une reference de ma base de donnée
        final DatabaseReference RootRef;
        //la réference de la route
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check si l'utilisateur existe dans notre base de donnée ou non(primary key is the phone number)s
                //si le numero de tel n'existe pas
                if(!(dataSnapshot.child("Users").child(phone).exists())){
                    //on va créer un nouveau compte pour cela on va utiliser une hashmap
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("phone", phone);
                    userdataMap.put("password", password);
                    userdataMap.put("name", name);

                    //maintennat avec l'utilisation de rootlef on va créer un notre form
                    RootRef.child("Users").child(phone).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           //si le task est réussi on va afficher félicitation votre compte a été créer
                                                           if (task.isSuccessful()){
                                                               Toast.makeText(RegisterActivity.this,"Gongratulation your account has been created.",Toast.LENGTH_SHORT).show();
                                                               loadingBar.dismiss();

                                                               //envoyer l'utilisateur a loginactivity
                                                               Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                               startActivity(intent);
                                                           }
                                                           else{
                                                               loadingBar.dismiss();
                                                               Toast.makeText(RegisterActivity.this,"network Error Please try again.",Toast.LENGTH_SHORT).show();
                                                           }
                                                       }
                                                   }
                            );


                }
                else{
                    //si non on va juste informer l'utlisateur que son compte existe
                    Toast.makeText(RegisterActivity.this,"this "+phone +" already exist.",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this,"please try using another phone number.",Toast.LENGTH_SHORT).show();
                    //on peut envoyer carrèment l'utilisateur à la page d'accueil
                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //validation




    }
}