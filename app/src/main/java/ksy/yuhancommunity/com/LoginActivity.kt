package ksy.yuhancommunity.com

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {
            createAndLoginEmail()
        }

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
}
