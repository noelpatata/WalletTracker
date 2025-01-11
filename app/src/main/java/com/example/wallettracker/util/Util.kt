package com.example.wallettracker.util

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Util {

    fun createAndWriteToCache(context: Context, fileName: String, data: String, isSuccess: (Boolean) -> (Unit)) {

        val cacheDir: File = context.cacheDir
        val myFile = File(cacheDir, fileName)

        try {
            BufferedOutputStream(FileOutputStream(myFile)).use { bos ->
                bos.write(data.toByteArray())
                isSuccess(true)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            isSuccess(false)
        }
    }
    fun readFromCache(context: Context, fileName: String): String {
        try{
            val cacheDir: File = context.cacheDir
            val myFile = File(cacheDir, fileName)
            return myFile.readText()
        }catch (e: Exception){
            return ""
        }


    }
}