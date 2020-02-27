package bg.dihanov.customviewexamples

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import bg.dihanov.customviewexamples.views.graph.Marker
import bg.dihanov.customviewexamples.views.misc.Color
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val markers = mutableListOf<Marker>().apply {
            this.add(Marker(value = 400))
            this.add(Marker(value = 130))
            this.add(Marker(value = 45))
            this.add(Marker(value = 5))
            this.add(Marker(value = 20))
            this.add(Marker(value = 442))
            this.add(Marker(value = 400))
            this.add(Marker(value = 22))
            this.add(Marker(value = 104))
            this.add(Marker(value = 103))
            this.add(Marker(value = 65))
            this.add(Marker(value = 22))
        }

        val weeks = mutableListOf<String>().apply {
            this.add("week 1")
            this.add("week 2")
            this.add("week 3")
            this.add("week 4")
            this.add("week 5")
        }


        ValueAnimator.ofInt(0, 100).apply {
            addUpdateListener { updatedAnimation ->
                val progress = updatedAnimation.animatedValue as Int
                progress_circular.setPercentage(progress)
//                progress_arc.progress = updatedAnimation.animatedValue as Int
//                colorful_progressbar.progress = progress
            }
            interpolator = LinearInterpolator()
            duration = 10000
            start()
        }

        graph.setMarkersAndWeeks(markers, weeks)
        normal_graph.setMarkersAndWeeks(markers, weeks)
        indicator.colors = listOf(Color.RED, Color.BLUE, Color.GREEN)

        progress_arc.progressToValueManually(10000)
        colorful_progressbar.progressToManually(10000)
    }
}
