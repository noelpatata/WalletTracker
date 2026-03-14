package win.downops.wallettracker.ui.importSheet

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SharedCsvViewModel @Inject constructor() : ViewModel() {
    var pendingUri: Uri? = null
}
