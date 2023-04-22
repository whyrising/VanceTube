package com.github.whyrising.vancetube.modules.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whyrising.vancetube.modules.designsystem.R
import com.github.whyrising.vancetube.modules.designsystem.data.ChannelVm
import com.github.whyrising.vancetube.modules.designsystem.data.PlaylistVm
import com.github.whyrising.vancetube.modules.designsystem.data.VideoViewModel

@Composable
fun VideoItemPortrait(
  modifier: Modifier = Modifier,
  videoInfoTextStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
  viewModel: VideoViewModel,
  thumbnailHeight: Dp
) {
  Column(modifier = Modifier.clickable { /*todo:*/ }) {
    val videoLength = when {
      viewModel.isUpcoming -> stringResource(R.string.upcoming)
      else -> viewModel.length
    }
    Thumbnail(
      modifier = Modifier
        .fillMaxWidth()
        .height(thumbnailHeight),
      url = viewModel.thumbnail,
      videoLength = videoLength
    )
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(top = 8.dp, end = 4.dp, bottom = 24.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        VideoItemTitle(title = viewModel.title)
        Spacer(modifier = Modifier.height(4.dp))
        VideoItemInfo(
          info = viewModel.info,
          textStyle = videoInfoTextStyle
        )
      }

      Spacer(modifier = Modifier.width(24.dp))

      VideoItemMoreButton()
    }
  }
}

@Composable
fun VideoListItemLandscapeCompact(viewModel: VideoViewModel) {
  Row(
    modifier = Modifier
      .testTag("video")
      .padding(vertical = 8.dp)
      .clickable { /*todo:*/ }
  ) {
    Thumbnail(
      modifier = Modifier.weight(.24f),
      url = viewModel.thumbnail,
      videoLength = viewModel.length
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(.8f)) {
      VideoItemTitle(title = viewModel.title)
      Spacer(modifier = Modifier.height(4.dp))
      VideoItemInfo(info = viewModel.info)
    }

    VideoItemMoreButton()
  }
}

@Composable
fun SearchSuggestionItem(text: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(50.dp)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Search,
      contentDescription = "Suggestion icon",
      modifier = Modifier.weight(weight = .1f)
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = text,
      modifier = Modifier.weight(weight = 1f),
      style = LocalTextStyle.current.copy(lineHeight = 18.sp)
    )
    Spacer(modifier = Modifier.width(16.dp))
    Icon(
      imageVector = Icons.Default.ArrowOutward,
      contentDescription = "Suggestion icon",
      modifier = Modifier.weight(weight = .1f)
    )
  }
}

@Composable
fun ChannelItem(modifier: Modifier = Modifier, vm: ChannelVm) {
  val typography = MaterialTheme.typography
  ListItem(
    modifier = modifier.clickable { /*TODO*/ },
    leadingContent = {
      ChannelAvatar(
        modifier = Modifier.padding(horizontal = 40.dp),
        url = vm.authorThumbnail
      )
    },
    headlineContent = {
      Text(text = vm.author, style = typography.labelLarge)
      val textStyle = typography.bodySmall.copy(
        color = LocalContentColor.current.copy(alpha = .6f),
        fontSize = 12.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = vm.handle, style = textStyle)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = "${vm.subCount} subscribers", style = textStyle)
      val colorScheme = MaterialTheme.colorScheme
      Button(
        modifier = Modifier
          .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
        onClick = { /*TODO*/ },
        colors = ButtonDefaults.buttonColors(
          containerColor = colorScheme.onSurface,
          contentColor = colorScheme.surface
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
      ) {
        Text(
          text = stringResource(R.string.subscribe),
          style = typography.labelMedium
        )
      }
    }
  )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchSuggestionItemDarkPreview() {
  SearchSuggestionItem(
    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do" +
      " eiusmod tempor "
  ) {}
}

@Composable
fun PlayListPortrait(
  modifier: Modifier = Modifier,
  viewModel: PlaylistVm,
  thumbnailHeight: Dp
) {
  Column(modifier = Modifier.clickable { /*todo:*/ }) {
    Box {
      ThumbnailImage(
        modifier = Modifier
          .fillMaxWidth()
          .height(thumbnailHeight),
        url = viewModel.thumbnailUrl
      )
      Row(
        modifier = Modifier
          .align(alignment = Alignment.BottomCenter)
          .background(color = Color.Black.copy(alpha = .4f))
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.PlaylistPlay,
          contentDescription = "",
          tint = Color.White
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = viewModel.videoCount, color = Color.White)
      }
    }

    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(top = 8.dp, end = 4.dp, bottom = 24.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        VideoItemTitle(title = viewModel.title)
        Spacer(modifier = Modifier.height(4.dp))
        VideoItemInfo(
          info = AnnotatedString(viewModel.author),
          textStyle = TextStyle.Default.copy(fontSize = 12.sp)
        )
      }

      Spacer(modifier = Modifier.width(24.dp))

      VideoItemMoreButton()
    }
  }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChannelItemDarkPreview() {
  ChannelItem(
    vm = ChannelVm(
      id = "authorId",
      author = "author",
      subCount = "14.1M",
      handle = "@authorId",
      authorThumbnail = ""
    )
  )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlayListPortraitPreview() {
  val density: Density = LocalDensity.current
  PlayListPortrait(
    viewModel = PlaylistVm(
      author = "author",
      authorId = "id",
      title = "Title",
      videoCount = "13",
      thumbnailUrl = "",
      playlistId = "id",
      authorUrl = ""
    ),
    thumbnailHeight = rememberThumbnailHeight()
  )
}
