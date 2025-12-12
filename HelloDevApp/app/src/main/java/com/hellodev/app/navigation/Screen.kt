package com.hellodev.app.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object SalesRecords : Screen("sales_records", "Sales Records")
    object AddSale : Screen("add_sale", "Add Sale")
    object MakeBill : Screen("make_bill", "Make Bill")
    object PendingPayments : Screen("pending_payments", "Pending Payments")
    object AddStock : Screen("add_stock", "Add Stock")
    object StockCategories : Screen("stock_categories", "Stock Categories")
    object AIAssistant : Screen("ai_assistant", "AI Assistant")
    object History : Screen("history", "History")
    object StockDrafts : Screen("stock_drafts", "Stock Drafts")
    object CreateDraft : Screen("create_draft", "Create Draft")
    object EditDraft : Screen("edit_draft/{draftId}", "Edit Draft") {
        fun createRoute(draftId: Int) = "edit_draft/$draftId"
    }
    object DraftSummary : Screen("draft_summary/{draftId}", "Draft Summary") {
        fun createRoute(draftId: Int) = "draft_summary/$draftId"
    }
}
