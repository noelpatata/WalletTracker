package com.example.wallettracker.ui.importSheet

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.wallettracker.databinding.FragmentImportSheetBinding
import java.io.File
import java.io.FileNotFoundException
import java.net.URI

class ImportSheet : Fragment() {
    private var _binding: FragmentImportSheetBinding? = null
    private val binding get() = _binding!!
    val PICK_CSV_FILE = 2

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportSheetBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.selectFile.setOnClickListener(){
            val a = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            openFile(a.absolutePath.toUri())
        }





        return root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(Intent.createChooser(intent, "Open CSV"), PICK_CSV_FILE)
        val csvFileUri = intent.data
        val list = parseCsvFromUri(csvFileUri.toString())
        val a = list
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

    // Método para leer el archivo CSV desde un URI y convertirlo en una lista de objetos
    fun parseCsvFromUri(uri: String): AccountDetails {
        val file = File(uri)

        if (!file.exists()) {
            throw FileNotFoundException("The file at URI $uri does not exist.")
        }

        val lines = file.readLines()

        // Parseamos los detalles de la cuenta
        val ibanLine = lines[0].split("\t")
        val iban = ibanLine[0]
        val availableBalance = ibanLine[1]
        val period = ibanLine[3]

        // Parseamos las transacciones
        val transactions = mutableListOf<Transaction>()
        for (i in 5 until lines.size) { // Empieza desde la línea 6, donde están las transacciones
            val transactionLine = lines[i].split("\t")
            if (transactionLine.size >= 4) {
                transactions.add(
                    Transaction(
                        concept = transactionLine[0],
                        date = transactionLine[1],
                        amount = transactionLine[2] + transactionLine[3],
                        balanceAfter = transactionLine[4] + transactionLine[5]
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
}