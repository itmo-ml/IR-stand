package ru.itmo.stand.util

import kotlin.system.measureTimeMillis

// TODO: use measureTime when it's stable
inline fun measureTimeSeconds(block: () -> Unit): Double = measureTimeMillis(block) / 1000.0
