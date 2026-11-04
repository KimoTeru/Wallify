package org.kimoteru.wallify.feature.settings.presentation.ui

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoLayoutCard
import org.kimoteru.wallify.core.model.settings.AppLanguage
import org.kimoteru.wallify.core.model.settings.SupportedLanguages
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.settings.PhotoColumnsUi
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ScreenLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.feature.settings.presentation.components.SettingsComponent
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
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_service_title
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_downloads
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_views

object SettingsContract {
    @Immutable
    data class UiState(
        val user: User? = null,
        val themeModes: List<ThemeModeUi> = ThemeModeUi.entries,
        val themeMode: ThemeModeUi = ThemeModeUi.System,
        val photoLayouts: List<PhotoLayoutUi> = PhotoLayoutUi.entries,
        val photoLayout: PhotoLayoutUi = PhotoLayoutUi.Masonry,
        val columnOptions: List<PhotoColumnsUi> = PhotoColumnsUi.entries,
        val photoColumns: PhotoColumnsUi = PhotoColumnsUi.Auto,
        val screenLayouts: List<ScreenLayoutUi> = ScreenLayoutUi.entries,
        val screenLayout: ScreenLayoutUi = ScreenLayoutUi.Auto,
        val languages: List<AppLanguage> = SupportedLanguages,

        val languageTag: String = SupportedLanguages.first().tag,
        val totalStats: TotalStats? = null,
        val monthStats: MonthStats? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTheme(val themeMode: ThemeModeUi) : UiEvent
        data class SelectPhotoLayout(val photoLayout: PhotoLayoutUi) : UiEvent

        /** `null` is the automatic number of columns. */
        data class SelectPhotoColumns(val columns: PhotoColumnsUi) : UiEvent

        data class SelectScreenLayout(val screenLayout: ScreenLayoutUi) : UiEvent

        data class SelectLanguage(val languageTag: String) : UiEvent

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
    val canUseExpandedLayout = rememberCanUseExpandedLayout()
    val scroll = rememberScrollState()

    FloatingBarScreen(
        modifier = modifier,
        scrolledPx = scroll.value,
        bar = {
            DefaultTopBar(
                title = stringResource(Res.string.nav_settings),
                titleStyle = appTypography.h2.bold,
                paddingValues = screenPadding.header
            )
        }
    ) { topPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blurSource(BlurSourceZIndex.Screen)
                .verticalScroll(scroll)
                .padding(
                    start = screenPadding.start,
                    end = screenPadding.end,
                    bottom = screenPadding.bottom
                )
                .padding(top = topPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Column(
            modifier = Modifier.widthIn(max = appSize.panel.contentMaxWidth),
            verticalArrangement = Arrangement.spacedBy(appPadding.medium)
        ) {
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
                    options = uiState.themeModes.map { mode -> stringResource(mode.label) },
                    selectedIndex = uiState.themeModes.indexOf(uiState.themeMode),
                    onSelect = { index ->
                        obtainEvent(SettingsContract.UiEvent.SelectTheme(uiState.themeModes[index]))
                    }
                )
            }

            SettingsSection(title = stringResource(Res.string.settings_language)) {
                GlassSegmentedControl(
                    modifier = Modifier.fillMaxWidth(),
                    options = uiState.languages.map { language -> language.name },
                    selectedIndex = uiState.languages.indexOfFirst { language ->
                        language.tag == uiState.languageTag
                    },
                    onSelect = { index ->
                        obtainEvent(SettingsContract.UiEvent.SelectLanguage(uiState.languages[index].tag))
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
                        options = uiState.screenLayouts.map { screenLayout -> stringResource(screenLayout.label) },
                        selectedIndex = uiState.screenLayouts.indexOf(uiState.screenLayout),
                        onSelect = { index ->
                            obtainEvent(SettingsContract.UiEvent.SelectScreenLayout(uiState.screenLayouts[index]))
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
                    uiState.photoLayouts.forEach { photoLayout ->
                        PhotoLayoutCard(
                            modifier = Modifier.weight(1f),
                            mode = photoLayout.gridMode,
                            label = stringResource(photoLayout.label),
                            selected = photoLayout == uiState.photoLayout,
                            onClick = { obtainEvent(SettingsContract.UiEvent.SelectPhotoLayout(photoLayout)) }
                        )
                    }
                }

                // The list always has its own number of columns, so the choice matters for the other two.
                if (uiState.photoLayout != PhotoLayoutUi.List) {
                    Text(
                        text = stringResource(Res.string.settings_columns).uppercase(),
                        style = appTypography.caps.semiBold,
                        color = appColor.text.subtitle
                    )

                    GlassSegmentedControl(
                        modifier = Modifier.fillMaxWidth(),
                        options = uiState.columnOptions.map { columns ->
                            columns.count?.toString() ?: stringResource(Res.string.settings_columns_auto)
                        },
                        selectedIndex = uiState.columnOptions.indexOf(uiState.photoColumns),
                        onSelect = { index ->
                            obtainEvent(SettingsContract.UiEvent.SelectPhotoColumns(uiState.columnOptions[index]))
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