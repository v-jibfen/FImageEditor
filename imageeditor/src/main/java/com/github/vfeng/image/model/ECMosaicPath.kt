package com.github.vfeng.image.model

import android.graphics.Path
import com.github.vfeng.image.model.ECMosaic.Companion.BASE_MOSAIC_WIDTH

/**
 * Created by vfeng on 2020/8/21.
 */
class ECMosaicPath {

    var width = BASE_MOSAIC_WIDTH

    var path: Path? = null
        get() {
            if (field == null) {
                field = Path()
            }
            return field
        }
}