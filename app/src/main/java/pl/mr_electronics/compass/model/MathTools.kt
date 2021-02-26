package pl.mr_electronics.compass.model

class MathTools {
    companion object {

        fun DegToAngle(angleSrc: Float, angleDest: Float): Float {
            var correction = angleDest - angleSrc
            if (Math.abs(correction) > 180) {
                if (correction < 0)
                    correction = (360 + correction)
                else
                    correction = -(360 - correction)
            }
            return correction
        }
    }

}