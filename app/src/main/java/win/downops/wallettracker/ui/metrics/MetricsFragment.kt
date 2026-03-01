package win.downops.wallettracker.ui.metrics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    private var categoryOptions: List<CategoryOption> = emptyList()
    private var monthOptions: List<MonthOption> = emptyList()

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
        setupSpinnerListeners()
        initObservers()
        loadData()
    }

    private fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.filterRow.visibility = View.GONE
        binding.scrollView.visibility = View.GONE
        binding.lblEmpty.visibility = View.GONE
        viewModel.loadMetrics()
    }

    private fun initObservers() {
        viewModel.categoryOptions.observe(viewLifecycleOwner) { options ->
            categoryOptions = options
            if (options.isNotEmpty()) setupCategorySpinner()
        }

        viewModel.monthOptions.observe(viewLifecycleOwner) { options ->
            monthOptions = options
            setupMonthSpinner()
            if (options.isNotEmpty()) binding.filterRow.visibility = View.VISIBLE
        }

        viewModel.metricsResult.observe(viewLifecycleOwner) { result ->
            binding.loadingPanel.visibility = View.GONE
            when (result) {
                is AppResult.Success -> {
                    binding.scrollView.visibility = View.VISIBLE
                    binding.lblEmpty.visibility = View.GONE
                    displayMetrics(result.data)
                }
                is AppResult.Error -> {
                    Logger.log(result.message)
                    binding.scrollView.visibility = View.GONE
                    binding.lblEmpty.text = result.message
                    binding.lblEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        val labels = mutableListOf("All Categories") + categoryOptions.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        val selectedIdx = categoryOptions.indexOfFirst { it.id == viewModel.selectedCategoryId }
        binding.spinnerCategory.setSelection(if (selectedIdx >= 0) selectedIdx + 1 else 0)
    }

    private fun setupMonthSpinner() {
        val labels = mutableListOf("All Months") + monthOptions.map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = adapter

        val selectedIdx = monthOptions.indexOfFirst { it.key == viewModel.selectedMonthKey }
        binding.spinnerMonth.setSelection(if (selectedIdx >= 0) selectedIdx + 1 else 0)
    }

    private fun setupSpinnerListeners() {
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val catId = if (position == 0) null else categoryOptions.getOrNull(position - 1)?.id
                if (catId == viewModel.selectedCategoryId) return
                viewModel.setFilter(catId, viewModel.selectedMonthKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val monthKey = if (position == 0) null else monthOptions.getOrNull(position - 1)?.key
                if (monthKey == viewModel.selectedMonthKey) return
                viewModel.setFilter(viewModel.selectedCategoryId, monthKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
