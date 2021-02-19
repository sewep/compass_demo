package pl.mr_electronics.compass

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var arrow1 = listOf(PointF(-35.0f, 0.0f), PointF(0.0f, -300.0f), PointF(35.0f, 0.0f))
    var arrow2 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -220.0f), PointF(25.0f, 0.0f))
    var arrow3 = listOf(PointF(-8.0f, 0.0f), PointF(0.0f, 20.0f), PointF(8.0f, 0.0f))
    var arrow4 = listOf(PointF(-16.0f, 0.0f), PointF(0.0f, 34.0f), PointF(16.0f, 0.0f))
    var arrow5 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, 46.0f), PointF(25.0f, 0.0f))
    var arrow6 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -46.0f), PointF(25.0f, 0.0f))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawCompass(20f, 20f)
    }

    fun drawCompass(dirWalk: Float, dirDestination: Float) {
        val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(bitmap)


        paintCentralStar(canvas, dirWalk)
        paintTrianglesOnACircle(canvas, dirWalk)
        paintAzymutArrow(canvas, dirWalk, dirDestination)


        // set bitmap as background to ImageView
        compass_view.setImageBitmap(bitmap)
    }

    fun getPaintColor(color: String) : Paint {
        return getPaintColor(Color.parseColor(color))
    }

    fun getPaintColor(color: Int) : Paint {
        val paint_green = Paint()
        paint_green.setStyle(Paint.Style.FILL)
        paint_green.setColor(color)
        return paint_green
    }

    fun paintAzymutArrow(canvas: Canvas, dirWalk: Float, direction: Float) {
        val paint_green = getPaintColor("#00cc00")

        var trans = PointF(400.0f,400.0f)
        var trans2 = PointF(0.0f,-354.0f)
        paintPolygon(canvas, paint_green, arrow6, trans, direction - dirWalk, trans2)
    }

    fun paintTrianglesOnACircle(canvas: Canvas, dirWalk: Float) {
        val paint_red = getPaintColor(Color.RED)
        val paint_gray = getPaintColor("#202020")

        var trans = PointF(400.0f,400.0f)
        var trans2 = PointF(0.0f,-350.0f)

        for (deg in 0..359 step 10) {
            var list_pkt = arrow3
            var paint_c = paint_gray
            if (deg == 0) paint_c = paint_red
            if (deg % 90 == 0) list_pkt = arrow5
            if ((deg-50) % 90 == 0) list_pkt = arrow4

            paintPolygon(canvas, paint_c, list_pkt, trans, deg.toFloat() - dirWalk, trans2)
        }
    }

    fun paintCentralStar(canvas: Canvas, dirWalk: Float) {
        val paint_red = getPaintColor(Color.RED)
        val paint_gray = getPaintColor("#202020")
        val paint_white = getPaintColor(Color.WHITE)


        var trans = PointF(400.0f,400.0f)

        paintPolygon(canvas, paint_red, arrow1, trans, 0.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 90.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 180.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 270.0f - dirWalk)

        paintPolygon(canvas, paint_gray, arrow2, trans, 50.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 140.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 230.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 320.0f - dirWalk)

        paintCircle(canvas, paint_gray, trans, 70)
        paintCircle(canvas, paint_white, trans, 50)
        paintCircle(canvas, paint_gray, trans, 30)
    }

    fun paintCircle(canvas: Canvas, paint: Paint, translate: PointF, r: Int) {
        var shapeDrawable: ShapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.setBounds(
                translate.x.toInt()-r,
                translate.y.toInt()-r,
                translate.x.toInt()+r,
                translate.y.toInt()+r)
        shapeDrawable.getPaint().setColor(paint.color)
        shapeDrawable.draw(canvas)
    }

    fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float) {
        paintPolygon(canvas, paint, points, translate, deg, PointF(0.0f, 0.0F));
    }

    fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float, translate2: PointF) {
        if (points.isEmpty()) return

        var m = Matrix();
        m.postTranslate(translate2.x, translate2.y)
        m.postRotate(deg)
        m.postTranslate(translate.x, translate.y)

        canvas.save()
        canvas.setMatrix(m)

        // Draw
        val path = Path()
        path.reset()
        path.moveTo(points.last().x, points.last().y)
        for (pkt in points) {
            path.lineTo(pkt.x, pkt.y)
        }
        canvas.drawPath(path, paint)

        canvas.restore()
    }
}