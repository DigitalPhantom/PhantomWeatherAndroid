/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 - 2022 Yoel Nunez <dev@nunez.guru>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package net.digitalphantom.app.weatherapp

import android.content.res.Configuration
import android.preference.PreferenceActivity
import androidx.appcompat.app.AppCompatDelegate
import android.os.Bundle
import net.digitalphantom.app.weatherapp.fragments.SettingsFragment
import android.view.MenuInflater
import android.view.View
import androidx.annotation.LayoutRes
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import net.digitalphantom.app.weatherapp.R

class SettingsActivity : PreferenceActivity() {
    private var delegate: AppCompatDelegate? = null
        private get() {
            if (field == null) {
                field = AppCompatDelegate.create(this, null)
                setTitle(R.string.settings)
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate!!.installViewFactory()
        delegate!!.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate!!.onPostCreate(savedInstanceState)
    }

    val supportActionBar: ActionBar?
        get() = delegate!!.supportActionBar

    fun setSupportActionBar(toolbar: Toolbar?) {
        delegate!!.setSupportActionBar(toolbar)
    }

    override fun getMenuInflater(): MenuInflater {
        return delegate!!.menuInflater
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        delegate!!.setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        delegate!!.setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate!!.setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate!!.addContentView(view, params)
    }

    override fun onPostResume() {
        super.onPostResume()
        delegate!!.onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        delegate!!.setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        delegate!!.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        delegate!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate!!.onDestroy()
    }

    override fun invalidateOptionsMenu() {
        delegate!!.invalidateOptionsMenu()
    }
}