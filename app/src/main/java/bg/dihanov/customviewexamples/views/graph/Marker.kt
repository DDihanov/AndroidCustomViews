package bg.dihanov.customviewexamples.views.graph

import androidx.annotation.IntegerRes

data class Marker(val currentPos: CurrentPosition = CurrentPosition(0f, 0f), val value: Int = 0)

data class CurrentPosition(var x: Float, var y: Float)