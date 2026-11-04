package org.kimoteru.wallify.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.toCompactString
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberCanUseExpandedLayout
import org.kimoteru.wallify.core.designsystem.ui.avatar.UserAvatar
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurSourceZIndex
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassCard
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassStat
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberHideOnScrollConnection
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoLayoutCard
import org.kimoteru.wallify.core.model.settings.PhotoLayout
import org.kimoteru.wallify.core.model.settings.ScreenLayout
import org.kimoteru.wallify.core.model.settings.SupportedLanguages
import org.kimoteru.wallify.core.model.settings.ThemeMode
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.auth_sign_out
import org.kimoteru.wallify.core.resources.generated.resources.nav_settings
import org.kimoteru.wallify.core.resources.generated.resources.settings_about
import org.kimoteru.wallify.core.resources.generated.resources.settings_about_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_account
import org.kimoteru.wallify.core.resources.generated.resources.settings_appearance
import org.kimoteru.wallify.core.resources.generated.resources.settings_columns
import org.kimoteru.wallify.core.resources.generated.resources.settings_columns_auto
import org.kimoteru.wallify.core.resources.generated.resources.settings_language
import org.kimoteru.wallify.core.resources.generated.resources.settings_language_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_language_system
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_grid
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_list
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_masonry
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_auto
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_compact
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_expanded
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_medium
import org.kimoteru.wallify.core.resources.generated.resources.settings_service_title
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_downloads
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_views
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_day
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_night
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_system

object SettingsContract {
    @Immutable
    data class UiState(
        val user: User? = null,
        val themeMode: ThemeMode = ThemeMode.System,
        val photoLayout: PhotoLayout = PhotoLayout.Masonry,
        val photoColumns: Int? = null,
        val screenLayout: ScreenLayout = ScreenLayout.Auto,

        /** Language of the app and of photo search; `null` follows the device. */
        val languageTag: String? = null,
        val totalStats: TotalStats? = null,
        val monthStats: MonthStats? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTheme(val themeMode: ThemeMode) : UiEvent
        data class SelectPhotoLayout(val photoLayout: PhotoLayout) : UiEvent

        /** `null` is the automatic number of columns. */
        data class SelectPhotoColumns(val columns: Int?) : UiEvent

        data class SelectScreenLayout(val screenLayout: ScreenLayout) : UiEvent

        /** `null` follows the device language. */
        data class SelectLanguage(val languageTag: String?) : UiEvent

        data object SignOut : UiEvent
    }
}

@Preview
@Composable
fun PreviewSettingsUi() {
    AppTheme {
        SettingsUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : SettingsComponent {
                @Composable
                override fun getUiState(): SettingsContract.UiState = SettingsContract.UiState()
            }
        )
    }
}

@Composable
fun SettingsUi(
    modifier: Modifier = Modifier,
    component: SettingsComponent
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val screenPadding = rememberScreenPadding()

    val themeModes = ThemeMode.entries
    val photoLayouts = PhotoLayout.entries
    val columnOptions = listOf(1, 2, 3, 4)
    val screenLayouts = ScreenLayout.entries
    val canUseExpandedLayout = rememberCanUseExpandedLayout()
    val languages = SupportedLanguages

    Column(
        modifier = modifier
            .fillMaxSize()
            .blurSource(BlurSourceZIndex.Screen)
            .nestedScroll(rememberHideOnScrollConnection(LocalNavigationVisibility.current))
            .verticalScroll(rememberScrollState())
            .padding(screenPadding.full),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = appSize.panel.contentMaxWidth),
            verticalArrangement = Arrangement.spacedBy(appPadding.medium)
        ) {
            Text(
                text = stringResource(Res.string.nav_settings),
                style = appTypography.h2.bold,
                color = appColor.text.title
            )

            uiState.user?.let { user ->
                SettingsSection(title = stringResource(Res.string.settings_account)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            imageUrl = user.profileImage?.medium,
                            name = user.name,
                            size = appSize.avatar.large
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = appTypography.h5.semiBold,
                                color = appColor.text.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = user.email ?: "@${user.username}",
                                style = appTypography.h7.medium,
                                color = appColor.text.subtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    GlassButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.auth_sign_out),
                        icon = AppIcons.Logout,
                        onClick = { obtainEvent(SettingsContract.UiEvent.SignOut) }
                    )
                }
            }

            SettingsSection(title = stringResource(Res.string.settings_appearance)) {
                GlassSegmentedControl(
                    modifier = Modifier.fillMaxWidth(),
                    options = listOf(
                        stringResource(Res.string.settings_theme_system),
                        stringResource(Res.string.settings_theme_night),
                        stringResource(Res.string.settings_theme_day)
                    ),
                    selectedIndex = themeModes.indexOf(uiState.themeMode),
                    onSelect = { index -> obtainEvent(SettingsContract.UiEvent.SelectTheme(themeModes[index])) }
                )
            }

            SettingsSection(title = stringResource(Res.string.settings_language)) {
                GlassSegmentedControl(
                    modifier = Modifier.fillMaxWidth(),
                    options = listOf(stringResource(Res.string.settings_language_system)) +
                        languages.map { language -> language.name },
                    selectedIndex = languages.indexOfFirst { language -> language.tag == uiState.languageTag } + 1,
                    onSelect = { index ->
                        obtainEvent(SettingsContract.UiEvent.SelectLanguage(languages.getOrNull(index - 1)?.tag))
                    }
                )

                Text(
                    text = stringResource(Res.string.settings_language_description),
                    style = appTypography.h7.regular,
                    color = appColor.text.subtitle
                )
            }

            // Phones never reach the Expanded layout, so there is nothing to choose between there.
            if (canUseExpandedLayout) {
                SettingsSection(title = stringResource(Res.string.settings_screen_layout)) {
                    GlassSegmentedControl(
                        modifier = Modifier.fillMaxWidth(),
                        options = screenLayouts.map { screenLayout -> stringResource(screenLayout.labelRes()) },
                        selectedIndex = screenLayouts.indexOf(uiState.screenLayout),
                        onSelect = { index ->
                            obtainEvent(SettingsContract.UiEvent.SelectScreenLayout(screenLayouts[index]))
                        }
                    )

                    Text(
                        text = stringResource(Res.string.settings_screen_layout_description),
                        style = appTypography.h7.regular,
                        color = appColor.text.subtitle
                    )
                }
            }

            SettingsSection(title = stringResource(Res.string.settings_layout)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(appPadding.small)
                ) {
                    photoLayouts.forEach { photoLayout ->
                        PhotoLayoutCard(
                            modifier = Modifier.weight(1f),
                            mode = photoLayout.toGridMode(),
                            label = stringResource(photoLayout.labelRes()),
                            selected = photoLayout == uiState.photoLayout,
                            onClick = { obtainEvent(SettingsContract.UiEvent.SelectPhotoLayout(photoLayout)) }
                        )
                    }
                }

                // The list always has its own number of columns, so the choice matters for the other two.
                if (uiState.photoLayout != PhotoLayout.List) {
                    Text(
                        text = stringResource(Res.string.settings_columns).uppercase(),
                        style = appTypography.caps.semiBold,
                        color = appColor.text.subtitle
                    )

                    GlassSegmentedControl(
                        modifier = Modifier.fillMaxWidth(),
                        options = listOf(stringResource(Res.string.settings_columns_auto)) +
                            columnOptions.map { columns -> columns.toString() },
                        selectedIndex = columnOptions.indexOf(uiState.photoColumns) + 1,
                        onSelect = { index ->
                            obtainEvent(SettingsContract.UiEvent.SelectPhotoColumns(columnOptions.getOrNull(index - 1)))
                        }
                    )
                }

                Text(
                    text = stringResource(Res.string.settings_layout_description),
                    style = appTypography.h7.regular,
                    color = appColor.text.subtitle
                )
            }

            if (uiState.totalStats != null || uiState.monthStats != null) {
                SettingsSection(title = stringResource(Res.string.settings_service_title)) {
                    uiState.totalStats?.let { total ->
                        StatsRow(
                            firstValue = total.photos.toCompactString(),
                            firstLabel = stringResource(Res.string.settings_stat_photos),
                            secondValue = total.photographers.toCompactString(),
                            secondLabel = stringResource(Res.string.settings_stat_photographers)
                        )

                        StatsRow(
                            firstValue = total.downloads.toCompactString(),
                            firstLabel = stringResource(Res.string.settings_stat_downloads),
                            secondValue = total.views.toCompactString(),
                            secondLabel = stringResource(Res.string.settings_stat_views)
                        )
                    }

                    uiState.monthStats?.let { month ->
                        StatsRow(
                            firstValue = month.newPhotos.toCompactString(),
                            firstLabel = stringResource(Res.string.settings_stat_new_photos),
                            secondValue = month.newPhotographers.toCompactString(),
                            secondLabel = stringResource(Res.string.settings_stat_new_photographers)
                        )
                    }
                }
            }

            SettingsSection(title = stringResource(Res.string.settings_about)) {
                Text(
                    text = stringResource(Res.string.settings_about_description),
                    style = appTypography.h6.regular,
                    color = appColor.text.common
                )
            }
        }
    }
}

private fun PhotoLayout.toGridMode(): PhotoGridMode = when (this) {
    PhotoLayout.Masonry -> PhotoGridMode.Masonry
    PhotoLayout.Grid -> PhotoGridMode.Grid
    PhotoLayout.List -> PhotoGridMode.List
}

private fun ScreenLayout.labelRes(): StringResource = when (this) {
    ScreenLayout.Auto -> Res.string.settings_screen_layout_auto
    ScreenLayout.Compact -> Res.string.settings_screen_layout_compact
    ScreenLayout.Medium -> Res.string.settings_screen_layout_medium
    ScreenLayout.Expanded -> Res.string.settings_screen_layout_expanded
}

private fun PhotoLayout.labelRes(): StringResource = when (this) {
    PhotoLayout.Masonry -> Res.string.settings_layout_masonry
    PhotoLayout.Grid -> Res.string.settings_layout_grid
    PhotoLayout.List -> Res.string.settings_layout_list
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = appTypography.caps.semiBold,
            color = appColor.text.subtitle
        )

        Column(
            modifier = Modifier.padding(top = appPadding.medium),
            verticalArrangement = Arrangement.spacedBy(appPadding.medium)
        ) {
            content()
        }
    }
}

@Composable
private fun StatsRow(
    firstValue: String,
    firstLabel: String,
    secondValue: String,
    secondLabel: String
) {
    val appPadding = AppTheme.padding

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small)
    ) {
        GlassStat(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = firstValue,
            label = firstLabel
        )

        GlassStat(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = secondValue,
            label = secondLabel
        )
    }
}