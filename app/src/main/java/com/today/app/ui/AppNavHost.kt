package com.today.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.Title
import com.today.app.ui.catalog.CatalogScreen
import com.today.app.ui.circle.CircleDetailScreen
import com.today.app.ui.circle.CirclesScreen
import com.today.app.ui.detail.DetailScreen
import com.today.app.ui.diary.DiaryFormScreen
import com.today.app.ui.diary.QuickLogSheet
import com.today.app.ui.feed.FeedScreen
import com.today.app.ui.home.HomeScreen
import com.today.app.ui.lists.CustomListDetailScreen
import com.today.app.ui.lists.CustomListsScreen
import com.today.app.ui.profile.ProfileScreen
import com.today.app.ui.profile.PublicProfileScreen
import com.today.app.ui.recap.RecapScreen
import com.today.app.ui.schedule.ReleaseScheduleScreen
import com.today.app.ui.search.SearchScreen
import com.today.app.ui.settings.ImportExportScreen
import com.today.app.ui.social.MyOpinionsScreen
import com.today.app.ui.stats.StatsScreen
import com.today.app.ui.watchlist.WatchlistScreen

private data class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    Tab(ROUTE_HOME, "Hari ini", Icons.Filled.Today),
    Tab(ROUTE_CATALOG, "Jelajah", Icons.Filled.Explore),
    Tab(ROUTE_WATCHLIST, "Watchlist", Icons.Filled.Bookmark),
    Tab(ROUTE_LISTS, "Daftar", Icons.AutoMirrored.Filled.PlaylistPlay),
    Tab(ROUTE_PROFILE, "Profil", Icons.Filled.Person)
)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    var quickLogTitle by remember { mutableStateOf<Title?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(imageVector = tab.icon, contentDescription = tab.label)
                            },
                            label = { Text(text = tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    onLogNew = { navController.navigate(ROUTE_SEARCH) },
                    onQuickComplete = { navController.openDiaryEdit(it) },
                    onEntryClick = { navController.openDiaryEdit(it) },
                    onOpenTitle = { navController.openDetail(it) },
                    onOpenFeed = { navController.navigate(ROUTE_FEED) },
                    onOpenSchedule = { navController.navigate(ROUTE_RELEASE_SCHEDULE) }
                )
            }

            composable(ROUTE_CATALOG) {
                CatalogScreen(
                    onTitleClick = { navController.openDetail(it) },
                    onSearchClick = { navController.navigate(ROUTE_SEARCH) },
                    onQuickLog = { quickLogTitle = it }
                )
            }

            composable(ROUTE_WATCHLIST) {
                WatchlistScreen(
                    onTitleClick = { item ->
                        navController.openDiaryForm(
                            Title(
                                id = item.titleId,
                                mediaType = item.mediaType,
                                name = item.titleName,
                                originalName = "",
                                country = "",
                                language = "",
                                genre = "",
                                releaseDate = "",
                                rating = 0.0,
                                popularity = 0.0,
                                overview = "",
                                posterPath = item.posterPath,
                                backdropPath = null
                            )
                        )
                    }
                )
            }

            composable(ROUTE_LISTS) {
                CustomListsScreen(
                    onOpenList = { navController.openListDetail(it) }
                )
            }

            composable(ROUTE_STATS) {
                StatsScreen(onBack = { navController.popBackStack() })
            }

            composable(ROUTE_RECAP) {
                RecapScreen(onBack = { navController.popBackStack() })
            }

            composable(ROUTE_IMPORT_EXPORT) {
                ImportExportScreen(onBack = { navController.popBackStack() })
            }

            composable(ROUTE_RELEASE_SCHEDULE) {
                ReleaseScheduleScreen(
                    onBack = { navController.popBackStack() },
                    onTitleClick = { release ->
                        navController.openDetail(
                            Title(
                                id = release.titleId,
                                mediaType = release.mediaType,
                                name = release.titleName,
                                originalName = "",
                                country = "",
                                language = "",
                                genre = "",
                                releaseDate = release.date,
                                rating = release.rating,
                                popularity = 0.0,
                                overview = "",
                                posterPath = release.posterPath,
                                backdropPath = null
                            )
                        )
                    }
                )
            }

            composable(ROUTE_PROFILE) {
                ProfileScreen(
                    onOpenStats = { navController.navigate(ROUTE_STATS) },
                    onOpenRecap = { navController.navigate(ROUTE_RECAP) },
                    onOpenImportExport = { navController.navigate(ROUTE_IMPORT_EXPORT) },
                    onOpenMyOpinions = { navController.navigate(ROUTE_MY_OPINIONS) },
                    onOpenFeed = { navController.navigate(ROUTE_FEED) },
                    onOpenCircles = { navController.navigate(ROUTE_CIRCLES) }
                )
            }

            composable(ROUTE_FEED) {
                FeedScreen(
                    onBack = { navController.popBackStack() },
                    onTitleClick = { navController.openDetail(it) },
                    onAuthorClick = { uid, name -> navController.openPublicProfile(uid, name) }
                )
            }

            composable(ROUTE_CIRCLES) {
                CirclesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCircle = { navController.openCircleDetail(it) }
                )
            }

            composable(ROUTE_CIRCLE_DETAIL) { entry ->
                val args = navController.previousBackStackEntry?.savedStateHandle
                val circleId = args?.get<String>(KEY_CIRCLE_ID)
                    ?: entry.savedStateHandle.get<String>(KEY_CIRCLE_ID)
                if (circleId.isNullOrBlank()) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    CircleDetailScreen(
                        circleId = circleId,
                        onBack = { navController.popBackStack() },
                        onTitleClick = { navController.openDetail(it) },
                        onAuthorClick = { uid, name -> navController.openPublicProfile(uid, name) }
                    )
                }
            }

            composable(ROUTE_PUBLIC_PROFILE) { entry ->
                val args = navController.previousBackStackEntry?.savedStateHandle
                val userId = args?.get<String>(KEY_USER_ID) ?: entry.savedStateHandle.get<String>(KEY_USER_ID)
                val userName = args?.get<String>(KEY_USER_NAME) ?: entry.savedStateHandle.get<String>(KEY_USER_NAME)
                if (userId.isNullOrBlank()) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    PublicProfileScreen(
                        targetUid = userId,
                        fallbackName = userName ?: "",
                        onBack = { navController.popBackStack() },
                        onTitleClick = { navController.openDetail(it) }
                    )
                }
            }

            composable(ROUTE_MY_OPINIONS) {
                MyOpinionsScreen(
                    onBack = { navController.popBackStack() },
                    onTitleClick = { navController.openDetail(it) }
                )
            }

            composable(ROUTE_SEARCH) {
                SearchScreen(
                    onTitleClick = { navController.openDetail(it) },
                    onQuickLog = { quickLogTitle = it },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_DETAIL) { entry ->
                val incoming = navController.previousBackStackEntry?.savedStateHandle?.get<Title>(KEY_TITLE)
                    ?: entry.savedStateHandle.get<Title>(KEY_TITLE)
                var resolved by remember { mutableStateOf<Title?>(null) }
                if (incoming != null) resolved = incoming
                val title = resolved
                if (title == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    DetailScreen(
                        title = title,
                        onBack = { navController.popBackStack() },
                        onLog = { navController.openDiaryForm(title) },
                        onEditEntry = { navController.openDiaryEdit(it) },
                        onRewatch = { navController.openDiaryRewatch(it) },
                        onOpenTitle = { navController.openDetail(it) }
                    )
                }
            }

            composable(ROUTE_DIARY_FORM) { entry ->
                val args = navController.previousBackStackEntry?.savedStateHandle
                val title = args?.get<Title>(KEY_TITLE) ?: entry.savedStateHandle.get<Title>(KEY_TITLE)
                val diary = args?.get<DiaryEntry>(KEY_DIARY) ?: entry.savedStateHandle.get<DiaryEntry>(KEY_DIARY)
                val rewatchOf = args?.get<Long>(KEY_REWATCH_OF) ?: entry.savedStateHandle.get<Long>(KEY_REWATCH_OF)
                DiaryFormScreen(
                    title = title,
                    diary = diary,
                    rewatchOf = rewatchOf,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_LIST_DETAIL) { entry ->
                val listId = navController.previousBackStackEntry?.savedStateHandle?.get<Long>(KEY_LIST_ID)
                    ?: entry.savedStateHandle.get<Long>(KEY_LIST_ID) ?: 0L
                CustomListDetailScreen(
                    listId = listId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    quickLogTitle?.let { title ->
        QuickLogSheet(
            title = title,
            onDismiss = { quickLogTitle = null },
            onSaved = { quickLogTitle = null },
            onFullForm = {
                quickLogTitle = null
                navController.openDiaryForm(title)
            }
        )
    }
}
