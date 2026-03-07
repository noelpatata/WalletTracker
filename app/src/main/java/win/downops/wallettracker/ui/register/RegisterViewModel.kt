package win.downops.wallettracker.ui.register

import Cryptography
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.LoginRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Session
import win.downops.wallettracker.di.AppMode
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val loginRepo: LoginRepository,
    private val sessionRepo: SessionRepository,
    private val appMode: AppMode
) : ViewModel() {

    private val _registerResult = MutableLiveData<AppResult<Unit>>()
    val registerResult: LiveData<AppResult<Unit>> = _registerResult

    fun register(username: String, password: String) {
        viewModelScope.launch {
            val credentials = LoginRequest(username, password)

            val registerResult = loginRepo.register(credentials)
            if (registerResult is AppResult.Error) {
                _registerResult.postValue(registerResult)
                return@launch
            }

            val loginResponse = loginRepo.login(credentials)
            if (loginResponse is AppResult.Error) {
                _registerResult.postValue(loginResponse)
                return@launch
            }
            val jwt = (loginResponse as AppResult.Success).data?.token
                ?: run {
                    _registerResult.postValue(AppResult.Error("Login returned no token"))
                    return@launch
                }

            val (privateKey, publicKey) = Cryptography().generateKeys()

            val setKeyResult = loginRepo.setUserClientPubKey(jwt, ServerPubKeyRequest(publicKey))
            if (setKeyResult is AppResult.Error) {
                _registerResult.postValue(setKeyResult)
                return@launch
            }

            val serverKeyResult = loginRepo.getUserServerPubKey(jwt)
            if (serverKeyResult is AppResult.Error) {
                _registerResult.postValue(serverKeyResult)
                return@launch
            }
            val serverPublicKey = (serverKeyResult as AppResult.Success).data?.publicKey
                ?: run {
                    _registerResult.postValue(AppResult.Error("Could not retrieve server public key"))
                    return@launch
                }

            val oldSession = sessionRepo.getFirstSession()
            val session = Session().apply {
                id = oldSession?.id ?: 0
                token = jwt
                this.privateKey = privateKey
                this.serverPublicKey = serverPublicKey
                cipheredCredentials = ""
                iv = ""
                fingerPrint = false
                online = true
            }

            if (oldSession == null) sessionRepo.insert(session)
            else sessionRepo.edit(session)

            appMode.isOnline = true
            _registerResult.postValue(AppResult.Success("Registration successful", Unit))
        }
    }
}
