package pl.mr_electronics.compass

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.values
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var arrow1 = listOf(PointF(-35.0f, 0.0f), PointF(0.0f, -320.0f), PointF(35.0f, 0.0f))
    var arrow2 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -230.0f), PointF(25.0f, 0.0f))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawCompass()
    }

    fun drawCompass() {
        val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(bitmap)

        paintCentralStar(canvas)



        // set bitmap as background to ImageView
        compass_view.setImageBitmap(bitmap)
    }

    fun paintCentralStar(canvas: Canvas) {
        val paint_red = Paint()
        paint_red.setStyle(Paint.Style.FILL)
        paint_red.setColor(Color.RED);
        val paint_gray = Paint()
        paint_gray.setStyle(Paint.Style.FILL)
        paint_gray.setColor(Color.parseColor("#202020"));
        val paint_white = Paint()
        paint_white.setColor(Color.WHITE);


        var trans = PointF(400.0f,400.0f)

        paintPolygon(canvas, paint_red, arrow1, trans, 0.0f)
        paintPolygon(canvas, paint_gray, arrow1, trans, 90.0f)
        paintPolygon(canvas, paint_gray, arrow1, trans, 180.0f)
        paintPolygon(canvas, paint_gray, arrow1, trans, 270.0f)

        paintPolygon(canvas, paint_gray, arrow2, trans, 45.0f)
        paintPolygon(canvas, paint_gray, arrow2, trans, 135.0f)
        paintPolygon(canvas, paint_gray, arrow2, trans, 225.0f)
        paintPolygon(canvas, paint_gray, arrow2, trans, 315.0f)

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
        if (points.isEmpty()) return

        var m = Matrix();
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