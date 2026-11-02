package org.kimoteru.wallify.core.designsystem.ui.tab_row

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animatedAlpha
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.divider.DefaultHorizontalDivider

@Composable
fun DefaultTabRow(
    modifier: Modifier = Modifier,
    tabs: List<String>,
    selectedTabIndex: Int,
    onChangeTab: (Int) -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.clip(RoundedCornerShape(appCorner.medium)),
        containerColor = appColor.basic.primary.copy(alpha = appAlpha.smallSpecial),
        indicator = { tabPositions ->
            if (tabPositions.size > selectedTabIndex) {
                val transition = updateTransition(selectedTabIndex)

                val offset by transition.animateDp(
                    transitionSpec = { spring(stiffness = Spring.StiffnessMedium) }
                ) { page -> tabPositions[page].left }

                val width by transition.animateDp(
                    transitionSpec = { spring(stiffness = Spring.StiffnessMedium) }
                ) { page -> tabPositions[page].width }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(appPadding.smallExtra.half)
                        .zIndex(-1f)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = offset)
                            .width(width)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(appCorner.medium))
                            .background(appColor.basic.primary)
                    )
                }
            }
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                modifier = Modifier.fillMaxWidth(),
                selected = selectedTabIndex == index,
                onClick = { onChangeTab(index) },
                interactionSource = object : MutableInteractionSource {
                    override val interactions: Flow<Interaction> = emptyFlow()
                    override suspend fun emit(interaction: Interaction) {}
                    override fun tryEmit(interaction: Interaction) = true
                }
            ) {
                val targetColor =
                    if (selectedTabIndex == index) appColor.text.reversed
                    else appColor.basic.primary

                val animatedColor by animateColorAsState(targetValue = targetColor)

                Text(
                    text = tab,
                    modifier = Modifier.padding(vertical = appPadding.mediumSpecial),
                    style = appTypography.h6.medium.withColor(animatedColor)
                )
            }
        }
    }
}

@Composable
fun TextTabRow(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabClick: (Int) -> Unit
) {
    val appSize = AppTheme.size
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val density = LocalDensity.current

    Box(modifier = modifier) {
        DefaultHorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = appPadding.medium),
            horizontalArrangement = Arrangement.spacedBy(appPadding.small)
        ) {
            tabs.forEachIndexed { index, tabLabel ->
                val selected = selectedTabIndex == index

                var tabWidth by remember { mutableStateOf(Dp.Hairline) }

                Column(
                    modifier = Modifier.debouncedClickable(animateClick = false) {
                        onTabClick(index)
                    }
                ) {
                    Text(
                        modifier = Modifier
                            .onSizeChanged {
                                with(density) {
                                    val width = it.width.toDp()
                                    if (width > tabWidth) {
                                        tabWidth = width
                                    }
                                }
                            }
                            .padding(appPadding.small),
                        text = tabLabel,
                        maxLines = 2,
                        style = appTypography.h5.medium,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) appColor.basic.primary else appColor.text.subtitle
                    )

                    DefaultHorizontalDivider(
                        modifier = Modifier
                            .width(tabWidth)
                            .animatedAlpha(selected),
                        color = appColor.basic.primary,
                        thickness = appSize.divider.thickness + appSize.divider.thickness
                    )
                }
            }
        }
    }
}