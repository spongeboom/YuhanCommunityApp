package ksy.yuhancommunity.com

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import ksy.yuhancommunity.com.model.ContentDTO

class DetailviewFragment : Fragment(){
    private var firestore:FirebaseFirestore? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firestore = FirebaseFirestore.getInstance()

        val view = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_detail,container,false)
        view.detailviewfragment_recyclerview.adapter = DetailRecyclerviewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class DetailRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val contentDTOs : ArrayList<ContentDTO>
        val contentUidList : ArrayList<String>


        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()

            // currentUser Uid
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            // orderby = timestamp
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            // user id
            viewHolder.detailviewitem_profile_text.text = contentDTOs[position].userId

            // image
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .into(viewHolder.detailviewitem_content_image)

            // description
            viewHolder.detailviewitem_explain_text.text = contentDTOs[position].explain

            // favorite counter set
            viewHolder.detailviewitem_favoritecounter_text.text = "좋아요 " +contentDTOs[position].favoriteCount + "개"

            viewHolder.detailviewitem_favorite_image.setOnClickListener {
                favoriteEvent(position)
            }
            // like click
            if(contentDTOs[position].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)){
                viewHolder.detailviewitem_favorite_image.setImageResource(R.drawable.ic_favorite)
            }else{
                // don't click
                viewHolder.detailviewitem_favorite_image.setImageResource(R.drawable.ic_favorite_border)
            }

            viewHolder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_content,fragment).commit()
            }
        }

        private fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction{
                transaction ->
                    var uid = FirebaseAuth.getInstance().currentUser!!.uid
                    var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                    if(contentDTO!!.favorites.containsKey(uid)){
                        // status : like
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                        contentDTO?.favorites.remove(uid)
                    }else{
                        contentDTO?.favorites[uid] = true
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    }
                    transaction.set(tsDoc,contentDTO)
            }
        }
    }
}


