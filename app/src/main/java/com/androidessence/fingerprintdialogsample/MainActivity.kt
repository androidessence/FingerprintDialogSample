package com.androidessence.fingerprintdialogsample

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val manager = FingerprintManagerCompat.from(this)

            if (manager.isHardwareDetected && manager.hasEnrolledFingerprints()) {
                showFingerprintAuth()
            } else {
                Snackbar.make(view, "Fingerprint authentication is not supported.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFingerprintAuth() {
        val dialog = FingerprintDialog.newInstance(
                "Sign In",
                "Confirm fingerprint to continue."
        )
        dialog.show(supportFragmentManager, FingerprintDialog.FRAGMENT_TAG)
    }
}
