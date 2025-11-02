package com.example.assignment5_q1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//NEW IMPORTS
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


// Data class for Recipe
data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>
)

// Sealed class for Routes
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Add : Routes("add")
    object Settings : Routes("settings")
    object Detail : Routes("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}

// ViewModel to manage recipes
class RecipeViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(
        listOf(
            Recipe(
                id = 1,
                title = "Spaghetti Carbonara",
                ingredients = listOf("Pasta", "Eggs", "Bacon", "Parmesan", "Black pepper"),
                steps = listOf(
                    "Boil pasta until al dente",
                    "Fry bacon until crispy",
                    "Mix eggs and parmesan",
                    "Combine hot pasta with bacon",
                    "Add egg mixture and stir quickly",
                    "Season with black pepper"
                )
            ),
            Recipe(
                id = 2,
                title = "Chocolate Chip Cookies",
                ingredients = listOf("Flour", "Butter", "Sugar", "Eggs", "Chocolate chips", "Vanilla"),
                steps = listOf(
                    "Cream butter and sugar",
                    "Add eggs and vanilla",
                    "Mix in flour",
                    "Fold in chocolate chips",
                    "Bake at 350°F for 12 minutes"
                )
            )
        )
    )
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private var nextId = 3

    fun addRecipe(title: String, ingredients: List<String>, steps: List<String>) {
        val newRecipe = Recipe(
            id = nextId++,
            title = title,
            ingredients = ingredients,
            steps = steps
        )
        _recipes.value = _recipes.value + newRecipe
    }

    fun getRecipeById(id: Int): Recipe? {
        return _recipes.value.find { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: RecipeViewModel by lazy {
        ViewModelProvider(this)[RecipeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RecipeApp(viewModel)
            }
        }
    }
}

@Composable
fun RecipeApp(viewModel: RecipeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            RecipeBottomNav(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Routes.Detail.createRoute(recipeId))
                    }
                )
            }

            composable(
                route = Routes.Detail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getInt("id") ?: return@composable
                DetailScreen(
                    viewModel = viewModel,
                    recipeId = recipeId,
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Routes.Add.route) {
                AddRecipeScreen(
                    viewModel = viewModel,
                    onRecipeAdded = {
                        // Use popUpTo to go back to home and clear add screen
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun RecipeBottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFF3F4F6)
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.Home.route,
            onClick = {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = { Text("🏠", fontSize = 24.sp) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Add.route,
            onClick = {
                navController.navigate(Routes.Add.route) {
                    launchSingleTop = true
                }
            },
            icon = { Text("➕", fontSize = 24.sp) },
            label = { Text("Add") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Settings.route,
            onClick = {
                navController.navigate(Routes.Settings.route) {
                    launchSingleTop = true
                }
            },
            icon = { Text("⚙️", fontSize = 24.sp) },
            label = { Text("Settings") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Int) -> Unit
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 My Recipes", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🍳",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recipe.ingredients.size} ingredients • ${recipe.steps.size} steps",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text("→", fontSize = 24.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: RecipeViewModel,
    recipeId: Int,
    onBack: () -> Unit
) {
    val recipe = viewModel.getRecipeById(recipeId)

    if (recipe == null) {
        Text("Recipe not found", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🥘 Ingredients",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        recipe.ingredients.forEach { ingredient ->
                            Text(
                                text = "• $ingredient",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 Steps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        recipe.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = step,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipeViewModel,
    onRecipeAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("➕ Add Recipe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients (one per line)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 10
            )

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Steps (one per line)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 10
            )

            Button(
                onClick = {
                    if (title.isNotBlank() && ingredients.isNotBlank() && steps.isNotBlank()) {
                        val ingredientList = ingredients.split("\n").filter { it.isNotBlank() }
                        val stepList = steps.split("\n").filter { it.isNotBlank() }
                        viewModel.addRecipe(title, ingredientList, stepList)
                        onRecipeAdded()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                enabled = title.isNotBlank() && ingredients.isNotBlank() && steps.isNotBlank()
            ) {
                Text("Add Recipe", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3F4F6)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recipe Navigator",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Version 1.0", color = Color.Gray)
                    Text("A simple recipe browsing app", color = Color.Gray)
                }
            }
        }
    }
}