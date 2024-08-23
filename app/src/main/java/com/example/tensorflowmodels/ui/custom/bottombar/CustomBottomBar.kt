package com.example.tensorflowmodels.ui.custom.bottombar


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.tensorflowmodels.R

@Composable
fun CustomBottomBar(
    tabs: List<BottomBarItem>,
    currentDestination: NavDestination?,
    alwaysShowLabel: Boolean = false,
    bottomBarState: Boolean = true,
    onNavigateToDestination: (Screen) -> Unit = {},
) {
    if (bottomBarState) {
        AnimatedVisibility(
            visible = bottomBarState,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            BottomAppBar {
                tabs.forEach { item ->
                    val selected = currentDestination.isDestinationInHierarchy(item)
                    NavigationBarItem(
                        selected = selected,
                        label = {
                            Text(
                                text = stringResource(id = item.label),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        alwaysShowLabel = alwaysShowLabel,
                        icon = { IconBarItem(item) },
                        onClick = { onNavigateToDestination(item.route) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun IconBarItem(item: BottomBarItem) {
    Icon(
        imageVector = item.icon,
        contentDescription = stringResource(id = item.label),
        modifier = Modifier
            .size(20.dp),
        tint = MaterialTheme.colorScheme.secondary
    )
}

internal fun NavDestination?.isDestinationInHierarchy(tab: BottomBarItem) =
    this?.hierarchy?.any {
        it.route?.contains(tab.route::class.java.canonicalName.orEmpty(), true) ?: false
    } ?: false

@Preview
@Composable
internal fun NavigationPreview() {
    val items: List<Triple<Screen, Boolean, Int>> = listOf(
        Triple(Screen.Tensorflow, false, R.string.screen_flower),
        Triple(Screen.Tensorflow, false, R.string.screen_flower),
        Triple(Screen.Tensorflow, false, R.string.screen_flower),
    )

    val tabs = items.map {
        BottomBarItem(
            route = it.first,
            label = it.third,
            icon = Icons.Default.Face,
            enabled = it.second
        )
    }
    CustomBottomBar(
        bottomBarState = true,
        tabs = tabs,
        currentDestination = NavDestination("Home").apply { route = "Home" },
        alwaysShowLabel = true,
    ) {}
}