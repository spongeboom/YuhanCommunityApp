package ksy.yuhancommunity.com

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    private val PICK_IMAGE_FROM_ALBUM = 1001
    private var storage : FirebaseStorage? = null
    private var photoUri:Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()

        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        add_photo_image.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }
        add_photo_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                add_photo_image.setImageURI(data?.data)
            }else {
                finish()
            }
        }
    }

    private fun contentUpload(){
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_"+ timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        photoUri?.let { storageRef?.putFile(it)?.addOnSuccessListener {
          Toast.makeText(this,getString(R.string.upload_success),Toast.LENGTH_SHORT).show()
        }}
    }
}
