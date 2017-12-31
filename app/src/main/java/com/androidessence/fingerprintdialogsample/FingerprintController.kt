package com.androidessence.fingerprintdialogsample

import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView

/**
 * Defines the behavior required by any view used for fingerprint authentication.
 */
class FingerprintController(
        private val fingerprintManager: FingerprintManagerCompat,
        private val callback: Callback,
        private val title: TextView,
        private val subtitle: TextView,
        private val errorText: TextView,
        private val icon: ImageView) : FingerprintManagerCompat.AuthenticationCallback() {

    /**
     * The signal that gets called if the fingerprint authentication is cancelled.
     */
    private var cancellationSignal: CancellationSignal? = null

    /**
     * Boolean flag for whether or not authentication was cancelled by this controller or something else.
     */
    private var selfCancelled = false

    /**
     * Determines whether or not this device can support fingerprint authentication.
     */
    private val isFingerprintAuthAvailable: Boolean
        get() = fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()

    /**
     * Runnable that resets the icon and error text as necessary.
     */
    private val resetErrorTextRunnable: Runnable = Runnable {
        errorText.setTextColor(ContextCompat.getColor(errorText.context, R.color.hint_color))
        errorText.text = errorText.context.getString(R.string.touch_sensor)
        icon.setImageResource(R.drawable.ic_fingerprint_white_24dp)
    }

    init {
        errorText.post(resetErrorTextRunnable)
    }

    fun startListening(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        if (!isFingerprintAuthAvailable) return

        cancellationSignal = CancellationSignal()
        selfCancelled = false
        fingerprintManager.authenticate(cryptoObject, 0, cancellationSignal, this, null)
    }

    fun stopListening() {
        cancellationSignal?.let {
            selfCancelled = true
            it.cancel()
            cancellationSignal = null
        }
    }

    private fun showError(text: CharSequence?) {
        icon.setImageResource(R.drawable.ic_error_white_24dp)
        errorText.text = text
        errorText.setTextColor(ContextCompat.getColor(errorText.context, R.color.warning_color))
        errorText.removeCallbacks(resetErrorTextRunnable)
        errorText.postDelayed(resetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        if (!selfCancelled) {
            showError(errString)
            icon.postDelayed({
                callback.onError()
            }, ERROR_TIMEOUT_MILLIS)
        }
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        errorText.removeCallbacks(resetErrorTextRunnable)
        icon.setImageResource(R.drawable.ic_check_white_24dp)
        errorText.setTextColor(ContextCompat.getColor(errorText.context, R.color.success_color))
        errorText.text = errorText.context.getString(R.string.fingerprint_recognized)
        icon.postDelayed({
            callback.onAuthenticated()
        }, SUCCESS_DELAY_MILLIS)
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        showError(helpString)
    }

    override fun onAuthenticationFailed() {
        showError(errorText.context.getString(R.string.fingerprint_not_recognized))
    }

    fun setTitle(title: CharSequence) {
        this.title.text = title
    }

    fun setSubtitle(subtitle: CharSequence) {
        this.subtitle.text = subtitle
    }

    companion object {
        private val ERROR_TIMEOUT_MILLIS = 1600L
        private val SUCCESS_DELAY_MILLIS = 1300L
    }

    interface Callback {
        fun onAuthenticated()
        fun onError()
    }
}