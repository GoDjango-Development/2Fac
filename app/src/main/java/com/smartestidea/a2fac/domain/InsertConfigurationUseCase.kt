package com.smartestidea.a2fac.domain

import com.smartestidea.a2fac.data.ConfigurationRepository
import com.smartestidea.a2fac.data.model.Configuration
import javax.inject.Inject

class InsertConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(configuration:Configuration) = repository.insertConfiguration(configuration)
}