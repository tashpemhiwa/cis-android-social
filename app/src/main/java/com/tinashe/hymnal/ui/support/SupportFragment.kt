package com.tinashe.hymnal.ui.support

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tinashe.hymnal.BuildConfig
import com.tinashe.hymnal.R
import com.tinashe.hymnal.databinding.FragmentSupportBinding
import com.tinashe.hymnal.extensions.arch.observeNonNull
import com.tinashe.hymnal.extensions.view.inflateView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SupportFragment : Fragment(R.layout.fragment_support) {

    private val viewModel: SupportViewModel by viewModels()

    private var binding: FragmentSupportBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentSupportBinding.inflate(inflater, container, false).also {
            binding = it
            binding?.apply {
                tvPolicy.setOnClickListener {
                    launchWebUrl(getString(R.string.app_privacy_policy))
                }
                tvTerms.setOnClickListener {
                    launchWebUrl(getString(R.string.app_terms))
                }
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.inAppProductsLiveData.observeNonNull(viewLifecycleOwner) { products ->
            binding?.chipGroupInApp?.apply {
                removeAllViews()
                products.forEach { product ->
                    val chip: Chip = inflateView(
                        R.layout.chip_amount,
                        this,
                        false
                    ) as Chip
                    chip.apply {
                        id = product.sku.hashCode()
                        text = product.price
                    }
                    addView(chip)
                }

                setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
                    if (checkedId != -1) {
                        products.find { it.sku.hashCode() == checkedId }?.let {
                            viewModel.initiatePurchase(it, requireActivity())
                        }
                    }
                }
            }
        }
        viewModel.subscriptionsLiveData.observeNonNull(viewLifecycleOwner) { subs ->
            binding?.chipGroupSubs?.apply {
                removeAllViews()
                subs.forEach { product ->
                    val chip: Chip = inflateView(
                        R.layout.chip_amount,
                        this,
                        false
                    ) as Chip
                    chip.apply {
                        id = product.sku.hashCode()
                        text = getString(R.string.subscription_period, product.price)
                    }
                    addView(chip)
                }

                setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
                    if (checkedId != -1) {
                        subs.find { it.sku.hashCode() == checkedId }?.let {
                            viewModel.initiatePurchase(it, requireActivity())
                        }
                    }
                }
            }
        }

        viewModel.loadData(requireActivity())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.support_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_account_settings -> {
                launchWebUrl(getString(R.string.subscriptions_url))
                true
            }
            R.id.action_help -> {
                ShareCompat.IntentBuilder.from(requireActivity())
                    .setType("message/rfc822")
                    .addEmailTo(getString(R.string.app_email))
                    .setSubject("${getString(R.string.app_full_name)} v${BuildConfig.VERSION_NAME}")
                    .setChooserTitle(R.string.send_with)
                    .startChooser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchWebUrl(url: String) {
        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .enableUrlBarHiding()
            .setStartAnimations(requireContext(), R.anim.slide_up, android.R.anim.fade_out)
            .setExitAnimations(requireContext(), android.R.anim.fade_in, R.anim.slide_down)
        try {
            val intent = builder.build()
            intent.launchUrl(requireContext(), Uri.parse(url))
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}
