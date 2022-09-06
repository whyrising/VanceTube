package com.github.whyrising.vancetube.home

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.github.whyrising.recompose.regSub
import com.github.whyrising.recompose.subscribe
import com.github.whyrising.vancetube.R
import com.github.whyrising.vancetube.base.AppDb
import com.github.whyrising.vancetube.ui.theme.Blue300
import com.github.whyrising.y.core.get
import com.github.whyrising.y.core.v
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalTime
import java.math.RoundingMode
import java.text.DecimalFormat

fun formatSeconds(seconds: Int): String {
  val format = "%02d"
  val localTime = LocalTime.fromSecondOfDay(seconds)
  val s = format.format(localTime.second)
  return when (localTime.hour) {
    0 -> "${localTime.minute}:$s"
    else -> {
      val m = format.format(localTime.minute)
      "${localTime.hour}:$m:$s"
    }
  }
}

const val VIDEO_INFO_DIVIDER = " • "

fun formatVideoInfo(
  author: String,
  authorId: String,
  viewCount: String,
  viewsLabel: String,
  publishedText: String
): AnnotatedString = buildAnnotatedString {
  val str = "$author$VIDEO_INFO_DIVIDER$viewCount $viewsLabel" +
    "$VIDEO_INFO_DIVIDER$publishedText"
  val startIndex = 0
  val endIndex = startIndex + author.length
  append(str)
  addStyle(
    style = SpanStyle(color = Blue300),
    start = startIndex,
    end = endIndex
  )
  addStringAnnotation(
    tag = "author",
    annotation = authorId,
    start = startIndex,
    end = endIndex
  )
}

val OneDigitDecimalFormat = DecimalFormat("#.#").apply {
  roundingMode = RoundingMode.FLOOR
}
const val thousandsSign = "K"
const val MillionsSign = "M"
const val BillionsSign = "B"

fun formatViews(viewsCount: Long): String = when {
  viewsCount < 1000 -> "$viewsCount"
  viewsCount < 10_000 -> {
    val x = viewsCount / 1000f
    "${OneDigitDecimalFormat.format(x)}$thousandsSign".replace(".0", "")
  }
  viewsCount < 100_000 -> "${viewsCount / 1000}$thousandsSign"
  viewsCount < 1_000_000 -> "${viewsCount / 1000}$thousandsSign"
  viewsCount < 1_000_000_000 -> "${viewsCount / 1_000_000}$MillionsSign"
  else -> "${viewsCount / 1_000_000_000}$BillionsSign"
}

fun regHomeSubs(context: Context) {
  regSub<AppDb, HomePanelState>(queryId = home.state) { db, _ ->
    db[home.panel] as HomePanelState
  }

  regSub<HomePanelState, HomePanelState>(
    queryId = home.matrialised_state,
    signalsFn = { subscribe(v(home.state)) },
    placeholder = HOME_START,
    context = Dispatchers.Default,
    computationFn = { state, _ ->
      when (state) {
        is HomePanelState.Loaded -> {
          val formatted = state.popularVideos
            .fold(v<VideoViewModel>()) { acc, videoMetadata ->
              acc.conj(
                VideoViewModel(
                  id = videoMetadata.videoId,
                  authorId = videoMetadata.authorId,
                  title = videoMetadata.title,
                  thumbnail = videoMetadata.videoThumbnails[4].url,
                  length = formatSeconds(videoMetadata.lengthSeconds),
                  info = formatVideoInfo(
                    author = videoMetadata.author,
                    authorId = videoMetadata.authorId,
                    viewCount = formatViews(videoMetadata.viewCount),
                    viewsLabel = context.getString(R.string.views_label),
                    publishedText = videoMetadata.publishedText
                  )
                )
              )
            }
          HomePanelState.Materialised(formatted)
        }
        else -> state
      }
    }
  )
}
