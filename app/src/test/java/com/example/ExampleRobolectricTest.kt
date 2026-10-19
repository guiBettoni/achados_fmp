package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ItemRepository
import com.example.data.SessionManager
import com.example.model.ItemModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testAppNameString() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Achados da FMP", appName)
  }

  @Test
  fun testItemRepositoryInitialSeedAndFiltering() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ItemRepository.getInstance(context)
    val items = repo.allItems
    assertNotNull(items)
    assertTrue("Repository should have seeded items for FMP campus", items.size >= 4)

    // Test filter by category "Eletrônicos"
    val elecItems = repo.searchAndFilter("", "Eletrônicos", "Todos", "ALL")
    assertTrue("Should find at least 1 electronics item", elecItems.isNotEmpty())

    // Test search by term "Fone"
    val searchResults = repo.searchAndFilter("Fone", "Todos", "Todos", "ALL")
    assertTrue("Should find Fone de ouvido", searchResults.any { it.title.contains("Fone") })
  }

  @Test
  fun testSessionManagerAndDarkMode() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val session = SessionManager(context)
    session.createLoginSession("Guilherme Bettoni", "guilherme@fmp.edu.br", "Estudante FMP")
    assertTrue(session.isLoggedIn)
    assertEquals("Guilherme Bettoni", session.userName)

    session.isDarkMode = true
    assertTrue(session.isDarkMode)
    session.isDarkMode = false
    org.junit.Assert.assertFalse(session.isDarkMode)
  }

  @Test
  fun testAddItemAndQueryMyItems() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ItemRepository.getInstance(context)

    val newItem = ItemModel(
      "test-item-99",
      "Calculadora HP 12C",
      "Eletrônicos",
      "LOST",
      "16/10/2026",
      "Laboratório 1",
      "Calculadora financeira esquecida.",
      "Com capa de couro preta.",
      "SEARCHING",
      "guilherme@fmp.edu.br",
      "Guilherme Bettoni",
      "Secretaria",
      0,
      System.currentTimeMillis()
    )
    repo.addItem(newItem)

    val fetched = repo.getItemById("test-item-99")
    assertNotNull(fetched)
    assertEquals("Calculadora HP 12C", fetched.title)

    val myLost = repo.getMyItems("guilherme@fmp.edu.br", "LOST")
    assertTrue("Should contain added item in my lost items", myLost.any { it.id == "test-item-99" })
  }
}
