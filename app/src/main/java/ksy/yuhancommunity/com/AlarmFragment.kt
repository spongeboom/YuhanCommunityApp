package ksy.yuhancommunity.com

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_alert.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import ksy.yuhancommunity.com.model.AlarmDTO

class AlarmFragment : Fragment() {

    private var alarmSnapshot: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alert, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerViewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val alarmDTOlist = ArrayList<AlarmDTO>()

        init {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            alarmSnapshot = FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid)
                .orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOlist.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents) {
                        alarmDTOlist.add(snapshot.toObject(AlarmDTO::class.java)!!)
                    }
                    alarmDTOlist.sortByDescending { it.timestamp }
                    notifyDataSetChanged()

                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)

        override fun getItemCount(): Int {
            return alarmDTOlist.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val profileImage = holder.itemView.commentviewitem_iv_profile
            val commentTextView = holder.itemView.commentviewitem_tv_profile

            FirebaseFirestore.getInstance().collection("profileImages")
                .document(alarmDTOlist[position].uid!!).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(activity!!)
                            .load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(profileImage)
                    }
                }

            when (alarmDTOlist[position].kind) {
                0 -> {
                    val str_0 = alarmDTOlist[position].userId + getString(R.string.alarm_favorite)
                    commentTextView.text = str_0
                }
                1 -> {
                    val str_1 = alarmDTOlist[position].userId + getString(R.string.alarm_who) + " \"" +
                            alarmDTOlist[position].message + "\" " + getString(R.string.alarm_comment)
                    commentTextView.text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOlist[position].userId + getString(R.string.alarm_follow)
                    commentTextView.text = str_2
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        alarmSnapshot?.remove()
    }
}