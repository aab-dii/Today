package com.today.app.ui

import androidx.navigation.NavHostController
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.Title

const val ROUTE_HOME = "home"
const val ROUTE_CATALOG = "catalog"
const val ROUTE_WATCHLIST = "watchlist"
const val ROUTE_LISTS = "lists"
const val ROUTE_STATS = "stats"
const val ROUTE_RECAP = "recap"
const val ROUTE_IMPORT_EXPORT = "import_export"
const val ROUTE_RELEASE_SCHEDULE = "release_schedule"
const val ROUTE_PROFILE = "profile"
const val ROUTE_MY_OPINIONS = "my_opinions"
const val ROUTE_FEED = "feed"
const val ROUTE_CIRCLES = "circles"
const val ROUTE_CIRCLE_DETAIL = "circle_detail"
const val ROUTE_SEARCH = "search"
const val ROUTE_DETAIL = "detail"
const val ROUTE_DIARY_FORM = "diary_form"
const val ROUTE_LIST_DETAIL = "list_detail"
const val ROUTE_PUBLIC_PROFILE = "public_profile"

const val KEY_TITLE = "title"
const val KEY_DIARY = "diary"
const val KEY_REWATCH_OF = "rewatch_of"
const val KEY_LIST_ID = "list_id"
const val KEY_USER_ID = "user_id"
const val KEY_USER_NAME = "user_name"
const val KEY_CIRCLE_ID = "circle_id"

private fun NavHostController.setArg(key: String, value: Any?) {
    currentBackStackEntry?.savedStateHandle?.set(key, value)
}

fun NavHostController.openDetail(title: Title) {
    setArg(KEY_TITLE, title)
    navigate(ROUTE_DETAIL)
}

fun NavHostController.openDiaryForm(title: Title) {
    setArg(KEY_DIARY, null)
    setArg(KEY_REWATCH_OF, null)
    setArg(KEY_TITLE, title)
    navigate(ROUTE_DIARY_FORM)
}

fun NavHostController.openDiaryEdit(entry: DiaryEntry) {
    setArg(KEY_REWATCH_OF, null)
    setArg(KEY_DIARY, entry)
    navigate(ROUTE_DIARY_FORM)
}

fun NavHostController.openDiaryRewatch(entry: DiaryEntry) {
    setArg(KEY_DIARY, entry)
    setArg(KEY_REWATCH_OF, entry.id)
    navigate(ROUTE_DIARY_FORM)
}

fun NavHostController.openListDetail(listId: Long) {
    setArg(KEY_LIST_ID, listId)
    navigate(ROUTE_LIST_DETAIL)
}

fun NavHostController.openPublicProfile(userId: String, userName: String? = null) {
    setArg(KEY_USER_ID, userId)
    setArg(KEY_USER_NAME, userName)
    navigate(ROUTE_PUBLIC_PROFILE)
}

fun NavHostController.openCircleDetail(circleId: String) {
    setArg(KEY_CIRCLE_ID, circleId)
    navigate(ROUTE_CIRCLE_DETAIL)
}
