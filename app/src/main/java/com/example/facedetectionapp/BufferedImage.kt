package com.example.facedetectionapp

class BufferedImage {

    // The width of the image.
    private val width: Int

    // The height of the image.
    private val height: Int

    // The array of pixels that make up the image.
    private val pixels: IntArray

    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
        this.pixels = IntArray(width * height)
    }

    // Gets the width of the image.
    fun getWidth(): Int {
        return width
    }

    // Gets the height of the image.
    fun getHeight(): Int {
        return height
    }

    // Gets the pixel at the specified location.
    fun getPixel(x: Int, y: Int): Int {
        return pixels[x + y * width]
    }

    // Sets the pixel at the specified location.
    fun setPixel(x: Int, y: Int, value: Int) {
        pixels[x + y * width] = value
    }

    // Converts the image to a byte array.
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(width * height * 4)
        for (i in 0 until width * height) {
            val pixel = pixels[i]
            byteArray[i * 4] = (pixel and 0xFF).toByte()
            byteArray[i * 4 + 1] = ((pixel shr 8) and 0xFF).toByte()
            byteArray[i * 4 + 2] = ((pixel shr 16) and 0xFF).toByte()
            byteArray[i * 4 + 3] = ((pixel shr 24) and 0xFF).toByte()
        }
        return byteArray
    }

}