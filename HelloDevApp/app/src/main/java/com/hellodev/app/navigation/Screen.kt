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
    object Calendar : Screen("calendar", "Calendar & Schedule")
    object AddEvent : Screen("add_event", "Add Event")
    object EditEvent : Screen("edit_event/{eventId}", "Edit Event") {
        fun createRoute(eventId: Int) = "edit_event/$eventId"
    }
    object Payments : Screen("payments", "Payments")
    object IncomingPayments : Screen("incoming_payments", "Incoming Payments")
    object OutgoingPayments : Screen("outgoing_payments", "Outgoing Payments")
    object AddPayment : Screen("add_payment/{type}", "Add Payment") {
        fun createRoute(type: String) = "add_payment/$type"
    }
    object PaymentDetails : Screen("payment_details/{paymentId}", "Payment Details") {
        fun createRoute(paymentId: Int) = "payment_details/$paymentId"
    }
    object Notes : Screen("notes", "Notes")
    object AddNote : Screen("add_note", "Add Note")
    object EditNote : Screen("edit_note/{noteId}", "Edit Note") {
        fun createRoute(noteId: Int) = "edit_note/$noteId"
    }
}
