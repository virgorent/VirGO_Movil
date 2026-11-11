package mx.virgo.core.domain

import javax.inject.Inject
import kotlin.random.Random

private const val CODE_MIN = 100_000
private const val CODE_MAX = 1_000_000

class GenerateRecoveryCodeUseCase
    @Inject
    constructor() {
        operator fun invoke(): String = Random.nextInt(CODE_MIN, CODE_MAX).toString()
    }
