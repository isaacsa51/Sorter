package com.serranoie.app.media.sorter.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboard : Screen("onboard")
    
    data object Sorter : Screen("sorter")
    
    data object Review : Screen("review")
    
    data object Settings : Screen("settings")
    
    companion object {
        val allScreens = listOf(Onboard, Sorter, Review, Settings)

        fun fromRoute(route: String): Screen? {
            return allScreens.find { it.route == route }
        }
    }
}
