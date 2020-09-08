package com.github.miracle.utils.tools.translate

import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


object ScriptInvocable {
    init {
        System.setProperty("nashorn.args", "--language=es6")
    }

    private fun Reader.createInvocable(engine: ScriptEngine): Invocable {
        engine.eval(this)
        return engine as Invocable
    }

    private val engineManager = ScriptEngineManager()
    private val engine: ScriptEngine = engineManager.getEngineByName("nashorn")
    val invocable =
        Files.newBufferedReader(Paths.get("tk.js"))
//        Files.newBufferedReader(Paths.get("C:\\Users\\cmdrj\\Desktop\\archived\\miracle\\src\\main\\kotlin\\com\\github\\miracle\\utils\\tools\\translate\\tk.js"))
            .createInvocable(engine)
}