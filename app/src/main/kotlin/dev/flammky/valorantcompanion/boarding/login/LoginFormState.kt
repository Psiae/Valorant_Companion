package dev.flammky.valorantcompanion.boarding.login

import android.os.Bundle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

class LoginFormIntents(
    val login: (
        self: LoginFormState,
        username: String,
        password: String,
        retain: Boolean,
    ) -> Unit
)

class LoginFormState(
    private val intents: LoginFormIntents
) {

    var inputSlot by mutableStateOf<LoginFormInputSlot>(LoginFormInputSlot())
        private set

    var retainLogin by mutableStateOf(false)
        private set

    var region by mutableStateOf("NA")
        private set

    val canLogin by derivedStateOf {
        inputSlot.username.isNotEmpty() && inputSlot.password.isNotEmpty()
    }

    fun login() {
        if (!canLogin) return
        val slot = inputSlot.apply { lock() }
        intents.login(this, slot.username, slot.password, retainLogin)
    }

    fun resetSlotPasswordWithExceptionMessage(msg: String) {
        inputSlot = LoginFormInputSlot()
            .apply {
                usernameInput(inputSlot.username)
                exceptionMessage(msg)
            }
    }

    fun selectRegion(region: String) {
        this.region = region
    }

    companion object {
        fun Saver(
            intents: LoginFormIntents
        ): Saver<LoginFormState, Bundle> {
            return Saver(
                save = { self ->
                    Bundle()
                        .apply {
                            putBundle(self::inputSlot.name, self.inputSlot.saveToBundle())
                        }
                },
                restore = { bundle ->
                    LoginFormState(intents)
                        .apply {
                            inputSlot.restore(bundle.getBundle(::inputSlot.name)!!)
                        }
                }
            )
        }
    }
}

class LoginFormInputSlot() {

    var locked = false
        private set

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var visiblePassword by mutableStateOf(false)
        private set

    var exceptionMessage by mutableStateOf("")
        private set

    fun lock()  {
        locked = true
    }

    fun exceptionMessage(msg: String) {
        if (locked) return
        exceptionMessage = msg
    }

    fun usernameInput(str: String) {
        if (locked) return
        username = str
    }

    fun passwordInput(str: String) {
        if (locked) return
        password = str
    }

    fun toggleVisiblePassword() {
        if (locked) return
        visiblePassword = !visiblePassword
    }

    fun restore(bundle: Bundle) {
        if (locked) return
        username = bundle.getString(::username.name) ?: ""
        password = bundle.getString(::password.name) ?: ""
        visiblePassword = bundle.getBoolean(::visiblePassword.name)
    }

    fun saveToBundle(): Bundle {
        return Bundle()
            .apply {
                putString(::username.name, username)
                putString(::password.name, password)
                putBoolean(::visiblePassword.name, visiblePassword)
            }
    }
}