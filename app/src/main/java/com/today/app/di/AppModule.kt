package com.today.app.di

import com.today.app.core.domain.usecase.CatalogInteractor
import com.today.app.core.domain.usecase.CatalogUseCase
import com.today.app.core.domain.usecase.DiaryInteractor
import com.today.app.core.domain.usecase.DiaryUseCase
import com.today.app.core.domain.usecase.ReleaseScheduleInteractor
import com.today.app.core.domain.usecase.ReleaseScheduleUseCase
import com.today.app.ui.auth.AuthViewModel
import com.today.app.ui.catalog.CatalogViewModel
import com.today.app.ui.circle.CircleDetailViewModel
import com.today.app.ui.circle.CirclesViewModel
import com.today.app.ui.detail.DetailViewModel
import com.today.app.ui.diary.DiaryFormViewModel
import com.today.app.ui.diary.QuickLogViewModel
import com.today.app.ui.feed.FeedViewModel
import com.today.app.ui.home.HomeViewModel
import com.today.app.ui.lists.AddToListViewModel
import com.today.app.ui.lists.CustomListDetailViewModel
import com.today.app.ui.lists.CustomListsViewModel
import com.today.app.ui.profile.ProfileViewModel
import com.today.app.ui.profile.PublicProfileViewModel
import com.today.app.ui.recap.RecapViewModel
import com.today.app.ui.schedule.ReleaseScheduleViewModel
import com.today.app.ui.search.SearchViewModel
import com.today.app.ui.settings.ImportExportViewModel
import com.today.app.ui.social.CommentsViewModel
import com.today.app.ui.social.OpinionViewModel
import com.today.app.ui.stats.StatsViewModel
import com.today.app.ui.watchlist.WatchlistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<CatalogUseCase> { CatalogInteractor(get()) }
    factory<DiaryUseCase> { DiaryInteractor(get()) }
    factory<ReleaseScheduleUseCase> { ReleaseScheduleInteractor(get(), get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { CatalogViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { DetailViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { DiaryFormViewModel(get(), get()) }
    viewModel { QuickLogViewModel(get(), get()) }
    viewModel { WatchlistViewModel(get()) }
    viewModel { CustomListsViewModel(get()) }
    viewModel { CustomListDetailViewModel(get()) }
    viewModel { AddToListViewModel(get()) }
    viewModel { StatsViewModel(get()) }
    viewModel { RecapViewModel(get()) }
    viewModel { ImportExportViewModel(get()) }
    viewModel { ReleaseScheduleViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { PublicProfileViewModel(get(), get()) }
    viewModel { OpinionViewModel(get(), get()) }
    viewModel { FeedViewModel(get(), get()) }
    viewModel { CommentsViewModel(get(), get()) }
    viewModel { CirclesViewModel(get()) }
    viewModel { CircleDetailViewModel(get(), get(), get()) }
}
