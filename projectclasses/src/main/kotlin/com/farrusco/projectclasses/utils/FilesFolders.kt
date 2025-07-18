/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.projectclasses.utils

import android.content.Context
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import com.farrusco.projectclasses.common.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

@Suppress("unused")
class FilesFolders {

    private val arrFiles: MutableList<FolderProduct> = mutableListOf()

    fun getFilesFolder(folder: String, mSwRecursive: Boolean): MutableList<FolderProduct>{
        arrFiles.clear()
        listDirectory(folder, mSwRecursive)
        return arrFiles
    }

    private fun listDirectory(dirPath: String, mSwRecursive: Boolean) {
        val dir = File(dirPath)
        val firstLevelFiles = dir.listFiles()
        if (firstLevelFiles != null && firstLevelFiles.isNotEmpty()) {
            for (aFile in firstLevelFiles) {
                if (aFile.isDirectory && mSwRecursive) {
                    //System.out.println("[" + aFile.name.toString() + "]")
                    listDirectory(aFile.absolutePath, true)
                } else {
                    //System.out.println(aFile.name)
                    if (aFile.isFile && !aFile.name.startsWith(".")){
                        val folderproduct = FolderProduct()
                        folderproduct.folder = aFile.path.substring(0,aFile.path.length-aFile.name.length-1)
                        folderproduct.filename = aFile.name
                        folderproduct.level = 2
                        arrFiles.add(folderproduct)
                    }
                }
            }
        }
    }

    fun getFolder(folder: String, mSwRecursive: Boolean): MutableList<FolderProduct>{
        arrFiles.clear()
        listFolder(folder, mSwRecursive, 2,0)
        return arrFiles
    }

    private fun listFolder(folder: String, mSwRecursive: Boolean, level: Int, parentRow: Int) {
        val dir = File(folder)
        try {
            val fileproduct = FolderProduct()
            val firstLevelFiles = dir.listFiles{ pathname -> pathname.isDirectory }
            fileproduct.title = dir.path
            fileproduct.folder = dir.path
            fileproduct.isChecked = true
            fileproduct.level = level
            fileproduct.parentRow = parentRow
            arrFiles.add(fileproduct)
            val parent = arrFiles.size-1
            if (firstLevelFiles != null && firstLevelFiles.isNotEmpty()) {
                for (aFile in firstLevelFiles) {
                    if (aFile.isDirectory && mSwRecursive) {
                        listFolder(aFile.absolutePath, true, level+1, parent)
                    }
                }
            }
        } catch (e: Exception){
            Logging.d(Constants.APP_NAME + "/listFolder:\n" + e.stackTraceToString())
        }
    }
 
/*    fun connectToWiFi(context: Context, pin: String, ssid:String) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                    ConnectivityManager
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pin)
            .setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            .build()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                //showToast(context,context.getString(R.string.connection_success))
            }

            override fun onUnavailable() {
                super.onUnavailable()
                //showToast(context,context.getString(R.string.connection_fail))
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                //showToast(context,context.getString(R.string.out_of_range))
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)
    }*/

    companion object{
 /*  private fun File.bufferedOutputStream(size: Int = 8192) = BufferedOutputStream(this.outputStream(), size)
  private fun File.zipOutputStream(size: Int = 8192) = ZipOutputStream(this.bufferedOutputStream(size))
  private fun File.bufferedInputStream(size: Int = 8192) = BufferedInputStream(this.inputStream(), size)
  private fun File.asZipEntry() = ZipEntry(this.name)

  fun archive(files: List<File>, destination: File) =
      destination.zipOutputStream().use {
          files.forEach { file ->
              it.putNextEntry(file.asZipEntry())
              file.bufferedInputStream().use { bis -> bis.copyTo(it) }
          }
      }

  fun unpackZip(DIR: String, zipname: String): Boolean {
      val bufferSize = 8192  //2048
      try {
          val inp = FileInputStream("$DIR/$zipname")
          val zis = ZipInputStream(BufferedInputStream(inp,bufferSize))
          val ze: ZipEntry
          if (zis.nextEntry.also { ze=it } != null){
              val f = File(DIR, ze.name)
              val canonicalPath = f.canonicalPath
              if (!canonicalPath.startsWith(DIR)) {
                  // SecurityException, terminate app
                  zis.close()
                  // app will exit because the user is using this app illegal
                  exitProcess(-1)
              }
              if (ze.name.substring(0,ze.name.indexOf(".xml")) != zipname.substring(0,zipname.indexOf(".zip"))){
                  zis.close()
                  exitProcess(-1)
              }
              val baos = ByteArrayOutputStream()
              val buffer = ByteArray(bufferSize)
              var count: Int
              val fout = FileOutputStream("$DIR/${ze.name}")
              // reading and writing
              while (zis.read(buffer).also { count = it } != -1) {
                  baos.write(buffer, 0, count)
                  val bytes: ByteArray = baos.toByteArray()
                  fout.write(bytes)
                  baos.reset()
              }
              fout.close()
              zis.closeEntry()
          }
          inp.close()
          zis.close()
      } catch (e: IOException) {
          Logging.d(Constants.APP_NAME + "/unpackZip", "Failed to load\n$e")
          return false
      } catch (e: SecurityException) {
          Logging.e(Constants.APP_NAME + "/unpackZip", "SecurityException\n$e")
          exitProcess(-1)
      }
      return true
  }*/

        fun hasFileAccess(filename: String): Boolean{
          val f = File(filename)
          if (f.exists()){
              return try {
                  val fs = f.inputStream()
                  fs.close()
                  true
              } catch(e: FileNotFoundException){
                  false
              }
          }
          return false
        }

        fun fileCreationDate(fileName: String): String{
            val file = File(fileName)
            var creationTime:String
            try {
                val exif = ExifInterface(file)
                creationTime = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED).toString()
                if (creationTime == "null"){
                    val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                    creationTime = attr.creationTime().toString()
                }
            } catch (_: Exception){
                //val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                //creationTime = attr.creationTime().toString()
                return ""
            }

            if (creationTime.length >= 10) {
                val sYear = creationTime.substring(0, 4)
                val sMonth = creationTime.substring(5, 7)
                val sDay = creationTime.substring(8, 10)

                return LocalDate.of(sYear.toInt(), sMonth.toInt(), sDay.toInt()).toString()

            }

            return creationTime
        }

        fun getExtension(fileName: String): String {
            try {
                if (fileName.contains(".")) {
                    val filenameArray =
                        fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    return filenameArray[filenameArray.size - 1]
                } else {
                    return "dir"
                }
            } catch (_: java.lang.Exception) {
                return "err"
            }
        }

        fun fileFromAsset(context: Context, assetName: String): File {
            val outFile = File(context.cacheDir, assetName)
            if (assetName.contains("/")) {
                outFile.parentFile!!.mkdirs()
            }
            fileCopy(context.assets.open(assetName), outFile)
            return outFile
        }

        fun fileCopy(inputPath: String, inputFile: String, outputPath: String, outputFile: String, overwrite: Boolean = true) {
            val inputFilex = File("$inputPath/$inputFile")
            var outFile = File(outputPath)
            if (!outFile.exists()) {
                outFile.mkdirs()
            }
            outFile = File("$outputPath/$outputFile")
            try {
                inputFilex.copyTo(outFile, overwrite)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun fileCopy(inputStream: InputStream?, output: File?) {
            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(output)
                var read: Int
                val bytes = ByteArray(1024)
                while (inputStream!!.read(bytes).also { read = it } != -1) {
                    outputStream.write(bytes, 0, read)
                }
            } finally {
                try {
                    inputStream?.close()
                } finally {
                    outputStream?.close()
                }
            }
        }

        fun fileDownload(context: Context, assetName: String, filePath: String, fileName: String?){

            val dirPath = "${Environment.getExternalStorageDirectory()}/${filePath}"
            val outFile = File(dirPath)
            //Create New File if not present
            if (!outFile.exists()) {
                outFile.mkdirs()
            }
            val outFile1 = File(dirPath, "/$fileName.pdf")
            fileCopy(context.assets.open(assetName), outFile1)
        }

        fun isDuplicateFile(file1: File, file2: File): Boolean {
            return getFileMD5(file1).contentEquals(getFileMD5(file2))
        }

        private fun getFileMD5(file: File?): ByteArray? {
            if (file == null) return null
            var dis: DigestInputStream? = null
            try {
                val fis = FileInputStream(file)
                var md = MessageDigest.getInstance("MD5")
                dis = DigestInputStream(fis, md)
                val buffer = ByteArray(1024 * 256)
                while (true) {
                    if (dis.read(buffer) <= 0) break
                }
                md = dis.messageDigest
                return md.digest()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    dis?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return null
        }

        fun createMediaFolder(mediaFolder: String, createEmpty: Boolean = false) {
            val inputDir = File(mediaFolder)
            if (inputDir.exists()) {
                inputDir.deleteRecursively()
            }
            inputDir.mkdirs()
            /**create empty ".nomedia" files in the directories
             * to prevent gallery from including these images */
            if (createEmpty){
                val inputNoMedia = File("$mediaFolder/.nomedia")
                //if (!inputNoMedia.exists()) {
                try {
                    inputNoMedia.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                //}
            }
        }

        fun getOutputDirectory(): File {

            val outFile = File(Constants.resultsPath)
            if (!outFile.exists()) {
                outFile.mkdirs()
            }
            return outFile}


        fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder,
                SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension
            )
    }
}

class FolderProduct{
    var id: Int = 0
    var mainid: Int = 0
    var parentRow: Int = 0
    var level: Int = 0
    var title: String = ""
    var folder: String = ""
    var filename: String = ""
    var isChecked: Boolean = true
    var inUse: Boolean = false
    var gpsinfo = GPSInfo()
    var category: String = ""
    //lateinit var exif: ExifInterface
    fun creationDateStr(): String{
        return FilesFolders.fileCreationDate("$folder/$filename")
    }
}