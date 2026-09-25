package com.blrp.game.gui


class SpeedometerStatic private constructor() {
    companion object {
        val INSTANCE: SpeedometerStatic = SpeedometerStatic()
        var currentFuelProgress = -1
    }
}
