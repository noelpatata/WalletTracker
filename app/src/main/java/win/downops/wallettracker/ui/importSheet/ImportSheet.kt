package win.downops.wallettracker.ui.importSheet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import win.downops.wallettracker.R
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.databinding.FragmentImportSheetBinding

@AndroidEntryPoint
class ImportSheet : Fragment() {
    private lateinit var openCsvLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentImportSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImportSheetViewModel by viewModels()
    private val sharedCsvViewModel: SharedCsvViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    try {
                        val accountDetails = parseCsvFromUri(it)
                        if (accountDetails != null) {
                            viewModel.importCsv(accountDetails)
                        } else {
                            Toast.makeText(requireContext(), "Error parsing CSV", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error reading file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportSheetBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val sharedUri = sharedCsvViewModel.pendingUri
        sharedCsvViewModel.pendingUri = null
        if (sharedUri != null) {
            try {
                val accountDetails = parseCsvFromUri(sharedUri)
                if (accountDetails != null) {
                    viewModel.importCsv(accountDetails)
                } else {
                    Toast.makeText(requireContext(), "Error parsing CSV", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error reading file", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectFile.setOnClickListener {
            val a = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            openFile(a.absolutePath.toUri())
        }

        viewModel.importing.observe(viewLifecycleOwner) { isImporting ->
            binding.importingPanel.visibility = if (isImporting) View.VISIBLE else View.GONE
            binding.selectFile.visibility = if (isImporting) View.GONE else View.VISIBLE
        }

        viewModel.importProgress.observe(viewLifecycleOwner) { (current, total) ->
            binding.progressBar.max = total.coerceAtLeast(1)
            binding.progressBar.progress = current
            binding.lblImportProgress.text = "Importing: $current / $total"
        }

        viewModel.importResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            when (result) {
                is AppResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_importes)
                }
                is AppResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
            viewModel.onImportResultConsumed()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun readContentFromUri(uri: Uri): List<String>? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val lines = inputStream?.bufferedReader()?.use { it.readLines() }
            inputStream?.close()
            lines
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        openCsvLauncher.launch(intent)
    }

    fun parseCsvFromUri(uri: Uri): AccountDetails? {
        val lines = readContentFromUri(uri) ?: return null
        if (lines.size < 2) return null

        val valoresCuenta = lines[1].split(";")
        if (valoresCuenta.size < 3) return null
        val availableBalance = valoresCuenta[1]
        val period = valoresCuenta[2]

        val transactions = mutableListOf<Transaction>()
        for (i in 3 until lines.size - 1) {
            val transactionLine = lines[i].split(";")
            if (transactionLine.size >= 4) {
                transactions.add(
                    Transaction(
                        concept = transactionLine[0],
                        date = transactionLine[1],
                        amount = transactionLine[2],
                        balanceAfter = transactionLine[3]
                    )
                )
            }
        }

        return AccountDetails(
            availableBalance = availableBalance,
            period = period,
            transactions = transactions
        )
    }
}

data class Transaction(
    val concept: String,
    val date: String,
    val amount: String,
    val balanceAfter: String
)

data class AccountDetails(
    val availableBalance: String,
    val period: String,
    val transactions: List<Transaction>
)
