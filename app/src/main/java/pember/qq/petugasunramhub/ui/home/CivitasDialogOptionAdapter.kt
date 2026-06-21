package pember.qq.petugasunramhub.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pember.qq.petugasunramhub.databinding.ItemDialogOptionBinding

data class CivitasDialogOption(
    val label: String,
    val iconResId: Int,
    val action: () -> Unit
)

class CivitasDialogOptionAdapter(
    private val options: List<CivitasDialogOption>,
    private val onItemSelected: () -> Unit
) : RecyclerView.Adapter<CivitasDialogOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDialogOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size

    inner class ViewHolder(private val binding: ItemDialogOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: CivitasDialogOption) {
            binding.tvOptionLabel.text = option.label
            binding.imgOptionIcon.setImageResource(option.iconResId)
            
            binding.root.setOnClickListener {
                option.action()
                onItemSelected()
            }
        }
    }
}
