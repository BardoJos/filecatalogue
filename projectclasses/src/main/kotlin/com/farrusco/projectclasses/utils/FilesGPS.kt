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
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.FileNotFoundException
import java.util.Locale

class FilesGPS {

    companion object{

        //private val tagsList = arrayListOf<String>()

        fun gpsLocation(context: Context, dirname: String, filename: String): GPSInfo {
            val gpsinfo = GPSInfo()
            if (!FilesFolders.hasFileAccess("$dirname/$filename")){
                gpsinfo.mess = "No information available"
                gpsinfo.rtn = 2
                return gpsinfo
            }
            try{
                //val `is` = URL("your image url").openStream()
                //val bis = BufferedInputStream(FileInputStream(File("$dirname/$filename")))
                //val metadata = ImageMetadataReader.readMetadata(bis, 1)
                //val metadata = PngReader(inputFile).metadata

                val exif = ExifInterface("$dirname/$filename")
                // may be later - gpsinfo.exiftags = logExif (exif)
                //val exif1 =  ExifInterface(FileInputStream(File("$dirname/$filename")),ExifInterface.STREAM_TYPE_FULL_IMAGE_DATA)
                gpsinfo.rtn = 0
                if (exif.latLong != null) {
                    gpsinfo.latitude = exif.latLong!![0]
                    gpsinfo.longitude =exif.latLong!![1]

/* test test
            gpsinfo.mess = "No connection"
            gpsinfo.rtn = 1

            var adrl = ArrayList<Address>()
            adrl.add(adr)
            return gpsinfo
            //System.out.println("Latitude: " + latLong[0])
            //System.out.println("Longitude: " + latLong[1])
            */

                if (isOnline(context)){
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(gpsinfo.latitude, gpsinfo.longitude, 1) {
                                gpsinfo.address = it }
                        } else{
                            @Suppress("DEPRECATION")
                            gpsinfo.address = geocoder.getFromLocation(gpsinfo.latitude, gpsinfo.longitude, 1)
                        }
                        return gpsinfo
                    } catch (_: Exception) {
                        gpsinfo.mess = "No connection"
                        gpsinfo.rtn = 1
                        Logging.d( "Unable connect to GeoCoder")
                        // Auto-generated catch block
                        //e.printStackTrace()
                    }
                } else {
                    gpsinfo.mess = "No connection"
                    gpsinfo.rtn = 1
                }
            } else {
                gpsinfo.mess = "No information available"
                gpsinfo.rtn = 2
            }

            } catch(_: FileNotFoundException){
                gpsinfo.mess = "No information available"
                gpsinfo.rtn = 2
            }
            return gpsinfo
        }

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc != null && (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
            return false
        }

/*        private fun constExif (){
            if (tagsList.size>0) return
            tagsList.add(ExifInterface.TAG_IMAGE_WIDTH)
            tagsList.add(ExifInterface.TAG_IMAGE_WIDTH)
            tagsList.add(ExifInterface.TAG_IMAGE_WIDTH)
            tagsList.add(ExifInterface.TAG_IMAGE_LENGTH)
            tagsList.add(ExifInterface.TAG_BITS_PER_SAMPLE)
            tagsList.add(ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION)
            tagsList.add(ExifInterface.TAG_ORIENTATION)
            tagsList.add(ExifInterface.TAG_SAMPLES_PER_PIXEL)
            tagsList.add(ExifInterface.TAG_PLANAR_CONFIGURATION)
            tagsList.add(ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING)
            tagsList.add(ExifInterface.TAG_Y_CB_CR_POSITIONING)
            tagsList.add(ExifInterface.TAG_X_RESOLUTION)
            tagsList.add(ExifInterface.TAG_Y_RESOLUTION)
            tagsList.add(ExifInterface.TAG_RESOLUTION_UNIT)
            tagsList.add(ExifInterface.TAG_STRIP_OFFSETS)
            tagsList.add(ExifInterface.TAG_ROWS_PER_STRIP)
            tagsList.add(ExifInterface.TAG_STRIP_BYTE_COUNTS)
            tagsList.add(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)
            tagsList.add(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)
            tagsList.add(ExifInterface.TAG_TRANSFER_FUNCTION)
            tagsList.add(ExifInterface.TAG_WHITE_POINT)
            tagsList.add(ExifInterface.TAG_PRIMARY_CHROMATICITIES)
            tagsList.add(ExifInterface.TAG_Y_CB_CR_COEFFICIENTS)
            tagsList.add(ExifInterface.TAG_REFERENCE_BLACK_WHITE)
            tagsList.add(ExifInterface.TAG_DATETIME)
            tagsList.add(ExifInterface.TAG_IMAGE_DESCRIPTION)
            tagsList.add(ExifInterface.TAG_MAKE)
            tagsList.add(ExifInterface.TAG_MODEL)
            tagsList.add(ExifInterface.TAG_SOFTWARE)
            tagsList.add(ExifInterface.TAG_ARTIST)
            tagsList.add(ExifInterface.TAG_COPYRIGHT)
            tagsList.add(ExifInterface.TAG_EXIF_VERSION)
            tagsList.add(ExifInterface.TAG_FLASHPIX_VERSION)
            tagsList.add(ExifInterface.TAG_COLOR_SPACE)
            tagsList.add(ExifInterface.TAG_GAMMA)
            tagsList.add(ExifInterface.TAG_PIXEL_X_DIMENSION)
            tagsList.add(ExifInterface.TAG_PIXEL_Y_DIMENSION)
            tagsList.add(ExifInterface.TAG_COMPONENTS_CONFIGURATION)
            tagsList.add(ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL)
            tagsList.add(ExifInterface.TAG_MAKER_NOTE)
            tagsList.add(ExifInterface.TAG_USER_COMMENT)
            tagsList.add(ExifInterface.TAG_RELATED_SOUND_FILE)
            tagsList.add(ExifInterface.TAG_DATETIME_ORIGINAL)
            tagsList.add(ExifInterface.TAG_DATETIME_DIGITIZED)
            tagsList.add(ExifInterface.TAG_OFFSET_TIME)
            tagsList.add(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)
            tagsList.add(ExifInterface.TAG_OFFSET_TIME_DIGITIZED)
            tagsList.add(ExifInterface.TAG_SUBSEC_TIME)
            tagsList.add(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL)
            tagsList.add(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED)
            tagsList.add(ExifInterface.TAG_EXPOSURE_TIME)
            tagsList.add(ExifInterface.TAG_F_NUMBER)
            tagsList.add(ExifInterface.TAG_EXPOSURE_PROGRAM)
            tagsList.add(ExifInterface.TAG_SPECTRAL_SENSITIVITY)
            tagsList.add(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
            tagsList.add(ExifInterface.TAG_OECF)
            tagsList.add(ExifInterface.TAG_SENSITIVITY_TYPE)
            tagsList.add(ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY)
            tagsList.add(ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX)
            tagsList.add(ExifInterface.TAG_ISO_SPEED)
            tagsList.add(ExifInterface.TAG_ISO_SPEED_LATITUDE_YYY)
            tagsList.add(ExifInterface.TAG_ISO_SPEED_LATITUDE_ZZZ)
            tagsList.add(ExifInterface.TAG_SHUTTER_SPEED_VALUE)
            tagsList.add(ExifInterface.TAG_APERTURE_VALUE)
            tagsList.add(ExifInterface.TAG_BRIGHTNESS_VALUE)
            tagsList.add(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)
            tagsList.add(ExifInterface.TAG_MAX_APERTURE_VALUE)
            tagsList.add(ExifInterface.TAG_SUBJECT_DISTANCE)
            tagsList.add(ExifInterface.TAG_METERING_MODE)
            tagsList.add(ExifInterface.TAG_LIGHT_SOURCE)
            tagsList.add(ExifInterface.TAG_FLASH)
            tagsList.add(ExifInterface.TAG_SUBJECT_AREA)
            tagsList.add(ExifInterface.TAG_FOCAL_LENGTH)
            tagsList.add(ExifInterface.TAG_FLASH_ENERGY)
            tagsList.add(ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE)
            tagsList.add(ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION)
            tagsList.add(ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION)
            tagsList.add(ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT)
            tagsList.add(ExifInterface.TAG_SUBJECT_LOCATION)
            tagsList.add(ExifInterface.TAG_EXPOSURE_INDEX)
            tagsList.add(ExifInterface.TAG_SENSING_METHOD)
            tagsList.add(ExifInterface.TAG_FILE_SOURCE)
            tagsList.add(ExifInterface.TAG_SCENE_TYPE)
            tagsList.add(ExifInterface.TAG_CFA_PATTERN)
            tagsList.add(ExifInterface.TAG_CUSTOM_RENDERED)
            tagsList.add(ExifInterface.TAG_EXPOSURE_MODE)
            tagsList.add(ExifInterface.TAG_WHITE_BALANCE)
            tagsList.add(ExifInterface.TAG_DIGITAL_ZOOM_RATIO)
            tagsList.add(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM)
            tagsList.add(ExifInterface.TAG_SCENE_CAPTURE_TYPE)
            tagsList.add(ExifInterface.TAG_GAIN_CONTROL)
            tagsList.add(ExifInterface.TAG_CONTRAST)
            tagsList.add(ExifInterface.TAG_SATURATION)
            tagsList.add(ExifInterface.TAG_SHARPNESS)
            tagsList.add(ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION)
            tagsList.add(ExifInterface.TAG_SUBJECT_DISTANCE_RANGE)
            tagsList.add(ExifInterface.TAG_IMAGE_UNIQUE_ID)
            tagsList.add(ExifInterface.TAG_CAMERA_OWNER_NAME)
            tagsList.add(ExifInterface.TAG_BODY_SERIAL_NUMBER)
            tagsList.add(ExifInterface.TAG_LENS_SPECIFICATION)
            tagsList.add(ExifInterface.TAG_LENS_MAKE)
            tagsList.add(ExifInterface.TAG_LENS_MODEL)
            tagsList.add(ExifInterface.TAG_LENS_SERIAL_NUMBER)
            //tagsList.add(ExifInterface.TAG_GPS_VERSION_ID)
            tagsList.add(ExifInterface.TAG_GPS_LATITUDE_REF)
            tagsList.add(ExifInterface.TAG_GPS_LATITUDE)
            tagsList.add(ExifInterface.TAG_GPS_LONGITUDE_REF)
            tagsList.add(ExifInterface.TAG_GPS_LONGITUDE)
            tagsList.add(ExifInterface.TAG_GPS_ALTITUDE_REF)
            tagsList.add(ExifInterface.TAG_GPS_ALTITUDE)
            tagsList.add(ExifInterface.TAG_GPS_TIMESTAMP)
            tagsList.add(ExifInterface.TAG_GPS_SATELLITES)
            tagsList.add(ExifInterface.TAG_GPS_STATUS)
            tagsList.add(ExifInterface.TAG_GPS_MEASURE_MODE)
            tagsList.add(ExifInterface.TAG_GPS_DOP)
            tagsList.add(ExifInterface.TAG_GPS_SPEED_REF)
            tagsList.add(ExifInterface.TAG_GPS_SPEED)
            tagsList.add(ExifInterface.TAG_GPS_TRACK_REF)
            tagsList.add(ExifInterface.TAG_GPS_TRACK)
            tagsList.add(ExifInterface.TAG_GPS_IMG_DIRECTION_REF)
            tagsList.add(ExifInterface.TAG_GPS_IMG_DIRECTION)
            tagsList.add(ExifInterface.TAG_GPS_MAP_DATUM)
            tagsList.add(ExifInterface.TAG_GPS_DEST_LATITUDE_REF)
            tagsList.add(ExifInterface.TAG_GPS_DEST_LATITUDE)
            tagsList.add(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF)
            tagsList.add(ExifInterface.TAG_GPS_DEST_LONGITUDE)
            tagsList.add(ExifInterface.TAG_GPS_DEST_BEARING_REF)
            tagsList.add(ExifInterface.TAG_GPS_DEST_BEARING)
            tagsList.add(ExifInterface.TAG_GPS_DEST_DISTANCE_REF)
            tagsList.add(ExifInterface.TAG_GPS_DEST_DISTANCE)
            tagsList.add(ExifInterface.TAG_GPS_PROCESSING_METHOD)
            tagsList.add(ExifInterface.TAG_GPS_AREA_INFORMATION)
            tagsList.add(ExifInterface.TAG_GPS_DATESTAMP)
            tagsList.add(ExifInterface.TAG_GPS_DIFFERENTIAL)
            tagsList.add(ExifInterface.TAG_GPS_H_POSITIONING_ERROR)
            tagsList.add(ExifInterface.TAG_INTEROPERABILITY_INDEX)
            tagsList.add(ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH)
            tagsList.add(ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH)
            tagsList.add(ExifInterface.TAG_DNG_VERSION)
            tagsList.add(ExifInterface.TAG_DEFAULT_CROP_SIZE)
            tagsList.add(ExifInterface.TAG_ORF_THUMBNAIL_IMAGE)
            tagsList.add(ExifInterface.TAG_ORF_PREVIEW_IMAGE_START)
            tagsList.add(ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH)
            tagsList.add(ExifInterface.TAG_ORF_ASPECT_FRAME)
            tagsList.add(ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER)
            tagsList.add(ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER)
            tagsList.add(ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER)
            tagsList.add(ExifInterface.TAG_RW2_SENSOR_TOP_BORDER)
            tagsList.add(ExifInterface.TAG_RW2_ISO)
            tagsList.add(ExifInterface.TAG_RW2_JPG_FROM_RAW)
            tagsList.add(ExifInterface.TAG_XMP)
            tagsList.add(ExifInterface.TAG_NEW_SUBFILE_TYPE)
            tagsList.add(ExifInterface.TAG_SUBFILE_TYPE)
        }

        fun logExif (exif: ExifInterface): ArrayMap<String, String> {
            val tags = ArrayMap<String, String>()
            constExif()
            tagsList.forEach {
                try {
                    if (exif.getAttribute(it) != null){
                        tags[it] = exif.getAttribute(it)
                        Logging.d(it + " = " + exif.getAttribute(it))
                    }
                } catch (e: Exception){
                    Logging.d("Error: $it")
                }
            }
            return tags
        }*/
    }
}

class GPSInfo{
    var latitude = 0.0
    var longitude = 0.0
    var mess: String=""
    var address: List<Address>? = null
    //var exiftags = ArrayMap<String, String>()
    var rtn: Int=0
}