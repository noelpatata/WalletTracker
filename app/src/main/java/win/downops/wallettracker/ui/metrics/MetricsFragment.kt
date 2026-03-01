package win.downops.wallettracker.ui.metrics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import win.downops.wallettracker.R
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.databinding.FragmentMetricsBinding
import win.downops.wallettracker.util.Logger

@AndroidEntryPoint
class MetricsFragment : Fragment() {

    private val viewModel: MetricsViewModel by viewModels()
    private var _binding: FragmentMetricsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        loadData()
    }

    private fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        binding.lblEmpty.visibility = View.GONE
        viewModel.loadMetrics()
    }

    private fun initObservers() {
        viewModel.metricsResult.observe(viewLifecycleOwner) { result ->
            binding.loadingPanel.visibility = View.GONE
            when (result) {
                is AppResult.Success -> {
                    binding.scrollView.visibility = View.VISIBLE
                    displayMetrics(result.data)
                }
                is AppResult.Error -> {
                    Logger.log(result.message)
                    binding.lblEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun displayMetrics(data: MetricsData) {
        binding.lblTotal.text = String.format("%.2f€", data.total)
        binding.lblCount.text = data.count.toString()
        binding.lblAverage.text = String.format("%.2f€", data.average)

        binding.categoryContainer.removeAllViews()
        data.categories.forEach { cat ->
            addMetricRow(
                binding.categoryContainer,
                cat.name,
                String.format("%.2f€", cat.total),
                cat.percentage.toInt()
            )
        }

        binding.monthlyContainer.removeAllViews()
        data.months.forEach { month ->
            addMetricRow(
                binding.monthlyContainer,
                month.label,
                String.format("%.2f€", month.total),
                month.percentage.toInt()
            )
        }
    }

    private fun addMetricRow(container: LinearLayout, label: String, value: String, progress: Int) {
        val rowView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_metric_row, container, false)

        rowView.findViewById<TextView>(R.id.lblMetricName).text = label
        rowView.findViewById<TextView>(R.id.lblMetricValue).text = value
        rowView.findViewById<ProgressBar>(R.id.progressMetric).progress = progress

        container.addView(rowView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
