package maldonado.indetect.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import maldonado.indetect.R

class ImageAdapter(_ctx: Context, _uploads: List<Upload>): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(){
    private var context: Context = _ctx
    private var uploads: List<Upload> = _uploads
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return uploads.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uploadCurrent = uploads[position]
        holder.textName.text = uploadCurrent.getName()

        Picasso.Builder(context).build()
            .load(uploadCurrent.getUrl())
            .fit()
            .centerInside()
            .into(holder.imageView)
    }



    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var textName: TextView = itemView.findViewById(R.id.item_name)
        var imageView: ImageView = itemView.findViewById(R.id.item_image)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        }
    }

    interface OnItemClickListener{
        fun OnItemClick(position: Int)
        fun OnWhatEverClick(position: Int)
        fun OnDeleteClick(position: Int)
    }

    fun setOnItemClickListener(_listener: OnItemClickListener){
        listener = _listener
    }
}