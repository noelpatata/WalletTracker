package win.downops.wallettracker.ui.importSheet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import win.downops.wallettracker.databinding.FragmentImportSheetBinding

class ImportSheet : Fragment() {
    private lateinit var openCsvLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentImportSheetBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportSheetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        CSVFileCallBack()

        binding.selectFile.setOnClickListener(){
            val a = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            openFile(a.absolutePath.toUri())
        }





        return root
    }

    private fun CSVFileCallBack() {
        openCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data // Use .data to get the Uri directly
                if (uri != null) {
                    try {
                        val list = parseCsvFromUri(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.e("CSVFileCallBack", "No Uri returned from file picker.")
                }
            }
        }
    }
    private fun readContentFromUri(uri: Uri): List<String>? {
        return try {
            val contentResolver = requireContext().contentResolver // Replace `context` with your Activity or Context reference
            val inputStream = contentResolver.openInputStream(uri)
            val lines = inputStream?.bufferedReader()?.use { it.readLines() }
            inputStream?.close()
            return lines
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


    // Método para leer el archivo CSV desde un URI y convertirlo en una lista de objetos
    fun parseCsvFromUri(uri: Uri): AccountDetails? {


        val lines = readContentFromUri(uri)

        if (lines != null) {
            val valoresCuenta = lines[1].split(";")
            val iban = valoresCuenta[0]
            val availableBalance = valoresCuenta[1]
            val period = valoresCuenta[2] //dd-mm-yyyy

            // Parseamos las transacciones
            val transactions = mutableListOf<Transaction>()
            for (i in 3 until lines.size-1) {
                val transactionLine = lines[i].split(";")
                if (transactionLine.size >= 4) {
                    transactions.add(
                        Transaction(
                            concept = transactionLine[0],
                            date = transactionLine[1],//dd-mm-yyyy
                            amount = transactionLine[2],
                            balanceAfter = transactionLine[3]
                        )
                    )
                }
            }

            return AccountDetails(
                iban = iban,
                availableBalance = availableBalance,
                period = period,
                transactions = transactions
            )
        }
        return null


    }
}
data class Transaction(
    val concept: String,
    val date: String,
    val amount: String,
    val balanceAfter: String
)

// Clase para representar el encabezado general del CSV
data class AccountDetails(
    val iban: String,
    val availableBalance: String,
    val period: String,
    val transactions: List<Transaction>
)