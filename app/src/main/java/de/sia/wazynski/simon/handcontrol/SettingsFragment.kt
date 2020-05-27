package de.sia.wazynski.simon.handcontrol

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment: PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    companion object {
        const val ProtocolKey = "protocol"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        preferenceScreen.findPreference<ListPreference>(ProtocolKey)?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == ProtocolKey) {
            val protocol = ArduinoProtocol.from(newValue as String)
            println("changed: ${protocol?.name}")
        }
        return true
    }

}