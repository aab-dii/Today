package com.today.app.core.data.source.local

import androidx.paging.PagingSource
import com.today.app.core.data.source.local.entity.TitleEntity
import com.today.app.core.data.source.local.room.TitleDao

class LocalDataSource(private val titleDao: TitleDao) {

    fun getPagedTitles(): PagingSource<Int, TitleEntity> = titleDao.getPagedTitles()
}
