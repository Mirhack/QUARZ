/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mirhack.quarz.imageProcessing.graphics

import android.graphics.*
import com.mirhack.quarz.imageProcessing.GraphicOverlay
import com.mirhack.quarz.imageProcessing.GraphicOverlay.Graphic

/**
 * Draw camera image to background.
 */
class RectangleGraphic(overlay: GraphicOverlay?, private var rect: Rect) : Graphic(
    overlay!!
) {
    private val paint = Paint().apply {
        color = Color.argb(60,0,202,0)
    }
    private val path = Path()

    override fun draw(canvas: Canvas?) {
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CW
        )
        path.transform(getTransformationMatrix())
        canvas!!.drawPath(path, paint)
    }

}