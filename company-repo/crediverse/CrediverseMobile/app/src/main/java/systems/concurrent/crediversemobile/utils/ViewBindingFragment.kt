package systems.concurrent.crediversemobile.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class ViewBindingFragment<Binding : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding
) : Fragment() {

    abstract val thisPage: NavigationManager.Page?

    override fun onResume() {
        super.onResume()
        if (thisPage == null) return
        NavigationManager.resumePage(thisPage!!)
    }

    private val _tag = this::class.java.kotlin.simpleName

    private var binding: Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return bindingInflater(inflater, container, false).apply { binding = this }.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun requireBinding(): Binding = binding
        ?: throw IllegalStateException("You used the binding before onCreateView() or after onDestroyView()")

    protected fun useBindingOrNull(bindingUse: (Binding?) -> Unit) {
        return try {
            bindingUse(requireBinding())
        } catch (e: Exception) {
            Log.w(_tag, "useBindingOrNull. Err: " + e.message.toString())
            bindingUse(null)
        }
    }

    protected val bindingOrNull
        get(): Binding? {
            return try {
                requireBinding()
            } catch (e: Exception) {
                Log.w(_tag, "bindingOrNull. Err: " + e.message.toString())
                null
            }
        }

    protected fun useBinding(bindingUse: (Binding) -> Unit) {
        bindingUse(requireBinding())
    }
}