package com.boji.backend.repository

import com.boji.backend.model.Household
import org.springframework.data.jpa.repository.JpaRepository

interface HouseholdRepository : JpaRepository<Household, Long> {
    fun findByCode(code: String): Household?
    fun existsByName(name: String): Boolean
    fun existsByCode(code: String): Boolean
}