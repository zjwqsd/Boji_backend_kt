package com.boji.backend.repository

import com.boji.backend.model.PurchaseItem
import org.springframework.data.jpa.repository.JpaRepository

interface PurchaseItemRepository : JpaRepository<PurchaseItem, Long>
