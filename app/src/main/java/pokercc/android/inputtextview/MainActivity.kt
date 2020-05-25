package pokercc.android.inputtextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edit_text_group.setTemplateText("ABCD")
        qwert_input_method.showNumber = true
        qwert_input_method.inputMethodListener = object : InputMethodListener {
            override fun onInputPreviewStart(char: Char) {


            }

            override fun onInputPreviewEnd() {
            }

            override fun onInput(char: Char) {
                Toast.makeText(applicationContext, "$char", Toast.LENGTH_SHORT).show()
            }

            override fun onDelete() {
            }
        }
    }
}
