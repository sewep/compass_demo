package pl.mr_electronics.compass

import org.junit.Test

import org.junit.Assert.*
import pl.mr_electronics.compass.model.CompassModel
import java.io.Console

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTest {

    @Test
    fun angleCorrectionCheck() {

        var cModel = CompassModel()

        val currarr: FloatArray = floatArrayOf( 50f, 0f, 0f, 0f, 300f, 50f, 10f, 200f )
        val destarr: FloatArray = floatArrayOf( 0f, 50f, 180f, 270f, 350f, 350f, 200f, 10f )

        for (i in destarr.indices) {
            cModel.oriantationToSet = destarr[i]
            cModel.currOrientation = currarr[i]
            val corr = cModel.calcCorrectionAngle()
            println("" + currarr[i] + " -> " + destarr[i] + " correction: " + corr + ";  corr=" + (destarr[i] - currarr[i]))
        }



        assertEquals(4, 2 + 2)
    }
}