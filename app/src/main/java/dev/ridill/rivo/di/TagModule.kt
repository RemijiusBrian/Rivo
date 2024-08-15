package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.tags.data.local.TagsDao
import dev.ridill.rivo.tags.data.repository.TagsRepositoryImpl
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import dev.ridill.rivo.tags.presentation.addEditTag.AddEditTagViewModel

@Module
@InstallIn(ViewModelComponent::class)
object TagModule {

    @Provides
    fun provideTagsDao(db: RivoDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideTagsRepository(dao: TagsDao): TagsRepository = TagsRepositoryImpl(dao)

    @Provides
    fun provideAddEditTagEventBus(): EventBus<AddEditTagViewModel.AddEditTagEvent> = EventBus()
}