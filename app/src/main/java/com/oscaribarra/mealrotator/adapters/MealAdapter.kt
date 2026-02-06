import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oscaribarra.mealrotator.R
import com.oscaribarra.mealrotator.databinding.ItemMealBinding
import com.oscaribarra.mealrotator.models.Meal

class MealAdapter(private val onClick: (Meal) -> Unit) :
    ListAdapter<Meal, MealAdapter.MealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(private val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: Meal) {
            binding.mealName.text = meal.name
            binding.root.setOnClickListener { onClick(meal) }
            binding.pinButton.setImageResource(
                if (meal.isPinned) R.drawable.ic_pinned else R.drawable.ic_unpinned
            )
            binding.pinButton.setOnClickListener {
                onClick(meal.copy(isPinned = !meal.isPinned))
            }
        }
    }

    class MealDiffCallback : DiffUtil.ItemCallback<Meal>() {
        override fun areItemsTheSame(oldItem: Meal, newItem: Meal) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Meal, newItem: Meal) = oldItem == newItem
    }
}