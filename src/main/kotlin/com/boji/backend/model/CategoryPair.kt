package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "category_pairs", uniqueConstraints = [UniqueConstraint(columnNames = ["category1", "category2"])])
data class CategoryPair(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val category1: String = "",

    @Column(nullable = false)
    val category2: String = ""
)
