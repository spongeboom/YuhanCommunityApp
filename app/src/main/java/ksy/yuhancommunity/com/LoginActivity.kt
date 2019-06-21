package ksy.yuhancommunity.com

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var GOOGLE_LOGIN_CODE = 8888
    private var callbackManager:CallbackManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()


        email_login_button.setOnClickListener { createAndLoginEmail() }

        google_login_button.setOnClickListener { googleLogin() }

        facebook_login_button.setOnClickListener { facebookLogin() }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        callbackManager = CallbackManager.Factory.create()
    }

    private fun createAndLoginEmail(){
        auth?.createUserWithEmailAndPassword(email_edit.text.toString(),password_edit.text.toString())?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this,"회원가입 실패", Toast.LENGTH_SHORT).show()
            }else{
                // login
                signEmail()
            }
        }
    }

    private fun signEmail(){
        auth?.signInWithEmailAndPassword(email_edit.text.toString(),password_edit.text.toString())?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }else{
                Toast.makeText(this,"로그인이 실패하였습니다. 아이디와 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun googleLogin(){
        val signInIntent =  googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }
        }
    }

    private fun facebookLogin(){
        LoginManager
            .getInstance()
            .logInWithReadPermissions(this,Arrays.asList("public_profile","email"))
        LoginManager
            .getInstance()
            .registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }

    private fun handleFacebookAccessToken(token:AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveMainPage(auth?.currentUser)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        moveMainPage(auth?.currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data)

        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result =  Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                firebaseAuthWithGoogle(result.signInAccount!!)
            }
        }
    }
}
