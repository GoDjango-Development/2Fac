package com.smartestidea.a2fac.domain

import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.ConfigurationRepository
import javax.inject.Inject

class DeleteConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(id:String,type: TYPE) = repository.deleteConfiguration(id,type)
}