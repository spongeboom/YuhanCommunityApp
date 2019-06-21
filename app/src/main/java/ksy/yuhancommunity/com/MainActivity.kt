package ksy.yuhancommunity.com

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val PICK_PROFILE_FROM_ALBUM = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_home -> {
                val detailviewFragment = DetailviewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailviewFragment).commit()
                return true
            }
            R.id.action_search -> {
                val gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()

                return true
            }
            R.id.action_add_photo -> {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm -> {
                val alertFragment = AlertFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alertFragment).commit()
                return true
            }
            R.id.action_account -> {
                val userFragment = UserFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            val imageUrl = data?.data
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages")
                .child(uid)

            imageUrl?.let {
                storageRef.putFile(it).addOnFailureListener {
                    Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnCompleteListener { taskSnapshot ->
                        Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                        val url = taskSnapshot.result.toString()
                        val map = HashMap<String,Any>()
                        map["image"] = url
                        FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
                    }
                }
            }
        }
    }
}
