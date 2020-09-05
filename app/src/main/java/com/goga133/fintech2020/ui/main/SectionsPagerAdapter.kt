package com.goga133.fintech2020.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.goga133.fintech2020.R

// Название разделов
private val TAB_TITLES = arrayOf(
    R.string.tab_section_random,
    R.string.tab_section_latest,
    R.string.tab_section_top,
    R.string.tab_section_hot
)

// Адаптер для TabLayout
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}