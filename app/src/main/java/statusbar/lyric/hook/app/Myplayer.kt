/*
 * StatusBarLyric
 * Copyright (C) 2021-2022 fkj@fkj233.cn
 * https://github.com/577fkj/StatusBarLyric
 *
 * This software is free opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by 577fkj.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/577fkj/StatusBarLyric/blob/main/LICENSE>.
 */

package statusbar.lyric.hook.app

import android.content.Context
import de.robv.android.xposed.callbacks.XC_LoadPackage
import statusbar.lyric.hook.BaseHook
import statusbar.lyric.utils.LogUtils
import statusbar.lyric.utils.Utils
import statusbar.lyric.utils.ktx.hookAfterMethod


class Myplayer(private val lpparam: XC_LoadPackage.LoadPackageParam): BaseHook(lpparam) {
    override fun hook(){
        "remix.myplayer.util.p".hookAfterMethod("o", Context::class.java, classLoader = lpparam.classLoader) {
            it.result = true
        }
        "remix.myplayer.service.MusicService".hookAfterMethod("n1", String::class.java, classLoader = lpparam.classLoader) {
            val context: Context = it.thisObject as Context
            LogUtils.e("myplayer: " + it.args[0].toString())
            Utils.sendLyric(context, it.args[0].toString(), "Myplayer")
        }
    }
}