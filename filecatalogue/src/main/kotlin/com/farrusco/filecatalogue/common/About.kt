package com.farrusco.filecatalogue.common

import com.farrusco.filecatalogue.R
import com.farrusco.projectclasses.common.AboutActivity

class About : AboutActivity() {
    override val resourceAbout: Int
        get() {
            return R.string.about_text1
        }
}