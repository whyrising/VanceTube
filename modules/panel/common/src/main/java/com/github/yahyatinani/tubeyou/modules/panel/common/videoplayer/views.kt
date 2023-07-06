package com.github.yahyatinani.tubeyou.modules.panel.common.videoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.github.yahyatinani.tubeyou.modules.designsystem.component.Thumbnail
import com.github.yahyatinani.tubeyou.modules.designsystem.theme.Grey300
import com.github.yahyatinani.tubeyou.modules.panel.common.R
import io.github.yahyatinani.y.core.collections.IPersistentMap

@SuppressLint("NewApi")
@Composable
@OptIn(UnstableApi::class)
fun VideoView(
  modifier: Modifier = Modifier,
  streamData: IPersistentMap<Any, Any>?,
  thumbnail: String?,
  showPlayerThumbnail: Boolean,
  showPlayerLoading: Boolean,
  isCollapsed: Boolean,
  context: Context = LocalContext.current
) {
  val orientation = LocalConfiguration.current.orientation

  Box {
    if (streamData != null) {
      AndroidView(
        modifier = modifier.apply {
          if (orientation == ORIENTATION_LANDSCAPE) {
            padding(start = 26.dp)
          }
        },
        factory = {
          regPlaybackFxs()
          regPlaybackEvents()
          PlayerView(context).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.black))
          }
        }
      ) {
        it.player = getExoPlayer(context, streamData)
        it.useController = !isCollapsed || orientation == ORIENTATION_LANDSCAPE
      }
    }

    if (showPlayerThumbnail) {
      Thumbnail(
        url = thumbnail,
        modifier = modifier
      )
    }
    if (showPlayerLoading) {
      CircularProgressIndicator(
        modifier = Modifier
          .align(Alignment.Center)
          .size(56.dp),
        color = Grey300.copy(alpha = .4f)
      )
    }
  }
}
