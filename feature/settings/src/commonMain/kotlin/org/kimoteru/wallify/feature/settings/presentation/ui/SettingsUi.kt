package org.kimoteru.wallify.feature.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberIsShortWindow
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoLayoutCard
import org.kimoteru.wallify.core.model.settings.AppLanguage
import org.kimoteru.wallify.core.model.settings.SupportedLanguages
import org.kimoteru.wallify.core.model.stats.EndpointStat
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.RateLimit
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.photo.AvatarUi
import org.kimoteru.wallify.core.presentation.photo.UserAvatar
import org.kimoteru.wallify.core.presentation.settings.ImageQualityUi
import org.kimoteru.wallify.core.presentation.settings.PhotoColumnsUi
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ScreenLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.feature.settings.presentation.components.SettingsComponent
import org.kimoteru.wallify.feature.settings.presentation.ui.utils.EndpointShare
import org.kimoteru.wallify.feature.settings.presentation.ui.utils.ImageQualityChoice
import org.kimoteru.wallify.feature.settings.presentation.ui.utils.RequestDays
import org.kimoteru.wallify.feature.settings.presentation.ui.utils.SettingsSection
import org.kimoteru.wallify.feature.settings.presentation.ui.utils.StatsRow
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.auth_sign_out
import org.kimoteru.wallify.core.resources.generated.resources.nav_settings
import org.kimoteru.wallify.core.resources.generated.resources.settings_about
import org.kimoteru.wallify.core.resources.generated.resources.settings_about_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_account
import org.kimoteru.wallify.core.resources.generated.resources.settings_appearance
import org.kimoteru.wallify.core.resources.generated.resources.settings_columns
import org.kimoteru.wallify.core.resources.generated.resources.settings_columns_auto
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_detail
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_download
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_list
import org.kimoteru.wallify.core.resources.generated.resources.settings_language
import org.kimoteru.wallify.core.resources.generated.resources.settings_language_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_empty
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_remaining
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_remaining_unknown
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_remaining_value
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_reset
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_title
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_top
import org.kimoteru.wallify.core.resources.generated.resources.settings_requests_total
import org.kimoteru.wallify.core.resources.generated.resources.settings_screen_layout_description
import org.kimoteru.wallify.core.resources.generated.resources.settings_service_title
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_downloads
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_new_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photographers
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_photos
import org.kimoteru.wallify.core.resources.generated.resources.settings_stat_views

object SettingsContract {
    /** The account block: the name, the line under it (the email, or the handle without one) and the avatar. */
    @Immutable
    data class Account(
        val name: String,
        val subtitle: String,
        val avatar: AvatarUi,
    )

    @Immutable
    data class UiState(
        /** The signed-in user, ready to draw; `null` while there is none. */
        val account: Account? = null,
        val themeModes: List<ThemeModeUi> = ThemeModeUi.entries,
        val themeMode: ThemeModeUi = ThemeModeUi.System,
        val photoLayouts: List<PhotoLayoutUi> = PhotoLayoutUi.entries,
        val photoLayout: PhotoLayoutUi = PhotoLayoutUi.Masonry,
        val columnOptions: List<PhotoColumnsUi> = PhotoColumnsUi.entries,
        val photoColumns: PhotoColumnsUi = PhotoColumnsUi.Auto,
        /** The list always has its own number of columns, so the choice is shown for the other layouts only. */
        val isPhotoColumnsAvailable: Boolean = true,
        val screenLayouts: List<ScreenLayoutUi> = ScreenLayoutUi.entries,
        val screenLayout: ScreenLayoutUi = ScreenLayoutUi.Auto,
        /** The original is only for downloads: on screen it is a huge file for no visible gain. */
        val viewImageQualities: List<ImageQualityUi> = ImageQualityUi.entries - ImageQualityUi.Original,
        val downloadImageQualities: List<ImageQualityUi> = ImageQualityUi.entries,
        val listImageQuality: ImageQualityUi = ImageQualityUi.Medium,
        val detailImageQuality: ImageQualityUi = ImageQualityUi.High,
        val downloadImageQuality: ImageQualityUi = ImageQualityUi.Original,
        val languages: List<AppLanguage> = SupportedLanguages,

        val languageTag: String = SupportedLanguages.first().tag,
        val totalStats: TotalStats? = null,
        val monthStats: MonthStats? = null,
        val requestTotal: Long = 0L,
        /** There is something to reset: at least one request is counted. */
        val canResetRequestStats: Boolean = false,
        /** The most requested endpoints, the first is requested most. */
        val topEndpoints: List<EndpointStat> = emptyList(),
        val rateLimit: RateLimit? = null,
        /** Requests per local day for the last days, the oldest first and today last; days without requests are 0. */
        val requestDays: List<Long> = emptyList(),
        val requestDaysTotal: Long = 0L,
        val requestDaysMax: Long = 0L
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTheme(val themeMode: ThemeModeUi) : UiEvent
        data class SelectPhotoLayout(val photoLayout: PhotoLayoutUi) : UiEvent

        /** `null` is the automatic number of columns. */
        data class SelectPhotoColumns(val columns: PhotoColumnsUi) : UiEvent

        data class SelectScreenLayout(val screenLayout: ScreenLayoutUi) : UiEvent

        data class SelectListImageQuality(val quality: ImageQualityUi) : UiEvent

        data class SelectDetailImageQuality(val quality: ImageQualityUi) : UiEvent

        data class SelectDownloadImageQuality(val quality: ImageQualityUi) : UiEvent

        data class SelectLanguage(val languageTag: String) : UiEvent

        data object SignOut : UiEvent

        data object ResetRequestStats : UiEvent
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
    val isShortWindow = rememberIsShortWindow()
    val scroll = rememberScrollState()

    FloatingBarScreen(
        modifier = modifier,
        scrolledPx = { scroll.value },
        bar = {
            DefaultTopBar(
                title = stringResource(Res.string.nav_settings),
                titleStyle = if (isShortWindow) appTypography.h5.semiBold else appTypography.h2.bold,
                paddingValues = screenPadding.header
            )
        }
    ) { topPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            uiState.account?.let { account ->
                SettingsSection(title = stringResource(Res.string.settings_account)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatar = account.avatar,
                            size = appSize.avatar.large
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = appTypography.h5.semiBold,
                                color = appColor.text.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = account.subtitle,
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

                if (uiState.isPhotoColumnsAvailable) {
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

            SettingsSection(title = stringResource(Res.string.settings_image_quality)) {
                ImageQualityChoice(
                    title = stringResource(Res.string.settings_image_quality_list),
                    qualities = uiState.viewImageQualities,
                    selected = uiState.listImageQuality,
                    onSelect = { obtainEvent(SettingsContract.UiEvent.SelectListImageQuality(it)) }
                )

                ImageQualityChoice(
                    title = stringResource(Res.string.settings_image_quality_detail),
                    qualities = uiState.viewImageQualities,
                    selected = uiState.detailImageQuality,
                    onSelect = { obtainEvent(SettingsContract.UiEvent.SelectDetailImageQuality(it)) }
                )

                ImageQualityChoice(
                    title = stringResource(Res.string.settings_image_quality_download),
                    qualities = uiState.downloadImageQualities,
                    selected = uiState.downloadImageQuality,
                    onSelect = { obtainEvent(SettingsContract.UiEvent.SelectDownloadImageQuality(it)) }
                )

                Text(
                    text = stringResource(Res.string.settings_image_quality_description),
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

            SettingsSection(title = stringResource(Res.string.settings_requests_title)) {
                StatsRow(
                    firstValue = uiState.requestTotal.toCompactString(),
                    firstLabel = stringResource(Res.string.settings_requests_total),
                    secondValue = uiState.rateLimit?.let { rateLimit ->
                        stringResource(
                            Res.string.settings_requests_remaining_value,
                            rateLimit.remaining.toLong().toCompactString(),
                            rateLimit.limit.toLong().toCompactString()
                        )
                    } ?: stringResource(Res.string.settings_requests_remaining_unknown),
                    secondLabel = stringResource(Res.string.settings_requests_remaining)
                )

                RequestDays(
                    days = uiState.requestDays,
                    total = uiState.requestDaysTotal,
                    max = uiState.requestDaysMax
                )

                Text(
                    text = stringResource(Res.string.settings_requests_top).uppercase(),
                    style = appTypography.caps.semiBold,
                    color = appColor.text.subtitle
                )

                if (uiState.topEndpoints.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.settings_requests_empty),
                        style = appTypography.h6.regular,
                        color = appColor.text.common
                    )
                } else {
                    uiState.topEndpoints.forEach { endpoint ->
                        EndpointShare(
                            endpoint = endpoint.endpoint,
                            count = endpoint.count,
                            total = uiState.requestTotal
                        )
                    }
                }

                GlassButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.settings_requests_reset),
                    icon = AppIcons.Refresh,
                    enabled = uiState.canResetRequestStats,
                    onClick = { obtainEvent(SettingsContract.UiEvent.ResetRequestStats) }
                )

                Text(
                    text = stringResource(Res.string.settings_requests_description),
                    style = appTypography.h7.regular,
                    color = appColor.text.subtitle
                )
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