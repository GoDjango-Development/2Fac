package com.smartestidea.a2fac.domain

import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.ConfigurationRepository
import com.smartestidea.a2fac.data.model.Configuration
import javax.inject.Inject

class UpdatePosUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(id:String,pos:Int,type: TYPE) = repository.updatePos(id,pos,type)
}