package ksy.yuhancommunity.com

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import ksy.yuhancommunity.com.model.ContentDTO
import ksy.yuhancommunity.com.model.FollowDTO


class UserFragment : Fragment() {

    private var fragmentView: View? = null
    private val PICK_PROFILE_FROM_ALBUM = 10
    private var firestore: FirebaseFirestore? = null

    var currentUserUid: String? = null // current UID

    var uid: String? = null // choose uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (arguments != null) {
            uid = arguments!!.getString("destinationUid")
        } else {
            uid = currentUserUid
        }

        firestore = FirebaseFirestore.getInstance()

        fragmentView = inflater.inflate(R.layout.fragment_user, container, false)

        fragmentView?.account_profile_image?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                activity!!.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
            }

        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        fragmentView?.account_btn_follow_signout?.setOnClickListener {
            requestFollow()
        }

        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        getProfileImages()
    }

    private fun requestFollow() {
        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)

        firestore?.runTransaction { transaction ->
            var followingDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)

            if (followingDTO == null) {
                // 아무도 팔로잉 하지 않았을 경우
                followingDTO = FollowDTO()
                followingDTO.followingCount = 1
                followingDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followingDTO)
                return@runTransaction
            }

            if (followingDTO.followings.containsKey(uid)) {
                // 내 아이디가 제 3자를 이미 팔로잉 하고 있을 경우
                followingDTO?.followingCount = followingDTO?.followingCount - 1
                followingDTO?.followings.remove(uid)
            } else {
                // 내가 제3자를 팔로잉 하지 않았을 경우
                followingDTO.followingCount = followingDTO.followingCount + 1
                followingDTO.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing, followingDTO)
            return@runTransaction
        }

        var tsDocFollower = firestore!!.collection("users").document(uid!!)

        firestore?.runTransaction { transaction ->
            var followerDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)

            if (followerDTO == null) {
                // 아무도 팔로워 하지 않은 경우
                followerDTO = FollowDTO()
                followerDTO!!.followerCount = 1
                followerDTO!!.followers[currentUserUid!!] = true

                transaction.set(tsDocFollowing, followerDTO)
                return@runTransaction
            }
            if (followerDTO.followers.containsKey(currentUserUid!!)) {
                // 다른 유저를 내가 팔로잉 하고 있을 경우
                followerDTO.followerCount = followerDTO.followerCount - 1
                followerDTO.followers.remove(currentUserUid!!)

                transaction.set(tsDocFollower, followerDTO)
            } else {
                // 다른 유저를 내가 팔로워 하지 않았을 경우
                followerDTO.followerCount = followerDTO.followerCount + 1
                followerDTO.followers[currentUserUid!!] = true
            }
            transaction.set(tsDocFollower, followerDTO)
            return@runTransaction
        }

    }

    private fun getProfileImages() {


        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot?.data != null) {
                    var url = documentSnapshot.data!!["image"]
                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView!!.account_profile_image)
                }
            }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var contentDTOs: ArrayList<ContentDTO>

        init {
            contentDTOs = ArrayList()
            firestore?.collection("images")?.whereEqualTo("uid", currentUserUid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    for (snapshot in querySnapshot!!.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    account_tv_post_count.text = contentDTOs.size.toString()
                    notifyDataSetChanged()

                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)

        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(imageview)
        }
    }
}