package com.today.app.social.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.today.app.social.data.repository.AuthRepositoryImpl
import com.today.app.social.data.repository.CircleRepositoryImpl
import com.today.app.social.data.repository.SocialRepositoryImpl
import com.today.app.social.data.source.AuthDataSource
import com.today.app.social.data.source.CircleDataSource
import com.today.app.social.data.source.SocialDataSource
import com.today.app.social.data.source.firebase.FirebaseAuthDataSource
import com.today.app.social.data.source.firebase.FirestoreCircleDataSource
import com.today.app.social.data.source.firebase.FirestoreSocialDataSource
import com.today.app.social.domain.repository.AuthRepository
import com.today.app.social.domain.repository.CircleRepository
import com.today.app.social.domain.repository.SocialRepository
import org.koin.dsl.module

val socialModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single<AuthDataSource> { FirebaseAuthDataSource(get()) }
    single<SocialDataSource> { FirestoreSocialDataSource(get(), get()) }
    single<CircleDataSource> { FirestoreCircleDataSource(get(), get()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SocialRepository> { SocialRepositoryImpl(get()) }
    single<CircleRepository> { CircleRepositoryImpl(get()) }
}
