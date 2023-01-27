package com.smartestidea.a2fac.domain

import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.ConfigurationRepository
import javax.inject.Inject

class UpdateStateUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(isOn:Boolean,id:Int,type: TYPE) = repository.setState(isOn, id,type)
}