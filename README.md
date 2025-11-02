How/where I used AI to build this app:
I built this recipe navigation app primarily through hands-on coding, Android documentation, and experimentation. 
I used Claude AI as a supplementary tool to verify syntax and check best practices, similar to consulting Stack Overflow. 
The core architecture - sealed routes, NavHost setup, and ViewModel structure - came from class inspiration and browsing material on the internet.
I designed the three-tab bottom navigation and recipe list UI myself, occasionally asking AI to confirm I was using the correct component properties.

Where AI misunderstood navigation:
When consulting AI for navigation, AI made several mistakes that required some fixing. The most significant issue was with backstack management - 
AI initially suggested popUpTo(0) which doesn't work in Jetpack Compose Navigation. The correct approach is popUpTo(Routes.Home.route) { inclusive = false }. 
AI also forgot to include launchSingleTop = true in the bottom navigation, which would cause multiple instances of the same screen to stack up. 
