package com.serranoie.app.media.sorter.domain.settings

import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.appSettings
}
