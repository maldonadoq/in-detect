package maldonado.indetect.adapter

import android.content.Context
import android.view.*
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


    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener
    ,View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        var textName: TextView = itemView.findViewById(R.id.item_name)
        var imageView: ImageView = itemView.findViewById(R.id.item_image)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            val doDelete = menu?.add(Menu.NONE, 1, 1, "Delete")
            val doRename = menu?.add(Menu.NONE, 2, 2, "Rename")

            doDelete?.setOnMenuItemClickListener(this)
            doRename?.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                when(menuItem?.itemId){
                    1 -> {
                        listener.onDeleteClick(position)
                        return true
                    }
                    2 -> {
                        listener.onRenameClick(position)
                        return true
                    }
                }
            }

            return false
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onRenameClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(_listener: OnItemClickListener){
        listener = _listener
    }
}