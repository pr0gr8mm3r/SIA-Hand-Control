package de.sia.wazynski.simon.handcontrol

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsDialog : BottomSheetDialogFragment() {

    private var callback: SettingsCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tfe_pn_fragment_settings, container, false)

        root.findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { dismiss() }

        childFragmentManager.beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = activity as SettingsCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    interface SettingsCallback {
        fun onProtocolSet(protocol: ArduinoProtocol)
    }
}
