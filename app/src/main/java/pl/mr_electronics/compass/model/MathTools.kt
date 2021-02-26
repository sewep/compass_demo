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

        fun MettersToHumanRedable(meters: Float): String {
            if (meters > 1000f)
                return String.format("%.1f km", meters / 1000f)
            else
                return String.format("%.0f m", meters)
        }
    }

}