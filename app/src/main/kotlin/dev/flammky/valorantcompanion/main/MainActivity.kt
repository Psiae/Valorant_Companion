package dev.flammky.valorantcompanion.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.flammky.valorantcompanion.root.setRootContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootContent()
    }
}