package de.sia.wazynski.simon.handcontrol

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private var callback: SettingsCallback? = null

    companion object {
        const val ProtocolKey = "protocol"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        preferenceScreen.findPreference<ListPreference>(ProtocolKey)?.onPreferenceChangeListener =
            this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == ProtocolKey) {
            val protocol = ArduinoProtocol.from(newValue as String)
            if (protocol != null) {
                println("changed: ${protocol.name}")
                callback?.onProtocolSet(protocol)
            }
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = activity as SettingsCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

}