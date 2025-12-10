package com.hellodev.app.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object AddStock : Screen("add_stock", "Add Stock")
    object Sales : Screen("sales", "Sales")
    object AddSale : Screen("add_sale", "Add Sale")
}
