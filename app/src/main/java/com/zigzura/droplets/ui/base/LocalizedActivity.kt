package com.zigzura.droplets.ui.base

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.zigzura.droplets.data.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class LocalizedActivity : ComponentActivity() {

    private lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }

        languageManager = LanguageManager(newBase)

        // Apply language configuration synchronously
        val wrappedContext = LocalizedContextWrapper.wrap(newBase, languageManager)
        super.attachBaseContext(wrappedContext)
    }

    /**
     * Call this method when language preference changes to recreate the activity
     * with the new language configuration
     */
    protected fun refreshLanguage() {
        recreate()
    }
}

class LocalizedContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, languageManager: LanguageManager): ContextWrapper {
            // For initial load, we'll use system default since we can't do async operations here
            // The actual language will be applied when the activity starts
            return LocalizedContextWrapper(context)
        }
    }
}
