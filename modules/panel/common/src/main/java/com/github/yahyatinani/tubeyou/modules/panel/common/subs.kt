package com.github.yahyatinani.tubeyou.modules.panel.common

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.media3.common.MimeTypes
import com.github.yahyatinani.tubeyou.modules.designsystem.core.VIDEO_INFO_DIVIDER
import com.github.yahyatinani.tubeyou.modules.designsystem.core.formatSubCount
import com.github.yahyatinani.tubeyou.modules.designsystem.core.formatViews
import com.github.yahyatinani.tubeyou.modules.designsystem.data.VideoViewModel
import com.github.yahyatinani.tubeyou.modules.panel.common.videoplayer.CommentsListState
import com.github.yahyatinani.tubeyou.modules.panel.common.videoplayer.PlayerState
import com.github.yahyatinani.tubeyou.modules.panel.common.videoplayer.createDashSource
import io.github.yahyatinani.recompose.fsm.fsm
import io.github.yahyatinani.recompose.regSub
import io.github.yahyatinani.recompose.subs.Query
import io.github.yahyatinani.y.core.collections.IPersistentMap
import io.github.yahyatinani.y.core.get
import io.github.yahyatinani.y.core.l
import io.github.yahyatinani.y.core.m
import io.github.yahyatinani.y.core.v
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime

enum class Stream {
  title,
  uploader,
  video_uri,
  aspect_ratio,
  thumbnail,
  quality_list,
  current_quality,
  mime_type,
  views,
  date,
  avatar,
  sub_count,
  channel_name,
  likes_count,
  description,
  height,
  comments,
  comments_count,
  highlight_comment,
  highlight_comment_avatar,
  comments_disabled
}

fun ratio(streamData: StreamData): Float {
  val default = 16 / 9f
  if (streamData.livestream) return default

  val stream = streamData.videoStreams.firstOrNull {
    val codec: String? = it.codec
    codec != null && codec.contains("avc1")
  } ?: return default

  val w = stream.width / 240f
  val h = stream.height / 240f
  return w / h
}

/**
 * @param date eg. 2020-03-27
 */
fun timeAgoFormat(date: String): String {
  val localDate = date.toLocalDate().periodUntil(
    Clock.System.now().toLocalDateTime(
      TimeZone.currentSystemDefault()
    ).date
  )

  val years = localDate.years
  val months = localDate.months
  val days = localDate.days

  return when {
    years > 0 -> "${years}y ago"
    months > 0 -> "${months}m ago"
    days > 0 -> "${days}d ago"
    else -> "today"
  }
}

private fun shorten(uploaded: String): String = uploaded
  .replace(" years", "y")
  .replace(" months", "m")
  .replace(" days", "d")
  .replace(" hours", "h")

@OptIn(ExperimentalMaterial3Api::class)
fun regCommonSubs() {
  regSub("playback_fsm") { db: AppDb, _: Query ->
    db["playback_fsm"]
  }

  regSub<IPersistentMap<Any, Any>, Any?>(
    queryId = "currently_playing",
    initialValue = null,
    inputSignal = v("playback_fsm")
  ) { playbackFsm: IPersistentMap<Any, Any>?, _, (_, context) ->
    val playbackMachine = get<Any>(playbackFsm, fsm._state)
    val playerRegion = get<Any>(playbackMachine, ":player")
    val stream = get<StreamData>(playbackFsm, "stream_data")

    if (playerRegion == null || playerRegion == PlayerState.LOADING ||
      playbackFsm == null ||
      stream == null
    ) {
      return@regSub null
    }

    println("playerRegion $playerRegion")

    val currentQuality = playbackFsm["current_quality"]
    val ql = get<List<Pair<String, Int>>>(playbackFsm, "quality_list") ?: l()
    val qualityList = ql.map {
      val a = if (currentQuality == it.second) "${it.first} ✓" else it.first
      a to it.second
    }

    val cq = if (currentQuality == null) "" else "${currentQuality}p"

    val viewModel = get<VideoViewModel>(playbackFsm, "videoVm")!!

    val views = formatViews(stream.views)

    val views1 = if (viewModel.isLiveStream) {
      views + " watching" + " Started ${timeAgoFormat(stream.uploadDate)}"
    } else {
      "$views views"
    }

    val m = m<Any, Any?>(
      Stream.title to stream.title,
      Stream.uploader to stream.uploader,
      Stream.thumbnail to stream.thumbnailUrl,
      Stream.quality_list to qualityList,
      Stream.current_quality to cq,
      Stream.aspect_ratio to ratio(stream),
      Stream.views to views1,
      Stream.date to shorten(viewModel.uploaded),
      Stream.channel_name to viewModel.uploaderName,
      Stream.avatar to stream.uploaderAvatar,
      Stream.sub_count to formatSubCount(stream.uploaderSubscriberCount),
      Stream.likes_count to formatViews(stream.likes),
      Stream.description to stream.description
    )

    if (stream.videoStreams.isNotEmpty()) {
      if (stream.livestream && stream.dash != null) {
        m.assoc(Stream.video_uri, stream.dash.toUri())
      } else {
        m.assoc(
          Stream.video_uri,
          createDashSource(stream, context as Context)
        )
      }.assoc(Stream.mime_type, MimeTypes.APPLICATION_MPD)
    } else {
      m.assoc(Stream.video_uri, stream.hls!!.toUri())
        .assoc(Stream.mime_type, MimeTypes.APPLICATION_M3U8)
    }
  }

  regSub<IPersistentMap<Any, Any>, Any>(
    queryId = Stream.comments,
    initialValue = AppendingPanelVm.Loading,
    inputSignal = v("playback_fsm")
  ) { playbackFsm: IPersistentMap<Any, Any>?, prev, _ ->
    val playbackMachine = get<Any>(playbackFsm, fsm._state)

    when (get<CommentsListState>(playbackMachine, ":comments_list")) {
      null, CommentsListState.LOADING -> AppendingPanelVm.Loading
      CommentsListState.LOADED -> {
        val comments = get<StreamComments>(playbackFsm, "stream_comments")!!

        val commentsSection = when {
          comments.disabled -> m(
            Stream.comments_disabled to true,
            Stream.comments_count to ""
          )

          comments.comments.isEmpty() -> m(
            Stream.comments_count to "",
            Stream.highlight_comment to ""
          )

          else -> {
            val firstComment = comments.comments[0]
            m(
              Stream.comments_count to formatSubCount(comments.commentCount),
              Stream.highlight_comment to HtmlCompat.fromHtml(
                firstComment.commentText,
                HtmlCompat.FROM_HTML_MODE_LEGACY
              ),
              Stream.highlight_comment_avatar to firstComment.thumbnail
            )
          }
        }

        val ret = comments.comments.map { comment ->
          m(
            "author" to "${comment.author} $VIDEO_INFO_DIVIDER " +
              comment.commentedTime,
            "author_avatar" to comment.thumbnail,
            "comment_text" to HtmlCompat.fromHtml(
              comment.commentText,
              HtmlCompat.FROM_HTML_MODE_LEGACY
            ),
            "likes_count" to formatSubCount(comment.likeCount),
            "replies_count" to comment.replyCount
          )
        }

        AppendingPanelVm.Loaded(
          data = Data(
            m(
              "comments_list" to ret,
              "comments_section" to commentsSection
            )
          )
        )
      }

      CommentsListState.REFRESHING -> {
        AppendingPanelVm.Refreshing((prev as AppendingPanelVm.Loaded).data)
      }

      CommentsListState.APPENDING -> {
        (prev as AppendingPanelVm.Loaded).copy(isAppending = true)
      }
    }
  }

  regSub("comments_sheet") { db: AppDb, _: Query ->
    val playbackFsm = db["playback_fsm"]
    val playbackMachine = get<Any>(playbackFsm, fsm._state)
    val commentsSheet =
      get<SheetValue>(playbackMachine, ":comments_sheet")
    commentsSheet ?: SheetValue.Hidden
  }

  regSub("description_sheet") { db: AppDb, _: Query ->
    val playbackFsm = db["playback_fsm"]
    val playbackMachine = get<Any>(playbackFsm, fsm._state)
    val commentsSheet =
      get<SheetValue>(playbackMachine, ":description_sheet")
    commentsSheet ?: SheetValue.Hidden
  }
}

@Immutable
data class Items(val list: List<Any> = v())

@Immutable
data class Data(val value: Any)

@Immutable
sealed class AppendingPanelVm(
  open val data: Data? = null,
  val isLoading: Boolean = false,
  open val isAppending: Boolean = false,
  val isRefreshing: Boolean = false,
  open val error: Int? = null
) {
  object Loading : AppendingPanelVm(isLoading = true)

  data class Refreshing(override val data: Data) :
    AppendingPanelVm(data = data, isRefreshing = true)

  data class Loaded(
    override val data: Data,
    override val isAppending: Boolean = false
  ) : AppendingPanelVm(data = data, isAppending = isAppending)

  data class Error(override val error: Int?) : AppendingPanelVm(error = error)
}
