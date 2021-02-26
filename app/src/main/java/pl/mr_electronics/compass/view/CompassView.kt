package pl.mr_electronics.compass.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pl.mr_electronics.compass.R

class CompassView : View {

    private var arrow1 = listOf(PointF(-0.045f, 0.0f), PointF(0.0f, -0.40f), PointF(0.045f, 0.0f))
    private var arrow2 = listOf(PointF(-0.035f, 0.0f), PointF(0.0f, -0.30f), PointF(0.035f, 0.0f))

    private var arrow3 = listOf(PointF(-0.01f, 0.0f), PointF(0.0f, 0.02f), PointF(0.01f, 0.0f))
    private var arrow4 = listOf(PointF(-0.02f, 0.0f), PointF(0.0f, 0.03f), PointF(0.02f, 0.0f))
    private var arrow5 = listOf(PointF(-0.03f, 0.0f), PointF(0.0f, 0.04f), PointF(0.03f, 0.0f))
    private var arrow6 = listOf(PointF(-0.03f, 0.0f), PointF(0.0f, -0.04f), PointF(0.03f, 0.0f))

    var ctx: Context? = null
    var wh: Int = 0
    var presentAngle = 0f
    var azymutAngle = 0f

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.ctx = context
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.ctx = context
    }

    constructor(context: Context?) : super(context) {
        this.ctx = context
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        // Math
        wh = Math.min(width, height)

        // Global transform
        canvas.save()
        canvas.translate((width / 2).toFloat(), (height / 2).toFloat())

        //presentAngle = 20f

        paintCentralStar(canvas, presentAngle)
        paintTrianglesOnACircle(canvas, presentAngle)
        paintAzymutArrow(canvas, presentAngle, azymutAngle)

        canvas.restore()
    }



    private fun getPaint(res_color: Int) : Paint {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, res_color)
        return  paint
    }

    private fun paintCircle(canvas: Canvas, paint: Paint, translate: PointF, r: Float) {
        val shapeDrawable = ShapeDrawable(OvalShape())
        var rc = (wh * r).toInt()
        shapeDrawable.setBounds(
            translate.x.toInt() - rc,
            translate.y.toInt() - rc,
            translate.x.toInt() + rc,
            translate.y.toInt() + rc
        )
        shapeDrawable.paint.color = paint.color
        shapeDrawable.draw(canvas)
    }

    private fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float) {
        paintPolygon(canvas, paint, points, translate, deg, PointF(0.0f, 0.0F))
    }

    private fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float, translate2: PointF) {
        if (points.isEmpty()) return

        val m = Matrix()
        m.postTranslate(translate2.x, translate2.y)
        m.postRotate(-deg)
        m.postTranslate(translate.x, translate.y)

        canvas.save()
        canvas.setMatrix(m)

        // Draw
        val path = Path()
        path.reset()
        path.moveTo(points.last().x, points.last().y)
        for (pkt in points) {
            path.lineTo(pkt.x * wh, pkt.y * wh)
        }
        canvas.drawPath(path, paint)

        canvas.restore()
    }

    private fun paintCentralStar(canvas: Canvas, dirWalk: Float) {

        val paintGray = getPaint(R.color.gray_800)
        val paintRed = getPaint(R.color.red)
        val trans = PointF((width / 2).toFloat(), (height / 2).toFloat())

        paintPolygon(canvas, paintRed, arrow1, trans, 0.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 90.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 180.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 270.0f - dirWalk)

        paintPolygon(canvas, paintGray, arrow2, trans, 45.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 135.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 225.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 315.0f - dirWalk)

        paintCircle(canvas, getPaint(R.color.gray_800), PointF(0F, 0F), 0.09F)
        paintCircle(canvas, getPaint(R.color.white), PointF(0F, 0F), 0.06F)
        paintCircle(canvas, getPaint(R.color.gray_800), PointF(0F, 0F), 0.04F)
    }

    private fun paintTrianglesOnACircle(canvas: Canvas, dirWalk: Float) {
        val paintRed = getPaint(R.color.red)
        val paintGray = getPaint(R.color.gray_800)

        val trans = PointF((width / 2).toFloat(), (height / 2).toFloat())
        val trans2 = PointF(0.0f,-0.45f * wh)

        for (deg in 0..359 step 9) {
            var listPkt = arrow3
            var paintC = paintGray
            if (deg == 0) paintC = paintRed
            if (deg % 90 == 0) listPkt = arrow5
            if ((deg-45) % 90 == 0) listPkt = arrow4

            paintPolygon(canvas, paintC, listPkt, trans, deg.toFloat() - dirWalk, trans2)
        }
    }

    private fun paintAzymutArrow(canvas: Canvas, dirWalk: Float, direction: Float) {
        val paintGreen = getPaint(R.color.c500)

        val trans = PointF((width / 2).toFloat(), (height / 2).toFloat())
        val trans2 = PointF(0.0f,-0.46f * wh)
        paintPolygon(canvas, paintGreen, arrow6, trans, -direction - dirWalk, trans2)
    }
}

