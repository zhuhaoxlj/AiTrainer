package com.lindum.mlkit.vision.utils

import com.lindum.mlkit.vision.entity.WrongPoseModel

/**
 * Temporary data holder to store objects that can't be serialized
 * and passed through Intents directly
 */
object TemporaryDataHolder {
    
    // Store the current wrong pose models list
    private var wrongPoseModels: List<WrongPoseModel>? = null
    
    /**
     * Set the list of wrong pose models
     */
    fun setWrongPoseModels(models: List<WrongPoseModel>) {
        wrongPoseModels = models
    }
    
    /**
     * Get the list of wrong pose models and clear the reference
     */
    fun getAndClearWrongPoseModels(): List<WrongPoseModel> {
        val result = wrongPoseModels ?: emptyList()
        wrongPoseModels = null
        return result
    }
} 