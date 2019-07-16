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
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val PICK_PROFILE_FROM_ALBUM = 10
    private lateinit var backPressHolder:OnBackPressHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress_bar.visibility = View.VISIBLE

        backPressHolder = OnBackPressHolder()

        // bottom nav view
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        registerPushToken()
    }

    override fun onBackPressed() {
        backPressHolder.onBackPressed()
    }

    private fun registerPushToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var map = mutableMapOf<String,Any?>()
                map["pushToken"] = token!!
                FirebaseFirestore.getInstance().collection("pushtokens")
                    .document(uid!!).set(map)
            })

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
        when(item.itemId){
            R.id.action_home -> {
                val detailviewFragment = DetailviewFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content,detailviewFragment)
                    .commit()
                return true
            }
            R.id.action_search -> {
                val gridFragment = GridFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content,gridFragment)
                    .commit()
                return true
            }
            R.id.action_add_photo -> {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }else{
                    Toast.makeText(this,"스토리지 읽기 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_favorite_alarm -> {
                val alarmfrgment = AlarmFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_content,alarmfrgment)
                    .commit()
                return true
            }
            R.id.action_account -> {
                val userFragment = UserFragment()
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val bundle = Bundle()
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content,userFragment)
                    .commit()
                return true
            }
        }
        return false
    }

    private fun setToolbarDefault(){
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
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


    inner class OnBackPressHolder(){
        private var backPressHolder : Long = 0
        fun onBackPressed(){
            if(System.currentTimeMillis() > backPressHolder + 2000){
                backPressHolder = System.currentTimeMillis()
                showBackToast()
                return
            }
            if(System.currentTimeMillis() <= backPressHolder + 2000){
                finishAffinity()
            }
        }
        fun showBackToast(){
            Toast.makeText(this@MainActivity,"한번더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
        }
    }
}