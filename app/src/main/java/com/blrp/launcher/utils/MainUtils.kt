package com.blrp.launcher.utils

import com.blrp.launcher.async.dto.response.FileInfo
import com.blrp.launcher.async.dto.response.LatestVersionInfoDto
import com.blrp.launcher.domain.enums.DownloadType

class MainUtils {
    companion object {
        @JvmStatic
        var usselesTex = mutableListOf(/*".dxt",*/ ".pvr", /*".etc"*/)

        @JvmStatic
        var type = DownloadType.RELOAD_GAME_FILES

        @JvmField
        var FILES_TO_RELOAD: MutableList<FileInfo> = mutableListOf()

        @JvmField
        var LATEST_APK_INFO: LatestVersionInfoDto? = null
    }
}