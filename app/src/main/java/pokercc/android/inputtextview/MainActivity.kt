package pokercc.android.inputtextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edit_text_group.setTemplateText("ABCDEF")
        qwert_input_method.inputMethodListener = edit_text_group
        toggle_button.setOnCheckedChangeListener { buttonView, isChecked ->
            qwert_input_method.showNumber = isChecked
        }
    }
}
