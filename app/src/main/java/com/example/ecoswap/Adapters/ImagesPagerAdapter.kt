import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoswap.databinding.ItemImageBinding
import com.example.ecoswap.databinding.PagerImageItemBinding

class ImagesPagerAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ImagesPagerAdapter.PagerVH>() {

    inner class PagerVH(val binding: PagerImageItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Glide.with(binding.root.context)
                .load(url)
                .into(binding.imgView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PagerVH(PagerImageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) =
        holder.bind(images[position])
}
